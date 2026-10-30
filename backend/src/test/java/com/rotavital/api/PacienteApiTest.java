package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PacienteRepository;
import com.rotavital.repositorio.UsuarioRepository;
import com.rotavital.servico.IdentificacaoService;
import com.rotavital.servico.PacienteService;
import com.rotavital.servico.RepositorioFalso;

// /api/v1/pacientes (PI3-194) do jeito que a tela recebe: status HTTP, Location e corpo.
public class PacienteApiTest
{
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 10, 29, 10, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final String CORPO_VALIDO = """
            {"hospitalId":"HS-01","nome":"Carlos Lima","sexo":"MASCULINO","dataNascimento":"1980-01-15",
             "tipoSanguineo":"A_POSITIVO","tipoComponente":"PLAQUETAS","unidadesNecessarias":3,
             "causa":"Leucemia","gravidade":"ATENCAO"}""";

    private final ObjectMapper json = new ObjectMapper();
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, "HS-01", null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João", PapelUsuario.DOADOR, null, TipoSanguineo.O_POSITIVO);
    private final Hospital hospital = new Hospital("HS-01", "Hospital da Restauração", new Endereco("Recife/PE", -8.05, -34.89));
    private final UUID idExistente = UUID.randomUUID();
    private final List<Paciente> naTabela = new ArrayList<>();
    private long campanhasDoPaciente;
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        naTabela.add(new Paciente(hospital, "Maria Souza", Sexo.FEMININO, LocalDate.of(1990, 5, 10),
                TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", Gravidade.CRITICO, AGORA));

        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico
                        : doador.getId().equals(argumentos[0]) ? doador : null));
        HospitalRepository hospitais = RepositorioFalso.respondendo(HospitalRepository.class, "findById",
                argumentos -> Optional.ofNullable("HS-01".equals(argumentos[0]) ? hospital : null));
        PacienteRepository pacientes = RepositorioFalso.respondendo(PacienteRepository.class, Map.of(
                "findById", argumentos -> idExistente.equals(argumentos[0]) ? naTabela.stream().findFirst() : Optional.empty(),
                "buscarComFiltros", argumentos -> naTabela,
                "save", argumentos -> comId((Paciente) argumentos[0]),
                "delete", argumentos -> null,
                "contarCampanhas", argumentos -> campanhasDoPaciente,
                "contarProcedimentos", argumentos -> 0L,
                "contarRequisicoes", argumentos -> 0L));
        Clock relogio = Clock.fixed(AGORA.toInstant(), RelogioConfig.FUSO);

        api = MockMvcBuilders.standaloneSetup(new PacienteController(
                        new PacienteService(pacientes, hospitais, new IdentificacaoService(usuarios), relogio), relogio))
                .setControllerAdvice(new TratadorDeErros())
                // Mesmo Jackson que o Spring Boot configura no app: datas saem como "AAAA-MM-DD".
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    // O id é gerado pelo Hibernate ao gravar; aqui o "save" falso faz esse papel.
    private Paciente comId(Paciente paciente)
    {
        try
        {
            java.lang.reflect.Field id = Paciente.class.getDeclaredField("id");
            id.setAccessible(true);
            if (id.get(paciente) == null)
            {
                id.set(paciente, UUID.fromString("00000000-0000-0000-0000-000000000175"));
            }
            return paciente;
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private MockHttpServletResponse executar(MockHttpServletRequestBuilder requisicao) throws Exception
    {
        return api.perform(requisicao).andReturn().getResponse();
    }

    private ErroDTO erro(MockHttpServletRequestBuilder requisicao) throws Exception
    {
        MockHttpServletResponse resposta = executar(requisicao);
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        ErroDTO erro = json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
        Assertions.assertEquals(erro.status(), resposta.getStatus());
        return erro;
    }

    private MockHttpServletRequestBuilder comCorpo(MockHttpServletRequestBuilder requisicao, UUID usuario, String corpo)
    {
        requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        return usuario == null ? requisicao : requisicao.header("X-Usuario-Id", usuario.toString());
    }

    @Test
    public void listaComIdadeEHospital() throws Exception
    {
        MockHttpServletResponse resposta = executar(MockMvcRequestBuilders.get("/api/v1/pacientes").param("status", "AGUARDANDO"));

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode paciente = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8)).get(0);
        Assertions.assertEquals("Maria Souza", paciente.get("nome").asText());
        Assertions.assertEquals("1990-05-10", paciente.get("dataNascimento").asText());
        Assertions.assertEquals(36, paciente.get("idade").asInt());
        Assertions.assertEquals("Hospital da Restauração", paciente.get("hospitalNome").asText());
        Assertions.assertEquals("AGUARDANDO", paciente.get("status").asText());
    }

