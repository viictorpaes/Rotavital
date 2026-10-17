package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.AtualizarBolsaHemocomponenteRequest;
import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Estoque;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

@Service
public class EstoqueService
{
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    // Limites das constraints ck_bolsa_volume_ml e ck_bolsa_temperatura_sensor. A temperatura é a faixa
    // física do sensor: fora da faixa ideal do componente é só alerta (BolsaHemocomponente.estaForaDaFaixa).
    private static final double VOLUME_MAXIMO_ML = 700.0;
    private static final double TEMPERATURA_MINIMA_SENSOR = -80.0;
    private static final double TEMPERATURA_MAXIMA_SENSOR = 60.0;

    // PI3-190: só bolsa ainda no estoque pode ser alterada, e só estes status podem ser escolhidos na edição.
    private static final Set<StatusBolsa> STATUS_ALTERAVEIS = EnumSet.of(StatusBolsa.DISPONIVEL, StatusBolsa.RESERVADA);
    private static final Set<StatusBolsa> STATUS_ESCOLHIVEIS =
            EnumSet.of(StatusBolsa.DISPONIVEL, StatusBolsa.RESERVADA, StatusBolsa.DESCARTADA);

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
     * Bolsas de todos os bancos, com filtros opcionais e combináveis, da que vence primeiro para a
     * que vence por último (FEFO). Filtro nulo (ou bancoId em branco) não restringe; nada batendo
     * devolve lista vazia. Página negativa, tamanho fora de 1 a 100 ou página tão alta que
     * page × size passa de Integer.MAX_VALUE lança DadosInvalidosException (400).
     */
    @Transactional(readOnly = true)
    public List<BolsaHemocomponente> listarBolsas(String bancoId, TipoComponente tipoComponente,
            TipoSanguineo tipoSanguineo, StatusBolsa status, int pagina, int tamanho)
    {
        List<String> problemas = new ArrayList<>();

        if (pagina < 0)
        {
            problemas.add("O parâmetro 'page' deve ser maior ou igual a 0.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO_PAGINA)
        {
            problemas.add("O parâmetro 'size' deve estar entre 1 e " + TAMANHO_MAXIMO_PAGINA + ".");
        }
        // O deslocamento (page × size) vai para a consulta como int: acima de Integer.MAX_VALUE o
        // Spring Data recusa a consulta, e a API responderia 500 em vez de 400.
        if (problemas.isEmpty() && (long) pagina * tamanho > Integer.MAX_VALUE)
        {
            problemas.add("O parâmetro 'page' é alto demais: page × size deve ser no máximo " + Integer.MAX_VALUE + ".");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }

        String banco = bancoId == null || bancoId.isBlank() ? null : bancoId;

        return bolsas.buscarComFiltros(banco, tipoComponente, tipoSanguineo, status, PageRequest.of(pagina, tamanho));
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
        conferirValidade(dados.tipoComponente(), dados.dataColeta(), dados.dataValidade(), hoje, problemas);
        conferirVolume(dados.volumeMl(), problemas);
        conferirTemperatura(dados.temperaturaCelsius(), problemas);
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

    // Regras de validade, volume e temperatura, iguais no cadastro (PI3-179) e na correção (PI3-190).
    private void conferirValidade(TipoComponente componente, LocalDate dataColeta, LocalDate dataValidade,
            LocalDate hoje, List<String> problemas)
    {
        if (dataValidade.isBefore(dataColeta))
        {
            problemas.add("A data de validade (" + DATA.format(dataValidade)
                    + ") não pode ser anterior à data de coleta (" + DATA.format(dataColeta) + ").");
            return;
        }

        long diasDeValidade = ChronoUnit.DAYS.between(dataColeta, dataValidade);

        if (diasDeValidade == 0)
        {
            problemas.add("A data de validade (" + DATA.format(dataValidade)
                    + ") precisa ser depois da data de coleta.");
        }
        else if (diasDeValidade > componente.getPrazoMaximoDias())
        {
            problemas.add("A validade de " + componente + " é de no máximo " + componente.getPrazoMaximoDias()
                    + " dias depois da coleta, e foram informados " + diasDeValidade + " dias.");
        }
        if (dataValidade.isBefore(hoje))
        {
            problemas.add("A bolsa venceu em " + DATA.format(dataValidade) + " e não pode entrar no estoque.");
        }
    }

    private void conferirVolume(double volumeMl, List<String> problemas)
    {
        if (volumeMl > VOLUME_MAXIMO_ML)
        {
            problemas.add("O volume (" + volumeMl + " ml) passa do máximo de " + VOLUME_MAXIMO_ML + " ml por bolsa.");
        }
    }

    private void conferirTemperatura(double temperaturaCelsius, List<String> problemas)
    {
        if (temperaturaCelsius < TEMPERATURA_MINIMA_SENSOR || temperaturaCelsius > TEMPERATURA_MAXIMA_SENSOR)
        {
            problemas.add("A temperatura (" + temperaturaCelsius + " °C) está fora do que o sensor mede ("
                    + TEMPERATURA_MINIMA_SENSOR + " °C a " + TEMPERATURA_MAXIMA_SENSOR + " °C).");
        }
    }

    /**
     * Confere uma alteração de bolsa (PI3-190) antes de aplicá-la. Corpo sem nenhum campo ou volume
     * menor ou igual a zero lança DadosInvalidosException (400). Bolsa que não está DISPONIVEL nem
     * RESERVADA, status que não pode ser escolhido aqui ou validade, volume e temperatura fora das
     * regras do cadastro lançam RegraDeNegocioException (422), com todos os problemas na mensagem.
     */
    public void validarAtualizacao(BolsaHemocomponente bolsa, AtualizarBolsaHemocomponenteRequest dados, LocalDate hoje)
    {
        if (dados == null || dados.vazio())
        {
            throw new DadosInvalidosException("Informe ao menos um campo para alterar: status, dataValidade, "
                    + "volumeMl, temperaturaCelsius ou localizacao.");
        }
        if (dados.volumeMl() != null && dados.volumeMl() <= 0)
        {
            throw new DadosInvalidosException("O campo 'volumeMl' deve ser maior que zero.");
        }

        // Em trânsito, entregue, utilizada ou descartada, a bolsa já saiu do estoque: o registro vira histórico.
        if (!STATUS_ALTERAVEIS.contains(bolsa.getStatus()))
        {
            throw new RegraDeNegocioException("A bolsa " + bolsa.getId() + " está " + bolsa.getStatus()
                    + " e não pode mais ser alterada: só bolsas DISPONIVEL ou RESERVADA podem.");
        }

        List<String> problemas = new ArrayList<>();

        // EM_TRANSITO, ENTREGUE e UTILIZADA vêm do transporte e do procedimento, não de uma edição manual.
        if (dados.status() != null && !STATUS_ESCOLHIVEIS.contains(dados.status()))
        {
            problemas.add("O status " + dados.status() + " não pode ser escolhido aqui: use DISPONIVEL, "
                    + "RESERVADA ou DESCARTADA.");
        }
        if (dados.dataValidade() != null)
        {
            conferirValidade(bolsa.getTipoComponente(), bolsa.getDataColeta(), dados.dataValidade(), hoje, problemas);
        }
        if (dados.volumeMl() != null)
        {
            conferirVolume(dados.volumeMl(), problemas);
        }
        if (dados.temperaturaCelsius() != null)
        {
            conferirTemperatura(dados.temperaturaCelsius(), problemas);
        }

        if (!problemas.isEmpty())
        {
            throw new RegraDeNegocioException(String.join(" ", problemas));
        }
    }

    /**
     * Altera uma bolsa do estoque (PI3-190): muda o status (reservar, liberar a reserva, descartar) e/ou
     * corrige validade, volume, temperatura e localização. Campo ausente fica como está; localização em
     * branco é apagada. Bolsa inexistente → 404; os demais erros são os de validarAtualizacao.
     */
    @Transactional
    public BolsaHemocomponente atualizarBolsa(String id, AtualizarBolsaHemocomponenteRequest dados)
    {
        BolsaHemocomponente bolsa = bolsas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + id + "."));

        validarAtualizacao(bolsa, dados, LocalDate.now(relogio));

        if (dados.dataValidade() != null)
        {
            bolsa.corrigirValidade(dados.dataValidade());
        }
        if (dados.volumeMl() != null)
        {
            bolsa.corrigirVolume(dados.volumeMl());
        }
        if (dados.temperaturaCelsius() != null)
        {
            bolsa.registrarTemperatura(dados.temperaturaCelsius());
        }
        if (dados.localizacao() != null)
        {
            bolsa.alterarLocalizacao(dados.localizacao().isBlank() ? null : dados.localizacao().strip());
        }
        if (dados.status() != null)
        {
            mudarStatus(bolsa, dados.status());
        }

        return bolsas.save(bolsa);
    }

    private void mudarStatus(BolsaHemocomponente bolsa, StatusBolsa novo)
    {
        switch (novo)
        {
            case RESERVADA -> bolsa.reservar();
            case DESCARTADA -> bolsa.descartar();
            case DISPONIVEL -> bolsa.liberarReserva();
            default -> throw new IllegalStateException("Status não escolhível: " + novo);
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
