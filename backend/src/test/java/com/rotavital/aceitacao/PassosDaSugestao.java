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

// HU-04 — Sugestão de lote por FEFO e compatibilidade ABO/Rh.
public class PassosDaSugestao
{
    @Autowired
    private TestRestTemplate api;

    private ResponseEntity<JsonNode> resposta;

    @Quando("peço {int} unidade(s) de {word} para um paciente {string} no banco {string}")
    public void pecoUnidades(int quantidade, String componente, String tipoPaciente, String bancoId)
    {
        resposta = api.getForEntity(
                "/api/v1/bancos/{bancoId}/sugestao-lote?tipoComponente={componente}&tipoSanguineo={tipo}&quantidade={quantidade}",
                JsonNode.class, bancoId, Vocabulario.componente(componente).name(),
                Vocabulario.tipoSanguineo(tipoPaciente).name(), quantidade);
    }

    @Entao("o sistema sugere os lotes {string}")
    public void oSistemaSugereOsLotes(String lotes)
    {
        Assertions.assertEquals(List.of(lotes.split(",\\s*")), lotesSugeridos());
    }

    @Entao("o sistema não sugere nenhum lote")
    public void oSistemaNaoSugereNenhumLote()
    {
        Assertions.assertEquals(List.of(), lotesSugeridos());
    }

    @Entao("a sugestão avisa que faltam {int} unidade(s)")
    public void aSugestaoAvisaQueFaltam(int faltam)
    {
        Assertions.assertEquals(faltam, resposta.getBody().get("faltam").asInt());
    }

    @Entao("o pedido de sugestão é recusado com status {int}")
    public void oPedidoERecusado(int status)
    {
        Assertions.assertEquals(status, resposta.getStatusCode().value());
    }

    private List<String> lotesSugeridos()
    {
        Assertions.assertEquals(200, resposta.getStatusCode().value(), "Resposta: " + resposta.getBody());

        List<String> ids = new ArrayList<>();
        resposta.getBody().get("lotes").forEach(lote -> ids.add(lote.get("id").asText()));
        return ids;
    }
}
