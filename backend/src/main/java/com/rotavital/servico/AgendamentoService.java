package com.rotavital.servico;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.doacao.NovoAgendamentoRequest;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.CampanhaDoacaoRepository;

/**
 * Agendamentos de doação dentro de uma campanha (HU-09, HU-10, PI3-192).
 * Quem agenda e remarca é o doador (decisão 5 de docs/PERFIS_DE_ACESSO.md); o médico acompanha
 * os agendamentos da campanha e registra se a doação foi feita, se o doador faltou ou se foi cancelada.
 */
@Service
public class AgendamentoService
{
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    // Horário do hemocentro, o mesmo do modal "Agende sua doação": segunda a sábado, das 07h às 19h.
    private static final LocalTime ABRE = LocalTime.of(7, 0);
    private static final LocalTime FECHA = LocalTime.of(19, 0);

    private final AgendamentoDoacaoRepository agendamentos;
    private final CampanhaDoacaoRepository campanhas;
    private final BancoDeSangueRepository bancos;
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    public AgendamentoService(AgendamentoDoacaoRepository agendamentos, CampanhaDoacaoRepository campanhas,
            BancoDeSangueRepository bancos, IdentificacaoService identificacao, Clock relogio)
    {
        this.agendamentos = agendamentos;
        this.campanhas = campanhas;
        this.bancos = bancos;
        this.identificacao = identificacao;
        this.relogio = relogio;
    }

    // Agendamentos de uma campanha, do horário mais próximo para o mais distante. Só médico e admin veem.
    @Transactional(readOnly = true)
    public List<AgendamentoDoacao> listarDaCampanha(UUID usuarioId, UUID campanhaId)
    {
        identificacao.exigir(usuarioId, "ver os agendamentos da campanha", PapelUsuario.MEDICO, PapelUsuario.ADMIN);
        buscarCampanha(campanhaId);
        return agendamentos.findByCampanhaIdOrderByAgendadoParaAsc(campanhaId);
    }

    // Agendamentos do próprio doador, do mais recente para o mais antigo.
    @Transactional(readOnly = true)
    public List<AgendamentoDoacao> listarDoDoador(UUID usuarioId)
    {
        Usuario doador = identificacao.exigir(usuarioId, "ver os seus agendamentos", PapelUsuario.DOADOR);
        return agendamentos.findByDoadorIdOrderByAgendadoParaDesc(doador.getId());
    }

    @Transactional
    public AgendamentoDoacao agendar(UUID usuarioId, UUID campanhaId, NovoAgendamentoRequest dados)
    {
        Usuario doador = identificacao.exigir(usuarioId, "agendar doação", PapelUsuario.DOADOR);
        CampanhaDoacao campanha = buscarCampanha(campanhaId);

        List<String> problemas = new ArrayList<>();
        if (dados == null || dados.bancoId() == null || dados.bancoId().isBlank())
        {
            problemas.add("O campo 'bancoId' é obrigatório.");
        }
        if (dados == null || dados.agendadoPara() == null)
        {
            problemas.add("O campo 'agendadoPara' é obrigatório.");
        }
        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }

        OffsetDateTime agora = OffsetDateTime.now(relogio);
        validarHorario(dados.agendadoPara(), agora);

        BancoDeSangue banco = bancos.findById(dados.bancoId())
                .orElseThrow(() -> new DadosInvalidosException(
                        "Nenhum banco de sangue encontrado com id " + dados.bancoId() + "."));

        if (!campanha.estaAtiva())
        {
            throw new RegraDeNegocioException("A campanha de " + campanha.getPaciente().getNome()
                    + " foi encerrada e não recebe mais agendamentos.");
        }
        // O banco também garante isso (uq_agendamento_doador_aberto); aqui a mensagem diz o que fazer.
        if (agendamentos.existsByDoadorIdAndStatus(doador.getId(), StatusAgendamento.AGENDADO))
        {
            throw new RegraDeNegocioException("Você já tem uma doação agendada. Remarque ou cancele essa antes de marcar outra.");
        }

