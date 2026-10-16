package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.servico.EstoqueService;
import com.rotavital.servico.RepositorioFalso;

// GET /api/v1/hemocomponentes do jeito que a tela recebe: lista JSON, filtros da query string
// convertidos para os enums e erros 400 em application/problem+json. O MockMvc chama o controller
// sem subir o app nem acessar o banco.
public class ListagemDeBolsasTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final ObjectMapper json = new ObjectMapper();
    private List<BolsaHemocomponente> bolsasEncontradas;
    private Object[] consultaFeita;
    private MockMvc api;

    @BeforeEach
    public void criarApiComUmaBolsa()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        bolsasEncontradas = List.of(new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco));

        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                "buscarComFiltros", argumentos ->
                {
                    consultaFeita = argumentos;
                    return bolsasEncontradas;
                });

        // A listagem não usa o repositório de bancos nem o resumo do estoque.
        api = MockMvcBuilders.standaloneSetup(new EstoqueController(new EstoqueService(null, bolsasFalsas), null))
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    @Test
    public void semParametrosUsaAPrimeiraPaginaDeVinte() throws Exception
    {
        JsonNode bolsas = lista(MockMvcRequestBuilders.get("/api/v1/hemocomponentes"));

        Assertions.assertEquals(1, bolsas.size());
        Assertions.assertArrayEquals(new Object[] { null, null, null, null },
                Arrays.copyOf(consultaFeita, 4));

        Pageable pagina = (Pageable) consultaFeita[4];
        Assertions.assertEquals(0, pagina.getPageNumber());
        Assertions.assertEquals(20, pagina.getPageSize());
    }

    @Test
    public void filtrosDaQueryStringChegamComoEnums() throws Exception
    {
        lista(MockMvcRequestBuilders.get("/api/v1/hemocomponentes")
                .param("bancoId", "BS-01")
                .param("tipoComponente", "HEMACIAS")
                .param("tipoSanguineo", "O_NEGATIVO")
                .param("status", "DISPONIVEL")
                .param("page", "1")
                .param("size", "5"));

        Assertions.assertEquals("BS-01", consultaFeita[0]);
        Assertions.assertEquals(TipoComponente.HEMACIAS, consultaFeita[1]);
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, consultaFeita[2]);
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, consultaFeita[3]);
        Assertions.assertEquals(5, ((Pageable) consultaFeita[4]).getOffset());
    }

    @Test
    public void cadaBolsaVemComOsCamposDoContrato() throws Exception
    {
        JsonNode bolsa = lista(MockMvcRequestBuilders.get("/api/v1/hemocomponentes")).get(0);

        Assertions.assertEquals("CH-1042", bolsa.get("id").asText());
        Assertions.assertEquals("HEMACIAS", bolsa.get("tipoComponente").asText());
        Assertions.assertEquals("O_NEGATIVO", bolsa.get("tipoSanguineo").asText());
        Assertions.assertEquals("DISPONIVEL", bolsa.get("status").asText());
        Assertions.assertEquals("BS-01", bolsa.get("bancoOrigemId").asText());
        Assertions.assertFalse(bolsa.get("foraDaFaixa").asBoolean());
    }

    @Test
    public void nadaBatendoDevolve200ComListaVazia() throws Exception
    {
        bolsasEncontradas = List.of();

        JsonNode bolsas = lista(MockMvcRequestBuilders.get("/api/v1/hemocomponentes").param("bancoId", "BS-99"));

        Assertions.assertEquals(0, bolsas.size());
    }

    @Test
    public void statusForaDaListaDevolve400ComOsValoresAceitos() throws Exception
    {
        Assertions.assertEquals(requisicaoInvalida("O parâmetro 'status' não aceita o valor 'VENCIDA'. Valores aceitos: "
                + "DISPONIVEL, RESERVADA, EM_TRANSITO, ENTREGUE, DESCARTADA, UTILIZADA."),
                erro(MockMvcRequestBuilders.get("/api/v1/hemocomponentes").param("status", "VENCIDA")));
    }

    @Test
    public void tamanhoAcimaDoLimiteDevolve400() throws Exception
    {
        Assertions.assertEquals(requisicaoInvalida("O parâmetro 'size' deve estar entre 1 e 100."),
                erro(MockMvcRequestBuilders.get("/api/v1/hemocomponentes").param("size", "500")));
    }

    @Test
    public void paginaAltaDemaisDevolve400() throws Exception
    {
        Assertions.assertEquals(requisicaoInvalida("O parâmetro 'page' é alto demais: page × size deve ser no máximo 2147483647."),
                erro(MockMvcRequestBuilders.get("/api/v1/hemocomponentes").param("page", "99999999").param("size", "100")));
    }

    @Test
    public void paginaComTextoDevolve400() throws Exception
    {
        Assertions.assertEquals(requisicaoInvalida("O parâmetro 'page' não aceita o valor 'primeira'. Use um número."),
                erro(MockMvcRequestBuilders.get("/api/v1/hemocomponentes").param("page", "primeira")));
    }

    private ErroDTO requisicaoInvalida(String detalhe)
    {
        return new ErroDTO(null, "Requisição inválida", 400, detalhe, "/api/v1/hemocomponentes");
    }

    // Faz a requisição, confere o 200 em JSON e devolve o array de bolsas.
    private JsonNode lista(MockHttpServletRequestBuilder requisicao) throws Exception
    {
        MockHttpServletResponse resposta = api.perform(requisicao).andReturn().getResponse();

        Assertions.assertEquals(200, resposta.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE, resposta.getContentType());
        JsonNode corpo = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertTrue(corpo.isArray());
        return corpo;
    }

    // Faz a requisição, confere que a resposta é problem+json e devolve o ErroDTO do corpo.
    private ErroDTO erro(MockHttpServletRequestBuilder requisicao) throws Exception
    {
        MockHttpServletResponse resposta = api.perform(requisicao).andReturn().getResponse();

        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        ErroDTO erro = json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
        Assertions.assertEquals(erro.status(), resposta.getStatus());
        return erro;
    }
}
