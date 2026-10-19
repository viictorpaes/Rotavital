package com.rotavital.servico;

import java.util.Optional;

/**
 * Confere e-mail e senha. Em produção é o Supabase Auth (SupabaseAuth); nos testes, uma lambda.
 * Vazio quando as credenciais não conferem, seja senha errada ou e-mail sem cadastro: quem chama não
 * precisa (e não deve) saber qual dos dois foi. Lança AutenticacaoIndisponivelException quando não
 * deu para perguntar.
 */
@FunctionalInterface
public interface VerificadorDeCredenciais
{
    Optional<SessaoAutenticada> verificar(String email, String senha);
}
