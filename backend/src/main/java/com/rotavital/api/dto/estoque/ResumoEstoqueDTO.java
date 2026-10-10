package com.rotavital.api.dto.estoque;

import java.util.List;

public record ResumoEstoqueDTO(
        String bancoDeSangueId,
        int totalUnidades,
        List<TotalPorTipoSanguineoDTO> tiposSanguineos,
        List<TotalPorComponenteDTO> componentes
)

{

}
