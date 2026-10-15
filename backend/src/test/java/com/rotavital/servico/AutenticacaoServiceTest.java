package com.rotavital.servico;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.UsuarioRepository;

// Regras do login (PI3-183) sem Supabase: o verificador de credenciais é uma lambda e o repositório é falso.
public class AutenticacaoServiceTest
{
    private static final UUID AUTH_MEDICO = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AUTH_SEM_CADASTRO = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final Usuario MEDICO = new Usuario(UUID.randomUUID(), AUTH_MEDICO, "Dra. Ana Lima",
            PapelUsuario.MEDICO, "HOSP-01", null);

    // Só "ana@rotavital.dev" com a senha "certa" passa; "sem-cadastro@rotavital.dev" passa no
    // Supabase Auth, mas não tem linha em usuario.
    private final VerificadorDeCredenciais supabaseFalso = (email, senha) ->
    {
        if (!"certa".equals(senha))
        {
            return Optional.empty();
        }
        return switch (email)
        {
            case "ana@rotavital.dev" -> Optional.of(new SessaoAutenticada(AUTH_MEDICO, "token-ana", 3600));
            case "sem-cadastro@rotavital.dev" -> Optional.of(new SessaoAutenticada(AUTH_SEM_CADASTRO, "token-x", 3600));
            default -> Optional.empty();
        };
    };

    private final UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class,
            "findByAuthUserId", argumentos -> Optional.ofNullable(AUTH_MEDICO.equals(argumentos[0]) ? MEDICO : null));

    private final AutenticacaoService servico = new AutenticacaoService(supabaseFalso, usuarios);

    @Test
    public void credenciaisCertasDevolvemOUsuarioEASessao()
    {
        UsuarioAutenticado autenticado = servico.entrar("ana@rotavital.dev", "certa");

        Assertions.assertSame(MEDICO, autenticado.usuario());
        Assertions.assertEquals("token-ana", autenticado.sessao().accessToken());
    }

    @Test
    public void emailComMaiusculasEEspacosEhNormalizado()
    {
        Assertions.assertSame(MEDICO, servico.entrar("  Ana@RotaVital.dev ", "certa").usuario());
    }

    @Test
    public void senhaErradaEUsuarioInexistenteTemAMesmaMensagem()
    {
        CredenciaisInvalidasException senhaErrada = Assertions.assertThrows(CredenciaisInvalidasException.class,
                () -> servico.entrar("ana@rotavital.dev", "errada"));
        CredenciaisInvalidasException inexistente = Assertions.assertThrows(CredenciaisInvalidasException.class,
                () -> servico.entrar("ninguem@rotavital.dev", "certa"));

        Assertions.assertEquals(AutenticacaoService.CREDENCIAIS_INVALIDAS, senhaErrada.getMessage());
        Assertions.assertEquals(senhaErrada.getMessage(), inexistente.getMessage());
    }

    @Test
    public void loginSemLinhaEmUsuarioEhRecusadoComoCredencialInvalida()
    {
        CredenciaisInvalidasException erro = Assertions.assertThrows(CredenciaisInvalidasException.class,
                () -> servico.entrar("sem-cadastro@rotavital.dev", "certa"));

        Assertions.assertEquals(AutenticacaoService.CREDENCIAIS_INVALIDAS, erro.getMessage());
    }

    @Test
    public void camposVaziosListamTodosOsProblemas()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.entrar(" ", null));

        Assertions.assertEquals("O campo 'email' é obrigatório. O campo 'senha' é obrigatório.", erro.getMessage());
    }

    @Test
    public void supabaseForaDoArSobeComoIndisponivel()
    {
        AutenticacaoService semSupabase = new AutenticacaoService((email, senha) ->
        {
            throw new AutenticacaoIndisponivelException("fora do ar");
        }, usuarios);

        Assertions.assertThrows(AutenticacaoIndisponivelException.class,
                () -> semSupabase.entrar("ana@rotavital.dev", "certa"));
    }
}
