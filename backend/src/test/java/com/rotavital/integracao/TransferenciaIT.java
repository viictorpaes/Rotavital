package com.rotavital.integracao;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.integracao.RelogioDeTeste.RelogioAjustavel;

// PI3-236 — Envio e recebimento pela API, gravando no Postgres com as constraints da migration.
@TesteDeIntegracao
public class TransferenciaIT
{
    @Autowired
    private TestRestTemplate api;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RelogioAjustavel relogio;

    private DadosDeTeste dados;
    private UUID medico;

    @BeforeEach
    public void redeComUmaBolsa()
    {
        relogio.ajustarPara(LocalDate.of(2026, 10, 1));
        dados = new DadosDeTeste(jdbc);
        dados.limpar();
        jdbc.execute("truncate table public.usuario cascade");
        dados.bancoDeSangue("BS-01");
        dados.hospital("hc-pe", "Hospital das Clínicas de PE");
        dados.conexao("BS-01", "hc-pe", 7.8, 18);
        dados.bolsa("CH-1", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, LocalDate.of(2026, 10, 11),
                4.0, "R1", StatusBolsa.DISPONIVEL);
        medico = dados.medico();
    }

    private HttpHeaders comoMedico()
    {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set("X-Usuario-Id", medico.toString());
        return cabecalhos;
    }

    private ResponseEntity<JsonNode> despachar(String bolsaId, String destinoId)
    {
        return api.postForEntity("/api/v1/transferencias",
                new HttpEntity<>(Map.of("bolsaId", bolsaId, "destinoId", destinoId), comoMedico()), JsonNode.class);
    }

    private ResponseEntity<JsonNode> receber(String id)
    {
        return api.postForEntity("/api/v1/transferencias/{id}/recebimento", new HttpEntity<>(comoMedico()), JsonNode.class, id);
    }

    private ResponseEntity<JsonNode> patch(String bolsaId, String status)
    {
        return api.exchange("/api/v1/hemocomponentes/{id}", HttpMethod.PATCH,
                new HttpEntity<>(Map.of("status", status)), JsonNode.class, bolsaId);
    }

    private String coluna(String nome, String bolsaId)
    {
        return jdbc.queryForObject("select " + nome + " from public.bolsa_hemocomponente where id = ?", String.class, bolsaId);
    }

    private String statusDaTransferencia(String id)
    {
        return jdbc.queryForObject("select status from public.transferencia_bolsa where id = ?::uuid", String.class, id);
    }

    @Test
    public void despachoGravaATransferenciaETiraABolsaDoEstoque()
    {
        ResponseEntity<JsonNode> resposta = despachar("CH-1", "hc-pe");

        Assertions.assertEquals(201, resposta.getStatusCode().value());
        JsonNode corpo = resposta.getBody();
        Assertions.assertEquals("BS-01", corpo.get("origemId").asText());
        Assertions.assertEquals("hc-pe", corpo.get("destinoId").asText());
        Assertions.assertEquals("EM_TRANSITO", corpo.get("status").asText());
        Assertions.assertEquals(18, corpo.get("duracaoMinutos").asInt());
        Assertions.assertEquals("CONEXOES_DA_REDE", corpo.get("fonte").asText());
        Assertions.assertEquals("Dra. Ana Lima", corpo.get("despachadaPorNome").asText());

        Assertions.assertEquals("EM_TRANSITO", dados.statusDaBolsa("CH-1"));
        Assertions.assertEquals("Em transporte refrigerado → Hospital das Clínicas de PE", coluna("localizacao", "CH-1"));
        Assertions.assertNull(coluna("hospital_atual_id", "CH-1"));
        Assertions.assertEquals("BS-01", coluna("banco_origem_id", "CH-1"));

        JsonNode ativas = api.getForEntity("/api/v1/transferencias?status=EM_TRANSITO", JsonNode.class).getBody();
        Assertions.assertEquals(1, ativas.size());
        Assertions.assertEquals("CH-1", ativas.get(0).get("bolsaId").asText());
    }

