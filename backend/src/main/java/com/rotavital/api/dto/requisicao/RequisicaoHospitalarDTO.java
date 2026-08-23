package com.rotavital.api.dto.requisicao;

import java.time.LocalDateTime;

import com.rotavital.dominio.StatusRequisicao;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record RequisicaoHospitalarDTO(
        String id,
        String hospitalId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        int quantidade,
        NivelUrgencia urgencia,
        LocalDateTime dataSolicitacao,
        StatusRequisicao status
)
{
        
}
