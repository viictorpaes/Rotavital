package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.doacao.NovoAgendamentoRequest;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.CampanhaDoacaoRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Agendar, remarcar e fechar agendamentos de uma campanha (PI3-192), com os repositórios em memória.
public class AgendamentoServiceTest
{
    private static final ZoneOffset RECIFE = ZoneOffset.ofHours(-3);
    // Segunda-feira, 02/11/2026, 10h em Recife.
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 2, 10, 0, 0, 0, RECIFE);
    // Quarta-feira, 04/11/2026, 09h30: dentro do expediente.
    private static final OffsetDateTime QUARTA = OffsetDateTime.of(2026, 11, 4, 9, 30, 0, 0, RECIFE);

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, "HS-01", null);
    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Equipe técnica", PapelUsuario.ADMIN, null, null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João Doador", PapelUsuario.DOADOR, null,
            TipoSanguineo.O_NEGATIVO);
    private final Usuario outroDoador = new Usuario(UUID.randomUUID(), null, "Rita Doadora", PapelUsuario.DOADOR, null,
            TipoSanguineo.A_POSITIVO);
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.06, -34.88));

    private CampanhaDoacao campanha;
    private AgendamentoDoacao agendamentoExistente;
    private boolean doadorJaTemAgendamento;
    private AgendamentoService servico;

    @BeforeEach
    public void montarServico()
    {
        Hospital hospital = new Hospital("HS-01", "Hospital da Restauração", new Endereco("Recife/PE", -8.05, -34.89));
        Paciente paciente = new Paciente(hospital, "Maria Souza", Sexo.FEMININO, LocalDate.of(1990, 5, 10),
                TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", Gravidade.CRITICO, AGORA);
        campanha = new CampanhaDoacao(paciente, medico, AGORA.minusDays(3));
        agendamentoExistente = new AgendamentoDoacao(doador, banco, campanha, QUARTA, AGORA.minusDays(1));

        List<Usuario> todos = List.of(medico, admin, doador, outroDoador);
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> todos.stream().filter(usuario -> usuario.getId().equals(argumentos[0])).findFirst());
        CampanhaDoacaoRepository campanhas = RepositorioFalso.respondendo(CampanhaDoacaoRepository.class, "findById",
                argumentos -> Optional.of(campanha));
        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, "findById",
                argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        AgendamentoDoacaoRepository agendamentos = RepositorioFalso.respondendo(AgendamentoDoacaoRepository.class, Map.of(
                "save", argumentos -> argumentos[0],
                "findById", argumentos -> Optional.of(agendamentoExistente),
                "existsByDoadorIdAndStatus", argumentos -> doadorJaTemAgendamento,
                "findByCampanhaIdOrderByAgendadoParaAsc", argumentos -> List.of(agendamentoExistente)));

        servico = new AgendamentoService(agendamentos, campanhas, bancos, new IdentificacaoService(usuarios),
                Clock.fixed(AGORA.toInstant(), RelogioConfig.FUSO));
    }

    @Test
    public void doadorAgendaNaCampanhaAtiva()
    {
        AgendamentoDoacao agendamento = servico.agendar(doador.getId(), UUID.randomUUID(),
                new NovoAgendamentoRequest("BS-01", QUARTA));

        Assertions.assertEquals(StatusAgendamento.AGENDADO, agendamento.getStatus());
        Assertions.assertSame(doador, agendamento.getDoador());
        Assertions.assertSame(campanha, agendamento.getCampanha());
        Assertions.assertSame(banco, agendamento.getBanco());
        Assertions.assertEquals(QUARTA, agendamento.getAgendadoPara());
        Assertions.assertEquals(AGORA, agendamento.getCriadoEm());
    }

    @Test
    public void medicoEAdminNaoAgendam()
    {
        NovoAgendamentoRequest pedido = new NovoAgendamentoRequest("BS-01", QUARTA);

        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.agendar(medico.getId(), UUID.randomUUID(), pedido));
        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.agendar(admin.getId(), UUID.randomUUID(), pedido));

        Assertions.assertEquals("Só doador pode agendar doação.", erro.getMessage());
    }

    @Test
    public void camposAusentesEBancoInexistenteSaoRecusados()
    {
        DadosInvalidosException vazio = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.agendar(doador.getId(), UUID.randomUUID(), new NovoAgendamentoRequest(" ", null)));
        DadosInvalidosException semBanco = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.agendar(doador.getId(), UUID.randomUUID(), new NovoAgendamentoRequest("BS-99", QUARTA)));

        Assertions.assertEquals("O campo 'bancoId' é obrigatório. O campo 'agendadoPara' é obrigatório.", vazio.getMessage());
        Assertions.assertEquals("Nenhum banco de sangue encontrado com id BS-99.", semBanco.getMessage());
    }

    @Test
    public void horarioNoPassadoNoDomingoOuForaDoExpedienteERecusado()
    {
        for (OffsetDateTime horario : List.of(
                AGORA.minusHours(1),
                OffsetDateTime.of(2026, 11, 8, 10, 0, 0, 0, RECIFE),
                OffsetDateTime.of(2026, 11, 4, 6, 59, 0, 0, RECIFE),
                OffsetDateTime.of(2026, 11, 4, 19, 1, 0, 0, RECIFE)))
        {
            Assertions.assertThrows(RegraDeNegocioException.class,
                    () -> servico.agendar(doador.getId(), UUID.randomUUID(), new NovoAgendamentoRequest("BS-01", horario)),
                    horario.toString());
        }
    }

    @Test
    public void horarioEmOutroFusoEConferidoNoHorarioDeRecife()
    {
        // 12h30 UTC = 09h30 em Recife: dentro do expediente.
        OffsetDateTime emUtc = QUARTA.withOffsetSameInstant(ZoneOffset.UTC);

        AgendamentoDoacao agendamento = servico.agendar(doador.getId(), UUID.randomUUID(),
                new NovoAgendamentoRequest("BS-01", emUtc));

        Assertions.assertEquals(StatusAgendamento.AGENDADO, agendamento.getStatus());
    }

    @Test
    public void campanhaEncerradaNaoRecebeAgendamento()
    {
        campanha.encerrar(AGORA.minusHours(2));

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.agendar(doador.getId(), UUID.randomUUID(), new NovoAgendamentoRequest("BS-01", QUARTA)));

        Assertions.assertEquals("A campanha de Maria Souza foi encerrada e não recebe mais agendamentos.", erro.getMessage());
    }

    @Test
    public void doadorComAgendamentoEmAbertoNaoMarcaOutro()
    {
        doadorJaTemAgendamento = true;

        Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.agendar(doador.getId(), UUID.randomUUID(), new NovoAgendamentoRequest("BS-01", QUARTA)));
    }

    @Test
    public void doadorRemarcaOProprioAgendamento()
    {
        OffsetDateTime sabado = OffsetDateTime.of(2026, 11, 7, 18, 0, 0, 0, RECIFE);

        AgendamentoDoacao agendamento = servico.remarcar(doador.getId(), UUID.randomUUID(), sabado);

        Assertions.assertEquals(sabado, agendamento.getAgendadoPara());
    }

    @Test
    public void ninguemRemarcaAgendamentoDeOutroDoadorNemOMedico()
    {
        Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.remarcar(outroDoador.getId(), UUID.randomUUID(), QUARTA.plusDays(1)));
        Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.remarcar(medico.getId(), UUID.randomUUID(), QUARTA.plusDays(1)));
    }

    @Test
    public void doadorSoCancela()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.mudarStatus(doador.getId(), UUID.randomUUID(), StatusAgendamento.REALIZADO));
        Assertions.assertTrue(erro.getMessage().startsWith("O doador só pode cancelar"));

        AgendamentoDoacao agendamento = servico.mudarStatus(doador.getId(), UUID.randomUUID(), StatusAgendamento.CANCELADO);

        Assertions.assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
    }

    @Test
    public void medicoRegistraDoacaoFeita()
    {
        AgendamentoDoacao agendamento = servico.mudarStatus(medico.getId(), UUID.randomUUID(), StatusAgendamento.REALIZADO);

        Assertions.assertEquals(StatusAgendamento.REALIZADO, agendamento.getStatus());
    }

    @Test
    public void faltaSoDepoisDoHorarioMarcado()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.mudarStatus(medico.getId(), UUID.randomUUID(), StatusAgendamento.FALTOU));

        Assertions.assertEquals("A falta só pode ser registrada depois do horário marcado (04/11/2026 às 09:30).",
                erro.getMessage());
    }

    @Test
    public void agendamentoFechadoNaoMudaMais()
    {
        agendamentoExistente.mudarStatus(StatusAgendamento.CANCELADO);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.mudarStatus(medico.getId(), UUID.randomUUID(), StatusAgendamento.REALIZADO));
        Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.remarcar(doador.getId(), UUID.randomUUID(), QUARTA.plusDays(1)));

        Assertions.assertEquals("O agendamento já foi cancelado e não pode mais ser alterado.", erro.getMessage());
    }

    @Test
    public void statusAgendadoOuAusenteERecusado()
    {
        Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.mudarStatus(medico.getId(), UUID.randomUUID(), StatusAgendamento.AGENDADO));
        Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.mudarStatus(medico.getId(), UUID.randomUUID(), null));
    }

    @Test
    public void doadorNaoVeAgendamentosDaCampanha()
    {
        Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.listarDaCampanha(doador.getId(), UUID.randomUUID()));
        Assertions.assertEquals(List.of(agendamentoExistente), servico.listarDaCampanha(admin.getId(), UUID.randomUUID()));
    }
}
