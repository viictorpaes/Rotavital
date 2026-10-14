package com.rotavital.aceitacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

// HU-02 — Caixa de alertas do painel.
public class PassosDosAlertas
{
    @Autowired
    private TestRestTemplate api;

    private ResponseEntity<JsonNode> resposta;

    @Quando("abro os alertas do banco {string}")
    public void abroOsAlertas(String bancoId)
    {
        resposta = api.getForEntity("/api/v1/bancos/{bancoId}/alertas", JsonNode.class, bancoId);
    }

    @Entao("vejo os alertas, nesta ordem:")
    public void vejoOsAlertas(DataTable esperados)
    {
        List<String> linhasEsperadas = new ArrayList<>();
        for (Map<String, String> linha : esperados.asMaps())
        {
            linhasEsperadas.add(linha.get("lote") + " " + linha.get("alerta") + " " + linha.get("nível"));
        }

        List<String> recebidas = new ArrayList<>();
        alertas().forEach(a -> recebidas.add(a.get("bolsaId").asText() + " " + a.get("tipo").asText() + " "
                + a.get("nivel").asText()));

        Assertions.assertEquals(linhasEsperadas, recebidas);
    }

    @Entao("o painel conta {int} alertas críticos")
    public void oPainelContaAlertasCriticos(int criticos)
    {
        Assertions.assertEquals(criticos, resposta.getBody().get("criticos").asInt());
    }

    @Entao("não há alerta para os lotes {string}")
    public void naoHaAlertaParaOsLotes(String lotes)
    {
        List<String> semAlerta = List.of(lotes.split(",\\s*"));
        alertas().forEach(a -> Assertions.assertFalse(semAlerta.contains(a.get("bolsaId").asText()),
                "Não deveria haver alerta para " + a.get("bolsaId").asText()));
    }

    private JsonNode alertas()
    {
        Assertions.assertEquals(200, resposta.getStatusCode().value(), "Resposta: " + resposta.getBody());
        return resposta.getBody().get("alertas");
    }
}