    @Test
    public void recebimentoPoeABolsaNoHospitalComPrecisao()
    {
        String id = despachar("CH-1", "hc-pe").getBody().get("id").asText();

        ResponseEntity<JsonNode> resposta = receber(id);

        Assertions.assertEquals(200, resposta.getStatusCode().value());
        Assertions.assertEquals("RECEBIDA", resposta.getBody().get("status").asText());
        Assertions.assertEquals("ENTREGUE", dados.statusDaBolsa("CH-1"));
        Assertions.assertEquals("hc-pe", coluna("hospital_atual_id", "CH-1"));
        Assertions.assertEquals("Hospital das Clínicas de PE", coluna("localizacao", "CH-1"));

        JsonNode bolsa = api.getForEntity("/api/v1/hemocomponentes/CH-1", JsonNode.class).getBody();
        Assertions.assertEquals("hc-pe", bolsa.get("hospitalAtualId").asText());
        Assertions.assertEquals("BS-01", bolsa.get("bancoOrigemId").asText());

        Assertions.assertEquals(422, receber(id).getStatusCode().value());
    }

    @Test
    public void bolsaEmTransferenciaNaoSaiDeNovoENemChegaPeloPatch()
    {
        String id = despachar("CH-1", "hc-pe").getBody().get("id").asText();

        Assertions.assertEquals(422, despachar("CH-1", "hc-pe").getStatusCode().value());
        Assertions.assertEquals(422, patch("CH-1", "ENTREGUE").getStatusCode().value());
        Assertions.assertEquals("EM_TRANSITO", statusDaTransferencia(id));
    }

    @Test
    public void descarteNoCaminhoEncerraATransferencia()
    {
        String id = despachar("CH-1", "hc-pe").getBody().get("id").asText();

        Assertions.assertEquals(200, patch("CH-1", "DESCARTADA").getStatusCode().value());

        Assertions.assertEquals("CANCELADA", statusDaTransferencia(id));
        Assertions.assertEquals(422, receber(id).getStatusCode().value());
    }

    @Test
    public void bolsaForaDaFaixaNaoSaiENadaEhGravado()
    {
        dados.bolsa("CH-2", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, LocalDate.of(2026, 10, 11),
                9.0, "R2", StatusBolsa.DISPONIVEL);

        Assertions.assertEquals(422, despachar("CH-2", "hc-pe").getStatusCode().value());

        Assertions.assertEquals("DISPONIVEL", dados.statusDaBolsa("CH-2"));
        Assertions.assertEquals(0, jdbc.queryForObject("select count(*) from public.transferencia_bolsa", Integer.class));
    }

    @Test
    public void semMedicoNaoHaEnvio()
    {
        ResponseEntity<JsonNode> resposta = api.postForEntity("/api/v1/transferencias",
                Map.of("bolsaId", "CH-1", "destinoId", "hc-pe"), JsonNode.class);

        Assertions.assertEquals(401, resposta.getStatusCode().value());
        Assertions.assertEquals("DISPONIVEL", dados.statusDaBolsa("CH-1"));
    }

    @Test
    public void oBancoRecusaDuasTransferenciasAtivasDaMesmaBolsa()
    {
        despachar("CH-1", "hc-pe");

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                insert into public.transferencia_bolsa
                    (bolsa_id, origem_id, destino_id, distancia_km, duracao_minutos, fonte_trajeto, chegada_prevista_em)
                values ('CH-1', 'BS-01', 'hc-pe', 7.8, 18, 'CONEXOES_DA_REDE', now() + interval '18 minutes')
                """));
    }

    @Test
    public void oBancoRecusaOrigemQueNaoEhHemocentroEDestinoQueNaoEhHospital()
    {
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                insert into public.transferencia_bolsa
                    (bolsa_id, origem_id, destino_id, distancia_km, duracao_minutos, fonte_trajeto, chegada_prevista_em)
                values ('CH-1', 'hc-pe', 'BS-01', 7.8, 18, 'CONEXOES_DA_REDE', now() + interval '18 minutes')
                """));
    }
}
