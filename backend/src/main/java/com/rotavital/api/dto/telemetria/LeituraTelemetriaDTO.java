package com.rotavital.api.dto.telemetria;

import java.time.LocalDateTime;

public record LeituraTelemetriaDTO(
        String entregaId,
        LocalDateTime timestamp,
        double latitude,
        double longitude,
        double temperaturaCelsius
)

{
        
}