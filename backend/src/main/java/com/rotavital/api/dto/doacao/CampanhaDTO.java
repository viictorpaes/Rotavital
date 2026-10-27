package com.rotavital.api.dto.doacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rotavital.dominio.StatusCampanha;

public record CampanhaDTO(
        UUID id,
        StatusCampanha status,
        OffsetDateTime publicadaEm,
        OffsetDateTime encerradaEm,
        String publicadaPorNome,
        long agendamentosEmAberto,
        PacienteDTO paciente
)
{
}
