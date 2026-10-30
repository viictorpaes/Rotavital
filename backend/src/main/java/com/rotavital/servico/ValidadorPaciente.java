package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.rotavital.api.dto.paciente.DadosDoPaciente;
import com.rotavital.dominio.Hospital;
import com.rotavital.repositorio.HospitalRepository;

/**
 * Confere os dados clínicos de um paciente, iguais no cadastro de pacientes (PI3-194) e na
 * campanha de doação (PI3-191). Campo ausente ou mal preenchido lança DadosInvalidosException (400),
 * com todos os problemas de uma vez; as faixas são as das constraints da tabela paciente.
 */
public class ValidadorPaciente
{
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final LocalDate NASCIMENTO_MINIMO = LocalDate.of(1900, 1, 2);
    private static final int UNIDADES_MINIMAS = 1;
    private static final int UNIDADES_MAXIMAS = 50;

    private final HospitalRepository hospitais;
    private final Clock relogio;

    public ValidadorPaciente(HospitalRepository hospitais, Clock relogio)
    {
        this.hospitais = hospitais;
        this.relogio = relogio;
    }

    /** Devolve o hospital informado, já conferido. */
    public Hospital validar(DadosDoPaciente dados)
    {
        if (dados == null)
        {
            throw new DadosInvalidosException("O corpo da requisição está vazio.");
        }

        List<String> problemas = new ArrayList<>();
        LocalDate hoje = LocalDate.now(relogio);

        if (vazio(dados.hospitalId()))
        {
            problemas.add("O campo 'hospitalId' é obrigatório.");
        }
        if (vazio(dados.nome()))
        {
            problemas.add("O campo 'nome' é obrigatório.");
        }
        if (dados.sexo() == null)
        {
            problemas.add("O campo 'sexo' é obrigatório.");
        }
        if (dados.dataNascimento() == null)
        {
            problemas.add("O campo 'dataNascimento' é obrigatório.");
        }
        else if (dados.dataNascimento().isAfter(hoje))
        {
            problemas.add("A data de nascimento (" + DATA.format(dados.dataNascimento())
                    + ") não pode ser depois de hoje (" + DATA.format(hoje) + ").");
        }
        else if (dados.dataNascimento().isBefore(NASCIMENTO_MINIMO))
        {
            problemas.add("A data de nascimento precisa ser depois de 01/01/1900.");
        }
        if (dados.tipoSanguineo() == null)
        {
            problemas.add("O campo 'tipoSanguineo' é obrigatório.");
        }
        if (dados.tipoComponente() == null)
        {
            problemas.add("O campo 'tipoComponente' é obrigatório.");
        }
        if (dados.unidadesNecessarias() == null)
        {
            problemas.add("O campo 'unidadesNecessarias' é obrigatório.");
        }
        else if (dados.unidadesNecessarias() < UNIDADES_MINIMAS || dados.unidadesNecessarias() > UNIDADES_MAXIMAS)
        {
            problemas.add("O campo 'unidadesNecessarias' deve ficar entre " + UNIDADES_MINIMAS + " e "
                    + UNIDADES_MAXIMAS + ".");
        }
        if (vazio(dados.causa()))
        {
            problemas.add("O campo 'causa' é obrigatório.");
        }
        if (dados.gravidade() == null)
        {
            problemas.add("O campo 'gravidade' é obrigatório.");
        }

        Hospital hospital = null;
        if (!vazio(dados.hospitalId()))
        {
            hospital = hospitais.findById(dados.hospitalId()).orElse(null);
            if (hospital == null)
            {
                problemas.add("Nenhum hospital encontrado com id " + dados.hospitalId() + ".");
            }
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }

        return hospital;
    }

    private static boolean vazio(String texto)
    {
        return texto == null || texto.isBlank();
    }
}
