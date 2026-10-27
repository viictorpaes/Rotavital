package com.rotavital.servico;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.UsuarioRepository;

/**
 * Descobre quem está agindo a partir do header X-Usuario-Id, que o frontend manda com o id
 * devolvido pelo POST /acessos. O token do Supabase ainda não é conferido nos outros endpoints:
 * aqui só se garante que o usuário existe e que o perfil dele pode fazer a ação.
 */
@Service
public class IdentificacaoService
{
    private final UsuarioRepository usuarios;

    public IdentificacaoService(UsuarioRepository usuarios)
    {
        this.usuarios = usuarios;
    }

    /**
     * O usuário do header, se o papel dele for um dos aceitos. Header ausente ou usuário
     * inexistente lança UsuarioNaoIdentificadoException (401); outro papel lança
     * AcessoNegadoException (403) com a mensagem de quem pode fazer a ação.
     */
    public Usuario exigir(UUID usuarioId, String acao, PapelUsuario... papeisAceitos)
    {
        if (usuarioId == null)
        {
            throw new UsuarioNaoIdentificadoException("Entre no sistema para " + acao + ".");
        }

        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoIdentificadoException(
                        "Sessão inválida: nenhum usuário encontrado com id " + usuarioId + ". Entre de novo."));

        if (Arrays.stream(papeisAceitos).noneMatch(papel -> papel == usuario.getPapel()))
        {
            throw new AcessoNegadoException("Só " + nomes(papeisAceitos) + " pode " + acao + ".");
        }

        return usuario;
    }

    private static String nomes(PapelUsuario[] papeis)
    {
        String[] nomes = Arrays.stream(papeis).map(IdentificacaoService::nome).toArray(String[]::new);
        if (nomes.length == 1)
        {
            return nomes[0];
        }
        return String.join(", ", Arrays.copyOf(nomes, nomes.length - 1)) + " ou " + nomes[nomes.length - 1];
    }

    private static String nome(PapelUsuario papel)
    {
        return switch (papel)
        {
            case MEDICO -> "médico";
            case DOADOR -> "doador";
            case ADMIN -> "admin";
        };
    }
}
