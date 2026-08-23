package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record NovaBolsaHemocomponenteRequest(
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataColeta,
        LocalDate dataValidade,
        String loteSintetico,
        double volumeMl,
        String bancoOrigemId
)
{
        
}
