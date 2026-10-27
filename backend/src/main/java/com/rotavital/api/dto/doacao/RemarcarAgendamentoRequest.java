package com.rotavital.api.dto.doacao;

import java.time.OffsetDateTime;

public record RemarcarAgendamentoRequest(
        OffsetDateTime agendadoPara
)
{
}
