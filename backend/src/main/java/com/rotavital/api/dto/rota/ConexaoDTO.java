package com.rotavital.api.dto.rota;

public record ConexaoDTO(
        String origemId,
        String destinoId,
        double distanciaKm,
        double tempoEstimadoMin
)
{
        
}
