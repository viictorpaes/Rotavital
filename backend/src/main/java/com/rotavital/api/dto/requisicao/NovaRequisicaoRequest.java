package com.rotavital.api.dto.requisicao;

import com.rotavital.dominio.AlaHospitalar;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Corpo de POST /requisicoes (HU-04). bancoId é o hemocentro que atende o pedido; hospitalId, ala e
 * leito dizem para onde a bolsa vai. Leito e paciente são opcionais.
 */
public record NovaRequisicaoRequest(
        String bancoId,
        String hospitalId,
        AlaHospitalar ala,
        String leito,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        Integer quantidade,
        NivelUrgencia urgencia,
        String paciente
)

{

}
