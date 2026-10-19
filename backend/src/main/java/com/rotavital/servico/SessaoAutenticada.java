package com.rotavital.servico;

import java.util.UUID;

// O que o Supabase Auth devolve quando e-mail e senha conferem: o id em auth.users e o token da sessão.
public record SessaoAutenticada(
        UUID authUserId,
        String accessToken,
        long expiraEmSegundos
)

{

}
