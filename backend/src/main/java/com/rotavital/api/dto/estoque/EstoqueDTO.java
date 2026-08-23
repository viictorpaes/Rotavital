package com.rotavital.api.dto.estoque;

import java.util.List;

public record EstoqueDTO(
        String bancoDeSangueId,
        int totalBolsas,
        List<BolsaHemocomponenteDTO> bolsas
)
{
        
}
