package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final LocalDate NASCIMENTO_MINIMO = LocalDate.of(1900, 1, 2);

    private final CampanhaDoacaoRepository campanhas;
    private final PacienteRepository pacientes;
    private final HospitalRepository hospitais;
    private final AgendamentoDoacaoRepository agendamentos;
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    public CampanhaService(CampanhaDoacaoRepository campanhas, PacienteRepository pacientes,
            HospitalRepository hospitais, AgendamentoDoacaoRepository agendamentos,
            IdentificacaoService identificacao, Clock relogio)
    {
        this.campanhas = campanhas;
        this.pacientes = pacientes;
        this.hospitais = hospitais;
        this.agendamentos = agendamentos;
        this.identificacao = identificacao;
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
        Hospital hospital = validar(dados);
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
        Hospital hospital = validar(dados);

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

    /**
     * Confere os dados do paciente. Campo ausente ou mal preenchido lança DadosInvalidosException (400),
     * com todos os problemas de uma vez. Devolve o hospital informado.
     */
    private Hospital validar(CampanhaRequest dados)
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
        else if (dados.unidadesNecessarias() < 1 || dados.unidadesNecessarias() > 50)
        {
            problemas.add("O campo 'unidadesNecessarias' deve ficar entre 1 e 50.");
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
