package com.rotavital.api.dto.doacao;

import java.time.LocalDate;
import java.util.UUID;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Paciente;
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
    // Usado pelas campanhas (PI3-191) e pelo cadastro de pacientes (PI3-194); a idade é calculada para "hoje".
    public static PacienteDTO de(Paciente paciente, LocalDate hoje)
    {
        return new PacienteDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getSexo(),
                paciente.getDataNascimento(),
                paciente.idadeEm(hoje),
                paciente.getTipoSanguineo(),
                paciente.getTipoComponente(),
                paciente.getUnidadesNecessarias(),
                paciente.getCausa(),
                paciente.getGravidade(),
                paciente.getStatus(),
                paciente.getHospital().getId(),
                paciente.getHospital().getNome());
    }
}
