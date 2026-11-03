package com.rotavital.api.dto.rota;

/**
 * Corpo do POST /campanhas/{id}/rota (PI3-262): onde o doador está agora, lido pela Geolocation
 * API do navegador. Vai no corpo, e não na URL, para a posição não aparecer nos logs de acesso
 * (o diagnóstico guarda as URLs das últimas requisições). Os dois nulos: só o destino.
 */
public record OrigemRequest(Double latitude, Double longitude)
{
}
