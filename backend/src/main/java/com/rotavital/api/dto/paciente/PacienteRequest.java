package com.rotavital.api.dto.paciente;

import java.time.LocalDate;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Corpo do POST e do PUT de /pacientes (PI3-194). Os mesmos campos da campanha de doação.
public record PacienteRequest(
        String hospitalId,
        String nome,
        Sexo sexo,
        LocalDate dataNascimento,
        TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente,
        Integer unidadesNecessarias,
        String causa,
        Gravidade gravidade
) implements DadosDoPaciente
{
}
