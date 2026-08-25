package com.rotavital.api.dto.requisicao;

import java.time.LocalDateTime;

import com.rotavital.dominio.StatusRequisicao;

public record AlocacaoDTO(
        String requisicaoId,
        String bolsaAlocadaId,
        LocalDateTime dataAlocacao,
        StatusRequisicao status
)

{
        
}