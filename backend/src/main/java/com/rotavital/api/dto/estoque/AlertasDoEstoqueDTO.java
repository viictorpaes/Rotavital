package com.rotavital.api.dto.estoque;

import java.util.List;

// Resposta de GET /bancos/{bancoId}/alertas (HU-02): caixa de avisos e card "Alertas Críticos" do painel.
public record AlertasDoEstoqueDTO(
        String bancoDeSangueId,
        int total,
        int criticos,
        List<AlertaDTO> alertas
)

{
        
}
