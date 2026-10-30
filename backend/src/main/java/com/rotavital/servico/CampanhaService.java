package com.rotavital.servico;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.doacao.CampanhaRequest;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.StatusCampanha;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.CampanhaDoacaoRepository;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PacienteRepository;

/**
 * Campanhas de doação (HU-06, PI3-191): o médico publica um paciente para os doadores,
 * edita os dados divulgados e encerra a campanha quando não precisa mais de doações.
 * Publicar cria o paciente e a campanha juntos, na mesma transação.
 */
@Service
public class CampanhaService
{
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CampanhaDoacaoRepository campanhas;
    private final PacienteRepository pacientes;
    private final AgendamentoDoacaoRepository agendamentos;
    private final IdentificacaoService identificacao;
    private final ValidadorPaciente validador;
    private final Clock relogio;

    public CampanhaService(CampanhaDoacaoRepository campanhas, PacienteRepository pacientes,
            HospitalRepository hospitais, AgendamentoDoacaoRepository agendamentos,
            IdentificacaoService identificacao, Clock relogio)
    {
        this.campanhas = campanhas;
        this.pacientes = pacientes;
        this.agendamentos = agendamentos;
        this.identificacao = identificacao;
        // A mesma conferência do cadastro de pacientes (PI3-194): uma campanha publica um paciente.
        this.validador = new ValidadorPaciente(hospitais, relogio);
        this.relogio = relogio;
    }

    // Campanhas da mais recente para a mais antiga; sem status, devolve ativas e encerradas.
    @Transactional(readOnly = true)
    public List<CampanhaDoacao> listar(StatusCampanha status)
    {
        if (status == null)
        {
            return campanhas.findAllByOrderByPublicadaEmDesc();
        }
        return campanhas.findByStatusOrderByPublicadaEmDesc(status);
    }

    // Quantos agendamentos em aberto cada campanha tem; campanha sem agendamento não aparece no mapa.
    @Transactional(readOnly = true)
    public Map<UUID, Long> contarAgendamentosEmAberto()
    {
        return agendamentos.campanhasDosAgendamentosEmAberto().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    // Campanha inexistente lança RecursoNaoEncontradoException (404 na API).
    @Transactional(readOnly = true)
    public CampanhaDoacao buscar(UUID id)
    {
        return campanhas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma campanha encontrada com id " + id + "."));
    }

    @Transactional
    public CampanhaDoacao publicar(UUID usuarioId, CampanhaRequest dados)
    {
        Usuario medico = identificacao.exigir(usuarioId, "publicar campanha de doação", PapelUsuario.MEDICO);
        Hospital hospital = validador.validar(dados);
        OffsetDateTime agora = OffsetDateTime.now(relogio);

        Paciente paciente = new Paciente(hospital, dados.nome(), dados.sexo(), dados.dataNascimento(),
                dados.tipoSanguineo(), dados.tipoComponente(), dados.unidadesNecessarias(), dados.causa(),
                dados.gravidade(), agora);
        pacientes.save(paciente);

        return campanhas.save(new CampanhaDoacao(paciente, medico, agora));
    }

    // Edita os dados do paciente divulgado. Só campanha ativa pode mudar.
    @Transactional
    public CampanhaDoacao editar(UUID usuarioId, UUID id, CampanhaRequest dados)
    {
        identificacao.exigir(usuarioId, "editar campanha de doação", PapelUsuario.MEDICO);
        CampanhaDoacao campanha = buscar(id);
        Hospital hospital = validador.validar(dados);

        if (!campanha.estaAtiva())
        {
            throw new RegraDeNegocioException("A campanha foi encerrada em " + DATA.format(campanha.getEncerradaEm().atZoneSameInstant(RelogioConfig.FUSO))
                    + " e não pode mais ser editada.");
        }

        Paciente paciente = campanha.getPaciente();
        paciente.atualizar(hospital, dados.nome(), dados.sexo(), dados.dataNascimento(), dados.tipoSanguineo(),
                dados.tipoComponente(), dados.unidadesNecessarias(), dados.causa(), dados.gravidade());
        pacientes.save(paciente);

        return campanha;
    }

    /**
     * Encerra a campanha e cancela os agendamentos que ainda estavam em aberto nela,
     * para nenhum doador ir ao hemocentro por uma campanha que já acabou.
     */
    @Transactional
    public CampanhaDoacao encerrar(UUID usuarioId, UUID id)
    {
        identificacao.exigir(usuarioId, "encerrar campanha de doação", PapelUsuario.MEDICO);
        CampanhaDoacao campanha = buscar(id);

        if (!campanha.estaAtiva())
        {
            throw new RegraDeNegocioException("A campanha já foi encerrada em "
                    + DATA.format(campanha.getEncerradaEm().atZoneSameInstant(RelogioConfig.FUSO)) + ".");
        }

        campanha.encerrar(OffsetDateTime.now(relogio));
        campanhas.save(campanha);

        for (AgendamentoDoacao agendamento : agendamentos.findByCampanhaIdAndStatus(id, StatusAgendamento.AGENDADO))
        {
            agendamento.mudarStatus(StatusAgendamento.CANCELADO);
            agendamentos.save(agendamento);
        }

        return campanha;
    }
}
