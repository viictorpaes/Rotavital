package com.rotavital.servico;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
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

    private final BancoDeSangueRepository bancos;
    private final BolsaHemocomponenteRepository bolsas;

    public EstoqueService(BancoDeSangueRepository bancos, BolsaHemocomponenteRepository bolsas)
    {
        this.bancos = bancos;
        this.bolsas = bolsas;
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
        else if (dados.dataValidade().isBefore(hoje))
        {
            problemas.add("A bolsa venceu em " + DATA.format(dados.dataValidade()) + " e não pode entrar no estoque.");
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
}
