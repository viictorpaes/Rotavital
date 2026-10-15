package com.rotavital.api.dto.estoque;

import java.util.List;

import com.rotavital.dominio.TipoSanguineo;

public record TotalPorTipoSanguineoDTO(
        TipoSanguineo tipoSanguineo,
        int unidades,
        boolean abaixoNivelSeguranca,
        List<TotalPorComponenteDTO> componentes
)

{

}
