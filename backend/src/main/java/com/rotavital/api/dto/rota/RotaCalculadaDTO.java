package com.rotavital.api.dto.rota;

import java.util.List;

public record RotaCalculadaDTO(
        String origemId,
        String destinoId,
        List<String> nos,
        double distanciaTotalKm,
        double tempoEstimadoMin,
        boolean dentroDaJanela
)
{

}