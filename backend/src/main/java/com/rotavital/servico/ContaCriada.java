package com.rotavital.servico;

import java.util.UUID;

/**
 * Conta nova no Supabase Auth: o id em auth.users e, se o projeto não exige confirmar o e-mail,
 * a sessão já aberta. Sem sessão, a pessoa só entra depois de clicar no link que o Supabase enviou.
 */
public record ContaCriada(
        UUID authUserId,
        SessaoAutenticada sessao
)

{
    public boolean precisaConfirmarEmail()
    {
        return sessao == null;
    }
}
