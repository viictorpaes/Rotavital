package com.rotavital.benchmark.model;

/**
 * Representa uma leitura de telemetria de um sensor IoT durante o transporte
 * de um hemocomponente da cadeia fria do Rota Vital.
 */
public record RegistroTelemetria(
        String idLeitura,
        String idBolsa,
        String tipoComponente, // HEMACIAS, PLAQUETAS, PLASMA
        double temperatura,
        double latitude,
        double longitude,
        int minutosEmTransito
) {}

