package com.rotavital.api.dto.doacao;

import java.time.OffsetDateTime;

// Corpo do POST /campanhas/{id}/agendamentos. O doador é quem está no header X-Usuario-Id.
public record NovoAgendamentoRequest(
        String bancoId,
        OffsetDateTime agendadoPara
)
{
}
