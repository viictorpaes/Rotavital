package com.rotavital.servico;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.paciente.DadosDoPaciente;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusPaciente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PacienteRepository;

/**
 * Cadastro de pacientes (PI3-194): consultar é livre; cadastrar, editar e remover exigem um médico,
 * como nas campanhas de doação (docs/PERFIS_DE_ACESSO.md). Os dados são conferidos pelo mesmo
 * ValidadorPaciente da campanha, que publica um paciente na mesma tabela.
 */
@Service
public class PacienteService
{
    // Fila clínica: o mais grave primeiro (ordem do enum Gravidade) e, na mesma gravidade, o mais antigo.
    private static final Comparator<Paciente> FILA = Comparator.comparing(Paciente::getGravidade)
            .thenComparing(Paciente::getCriadoEm);

    private final PacienteRepository pacientes;
    private final IdentificacaoService identificacao;
    private final ValidadorPaciente validador;
    private final Clock relogio;

    public PacienteService(PacienteRepository pacientes, HospitalRepository hospitais,
            IdentificacaoService identificacao, Clock relogio)
    {
        this.pacientes = pacientes;
        this.identificacao = identificacao;
        this.validador = new ValidadorPaciente(hospitais, relogio);
        this.relogio = relogio;
    }

    /** Pacientes com os filtros informados (nulo não restringe), na ordem da fila clínica. */
    @Transactional(readOnly = true)
    public List<Paciente> listar(StatusPaciente status, TipoSanguineo tipoSanguineo, Gravidade gravidade,
            String hospitalId)
    {
        String hospital = hospitalId == null || hospitalId.isBlank() ? null : hospitalId.strip();

        return pacientes.buscarComFiltros(status, tipoSanguineo, gravidade, hospital).stream()
                .sorted(FILA)
                .toList();
    }

    /** Paciente inexistente lança RecursoNaoEncontradoException (404 na API). */
    @Transactional(readOnly = true)
    public Paciente buscar(UUID id)
    {
        return pacientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum paciente encontrado com id " + id + "."));
    }

    /** Cadastra o paciente aguardando atendimento. */
    @Transactional
    public Paciente cadastrar(UUID usuarioId, DadosDoPaciente dados)
    {
        identificacao.exigir(usuarioId, "cadastrar paciente", PapelUsuario.MEDICO);
        Hospital hospital = validador.validar(dados);

        Paciente paciente = new Paciente(hospital, dados.nome(), dados.sexo(), dados.dataNascimento(),
                dados.tipoSanguineo(), dados.tipoComponente(), dados.unidadesNecessarias(), dados.causa(),
                dados.gravidade(), OffsetDateTime.now(relogio));

        return pacientes.save(paciente);
    }

    /**
     * Troca os dados clínicos. Só paciente AGUARDANDO pode mudar: atendido ou cancelado já é histórico.
     * Se ele estiver numa campanha ativa, os doadores passam a ver os dados novos.
     */
    @Transactional
    public Paciente editar(UUID usuarioId, UUID id, DadosDoPaciente dados)
    {
        identificacao.exigir(usuarioId, "editar paciente", PapelUsuario.MEDICO);
        Paciente paciente = buscar(id);
        Hospital hospital = validador.validar(dados);

        if (paciente.getStatus() != StatusPaciente.AGUARDANDO)
        {
            throw new RegraDeNegocioException("O paciente " + paciente.getNome() + " está " + paciente.getStatus()
                    + " e não pode mais ser editado: só paciente AGUARDANDO pode.");
        }

        paciente.atualizar(hospital, dados.nome(), dados.sexo(), dados.dataNascimento(), dados.tipoSanguineo(),
                dados.tipoComponente(), dados.unidadesNecessarias(), dados.causa(), dados.gravidade());

        return pacientes.save(paciente);
    }

    /**
     * Remove um cadastro feito por engano. Paciente com campanha de doação, procedimento ou requisição
     * fica (422): esses registros são histórico e apontam para ele.
     */
    @Transactional
    public void remover(UUID usuarioId, UUID id)
    {
        identificacao.exigir(usuarioId, "remover paciente", PapelUsuario.MEDICO);
        Paciente paciente = buscar(id);

        List<String> vinculos = new ArrayList<>();
        adicionarVinculo(vinculos, pacientes.contarCampanhas(id), "campanha de doação", "campanhas de doação");
        adicionarVinculo(vinculos, pacientes.contarProcedimentos(id), "procedimento", "procedimentos");
        adicionarVinculo(vinculos, pacientes.contarRequisicoes(id), "requisição", "requisições");

        if (!vinculos.isEmpty())
        {
            throw new RegraDeNegocioException("O paciente " + paciente.getNome() + " não pode ser removido: ele tem registros "
                    + "que precisam ficar no histórico (" + juntar(vinculos) + ").");
        }

        pacientes.delete(paciente);
    }

    private static void adicionarVinculo(List<String> vinculos, long quantidade, String singular, String plural)
    {
        if (quantidade > 0)
        {
            vinculos.add(quantidade + " " + (quantidade == 1 ? singular : plural));
        }
    }

    // ["1 campanha", "2 procedimentos", "1 requisição"] → "1 campanha, 2 procedimentos e 1 requisição"
    private static String juntar(List<String> partes)
    {
        if (partes.size() == 1)
        {
            return partes.get(0);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " e " + partes.get(partes.size() - 1);
    }
}
