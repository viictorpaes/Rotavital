package com.rotavital.api.dto.telemetria;

import java.time.LocalDateTime;

public record RegistrarTelemetriaRequest(
        String entregaId,
        LocalDateTime timestamp,
        double latitude,
        double longitude,
        double temperaturaCelsius
)

{

}