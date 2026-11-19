package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;
import com.rotavital.repositorio.UsuarioRepository;
import com.rotavital.servico.CalculadoraDeTrajeto;
import com.rotavital.servico.IdentificacaoService;
import com.rotavital.servico.RedeDistribuicaoService;
import com.rotavital.servico.RepositorioFalso;
import com.rotavital.servico.RotaNoMapaService;

// GET /api/v1/pontos (PI3-258) e /rotas/trajeto (PI3-259) do jeito que a tela de Rede recebe.
public class RedeApiTest
{
    private final ObjectMapper json = new ObjectMapper();
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital hospital = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        PontoDeRedeRepository pontos = RepositorioFalso.respondendo(PontoDeRedeRepository.class, "findAll",
                argumentos -> List.<PontoDeRedeBase>of(hospital, banco));
        ConexaoRepository conexoes = RepositorioFalso.respondendo(ConexaoRepository.class, "findAllByOrderByIdAsc",
                argumentos -> List.of(new Conexao(banco, hospital, 7.8, 18.0)));
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico : null));

        RedeDistribuicaoService rede = new RedeDistribuicaoService(pontos, conexoes);
        CalculadoraDeTrajeto semGoogle = (origem, destino) -> Optional.<Trajeto>empty();

        api = MockMvcBuilders.standaloneSetup(new RotaController(rede),
                        new RotaNoMapaController(new RotaNoMapaService(rede, semGoogle, new IdentificacaoService(usuarios))))
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

    @Test
    public void trajetoSemGoogleUsaAsConexoesEVemSemPolyline() throws Exception
    {
        MockHttpServletResponse resposta = api.perform(MockMvcRequestBuilders
                        .get("/api/v1/rotas/trajeto?origemId=BS-01&destinoId=hc-pe")
                        .header("X-Usuario-Id", medico.getId().toString()))
                .andReturn().getResponse();

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode rota = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals(List.of("BS-01", "hc-pe"), json.convertValue(rota.get("nos"), List.class));
        Assertions.assertEquals(7.8, rota.get("distanciaKm").asDouble());
        Assertions.assertEquals(18, rota.get("duracaoMinutos").asInt());
        Assertions.assertEquals(FonteDoTrajeto.CONEXOES_DA_REDE.name(), rota.get("fonte").asText());
        Assertions.assertTrue(rota.get("polyline").isNull());
    }

    @Test
    public void trajetoSemUsuarioResponde401() throws Exception
    {
        MockHttpServletResponse resposta = api.perform(MockMvcRequestBuilders
                        .get("/api/v1/rotas/trajeto?origemId=BS-01&destinoId=hc-pe"))
                .andReturn().getResponse();

        Assertions.assertEquals(401, resposta.getStatus());
    }
}
