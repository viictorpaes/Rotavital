package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.servico.EstatisticaDescritivaService;
import com.rotavital.servico.RepositorioFalso;

// GET /api/v1/bancos/{bancoId}/estoque/estatisticas do jeito que a tela de Estatísticas recebe.
public class EstatisticasDoEstoqueApiTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc api;

    @BeforeEach
    public void criarApi()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        banco.getEstoque().adicionarBolsa(new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_NEGATIVO, HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-0001", 450.0, 4.0, null, banco));
        banco.getEstoque().adicionarBolsa(new BolsaHemocomponente("CH-1043", TipoComponente.HEMACIAS,
                TipoSanguineo.O_NEGATIVO, HOJE.minusDays(5), HOJE.plusDays(10), "LOTE-0002", 350.0, 4.0, null, banco));

        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, "findById",
                argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        api = MockMvcBuilders.standaloneSetup(new EstatisticasDoEstoqueController(new EstatisticaDescritivaService(bancos, relogio)))
                .setControllerAdvice(new TratadorDeErros())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    private MockHttpServletResponse get(String bancoId) throws Exception
    {
        return api.perform(MockMvcRequestBuilders.get("/api/v1/bancos/" + bancoId + "/estoque/estatisticas"))
                .andReturn().getResponse();
    }

    @Test
    public void devolveAsDistribuicoesEAsMedidasPorComponente() throws Exception
    {
        MockHttpServletResponse resposta = get("BS-01");

        Assertions.assertEquals(200, resposta.getStatus());
        JsonNode corpo = json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
        Assertions.assertEquals("BS-01", corpo.get("bancoId").asText());
        Assertions.assertEquals("2026-10-01", corpo.get("dataDeReferencia").asText());
        Assertions.assertEquals(2, corpo.get("emEstoque").asLong());
        Assertions.assertEquals("O_NEGATIVO", corpo.get("porTipoSanguineo").get("moda").get(0).asText());
        Assertions.assertEquals(8, corpo.get("porTipoSanguineo").get("linhas").size());
        Assertions.assertEquals(100.0, corpo.get("porComponente").get("linhas").get(0).get("frequenciaRelativa").asDouble());

        JsonNode hemacias = corpo.get("componentes").get(0);
        Assertions.assertEquals("HEMACIAS", hemacias.get("componente").asText());
        Assertions.assertEquals(20.0, hemacias.get("diasAteVencer").get("media").asDouble());
        Assertions.assertEquals(400.0, hemacias.get("volumeMl").get("media").asDouble());
        // Plaquetas sem bolsa: as medidas vêm nulas, não zero.
        Assertions.assertTrue(corpo.get("componentes").get(2).get("diasAteVencer").get("media").isNull());
    }

    @Test
    public void bancoInexistenteDa404() throws Exception
    {
        Assertions.assertEquals(404, get("BS-99").getStatus());
    }
}
