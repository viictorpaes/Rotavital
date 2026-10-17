package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

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
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.servico.EstoqueService;
import com.rotavital.servico.RepositorioFalso;

// PATCH /api/v1/hemocomponentes/{id} (PI3-190) do jeito que a tela recebe: status HTTP e corpo.
public class EdicaoDeBolsaApiTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final String URL = "/api/v1/hemocomponentes/CH-1042";

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc api;

    @BeforeEach
    public void criarApiComABolsaCH1042()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        BolsaHemocomponente bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco);

        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.of(banco));
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                Map.of(
                        "findById", argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null),
                        "save", argumentos -> argumentos[0]));
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        api = MockMvcBuilders.standaloneSetup(new EstoqueController(new EstoqueService(bancosFalsos, bolsasFalsas, relogio), null))
                .setControllerAdvice(new TratadorDeErros())
                // Mesmo Jackson que o Spring Boot configura no app: datas saem como "AAAA-MM-DD".
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    private MockHttpServletResponse patch(String url, String corpo) throws Exception
    {
        return api.perform(MockMvcRequestBuilders.patch(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andReturn().getResponse();
    }

    private ErroDTO erro(String url, String corpo) throws Exception
    {
        MockHttpServletResponse resposta = patch(url, corpo);

        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        ErroDTO erro = json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
        Assertions.assertEquals(erro.status(), resposta.getStatus());
        return erro;
    }

    @Test
    public void reservarDevolve200ComABolsaAtualizada() throws Exception
    {
        MockHttpServletResponse resposta = patch(URL, "{\"status\":\"RESERVADA\"}");

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode bolsa = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals("RESERVADA", bolsa.get("status").asText());
        Assertions.assertEquals("CH-1042", bolsa.get("id").asText());
    }

    @Test
    public void correcaoDevolveOsNovosValores() throws Exception
    {
        MockHttpServletResponse resposta = patch(URL,
                "{\"dataValidade\":\"2026-10-20\",\"volumeMl\":400,\"temperaturaCelsius\":7.5,\"localizacao\":\"R2 · P1 · N1\"}");

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode bolsa = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals("2026-10-20", bolsa.get("dataValidade").asText());
        Assertions.assertEquals(400.0, bolsa.get("volumeMl").asDouble());
        Assertions.assertEquals(7.5, bolsa.get("temperaturaCelsius").asDouble());
        // 7,5 °C passa da faixa ideal das hemácias (2 a 6 °C): a correção é aceita e a bolsa vira alerta.
        Assertions.assertTrue(bolsa.get("foraDaFaixa").asBoolean());
        Assertions.assertEquals("R2 · P1 · N1", bolsa.get("localizacao").asText());
    }

    @Test
    public void bolsaInexistenteDevolve404() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhuma bolsa encontrada com id BOLSA-999.", "/api/v1/hemocomponentes/BOLSA-999"),
                erro("/api/v1/hemocomponentes/BOLSA-999", "{\"status\":\"RESERVADA\"}"));
    }

    @Test
    public void corpoVazioDevolve400() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Requisição inválida", 400,
                "Informe ao menos um campo para alterar: status, dataValidade, volumeMl, temperaturaCelsius ou "
                        + "localizacao.", URL),
                erro(URL, "{}"));
    }

    @Test
    public void statusForaDaListaDevolve400ComOsValoresAceitos() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Requisição inválida", 400,
                "O campo 'status' não aceita o valor 'JOGADA_FORA'. Valores aceitos: DISPONIVEL, RESERVADA, "
                        + "EM_TRANSITO, ENTREGUE, DESCARTADA, UTILIZADA.", URL),
                erro(URL, "{\"status\":\"JOGADA_FORA\"}"));
    }

    @Test
    public void statusDoTransporteDevolve422() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Regra de negócio violada", 422,
                "O status ENTREGUE não pode ser escolhido aqui: use DISPONIVEL, RESERVADA ou DESCARTADA.", URL),
                erro(URL, "{\"status\":\"ENTREGUE\"}"));
    }

    @Test
    public void descartadaNaoAceitaMaisAlteracao() throws Exception
    {
        Assertions.assertEquals(200, patch(URL, "{\"status\":\"DESCARTADA\"}").getStatus());

        Assertions.assertEquals(new ErroDTO(null, "Regra de negócio violada", 422,
                "A bolsa CH-1042 está DESCARTADA e não pode mais ser alterada: só bolsas DISPONIVEL ou RESERVADA podem.",
                URL),
                erro(URL, "{\"status\":\"DISPONIVEL\"}"));
    }
}
