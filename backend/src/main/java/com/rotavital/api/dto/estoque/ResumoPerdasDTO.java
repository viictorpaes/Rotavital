package com.rotavital.api.dto.estoque;

import java.util.List;

public record ResumoPerdasDTO(
        long totalBolsas,
        double totalVolumeMl,
        List<TotalPerdaDTO> porTipoSanguineo,
        List<TotalPerdaDTO> porComponente)
{
}
