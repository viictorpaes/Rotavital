package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.TransferenciaDeBolsaRepository;
import com.rotavital.repositorio.UsuarioRepository;
import com.rotavital.servico.CorrecaoDeBolsaService;
import com.rotavital.servico.EstoqueService;
import com.rotavital.servico.IdentificacaoService;
import com.rotavital.servico.MovimentacaoDeBolsaService;
import com.rotavital.servico.RepositorioFalso;

// PI3-243: o descarte pelo PATCH /hemocomponentes/{id}, com motivo padronizado e só para admin.
public class DescarteDeBolsaApiTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final ObjectMapper json = new ObjectMapper();
    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Admin Rota Vital", PapelUsuario.ADMIN, null, null);
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana", PapelUsuario.MEDICO, "BS-01", null);

    private BolsaHemocomponente bolsa;
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(45), HOJE.minusDays(3), "LOTE-0001", 450.0, 4.0, "R1 · P3", banco);

        BolsaHemocomponenteRepository bolsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, "findById",
                argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null));
        Map<UUID, Usuario> pessoas = Map.of(admin.getId(), admin, medico.getId(), medico);
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(pessoas.get(argumentos[0])));
        TransferenciaDeBolsaRepository transferencias = RepositorioFalso.respondendo(TransferenciaDeBolsaRepository.class,
                "findByBolsaIdAndStatus", argumentos -> Optional.empty());
        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, Map.of());
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        IdentificacaoService identificacao = new IdentificacaoService(usuarios);
        MovimentacaoDeBolsaService movimentacao = new MovimentacaoDeBolsaService(bolsas, transferencias, identificacao, relogio);
        CorrecaoDeBolsaService correcao = new CorrecaoDeBolsaService(bolsas, new EstoqueService(bancos, bolsas, relogio),
                identificacao, relogio);

        api = MockMvcBuilders.standaloneSetup(new MovimentacaoDeBolsaController(movimentacao, correcao))
                .setControllerAdvice(new TratadorDeErros())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    private MockHttpServletResponse patch(UUID usuario, String corpo) throws Exception
    {
        return api.perform(MockMvcRequestBuilders.patch("/api/v1/hemocomponentes/CH-1042")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo)
                .header("X-Usuario-Id", usuario.toString()))
                .andReturn().getResponse();
    }

    private JsonNode corpo(MockHttpServletResponse resposta) throws Exception
    {
        return json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    public void adminDescartaBolsaVencidaComMotivoEData() throws Exception
    {
        MockHttpServletResponse resposta = patch(admin.getId(),
                "{\"status\":\"DESCARTADA\",\"categoriaDescarte\":\"VALIDADE_EXPIRADA\",\"motivoDescarte\":\"Venceu na prateleira\"}");

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode bolsaDescartada = corpo(resposta);
        Assertions.assertEquals("DESCARTADA", bolsaDescartada.get("status").asText());
        Assertions.assertEquals("VALIDADE_EXPIRADA", bolsaDescartada.get("categoriaDescarte").asText());
        Assertions.assertEquals("Venceu na prateleira", bolsaDescartada.get("motivoDescarte").asText());
        Assertions.assertTrue(bolsaDescartada.get("dataDescarte").asText().startsWith("2026-10-01T12:00"));
        Assertions.assertEquals(StatusBolsa.DESCARTADA, bolsa.getStatus());
    }

    @Test
    public void medicoRecebe403() throws Exception
    {
        MockHttpServletResponse resposta = patch(medico.getId(),
                "{\"status\":\"DESCARTADA\",\"categoriaDescarte\":\"VALIDADE_EXPIRADA\"}");

        Assertions.assertEquals(403, resposta.getStatus());
        Assertions.assertEquals("Só admin pode descartar uma bolsa.", corpo(resposta).get("detail").asText());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }

    @Test
    public void semMotivoPadronizadoRecebe400() throws Exception
    {
        MockHttpServletResponse resposta = patch(admin.getId(), "{\"status\":\"DESCARTADA\",\"motivoDescarte\":\"Venceu\"}");

        Assertions.assertEquals(400, resposta.getStatus());
    }

    @Test
    public void motivoQueNaoExisteRecebe400() throws Exception
    {
        MockHttpServletResponse resposta = patch(admin.getId(), "{\"status\":\"DESCARTADA\",\"categoriaDescarte\":\"PERDIDA\"}");

        Assertions.assertEquals(400, resposta.getStatus());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }
}
