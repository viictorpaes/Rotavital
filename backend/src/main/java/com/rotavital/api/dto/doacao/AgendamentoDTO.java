package com.rotavital.api.dto.doacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.TipoSanguineo;

public record AgendamentoDTO(
        UUID id,
        UUID campanhaId,
        String pacienteNome,
        UUID doadorId,
        String doadorNome,
        TipoSanguineo doadorTipoSanguineo,
        String bancoId,
        String bancoNome,
        OffsetDateTime agendadoPara,
        StatusAgendamento status,
        OffsetDateTime criadoEm
)
{
}
