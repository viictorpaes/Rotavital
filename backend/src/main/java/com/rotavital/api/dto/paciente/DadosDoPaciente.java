package com.rotavital.api.dto.paciente;

import java.time.LocalDate;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Dados clínicos de um paciente, como chegam no corpo da requisição. O cadastro de pacientes
 * (PacienteRequest, PI3-194) e a campanha de doação (CampanhaRequest, PI3-191) mandam os mesmos
 * campos: com esta interface, o ValidadorPaciente confere os dois do mesmo jeito.
 */
public interface DadosDoPaciente
{
    String hospitalId();

    String nome();

    Sexo sexo();

    LocalDate dataNascimento();

    TipoSanguineo tipoSanguineo();

    TipoComponente tipoComponente();

    // Integer, e não int: o validador precisa saber quando o campo não veio no JSON.
    Integer unidadesNecessarias();

    String causa();

    Gravidade gravidade();
}
