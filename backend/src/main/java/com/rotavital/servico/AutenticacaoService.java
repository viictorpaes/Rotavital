package com.rotavital.servico;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.UsuarioRepository;

/**
 * Login com e-mail e senha (PI3-183). A senha é conferida pelo Supabase Auth; o perfil (papel)
 * vem da tabela usuario, ligada a auth.users por auth_user_id (decisão 4 de docs/PERFIS_DE_ACESSO.md).
 */
@Service
public class AutenticacaoService
{
    // Mesma mensagem para senha errada, e-mail sem cadastro e login sem linha em usuario (PI3-196).
    public static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha incorretos.";

    private static final Logger LOG = LoggerFactory.getLogger(AutenticacaoService.class);

    private final VerificadorDeCredenciais verificador;
    private final UsuarioRepository usuarios;

    public AutenticacaoService(VerificadorDeCredenciais verificador, UsuarioRepository usuarios)
    {
        this.verificador = verificador;
        this.usuarios = usuarios;
    }

    /**
     * Campo vazio lança DadosInvalidosException (400); credenciais que não conferem lançam
     * CredenciaisInvalidasException (401); Supabase Auth fora do ar lança
     * AutenticacaoIndisponivelException (503).
     */
    @Transactional(readOnly = true)
    public UsuarioAutenticado entrar(String email, String senha)
    {
        validarCampos(email, senha);

        SessaoAutenticada sessao = verificador.verificar(email.trim().toLowerCase(Locale.ROOT), senha)
                .orElseThrow(() -> new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS));

        // O login existe no Supabase Auth, mas ninguém o ligou a uma linha de usuario: sem papel não
        // há tela inicial. Para quem tenta entrar, responde como credencial inválida; o log diz o motivo.
        Usuario usuario = usuarios.findByAuthUserId(sessao.authUserId())
                .orElseThrow(() ->
                {
                    LOG.warn("Login aceito pelo Supabase Auth sem linha em usuario (auth_user_id = {}).",
                            sessao.authUserId());
                    return new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS);
                });

        return new UsuarioAutenticado(usuario, sessao);
    }

    private static void validarCampos(String email, String senha)
    {
        List<String> problemas = new ArrayList<>();

        if (email == null || email.isBlank())
        {
            problemas.add("O campo 'email' é obrigatório.");
        }
        if (senha == null || senha.isEmpty())
        {
            problemas.add("O campo 'senha' é obrigatório.");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }
    }
}
