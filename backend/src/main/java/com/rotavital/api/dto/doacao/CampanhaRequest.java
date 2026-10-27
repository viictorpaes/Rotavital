package com.rotavital.api.dto.doacao;

import java.time.LocalDate;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Corpo do POST e do PUT de /campanhas: os dados do paciente divulgado (HU-06).
// unidadesNecessarias é Integer para o serviço saber quando o campo não veio no JSON.
public record CampanhaRequest(
        String hospitalId,
        String nome,
        Sexo sexo,
        LocalDate dataNascimento,
        TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente,
        Integer unidadesNecessarias,
        String causa,
        Gravidade gravidade
)
{
}
