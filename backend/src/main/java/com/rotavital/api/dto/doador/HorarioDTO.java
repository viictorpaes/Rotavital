package com.rotavital.api.dto.doador;

import java.time.OffsetDateTime;

// Um horário da agenda do hemocentro (PI3-204). hora é "09:30", no fuso de Recife; inicio vai no agendamento.
public record HorarioDTO(
        String hora,
        OffsetDateTime inicio,
        int vagasRestantes,
        boolean disponivel
)
{
}
