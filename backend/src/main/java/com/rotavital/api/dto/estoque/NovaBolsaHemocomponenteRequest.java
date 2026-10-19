package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// temperaturaCelsius é Double (e não double) para a validação saber quando o campo não veio:
// com double, a falta chegaria como 0 °C, uma temperatura que existe. localizacao é opcional.
public record NovaBolsaHemocomponenteRequest(
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataColeta,
        LocalDate dataValidade,
        String loteSintetico,
        double volumeMl,
        Double temperaturaCelsius,
        String localizacao,
        String bancoOrigemId
)

{

}
