package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.servico.EstoqueService;
import com.rotavital.servico.RepositorioFalso;

// Respostas HTTP de erro do estoque, do jeito que a tela recebe: status, application/problem+json
// e a mensagem em detail. O MockMvc chama os controllers sem subir o app nem acessar o banco.
public class ErrosDoEstoqueTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    // Faz o papel do POST /api/v1/hemocomponentes até o cadastro (PI3-179) entrar: recebe o mesmo corpo
    // e passa pela mesma validação, então os erros testados aqui são os que o cadastro vai devolver.
    @RestController
    public static class CadastroDeBolsaDeTeste
    {
        private final EstoqueService estoqueService;

        public CadastroDeBolsaDeTeste(EstoqueService estoqueService)
        {
            this.estoqueService = estoqueService;
        }

        @PostMapping("/api/v1/hemocomponentes")
        public ResponseEntity<Void> registrar(@RequestBody NovaBolsaHemocomponenteRequest dados)
        {
            estoqueService.validarNovaBolsa(dados, HOJE);
            return ResponseEntity.status(201).build();
        }
    }

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc api;

    @BeforeEach
    public void criarApiComOBancoBS01()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        BolsaHemocomponente bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco);
        banco.getEstoque().adicionarBolsa(bolsa);

        // Só o BS-01 e a bolsa CH-1042 existem.
        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                "findById", argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null));
        EstoqueService servico = new EstoqueService(bancosFalsos, bolsasFalsas);

        // O resumo do estoque não é chamado nestes testes.
        api = MockMvcBuilders.standaloneSetup(new EstoqueController(servico, null), new CadastroDeBolsaDeTeste(servico))
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    @Test
    public void bolsaInexistenteDevolve404ComOId() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhuma bolsa encontrada com id BOLSA-999.", "/api/v1/hemocomponentes/BOLSA-999"),
                erro(MockMvcRequestBuilders.get("/api/v1/hemocomponentes/BOLSA-999")));
    }

    @Test
    public void bolsaExistenteDevolveOsDados() throws Exception
    {
        MockHttpServletResponse resposta = api.perform(MockMvcRequestBuilders.get("/api/v1/hemocomponentes/CH-1042"))
                .andReturn().getResponse();

        Assertions.assertEquals(200, resposta.getStatus());
        Assertions.assertEquals("CH-1042", json.readTree(resposta.getContentAsString()).get("id").asText());
    }

    @Test
    public void bancoInexistenteDevolve404() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhum banco de sangue encontrado com id BS-99", "/api/v1/bancos/BS-99/estoque"),
                erro(MockMvcRequestBuilders.get("/api/v1/bancos/BS-99/estoque")));
    }

    @Test
    public void tipoSanguineoForaDaListaDevolve400ComOsValoresAceitos() throws Exception
    {
        // "O-" é como a tela mostra o tipo; a API espera o nome do enum.
        Assertions.assertEquals(new ErroDTO(null, "Requisição inválida", 400,
                "O parâmetro 'tipoSanguineo' não aceita o valor 'O-'. Valores aceitos: A_POSITIVO, A_NEGATIVO, "
                        + "B_POSITIVO, B_NEGATIVO, AB_POSITIVO, AB_NEGATIVO, O_POSITIVO, O_NEGATIVO.",
                "/api/v1/bancos/BS-01/estoque"),
                erro(MockMvcRequestBuilders.get("/api/v1/bancos/BS-01/estoque").param("tipoSanguineo", "O-")));
    }

    @Test
    public void tipoComponenteForaDaListaNoCorpoDevolve400() throws Exception
    {
        Map<String, Object> corpo = bolsaValida();
        corpo.put("tipoComponente", "SANGUE_TOTAL");

        Assertions.assertEquals(requisicaoInvalidaNoCadastro("O campo 'tipoComponente' não aceita o valor "
                + "'SANGUE_TOTAL'. Valores aceitos: HEMACIAS, PLASMA, PLAQUETAS, CRIOPRECIPITADO."),
                erro(cadastro(json.writeValueAsString(corpo))));
    }

    @Test
    public void dataEmOutroFormatoDevolve400() throws Exception
    {
        Map<String, Object> corpo = bolsaValida();
        corpo.put("dataColeta", "26/09/2026");

        Assertions.assertEquals(requisicaoInvalidaNoCadastro("O campo 'dataColeta' não aceita o valor '26/09/2026'. "
                + "Use uma data no formato AAAA-MM-DD."),
                erro(cadastro(json.writeValueAsString(corpo))));
    }

    @Test
    public void volumeComTextoDevolve400() throws Exception
    {
        Map<String, Object> corpo = bolsaValida();
        corpo.put("volumeMl", "meio litro");

        Assertions.assertEquals(requisicaoInvalidaNoCadastro("O campo 'volumeMl' não aceita o valor 'meio litro'. "
                + "Use um número."),
                erro(cadastro(json.writeValueAsString(corpo))));
    }

    @Test
    public void jsonQuebradoDevolve400() throws Exception
    {
        Assertions.assertEquals(requisicaoInvalidaNoCadastro("O corpo da requisição está vazio ou não é um JSON válido."),
                erro(cadastro("{\"tipoComponente\": ")));
    }

    @Test
    public void campoObrigatorioFaltandoDevolve400() throws Exception
    {
        Map<String, Object> corpo = bolsaValida();
        corpo.remove("loteSintetico");

        Assertions.assertEquals(requisicaoInvalidaNoCadastro("O campo 'loteSintetico' é obrigatório."),
                erro(cadastro(json.writeValueAsString(corpo))));
    }

    @Test
    public void regraDoEstoqueQuebradaDevolve422() throws Exception
    {
        Map<String, Object> corpo = bolsaValida();
        corpo.put("dataValidade", "2026-09-20");

        Assertions.assertEquals(new ErroDTO(null, "Regra de negócio violada", 422,
                "A data de validade (20/09/2026) não pode ser anterior à data de coleta (26/09/2026).",
                "/api/v1/hemocomponentes"),
                erro(cadastro(json.writeValueAsString(corpo))));
    }

    // Corpo de uma bolsa que passa em todas as validações; cada teste estraga um campo.
    private Map<String, Object> bolsaValida()
    {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipoComponente", "HEMACIAS");
        corpo.put("tipoSanguineo", "O_POSITIVO");
        corpo.put("dataColeta", "2026-09-26");
        corpo.put("dataValidade", "2026-10-31");
        corpo.put("loteSintetico", "LOTE-SIM-0001");
        corpo.put("volumeMl", 450.0);
        corpo.put("bancoOrigemId", "BS-01");
        return corpo;
    }

    private ErroDTO requisicaoInvalidaNoCadastro(String detalhe)
    {
        return new ErroDTO(null, "Requisição inválida", 400, detalhe, "/api/v1/hemocomponentes");
    }

    private MockHttpServletRequestBuilder cadastro(String corpo)
    {
        return MockMvcRequestBuilders.post("/api/v1/hemocomponentes").contentType(MediaType.APPLICATION_JSON).content(corpo);
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
