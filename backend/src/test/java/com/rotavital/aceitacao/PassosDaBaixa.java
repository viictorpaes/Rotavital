package com.rotavital.aceitacao;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.rotavital.integracao.DadosDeTeste;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

// HU-08 — Baixa da bolsa usada no procedimento. O "Então" lê o status direto da tabela:
// confere que a mudança foi gravada no Postgres, não só devolvida pela API.
public class PassosDaBaixa
{
    @Autowired
    private TestRestTemplate api;

    @Autowired
    private JdbcTemplate jdbc;

    private ResponseEntity<JsonNode> resposta;

    @Quando("registro a bolsa {string} como {string}")
    public void registroABolsaComo(String bolsaId, String status)
    {
        HttpEntity<Map<String, String>> corpo = new HttpEntity<>(Map.of("status", Vocabulario.status(status).name()));
        resposta = api.exchange("/api/v1/hemocomponentes/{id}", HttpMethod.PATCH, corpo, JsonNode.class, bolsaId);
    }

    // O descarte exige um motivo (até 500 caracteres); sem ele a API responde 400.
    @Quando("descarto a bolsa {string} pelo motivo {string}")
    public void descartoABolsaPeloMotivo(String bolsaId, String motivo)
    {
        // PI3-243: quem descarta é o admin, e o motivo do cenário vai como observação de "Outro".
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set("X-Usuario-Id", new DadosDeTeste(jdbc).admin().toString());
        HttpEntity<Map<String, String>> corpo = new HttpEntity<>(
                Map.of("status", "DESCARTADA", "categoriaDescarte", "OUTRO", "motivoDescarte", motivo), cabecalhos);
        resposta = api.exchange("/api/v1/hemocomponentes/{id}", HttpMethod.PATCH, corpo, JsonNode.class, bolsaId);
    }

    @Entao("a bolsa {string} fica {string} no estoque")
    public void aBolsaFicaNoEstoque(String bolsaId, String status)
    {
        Assertions.assertEquals(200, resposta.getStatusCode().value(), "Resposta: " + resposta.getBody());
        aBolsaContinuaNoEstoque(bolsaId, status);
    }

    @Entao("a movimentação é recusada com a mensagem {string}")
    public void aMovimentacaoERecusada(String mensagem)
    {
        Assertions.assertEquals(422, resposta.getStatusCode().value(), "Resposta: " + resposta.getBody());
        Assertions.assertEquals(mensagem, resposta.getBody().get("detail").asText());
    }

    @Entao("a bolsa {string} continua {string} no estoque")
    public void aBolsaContinuaNoEstoque(String bolsaId, String status)
    {
        Assertions.assertEquals(Vocabulario.status(status).name(), new DadosDeTeste(jdbc).statusDaBolsa(bolsaId));
    }
}
