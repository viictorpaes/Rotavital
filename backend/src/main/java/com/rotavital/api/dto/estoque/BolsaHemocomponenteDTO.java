package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record BolsaHemocomponenteDTO(
        String id,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataColeta,
        LocalDate dataValidade,
        String loteSintetico,
        double volumeMl,
        StatusBolsa status,
        String bancoOrigemId
)

{
        
}