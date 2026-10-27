package com.rotavital.api.dto.doacao;

import java.time.LocalDate;
import java.util.UUID;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.StatusPaciente;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record PacienteDTO(
        UUID id,
        String nome,
        Sexo sexo,
        LocalDate dataNascimento,
        int idade,
        TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente,
        int unidadesNecessarias,
        String causa,
        Gravidade gravidade,
        StatusPaciente status,
        String hospitalId,
        String hospitalNome
)
{
}
