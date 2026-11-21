package com.rotavital.api;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// PI3-308: todo erro sai no formato ErroDTO (RFC 7807), inclusive os que ninguém previu.
public class TratadorDeErrosTest
{
    private final ObjectMapper json = new ObjectMapper();
    private MockMvc api;

    /** Endpoints de mentira que quebram de propósito, um jeito para cada tipo de falha. */
    @RestController
    static class ControllerQueQuebra
    {
        @GetMapping("/teste/npe")
        public String npe()
        {
            String nulo = null;
            return nulo.trim();
        }

        @GetMapping("/teste/estado")
        public String estado()
        {
            throw new IllegalStateException("detalhe interno com senha=segredo");
        }

        @GetMapping("/teste/banco-fora")
        public String bancoFora()
        {
            throw new CannotCreateTransactionException("Could not open JPA EntityManager for transaction");
        }

        @GetMapping("/teste/pool-sem-conexao")
        public String poolSemConexao()
        {
            throw new DataAccessResourceFailureException("Unable to acquire JDBC Connection");
        }

        @GetMapping("/teste/parametro")
        public String parametro(@RequestParam("quantidade") int quantidade)
        {
            return String.valueOf(quantidade);
        }

        @GetMapping("/teste/header")
        public String header(@RequestHeader("X-Obrigatorio") String valor)
        {
            return valor;
        }

        @GetMapping("/teste/rota-inexistente")
        public String rotaInexistente() throws NoResourceFoundException
        {
            throw new NoResourceFoundException(HttpMethod.GET, "api/v1/nao-existe");
        }

        @PostMapping(path = "/teste/json", consumes = MediaType.APPLICATION_JSON_VALUE)
        public String soJson(@RequestBody String corpo)
        {
            return corpo;
        }
    }

    @BeforeEach
    public void criarApi()
    {
        api = MockMvcBuilders.standaloneSetup(new ControllerQueQuebra())
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    private MockHttpServletResponse chamar(RequestBuilder requisicao) throws Exception
    {
        return api.perform(requisicao).andReturn().getResponse();
    }

    private JsonNode problema(MockHttpServletResponse resposta) throws Exception
    {
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        JsonNode corpo = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals(resposta.getStatus(), corpo.get("status").asInt());
        return corpo;
    }

    @Test
    public void nullPointerViraErro500NoFormatoErroDTO() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get("/teste/npe"));

        Assertions.assertEquals(500, resposta.getStatus());
        JsonNode corpo = problema(resposta);
        Assertions.assertEquals("Erro interno", corpo.get("title").asText());
        Assertions.assertEquals("/teste/npe", corpo.get("instance").asText());
        Assertions.assertTrue(corpo.get("detail").asText().matches(
                "Ocorreu um erro inesperado no servidor\\. Se continuar, informe o código [0-9a-f]{8} à equipe\\."),
                corpo.get("detail").asText());
    }

    @Test
    public void erro500NaoExpoeMensagemInternaNemStackTrace() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get("/teste/estado"));

        Assertions.assertEquals(500, resposta.getStatus());
        String corpo = resposta.getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertFalse(corpo.contains("segredo"), corpo);
        Assertions.assertFalse(corpo.contains("IllegalStateException"), corpo);
        Assertions.assertFalse(corpo.contains("at com.rotavital"), corpo);
        Assertions.assertFalse(problema(resposta).has("trace"));
    }

    @Test
    public void cadaErro500TemUmCodigoDiferente() throws Exception
    {
        String primeiro = problema(chamar(MockMvcRequestBuilders.get("/teste/npe"))).get("detail").asText();
        String segundo = problema(chamar(MockMvcRequestBuilders.get("/teste/npe"))).get("detail").asText();

        Assertions.assertNotEquals(primeiro, segundo);
    }

    @Test
    public void bancoForaDoArViraErro503() throws Exception
    {
        for (String caminho : new String[] { "/teste/banco-fora", "/teste/pool-sem-conexao" })
        {
            MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get(caminho));

            Assertions.assertEquals(503, resposta.getStatus(), caminho);
            Assertions.assertEquals("O banco de dados não está respondendo agora. Tente de novo em instantes.",
                    problema(resposta).get("detail").asText());
        }
    }

    @Test
    public void parametroObrigatorioFaltandoViraErro400() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get("/teste/parametro"));

        Assertions.assertEquals(400, resposta.getStatus());
        Assertions.assertEquals("O parâmetro 'quantidade' é obrigatório.", problema(resposta).get("detail").asText());
    }

    @Test
    public void headerObrigatorioFaltandoViraErro400() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get("/teste/header"));

        Assertions.assertEquals(400, resposta.getStatus());
        Assertions.assertEquals("O header 'X-Obrigatorio' é obrigatório.", problema(resposta).get("detail").asText());
    }

    @Test
    public void rotaInexistenteViraErro404() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.get("/teste/rota-inexistente"));

        Assertions.assertEquals(404, resposta.getStatus());
        Assertions.assertEquals("Nenhum endpoint responde a GET /teste/rota-inexistente.",
                problema(resposta).get("detail").asText());
    }

    @Test
    public void metodoNaoAceitoViraErro405ComOsMetodosPermitidos() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.delete("/teste/npe"));

        Assertions.assertEquals(405, resposta.getStatus());
        String detalhe = problema(resposta).get("detail").asText();
        Assertions.assertTrue(detalhe.startsWith("O método DELETE não é aceito em /teste/npe. Métodos aceitos: "), detalhe);
        Assertions.assertTrue(detalhe.contains("GET"), detalhe);
        Assertions.assertTrue(resposta.getHeader("Allow").contains("GET"));
    }

    @Test
    public void corpoQueNaoEJsonViraErro415() throws Exception
    {
        MockHttpServletResponse resposta = chamar(MockMvcRequestBuilders.post("/teste/json")
                .contentType(MediaType.TEXT_PLAIN).content("oi"));

        Assertions.assertEquals(415, resposta.getStatus());
        Assertions.assertEquals("Envie o corpo da requisição como application/json.",
                problema(resposta).get("detail").asText());
    }
}
