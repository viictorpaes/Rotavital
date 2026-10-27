package com.rotavital.api.dto.doacao;

import com.rotavital.dominio.StatusAgendamento;

public record AtualizarStatusAgendamentoRequest(
        StatusAgendamento status
)
{
}
