package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.doacao.CampanhaRequest;
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
import com.rotavital.dominio.StatusCampanha;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.CampanhaDoacaoRepository;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PacienteRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Publicar, editar e encerrar campanhas de doação (PI3-191), com os repositórios em memória.
public class CampanhaServiceTest
{
    // Segunda-feira, 02/11/2026, 10h em Recife.
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZoneOffset.ofHours(-3));

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, "HS-01", null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João Doador", PapelUsuario.DOADOR, null,
            TipoSanguineo.O_NEGATIVO);
    private final Hospital hospital = new Hospital("HS-01", "Hospital da Restauração", new Endereco("Recife/PE", -8.05, -34.89));
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.06, -34.88));

    private final List<Object> gravados = new ArrayList<>();
    private final List<AgendamentoDoacao> agendamentosDaCampanha = new ArrayList<>();
    private CampanhaDoacao campanhaExistente;
    private CampanhaService servico;

    @BeforeEach
    public void montarServico()
    {
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico
                        : doador.getId().equals(argumentos[0]) ? doador : null));
        HospitalRepository hospitais = RepositorioFalso.respondendo(HospitalRepository.class, "findById",
                argumentos -> Optional.ofNullable("HS-01".equals(argumentos[0]) ? hospital : null));
        PacienteRepository pacientes = RepositorioFalso.respondendo(PacienteRepository.class, "save",
                argumentos -> gravar(argumentos[0]));
        CampanhaDoacaoRepository campanhas = RepositorioFalso.respondendo(CampanhaDoacaoRepository.class, Map.of(
                "save", argumentos -> gravar(argumentos[0]),
                "findById", argumentos -> Optional.ofNullable(campanhaExistente)));
        AgendamentoDoacaoRepository agendamentos = RepositorioFalso.respondendo(AgendamentoDoacaoRepository.class, Map.of(
                "save", argumentos -> gravar(argumentos[0]),
                "findByCampanhaIdAndStatus", argumentos -> agendamentosDaCampanha.stream()
                        .filter(agendamento -> agendamento.getStatus() == argumentos[1])
                        .toList()));

        servico = new CampanhaService(campanhas, pacientes, hospitais, agendamentos,
                new IdentificacaoService(usuarios), Clock.fixed(AGORA.toInstant(), RelogioConfig.FUSO));

        Paciente paciente = new Paciente(hospital, "Maria Souza", Sexo.FEMININO, LocalDate.of(1990, 5, 10),
                TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", Gravidade.CRITICO, AGORA);
        campanhaExistente = new CampanhaDoacao(paciente, medico, AGORA.minusDays(3));
    }

    private Object gravar(Object entidade)
    {
        gravados.add(entidade);
        return entidade;
    }

    private static CampanhaRequest pedidoValido()
    {
        return new CampanhaRequest("HS-01", "  Carlos Lima ", Sexo.MASCULINO, LocalDate.of(1980, 1, 15),
                TipoSanguineo.A_POSITIVO, TipoComponente.PLAQUETAS, 3, " Leucemia ", Gravidade.ATENCAO);
    }

    @Test
    public void medicoPublicaCampanhaECriaOPaciente()
    {
        CampanhaDoacao campanha = servico.publicar(medico.getId(), pedidoValido());

        Assertions.assertEquals(StatusCampanha.ATIVA, campanha.getStatus());
        Assertions.assertSame(medico, campanha.getPublicadaPor());
        Assertions.assertEquals(AGORA, campanha.getPublicadaEm());
        Assertions.assertEquals("Carlos Lima", campanha.getPaciente().getNome());
        Assertions.assertEquals("Leucemia", campanha.getPaciente().getCausa());
        Assertions.assertSame(hospital, campanha.getPaciente().getHospital());
        Assertions.assertEquals(46, campanha.getPaciente().idadeEm(AGORA.toLocalDate()));
        // O paciente é gravado antes da campanha que aponta para ele.
        Assertions.assertEquals(List.of(campanha.getPaciente(), campanha), gravados);
    }

    @Test
    public void doadorNaoPublicaCampanha()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.publicar(doador.getId(), pedidoValido()));

        Assertions.assertEquals("Só médico pode publicar campanha de doação.", erro.getMessage());
        Assertions.assertTrue(gravados.isEmpty());
    }

    @Test
    public void semUsuarioNoHeaderPedeLogin()
    {
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class, () -> servico.publicar(null, pedidoValido()));
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class,
                () -> servico.publicar(UUID.randomUUID(), pedidoValido()));
    }

    @Test
    public void camposAusentesSaemTodosNaMesmaMensagem()
    {
        CampanhaRequest vazio = new CampanhaRequest(null, " ", null, null, null, null, null, "", null);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.publicar(medico.getId(), vazio));

        for (String campo : List.of("hospitalId", "nome", "sexo", "dataNascimento", "tipoSanguineo",
                "tipoComponente", "unidadesNecessarias", "causa", "gravidade"))
        {
            Assertions.assertTrue(erro.getMessage().contains("'" + campo + "'"), campo);
        }
    }

    @Test
    public void hospitalInexistenteNascimentoFuturoEUnidadesForaDaFaixaSaoRecusados()
    {
        CampanhaRequest pedido = new CampanhaRequest("HS-99", "Carlos", Sexo.MASCULINO, LocalDate.of(2027, 1, 1),
                TipoSanguineo.A_POSITIVO, TipoComponente.PLAQUETAS, 51, "Leucemia", Gravidade.ATENCAO);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.publicar(medico.getId(), pedido));

        Assertions.assertTrue(erro.getMessage().contains("Nenhum hospital encontrado com id HS-99."));
        Assertions.assertTrue(erro.getMessage().contains("não pode ser depois de hoje (02/11/2026)"));
        Assertions.assertTrue(erro.getMessage().contains("entre 1 e 50"));
    }

    @Test
    public void editarTrocaOsDadosDoPaciente()
    {
        CampanhaDoacao campanha = servico.editar(medico.getId(), UUID.randomUUID(), pedidoValido());

        Assertions.assertEquals("Carlos Lima", campanha.getPaciente().getNome());
        Assertions.assertEquals(TipoComponente.PLAQUETAS, campanha.getPaciente().getTipoComponente());
        Assertions.assertEquals(3, campanha.getPaciente().getUnidadesNecessarias());
    }

    @Test
    public void campanhaInexistenteDa404()
    {
        campanhaExistente = null;

        Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.editar(medico.getId(), UUID.randomUUID(), pedidoValido()));
    }

    @Test
    public void encerrarCancelaOsAgendamentosEmAberto()
    {
        AgendamentoDoacao emAberto = new AgendamentoDoacao(doador, banco, campanhaExistente, AGORA.plusDays(1), AGORA);
        AgendamentoDoacao realizado = new AgendamentoDoacao(doador, banco, campanhaExistente, AGORA.minusDays(1), AGORA.minusDays(2));
        realizado.mudarStatus(StatusAgendamento.REALIZADO);
        agendamentosDaCampanha.addAll(List.of(emAberto, realizado));

        CampanhaDoacao campanha = servico.encerrar(medico.getId(), UUID.randomUUID());

        Assertions.assertEquals(StatusCampanha.ENCERRADA, campanha.getStatus());
        Assertions.assertEquals(AGORA, campanha.getEncerradaEm());
        Assertions.assertEquals(StatusAgendamento.CANCELADO, emAberto.getStatus());
        Assertions.assertEquals(StatusAgendamento.REALIZADO, realizado.getStatus());
    }

    @Test
    public void campanhaEncerradaNaoEditaNemEncerraDeNovo()
    {
        campanhaExistente.encerrar(AGORA.minusDays(1));

        RegraDeNegocioException edicao = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.editar(medico.getId(), UUID.randomUUID(), pedidoValido()));
        RegraDeNegocioException encerramento = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.encerrar(medico.getId(), UUID.randomUUID()));

        Assertions.assertEquals("A campanha foi encerrada em 01/11/2026 e não pode mais ser editada.", edicao.getMessage());
        Assertions.assertEquals("A campanha já foi encerrada em 01/11/2026.", encerramento.getMessage());
    }
}
