package com.rotavital.api.dto.rota;

import java.time.LocalDateTime;

public record CalcularRotaRequest(
        String origemId,
        String destinoId,
        LocalDateTime janelaEntregaLimite
)
{
        
}
