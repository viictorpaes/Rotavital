package com.rotavital.aceitacao;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

// HU-03 — Estoque por tipo sanguíneo com detalhe de lote.
public class PassosDoEstoque
{
    @Autowired
    private TestRestTemplate api;

    private ResponseEntity<JsonNode> resposta;

    @Quando("consulto o estoque do banco {string} filtrando pelo tipo {string}")
    public void consultoOEstoqueFiltrandoPeloTipo(String bancoId, String tipo)
    {
        resposta = api.getForEntity("/api/v1/bancos/{bancoId}/estoque?tipoSanguineo={tipo}", JsonNode.class,
                bancoId, Vocabulario.tipoSanguineo(tipo).name());
    }

    @Entao("vejo só os lotes {string}, da validade mais próxima para a mais distante")
    public void vejoSoOsLotes(String lotes)
    {
        Assertions.assertEquals(200, resposta.getStatusCode().value());

        List<String> ids = new ArrayList<>();
        resposta.getBody().get("bolsas").forEach(bolsa -> ids.add(bolsa.get("id").asText()));
        Assertions.assertEquals(List.of(lotes.split(",\\s*")), ids);
    }

    @Entao("o lote {string} aparece marcado como fora da faixa de temperatura")
    public void oLoteApareceForaDaFaixa(String lote)
    {
        JsonNode bolsa = null;
        for (JsonNode candidata : resposta.getBody().get("bolsas"))
        {
            if (lote.equals(candidata.get("id").asText()))
            {
                bolsa = candidata;
            }
        }

        Assertions.assertNotNull(bolsa, "O lote " + lote + " não veio na consulta.");
        Assertions.assertTrue(bolsa.get("foraDaFaixa").asBoolean());
    }
}
