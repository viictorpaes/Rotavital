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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rotavital.api.dto.comum.ErroDTO;
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

// PATCH /api/v1/hemocomponentes/{id} do jeito que a tela recebe: a correção dos dados (HU-03) e a
// mudança de status (HU-08) no mesmo endereço, uma de cada vez.
public class EdicaoDeBolsaApiTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final ObjectMapper json = new ObjectMapper();
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana", PapelUsuario.MEDICO, "BS-01", null);
    private BolsaHemocomponente bolsa;
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-0001", 450.0, 4.0, "R1 · P3", banco);

        BolsaHemocomponenteRepository bolsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, Map.of(
                "findById", argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null),
                "existsByLoteSinteticoAndTipoComponenteAndIdNot", argumentos -> false));
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico : null));
        TransferenciaDeBolsaRepository transferencias = RepositorioFalso.respondendo(TransferenciaDeBolsaRepository.class,
                "findByBolsaIdAndStatus", argumentos -> Optional.empty());
        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, Map.of());
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        IdentificacaoService identificacao = new IdentificacaoService(usuarios);
        CorrecaoDeBolsaService correcao = new CorrecaoDeBolsaService(bolsas, new EstoqueService(bancos, bolsas, relogio),
                identificacao, relogio);
        MovimentacaoDeBolsaService movimentacao = new MovimentacaoDeBolsaService(bolsas, transferencias, identificacao, relogio);

        api = MockMvcBuilders.standaloneSetup(new MovimentacaoDeBolsaController(movimentacao, correcao))
                .setControllerAdvice(new TratadorDeErros())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    private MockHttpServletRequestBuilder patch(UUID usuario, String corpo)
    {
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders.patch("/api/v1/hemocomponentes/CH-1042")
                .contentType(MediaType.APPLICATION_JSON).content(corpo);
        return usuario == null ? requisicao : requisicao.header("X-Usuario-Id", usuario.toString());
    }

    private ErroDTO erro(MockHttpServletRequestBuilder requisicao) throws Exception
    {
        MockHttpServletResponse resposta = api.perform(requisicao).andReturn().getResponse();
        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        ErroDTO erro = json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
        Assertions.assertEquals(erro.status(), resposta.getStatus());
        return erro;
    }

    @Test
    public void correcaoDevolve200ComABolsaCorrigida() throws Exception
    {
        MockHttpServletResponse resposta = api.perform(patch(medico.getId(),
                "{\"volumeMl\":430,\"dataValidade\":\"2026-10-30\",\"localizacao\":\"R2 · P1\"}"))
                .andReturn().getResponse();

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode corpo = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals("CH-1042", corpo.get("id").asText());
        Assertions.assertEquals(430.0, bolsa.getVolumeMl());
        Assertions.assertEquals(LocalDate.of(2026, 10, 30), bolsa.getDataValidade());
        Assertions.assertEquals("R2 · P1", bolsa.getLocalizacao());
    }

    @Test
    public void statusECorrecaoJuntosDao400() throws Exception
    {
        ErroDTO erro = erro(patch(medico.getId(), "{\"status\":\"RESERVADA\",\"volumeMl\":430}"));

        Assertions.assertEquals(400, erro.status());
        Assertions.assertEquals("Mude o status ou corrija os dados da bolsa, uma coisa de cada vez.", erro.detail());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }

    @Test
    public void corpoVazioDa400() throws Exception
    {
        ErroDTO erro = erro(patch(medico.getId(), "{}"));

        Assertions.assertEquals(400, erro.status());
        Assertions.assertEquals("Envie o novo status ou ao menos um campo para corrigir.", erro.detail());
    }

    @Test
    public void correcaoSemUsuarioDa401() throws Exception
    {
        Assertions.assertEquals(401, erro(patch(null, "{\"volumeMl\":430}")).status());
        Assertions.assertEquals(450.0, bolsa.getVolumeMl());
    }

    @Test
    public void medicoCorrigindoTipoSanguineoDa403() throws Exception
    {
        ErroDTO erro = erro(patch(medico.getId(), "{\"tipoSanguineo\":\"A_POSITIVO\"}"));

        Assertions.assertEquals(403, erro.status());
        Assertions.assertEquals("Só admin pode corrigir o tipo sanguíneo de uma bolsa.", erro.detail());
    }

    @Test
    public void correcaoQueQuebraRegraDa422() throws Exception
    {
        ErroDTO erro = erro(patch(medico.getId(), "{\"volumeMl\":900}"));

        Assertions.assertEquals(422, erro.status());
        Assertions.assertEquals("O volume (900.0 ml) passa do máximo de 700.0 ml por bolsa.", erro.detail());
    }

    // O descarte em si é testado em DescarteDeBolsaApiTest; aqui só que ele não passa sem usuário (PI3-243).
    @Test
    public void descarteSemUsuarioDa401() throws Exception
    {
        ErroDTO erro = erro(patch(null, "{\"status\":\"DESCARTADA\",\"categoriaDescarte\":\"BOLSA_DANIFICADA\"}"));

        Assertions.assertEquals(401, erro.status());
        Assertions.assertEquals("Entre no sistema para descartar uma bolsa.", erro.detail());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }
}
