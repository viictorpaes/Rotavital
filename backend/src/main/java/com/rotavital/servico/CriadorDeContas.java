package com.rotavital.servico;

import java.util.Optional;

/**
 * Cria a conta de login (e-mail e senha) do doador que se cadastra pelo portal (PI3-203).
 * Em produção é o Supabase Auth (SupabaseAuth); nos testes, uma lambda.
 *
 * Vazio quando o e-mail já tem conta. Lança DadosInvalidosException quando o Supabase recusa
 * o e-mail ou a senha, e AutenticacaoIndisponivelException quando não deu para criar agora.
 */
@FunctionalInterface
public interface CriadorDeContas
{
    Optional<ContaCriada> criar(String email, String senha);
}
