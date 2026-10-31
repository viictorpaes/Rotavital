package com.rotavital.servico;

import java.time.OffsetDateTime;

// Um horário da agenda do hemocentro num dia (PI3-204): quantas vagas sobram e se ainda dá para marcar.
public record HorarioDaAgenda(
        OffsetDateTime inicio,
        int vagasRestantes,
        boolean disponivel
)

{

}
