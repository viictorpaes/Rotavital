package com.rotavital.api.dto.doador;

import com.rotavital.api.dto.acesso.AcessoDTO;

/**
 * Resposta do POST /doadores. acesso é o mesmo do POST /acessos, para o portal já entrar;
 * vem nulo quando o Supabase exige confirmar o e-mail antes do primeiro login.
 */
public record CadastroDoadorDTO(
        DoadorDTO doador,
        AcessoDTO acesso,
        boolean precisaConfirmarEmail
)
{
}
