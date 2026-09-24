package com.rotavital.benchmark.dto;

public record ItemExecucaoDTO(
        String modo,
        int threads,
        long tempoExecucaoMs,
        double speedup,
        long totalProcessado,
        long anomaliasDetectadas,
        long lotesCriticosDescarte,
        double mediaTemperatura,
        double desvioPadraoTemperatura,
        double indiceDegradacaoMedio
) {}

