package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Estoque;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

@Service
public class EstoqueService
{
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Limites das constraints ck_bolsa_volume_ml e ck_bolsa_temperatura_sensor. A temperatura é a faixa
    // física do sensor: fora da faixa ideal do componente é só alerta (BolsaHemocomponente.estaForaDaFaixa).
    private static final double VOLUME_MAXIMO_ML = 700.0;
    private static final double TEMPERATURA_MINIMA_SENSOR = -80.0;
    private static final double TEMPERATURA_MAXIMA_SENSOR = 60.0;

    private final BancoDeSangueRepository bancos;
    private final BolsaHemocomponenteRepository bolsas;
    private final Clock relogio;

    @Autowired
    public EstoqueService(BancoDeSangueRepository bancos, BolsaHemocomponenteRepository bolsas)
    {
        this(bancos, bolsas, Clock.system(ZoneId.of("America/Recife")));
    }

    // Os testes passam um relógio parado, para o "hoje" do cadastro não mudar com a data em que rodam.
    public EstoqueService(BancoDeSangueRepository bancos, BolsaHemocomponenteRepository bolsas, Clock relogio)
    {
        this.bancos = bancos;
        this.bolsas = bolsas;
        this.relogio = relogio;
    }

    /**
     * Bolsas do estoque de um banco de sangue, ordenadas por validade.
     * Vazio quando o banco não existe.
     *
     * A lista de bolsas é carregada do banco de dados só quando é acessada, e o projeto usa
     * open-in-view=false: por isso a leitura acontece aqui, dentro da transação.
     */
    @Transactional(readOnly = true)
    public Optional<List<BolsaHemocomponente>> buscarBolsas(String bancoId, TipoSanguineo tipoSanguineo)
    {
        return bancos.findById(bancoId).map(banco ->
        {
            Estoque estoque = banco.getEstoque();

            if (tipoSanguineo == null)
            {
                return new ArrayList<>(estoque.getBolsas());
            }

            return estoque.buscarPorTipoSanguineo(tipoSanguineo);
        });
    }