        return agendamentos.save(new AgendamentoDoacao(doador, banco, campanha, dados.agendadoPara(), agora));
    }

    // Só o próprio doador remarca, e só enquanto o agendamento está em aberto.
    @Transactional
    public AgendamentoDoacao remarcar(UUID usuarioId, UUID id, OffsetDateTime novoHorario)
    {
        Usuario doador = identificacao.exigir(usuarioId, "remarcar doação", PapelUsuario.DOADOR);
        AgendamentoDoacao agendamento = buscar(id);

        exigirDono(agendamento, doador);
        exigirEmAberto(agendamento);
        validarHorario(novoHorario, OffsetDateTime.now(relogio));

        agendamento.remarcar(novoHorario);
        return agendamentos.save(agendamento);
    }

    /**
     * Fecha o agendamento. O doador só cancela os seus; o médico cancela, registra a doação feita
     * ou a falta (essa só depois do horário marcado). AGENDADO não é aceito: para mudar o horário, remarque.
     */
    @Transactional
    public AgendamentoDoacao mudarStatus(UUID usuarioId, UUID id, StatusAgendamento novo)
    {
        Usuario usuario = identificacao.exigir(usuarioId, "alterar agendamento de doação",
                PapelUsuario.MEDICO, PapelUsuario.DOADOR);

        if (novo == null)
        {
            throw new DadosInvalidosException("O campo 'status' é obrigatório. Valores aceitos: REALIZADO, CANCELADO, FALTOU.");
        }
        if (novo == StatusAgendamento.AGENDADO)
        {
            throw new DadosInvalidosException("O status AGENDADO não é aceito aqui. Para mudar o horário, use PATCH /agendamentos/{id}.");
        }

        AgendamentoDoacao agendamento = buscar(id);

        if (usuario.getPapel() == PapelUsuario.DOADOR)
        {
            exigirDono(agendamento, usuario);
            if (novo != StatusAgendamento.CANCELADO)
            {
                throw new AcessoNegadoException("O doador só pode cancelar o agendamento; quem registra a doação ou a falta é o médico.");
            }
        }

        exigirEmAberto(agendamento);

        if (novo == StatusAgendamento.FALTOU && OffsetDateTime.now(relogio).isBefore(agendamento.getAgendadoPara()))
        {
            throw new RegraDeNegocioException("A falta só pode ser registrada depois do horário marcado ("
                    + formatar(agendamento.getAgendadoPara()) + ").");
        }

        agendamento.mudarStatus(novo);
        return agendamentos.save(agendamento);
    }

    private CampanhaDoacao buscarCampanha(UUID campanhaId)
    {
        return campanhas.findById(campanhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma campanha encontrada com id " + campanhaId + "."));
    }

    private AgendamentoDoacao buscar(UUID id)
    {
        return agendamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum agendamento encontrado com id " + id + "."));
    }

    private static void exigirDono(AgendamentoDoacao agendamento, Usuario doador)
    {
        if (!agendamento.getDoador().getId().equals(doador.getId()))
        {
            throw new AcessoNegadoException("Este agendamento é de outro doador.");
        }
    }

    private static void exigirEmAberto(AgendamentoDoacao agendamento)
    {
        if (!agendamento.estaEmAberto())
        {
            String situacao = switch (agendamento.getStatus())
            {
                case REALIZADO -> "foi realizado";
                case CANCELADO -> "foi cancelado";
                case FALTOU -> "foi marcado como falta";
                case AGENDADO -> "está em aberto";
            };
            throw new RegraDeNegocioException("O agendamento já " + situacao + " e não pode mais ser alterado.");
        }
    }

    // Horário no futuro e dentro do expediente do hemocentro, no fuso de Recife.
    private static void validarHorario(OffsetDateTime horario, OffsetDateTime agora)
    {
        if (horario == null)
        {
            throw new DadosInvalidosException("O campo 'agendadoPara' é obrigatório.");
        }
        if (!horario.isAfter(agora))
        {
            throw new RegraDeNegocioException("O horário da doação (" + formatar(horario) + ") precisa ser no futuro.");
        }

        ZonedDateTime local = horario.atZoneSameInstant(RelogioConfig.FUSO);
        LocalTime hora = local.toLocalTime();
        if (local.getDayOfWeek() == DayOfWeek.SUNDAY || hora.isBefore(ABRE) || hora.isAfter(FECHA))
        {
            throw new RegraDeNegocioException("O horário escolhido (" + formatar(horario)
                    + ") está fora do atendimento do hemocentro: segunda a sábado, das 07h às 19h.");
        }
    }

    private static String formatar(OffsetDateTime horario)
    {
        return DATA_HORA.format(horario.atZoneSameInstant(RelogioConfig.FUSO));
    }
}
