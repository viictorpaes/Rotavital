package com.rotavital.api.dto.telemetria;

import java.util.List;

import com.rotavital.dominio.StatusRequisicao;

public record MonitoramentoEntregaDTO(
        String entregaId,
        StatusRequisicao statusEntrega,
        List<LeituraTelemetriaDTO> leituras
)

{

}