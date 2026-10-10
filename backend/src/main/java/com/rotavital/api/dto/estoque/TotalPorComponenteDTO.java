package com.rotavital.api.dto.estoque;

import com.rotavital.dominio.TipoComponente;

public record TotalPorComponenteDTO(
        TipoComponente tipoComponente,
        int unidades
)

{

}
