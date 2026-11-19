package com.rotavital.api.dto.rota;

// Corpo do POST /transferencias (PI3-236): qual bolsa sai e para qual hospital. A origem é o hemocentro da bolsa.
public record NovaTransferenciaRequest(String bolsaId, String destinoId)
{
}
