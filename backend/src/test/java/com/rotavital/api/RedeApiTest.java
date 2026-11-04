package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;
import com.rotavital.servico.RedeDistribuicaoService;
import com.rotavital.servico.RepositorioFalso;

// GET /api/v1/pontos (PI3-258) do jeito que a tela de Rede recebe.
public class RedeApiTest
{
    private final ObjectMapper json = new ObjectMapper();
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital hospital = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        PontoDeRedeRepository pontos = RepositorioFalso.respondendo(PontoDeRedeRepository.class, "findAll",
                argumentos -> List.<PontoDeRedeBase>of(hospital, banco));
        ConexaoRepository conexoes = RepositorioFalso.respondendo(ConexaoRepository.class, "findAllByOrderByIdAsc",
                argumentos -> List.<Conexao>of());

        api = MockMvcBuilders.standaloneSetup(new RotaController(new RedeDistribuicaoService(pontos, conexoes)))
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    private JsonNode get(String url) throws Exception
    {
        MockHttpServletResponse resposta = api.perform(MockMvcRequestBuilders.get(url)).andReturn().getResponse();
        Assertions.assertEquals(200, resposta.getStatus());
        return json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void pontosTrazemTipoCoordenadasEEnderecoParaOMapa() throws Exception
    {
        JsonNode pontos = get("/api/v1/pontos");

        Assertions.assertEquals(2, pontos.size());
        Assertions.assertEquals("BS-01", pontos.get(0).get("id").asText());
        Assertions.assertEquals("BANCO_DE_SANGUE", pontos.get(0).get("tipo").asText());

        JsonNode hc = pontos.get(1);
        Assertions.assertEquals("HOSPITAL", hc.get("tipo").asText());
        Assertions.assertEquals(-8.0512, hc.get("latitude").asDouble());
        Assertions.assertEquals(-34.9478, hc.get("longitude").asDouble());
        Assertions.assertEquals("Av. Prof. Moraes Rego, 1235", hc.get("logradouro").asText());
    }
}
