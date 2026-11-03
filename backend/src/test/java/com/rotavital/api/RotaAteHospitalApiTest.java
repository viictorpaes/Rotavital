package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.CampanhaDoacaoRepository;
import com.rotavital.repositorio.UsuarioRepository;
import com.rotavital.servico.IdentificacaoService;
import com.rotavital.servico.RepositorioFalso;
import com.rotavital.servico.RotaAteHospitalService;

// POST /api/v1/campanhas/{id}/rota (PI3-262) do jeito que o modal "Como chegar" recebe.
public class RotaAteHospitalApiTest
{
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 3, 10, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final UUID ID_CAMPANHA = UUID.fromString("7d0c4b8e-0000-4000-8000-000000000237");
    private static final String URL = "/api/v1/campanhas/" + ID_CAMPANHA + "/rota";

    private final ObjectMapper json = new ObjectMapper();
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João Doador", PapelUsuario.DOADOR, null,
            TipoSanguineo.O_NEGATIVO);
    private final Hospital hospital = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235 - Cidade Universitária", -8.0512, -34.9478));
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        Paciente paciente = new Paciente(hospital, "Maria Souza", Sexo.FEMININO, LocalDate.of(1990, 5, 10),
                TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", Gravidade.CRITICO, AGORA);
        CampanhaDoacao campanha = new CampanhaDoacao(paciente, null, AGORA);

        CampanhaDoacaoRepository campanhas = RepositorioFalso.respondendo(CampanhaDoacaoRepository.class, "findById",
                argumentos -> Optional.ofNullable(ID_CAMPANHA.equals(argumentos[0]) ? campanha : null));
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(doador.getId().equals(argumentos[0]) ? doador : null));

        // "Google" com uma resposta fixa: 9,412 km em 19 min.
        RotaAteHospitalService servico = new RotaAteHospitalService(campanhas,
                (origem, destino) -> Optional.of(new Trajeto(9.412, 19, FonteDoTrajeto.GOOGLE_ROUTES)),
                new IdentificacaoService(usuarios));

        api = MockMvcBuilders.standaloneSetup(new RotaAteHospitalController(servico))
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    private MockHttpServletResponse executar(UUID usuario, String url, String corpo) throws Exception
    {
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders.post(url);
        if (corpo != null)
        {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        if (usuario != null)
        {
            requisicao.header("X-Usuario-Id", usuario.toString());
        }
        return api.perform(requisicao).andReturn().getResponse();
    }

    private ErroDTO erro(UUID usuario, String url, String corpo) throws Exception
    {
        MockHttpServletResponse resposta = executar(usuario, url, corpo);
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        return json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
    }

    @Test
    public void semCorpoDevolveODestinoComAsCoordenadasCadastradas() throws Exception
    {
        MockHttpServletResponse resposta = executar(doador.getId(), URL, null);

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode rota = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals(ID_CAMPANHA.toString(), rota.get("campanhaId").asText());
        Assertions.assertEquals("hc-pe", rota.at("/destino/hospitalId").asText());
        Assertions.assertEquals("Hospital das Clínicas de PE", rota.at("/destino/nome").asText());
        Assertions.assertEquals(-8.0512, rota.at("/destino/latitude").asDouble());
        Assertions.assertEquals(-34.9478, rota.at("/destino/longitude").asDouble());
        Assertions.assertTrue(rota.get("trajeto").isNull());
    }

    @Test
    public void comLocalizacaoDevolveDistanciaComUmaCasaETempo() throws Exception
    {
        MockHttpServletResponse resposta = executar(doador.getId(), URL, "{\"latitude\": -8.0578, \"longitude\": -34.8829}");

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode trajeto = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8)).get("trajeto");
        Assertions.assertEquals(9.4, trajeto.get("distanciaKm").asDouble());
        Assertions.assertEquals(19, trajeto.get("duracaoMinutos").asInt());
        Assertions.assertEquals("GOOGLE_ROUTES", trajeto.get("fonte").asText());
    }

    @Test
    public void semLoginDa401() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Não autorizado", 401,
                "Entre no sistema para calcular a rota até o hospital.", URL), erro(null, URL, null));
    }

    @Test
    public void campanhaQueNaoExisteDa404() throws Exception
    {
        UUID outra = UUID.fromString("00000000-0000-4000-8000-000000000404");
        String url = "/api/v1/campanhas/" + outra + "/rota";

        Assertions.assertEquals(new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhuma campanha encontrada com id " + outra + ".", url), erro(doador.getId(), url, null));
    }

    @Test
    public void latitudeForaDoMapaDa400() throws Exception
    {
        ErroDTO erro = erro(doador.getId(), URL, "{\"latitude\": 95, \"longitude\": -34.8829}");

        Assertions.assertEquals(400, erro.status());
        Assertions.assertEquals("Localização inválida: latitude precisa estar entre -90 e 90.", erro.detail());
    }

    @Test
    public void latitudeEmTextoDa400DizendoOCampo() throws Exception
    {
        ErroDTO erro = erro(doador.getId(), URL, "{\"latitude\": \"perto do hospital\", \"longitude\": -34.8829}");

        Assertions.assertEquals(400, erro.status());
        Assertions.assertTrue(erro.detail().contains("'latitude'"), erro.detail());
    }
}