    @Test
    public void filtroComValorQueNaoExisteDa400() throws Exception
    {
        ErroDTO erro = erro(MockMvcRequestBuilders.get("/api/v1/pacientes").param("gravidade", "GRAVE"));

        Assertions.assertEquals(400, erro.status());
        Assertions.assertTrue(erro.detail().contains("CRITICO, ATENCAO, ESTAVEL"), erro.detail());
    }

    @Test
    public void cadastroDevolve201ComLocation() throws Exception
    {
        MockHttpServletResponse resposta = executar(comCorpo(MockMvcRequestBuilders.post("/api/v1/pacientes"),
                medico.getId(), CORPO_VALIDO));

        Assertions.assertEquals(201, resposta.getStatus());
        Assertions.assertEquals("/api/v1/pacientes/00000000-0000-0000-0000-000000000175", resposta.getHeader("Location"));
        JsonNode paciente = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals("Carlos Lima", paciente.get("nome").asText());
        Assertions.assertEquals("PLAQUETAS", paciente.get("tipoComponente").asText());
    }

    @Test
    public void cadastroSemLoginDa401() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Não autorizado", 401, "Entre no sistema para cadastrar paciente.",
                "/api/v1/pacientes"), erro(comCorpo(MockMvcRequestBuilders.post("/api/v1/pacientes"), null, CORPO_VALIDO)));
    }

    @Test
    public void doadorNaoCadastra403() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Acesso negado", 403, "Só médico pode cadastrar paciente.",
                "/api/v1/pacientes"), erro(comCorpo(MockMvcRequestBuilders.post("/api/v1/pacientes"), doador.getId(), CORPO_VALIDO)));
    }

    @Test
    public void corpoComCampoFaltandoDa400() throws Exception
    {
        ErroDTO erro = erro(comCorpo(MockMvcRequestBuilders.post("/api/v1/pacientes"), medico.getId(),
                CORPO_VALIDO.replace("\"causa\":\"Leucemia\",", "")));

        Assertions.assertEquals(new ErroDTO(null, "Requisição inválida", 400, "O campo 'causa' é obrigatório.",
                "/api/v1/pacientes"), erro);
    }

    @Test
    public void edicaoDevolve200() throws Exception
    {
        MockHttpServletResponse resposta = executar(comCorpo(MockMvcRequestBuilders.put("/api/v1/pacientes/" + idExistente),
                medico.getId(), CORPO_VALIDO));

        Assertions.assertEquals(200, resposta.getStatus());
        Assertions.assertEquals("Carlos Lima", json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8)).get("nome").asText());
    }

    @Test
    public void inexistenteDa404() throws Exception
    {
        UUID outro = UUID.randomUUID();
        Assertions.assertEquals(new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhum paciente encontrado com id " + outro + ".", "/api/v1/pacientes/" + outro),
                erro(MockMvcRequestBuilders.get("/api/v1/pacientes/" + outro)));
    }

    @Test
    public void remocaoDevolve204() throws Exception
    {
        MockHttpServletResponse resposta = executar(MockMvcRequestBuilders.delete("/api/v1/pacientes/" + idExistente)
                .header("X-Usuario-Id", medico.getId().toString()));

        Assertions.assertEquals(204, resposta.getStatus());
    }

    @Test
    public void pacienteDeCampanhaNaoERemovido422() throws Exception
    {
        campanhasDoPaciente = 1;

        Assertions.assertEquals(new ErroDTO(null, "Regra de negócio violada", 422,
                "O paciente Maria Souza não pode ser removido: ele tem registros que precisam ficar no "
                        + "histórico (1 campanha de doação).", "/api/v1/pacientes/" + idExistente),
                erro(MockMvcRequestBuilders.delete("/api/v1/pacientes/" + idExistente)
                        .header("X-Usuario-Id", medico.getId().toString())));
    }
}
