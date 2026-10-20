package com.rotavital.integracao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.integracao.RelogioDeTeste.RelogioAjustavel;

// HU-08 · T8.3 — Integração com o banco: o PATCH grava o status no Postgres, o gatilho
// trg_bolsa_hemocomponente_atualizado_em (migration) roda, e um pedido recusado não grava nada.
@TesteDeIntegracao
public class BaixaDaBolsaIT
{
    @Autowired
    private TestRestTemplate api;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RelogioAjustavel relogio;

    private DadosDeTeste dados;

    @BeforeEach
    public void bolsaReservadaNoBS01()
    {
        relogio.ajustarPara(LocalDate.of(2026, 10, 1));
        dados = new DadosDeTeste(jdbc);
        dados.limpar();
        dados.bancoDeSangue("BS-01");
        dados.bolsa("CH-1", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, LocalDate.of(2026, 10, 11),
                4.0, "R1", StatusBolsa.RESERVADA);
    }

    private ResponseEntity<JsonNode> patch(String bolsaId, Map<String, String> corpo)
    {
        return api.exchange("/api/v1/hemocomponentes/{id}", HttpMethod.PATCH, new HttpEntity<>(corpo), JsonNode.class, bolsaId);
    }

    private Timestamp atualizadoEm(String bolsaId)
    {
        return jdbc.queryForObject("select atualizado_em from public.bolsa_hemocomponente where id = ?", Timestamp.class, bolsaId);
    }

    @Test
    public void baixaGravaOStatusEOGatilhoAtualizaOCarimbo()
    {
        Timestamp antes = atualizadoEm("CH-1");

        ResponseEntity<JsonNode> resposta = patch("CH-1", Map.of("status", "UTILIZADA"));

        Assertions.assertEquals(200, resposta.getStatusCode().value());
        Assertions.assertEquals("UTILIZADA", resposta.getBody().get("status").asText());
        Assertions.assertEquals("UTILIZADA", dados.statusDaBolsa("CH-1"));
        Assertions.assertTrue(atualizadoEm("CH-1").after(antes), "O gatilho deveria atualizar atualizado_em.");
    }

    @Test
    public void movimentacaoRecusadaNaoGravaNada()
    {
        Timestamp antes = atualizadoEm("CH-1");

        ResponseEntity<JsonNode> resposta = patch("CH-1", Map.of("status", "ENTREGUE"));

        Assertions.assertEquals(422, resposta.getStatusCode().value());
        Assertions.assertEquals("RESERVADA", dados.statusDaBolsa("CH-1"));
        Assertions.assertEquals(antes, atualizadoEm("CH-1"));
    }

    @Test
    public void bolsaInexistenteDevolve404()
    {
        Assertions.assertEquals(404, patch("BOLSA-999", Map.of("status", "UTILIZADA")).getStatusCode().value());
    }

    @Test
    public void semStatusDevolve400()
    {
        Assertions.assertEquals(400, patch("CH-1", Map.of()).getStatusCode().value());
    }
}
