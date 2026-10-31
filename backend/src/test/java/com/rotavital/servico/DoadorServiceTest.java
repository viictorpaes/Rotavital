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

import com.rotavital.api.dto.doador.AtualizarPerfilDoadorRequest;
import com.rotavital.api.dto.doador.NovoDoadorRequest;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Cadastro e perfil do doador (PI3-203), com o Supabase Auth e os repositórios no lugar de lambdas.
public class DoadorServiceTest
{
    private static final ZoneOffset RECIFE = ZoneOffset.ofHours(-3);
    // Sexta-feira, 30/10/2026, 10h em Recife.
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 10, 30, 10, 0, 0, 0, RECIFE);
    private static final UUID CONTA_NOVA = UUID.fromString("6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f");
    private static final SessaoAutenticada SESSAO = new SessaoAutenticada(CONTA_NOVA, "eyJ...", 3600);

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, "HS-01", null);
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.06, -34.88));

    private final List<Usuario> gravados = new ArrayList<>();
    private final List<AgendamentoDoacao> agendamentosDoDoador = new ArrayList<>();
    // O que o Supabase Auth responde no cadastro e no login.
    private Optional<ContaCriada> contaCriada;
    private Optional<SessaoAutenticada> loginAceito = Optional.empty();
    private int contasCriadas;
    private DoadorService servico;

    @BeforeEach
    public void montarServico()
    {
        contaCriada = Optional.of(new ContaCriada(CONTA_NOVA, SESSAO));
        gravados.add(medico);

        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, Map.of(
                "existsByEmailIgnoreCase", argumentos -> gravados.stream()
                        .anyMatch(usuario -> ((String) argumentos[0]).equalsIgnoreCase(usuario.getEmail())),
                "findByAuthUserId", argumentos -> gravados.stream()
                        .filter(usuario -> argumentos[0].equals(usuario.getAuthUserId())).findFirst(),
                "findById", argumentos -> gravados.stream()
                        .filter(usuario -> usuario.getId().equals(argumentos[0])).findFirst(),
                "save", argumentos ->
                {
                    Usuario usuario = (Usuario) argumentos[0];
                    if (!gravados.contains(usuario))
                    {
                        gravados.add(usuario);
                    }
                    return usuario;
                }));
        AgendamentoDoacaoRepository agendamentos = RepositorioFalso.respondendo(AgendamentoDoacaoRepository.class,
                "findByDoadorIdOrderByAgendadoParaDesc", argumentos -> agendamentosDoDoador);

        CriadorDeContas contas = (email, senha) ->
        {
            contasCriadas++;
            return contaCriada;
        };
        VerificadorDeCredenciais verificador = (email, senha) -> loginAceito;

        servico = new DoadorService(contas, verificador, usuarios, agendamentos, new IdentificacaoService(usuarios),
                Clock.fixed(AGORA.toInstant(), RelogioConfig.FUSO));
    }

    @Test
    public void doadorSeCadastraEJaEntra()
    {
        DoadorCadastrado cadastrado = servico.cadastrar(new NovoDoadorRequest("  Rita   de Souza ", " Rita@Email.com ",
                "segredo1", LocalDate.of(1995, 4, 20), TipoSanguineo.A_POSITIVO, "(81) 99876-5432"));

        Usuario doador = cadastrado.doador();
        Assertions.assertEquals(PapelUsuario.DOADOR, doador.getPapel());
        Assertions.assertEquals(CONTA_NOVA, doador.getAuthUserId());
        Assertions.assertEquals("Rita de Souza", doador.getNome());
        Assertions.assertEquals("rita@email.com", doador.getEmail());
        Assertions.assertEquals("81998765432", doador.getTelefone());
        Assertions.assertEquals(TipoSanguineo.A_POSITIVO, doador.getTipoSanguineo());
        Assertions.assertEquals(31, doador.idadeEm(AGORA.toLocalDate()));
        Assertions.assertNotNull(doador.getId());
        Assertions.assertSame(SESSAO, cadastrado.sessao());
        Assertions.assertTrue(gravados.contains(doador));
    }

    @Test
    public void tipoSanguineoETelefoneSaoOpcionais()
    {
        DoadorCadastrado cadastrado = servico.cadastrar(new NovoDoadorRequest("Rita", "rita@email.com", "segredo1",
                LocalDate.of(1995, 4, 20), null, "  "));

        Assertions.assertNull(cadastrado.doador().getTipoSanguineo());
        Assertions.assertNull(cadastrado.doador().getTelefone());
    }

    @Test
    public void comConfirmacaoDeEmailOCadastroFicaSemSessao()
    {
        contaCriada = Optional.of(new ContaCriada(CONTA_NOVA, null));

        DoadorCadastrado cadastrado = servico.cadastrar(novo("rita@email.com"));

        Assertions.assertTrue(cadastrado.precisaConfirmarEmail());
        Assertions.assertEquals(CONTA_NOVA, cadastrado.doador().getAuthUserId());
    }

    @Test
    public void camposObrigatoriosVemTodosNaMesmaMensagem()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(new NovoDoadorRequest(" ", "", null, null, null, null)));

        Assertions.assertEquals("O campo 'nome' é obrigatório. O campo 'email' é obrigatório. "
                + "O campo 'senha' é obrigatório. O campo 'dataNascimento' é obrigatório.", erro.getMessage());
        Assertions.assertEquals(0, contasCriadas, "não cria a conta no Supabase com dado errado");
    }

    @Test
    public void emailSenhaETelefoneForaDoFormatoSaoRecusados()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(new NovoDoadorRequest("Rita", "rita.email.com", "123", LocalDate.of(1995, 4, 20),
                        null, "9876-5432")));

        Assertions.assertEquals("O e-mail rita.email.com não é válido. A senha precisa ter pelo menos 6 caracteres. "
                + "O telefone precisa ter DDD e 8 ou 9 dígitos, como (81) 99876-5432.", erro.getMessage());
    }

    @Test
    public void soQuemTemDe16A69AnosPodeSeCadastrar()
    {
        DadosInvalidosException novo = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(comNascimento(LocalDate.of(2010, 10, 31))));
        DadosInvalidosException velho = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(comNascimento(LocalDate.of(1956, 10, 30))));
        DadosInvalidosException futuro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(comNascimento(LocalDate.of(2026, 10, 30))));

        Assertions.assertEquals("Para doar sangue é preciso ter entre 16 e 69 anos (pela data informada, você tem 15).",
                novo.getMessage());
        Assertions.assertTrue(velho.getMessage().endsWith("você tem 70)."));
        Assertions.assertEquals("A data de nascimento precisa ser no passado.", futuro.getMessage());

        // 16 anos completos hoje e 69 anos até a véspera dos 70 são aceitos.
        Assertions.assertNotNull(servico.cadastrar(comNascimento(LocalDate.of(2010, 10, 30))));
    }

    @Test
    public void emailQueJaTemPerfilNaoCriaOutraConta()
    {
        servico.cadastrar(novo("rita@email.com"));

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.cadastrar(novo("RITA@email.com")));

        Assertions.assertEquals("Já existe uma conta com este e-mail. Entre com a sua senha.", erro.getMessage());
        Assertions.assertEquals(1, contasCriadas);
    }

    @Test
    public void contaDoSupabaseSemPerfilTerminaOCadastroComASenhaCerta()
    {
        // O Supabase já tem a conta (cadastro que parou no meio): com a senha certa, o login devolve a sessão.
        contaCriada = Optional.empty();
        loginAceito = Optional.of(SESSAO);

        DoadorCadastrado cadastrado = servico.cadastrar(novo("rita@email.com"));

        Assertions.assertEquals(CONTA_NOVA, cadastrado.doador().getAuthUserId());
        Assertions.assertSame(SESSAO, cadastrado.sessao());
    }

    @Test
    public void contaDoSupabaseComSenhaErradaOuJaLigadaEhEmailComDono()
    {
        contaCriada = Optional.empty();

        Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.cadastrar(novo("rita@email.com")));

        // O médico entra com este e-mail, mas a linha dele em usuario não tem o e-mail preenchido.
        Usuario medicoComConta = new Usuario(UUID.randomUUID(), CONTA_NOVA, "Dr. Paulo", PapelUsuario.MEDICO, "HS-01", null);
        gravados.add(medicoComConta);
        loginAceito = Optional.of(SESSAO);

        Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.cadastrar(novo("paulo@hospital.com")));
    }

    @Test
    public void perfilMostraOsDadosEOResumoDasDoacoes()
    {
        Usuario doador = servico.cadastrar(novo("rita@email.com")).doador();
        AgendamentoDoacao proxima = new AgendamentoDoacao(doador, banco, null, AGORA.plusDays(5), AGORA);
        AgendamentoDoacao feita = new AgendamentoDoacao(doador, banco, null, AGORA.minusMonths(4), AGORA.minusMonths(5));
        feita.mudarStatus(StatusAgendamento.REALIZADO);
        AgendamentoDoacao antiga = new AgendamentoDoacao(doador, banco, null, AGORA.minusMonths(9), AGORA.minusMonths(10));
        antiga.mudarStatus(StatusAgendamento.REALIZADO);
        AgendamentoDoacao cancelada = new AgendamentoDoacao(doador, banco, null, AGORA.minusMonths(6), AGORA.minusMonths(7));
        cancelada.mudarStatus(StatusAgendamento.CANCELADO);
        agendamentosDoDoador.addAll(List.of(proxima, feita, cancelada, antiga));

        PerfilDoador perfil = servico.perfil(doador.getId());

        Assertions.assertSame(doador, perfil.doador());
        Assertions.assertEquals(31, perfil.idade());
        Assertions.assertEquals(2, perfil.doacoesRealizadas());
        Assertions.assertEquals(AGORA.minusMonths(4), perfil.ultimaDoacao());
        Assertions.assertEquals(AGORA.plusDays(5), perfil.proximaDoacao());
    }

    @Test
    public void doadorCorrigeOsProprioDados()
    {
        Usuario doador = servico.cadastrar(novo("rita@email.com")).doador();

        PerfilDoador perfil = servico.atualizar(doador.getId(), new AtualizarPerfilDoadorRequest("Rita Souza",
                LocalDate.of(1990, 1, 2), TipoSanguineo.O_NEGATIVO, "81 3333-4444"));

        Assertions.assertEquals("Rita Souza", perfil.doador().getNome());
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, perfil.doador().getTipoSanguineo());
        Assertions.assertEquals("8133334444", perfil.doador().getTelefone());
        Assertions.assertEquals(36, perfil.idade());
        Assertions.assertEquals("rita@email.com", perfil.doador().getEmail(), "o e-mail é o login e não muda");
    }

    @Test
    public void soODoadorVeEAlteraOPerfil()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.perfil(medico.getId()));
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class, () -> servico.perfil(null));
        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.atualizar(medico.getId(),
                new AtualizarPerfilDoadorRequest("Ana", LocalDate.of(1980, 1, 1), null, null)));

        Assertions.assertEquals("Só doador pode ver o seu perfil.", erro.getMessage());
    }

    private static NovoDoadorRequest novo(String email)
    {
        return new NovoDoadorRequest("Rita", email, "segredo1", LocalDate.of(1995, 4, 20), null, null);
    }

    private static NovoDoadorRequest comNascimento(LocalDate dataNascimento)
    {
        return new NovoDoadorRequest("Rita", "rita" + dataNascimento + "@email.com", "segredo1", dataNascimento, null, null);
    }
}
