package com.rotavital.api.dto.requisicao;

import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record NovaRequisicaoRequest(
        String hospitalId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        int quantidade,
        NivelUrgencia urgencia
)

{
        
}