    /**
     * Uma bolsa pelo id. Bolsa inexistente lança RecursoNaoEncontradoException (404 na API),
     * para a consulta, a edição e o descarte responderem a mesma mensagem.
     */
    @Transactional(readOnly = true)
    public BolsaHemocomponente buscarBolsa(String id)
    {
        return bolsas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + id + "."));
    }

    /**
     * Confere os dados de uma bolsa nova antes do cadastro. Campo ausente ou mal preenchido lança
     * DadosInvalidosException (400); dados que quebram uma regra do estoque lançam
     * RegraDeNegocioException (422). A mensagem traz todos os problemas de uma vez, para a tela
     * mostrar tudo o que precisa ser corrigido.
     */
    public void validarNovaBolsa(NovaBolsaHemocomponenteRequest dados, LocalDate hoje)
    {
        List<String> problemas = new ArrayList<>();

        if (dados.tipoComponente() == null)
        {
            problemas.add("O campo 'tipoComponente' é obrigatório.");
        }
        if (dados.tipoSanguineo() == null)
        {
            problemas.add("O campo 'tipoSanguineo' é obrigatório.");
        }
        if (dados.dataColeta() == null)
        {
            problemas.add("O campo 'dataColeta' é obrigatório.");
        }
        if (dados.dataValidade() == null)
        {
            problemas.add("O campo 'dataValidade' é obrigatório.");
        }
        if (dados.loteSintetico() == null || dados.loteSintetico().isBlank())
        {
            problemas.add("O campo 'loteSintetico' é obrigatório.");
        }
        // volumeMl é double: quando o campo não vem no JSON, chega como 0.
        if (dados.volumeMl() <= 0)
        {
            problemas.add("O campo 'volumeMl' é obrigatório e deve ser maior que zero.");
        }
        if (dados.temperaturaCelsius() == null)
        {
            problemas.add("O campo 'temperaturaCelsius' é obrigatório.");
        }
        if (dados.bancoOrigemId() == null || dados.bancoOrigemId().isBlank())
        {
            problemas.add("O campo 'bancoOrigemId' é obrigatório.");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }

        // Daqui em diante todos os campos estão preenchidos; faltam as regras do estoque.
        if (dados.dataColeta().isAfter(hoje))
        {
            problemas.add("A data de coleta (" + DATA.format(dados.dataColeta())
                    + ") não pode ser depois de hoje (" + DATA.format(hoje) + ").");
        }
        if (dados.dataValidade().isBefore(dados.dataColeta()))
        {
            problemas.add("A data de validade (" + DATA.format(dados.dataValidade())
                    + ") não pode ser anterior à data de coleta (" + DATA.format(dados.dataColeta()) + ").");
        }
        else
        {
            long diasDeValidade = ChronoUnit.DAYS.between(dados.dataColeta(), dados.dataValidade());
            TipoComponente componente = dados.tipoComponente();

            if (diasDeValidade == 0)
            {
                problemas.add("A data de validade (" + DATA.format(dados.dataValidade())
                        + ") precisa ser depois da data de coleta.");
            }
            else if (diasDeValidade > componente.getPrazoMaximoDias())
            {
                problemas.add("A validade de " + componente + " é de no máximo " + componente.getPrazoMaximoDias()
                        + " dias depois da coleta, e foram informados " + diasDeValidade + " dias.");
            }
            if (dados.dataValidade().isBefore(hoje))
            {
                problemas.add("A bolsa venceu em " + DATA.format(dados.dataValidade()) + " e não pode entrar no estoque.");
            }
        }
        if (dados.volumeMl() > VOLUME_MAXIMO_ML)
        {
            problemas.add("O volume (" + dados.volumeMl() + " ml) passa do máximo de " + VOLUME_MAXIMO_ML + " ml por bolsa.");
        }
        if (dados.temperaturaCelsius() < TEMPERATURA_MINIMA_SENSOR || dados.temperaturaCelsius() > TEMPERATURA_MAXIMA_SENSOR)
        {
            problemas.add("A temperatura (" + dados.temperaturaCelsius() + " °C) está fora do que o sensor mede ("
                    + TEMPERATURA_MINIMA_SENSOR + " °C a " + TEMPERATURA_MAXIMA_SENSOR + " °C).");
        }
        if (bolsas.existsByLoteSinteticoAndTipoComponente(dados.loteSintetico().strip(), dados.tipoComponente()))
        {
            problemas.add("Já existe uma bolsa de " + dados.tipoComponente() + " do lote " + dados.loteSintetico().strip()
                    + ": cada doação gera no máximo uma bolsa de cada componente.");
        }
        if (bancos.findById(dados.bancoOrigemId()).isEmpty())
        {
            problemas.add("Nenhum banco de sangue encontrado com id " + dados.bancoOrigemId() + ".");
        }

        if (!problemas.isEmpty())
        {
            throw new RegraDeNegocioException(String.join(" ", problemas));
        }
    }

    /**
     * Registra uma bolsa nova no estoque (PI3-179), com status DISPONIVEL. Os dados passam antes por
     * validarNovaBolsa, então os erros são os mesmos 400 e 422 da validação. O id é gerado aqui, a partir
     * da sigla do componente (ex.: a próxima de hemácias depois de CH-1090 é CH-1091).
     */
    @Transactional
    public BolsaHemocomponente cadastrarBolsa(NovaBolsaHemocomponenteRequest dados)
    {
        validarNovaBolsa(dados, LocalDate.now(relogio));

        // A validação já confirmou que o banco existe; na mesma transação, esta busca não vai de novo ao banco.
        BancoDeSangue banco = bancos.findById(dados.bancoOrigemId()).orElseThrow();
        String localizacao = dados.localizacao() == null || dados.localizacao().isBlank()
                ? null
                : dados.localizacao().strip();

        BolsaHemocomponente bolsa = new BolsaHemocomponente(
                proximoId(dados.tipoComponente()),
                dados.tipoComponente(),
                dados.tipoSanguineo(),
                dados.dataColeta(),
                dados.dataValidade(),
                dados.loteSintetico().strip(),
                dados.volumeMl(),
                dados.temperaturaCelsius(),
                localizacao,
                banco);

        return bolsas.save(bolsa);
    }

    /**
     * Próximo id livre do componente: o maior número já usado dentro da faixa dele, mais um.
     * Ids fora da faixa (como a bolsa de teste CH-9999) não entram na conta.
     */
    private String proximoId(TipoComponente componente)
    {
        int inicio = componente.getInicioDaFaixaDeIds();
        int fim = inicio + 999;
        String prefixo = componente.getSigla() + "-";
        int maiorUsado = inicio;

        for (String id : bolsas.buscarIdsComSigla(componente.getSigla()))
        {
            String numero = id.substring(prefixo.length());

            if (numero.matches("\\d{4}"))
            {
                int valor = Integer.parseInt(numero);

                if (valor > maiorUsado && valor <= fim)
                {
                    maiorUsado = valor;
                }
            }
        }

        if (maiorUsado == fim)
        {
            throw new IllegalStateException("Os ids de " + componente + " acabaram (" + prefixo + inicio + " a "
                    + prefixo + fim + ").");
        }

        return prefixo + (maiorUsado + 1);
    }
}
