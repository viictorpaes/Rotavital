package com.rotavital.servico;

// O Supabase Auth não respondeu, respondeu com erro ou a chave dele não está configurada.
// A API responde 503: o problema não é do usuário, e ele pode tentar de novo depois.
public class AutenticacaoIndisponivelException extends RuntimeException
{
    public AutenticacaoIndisponivelException(String mensagem)
    {
        super(mensagem);
    }

    public AutenticacaoIndisponivelException(String mensagem, Throwable causa)
    {
        super(mensagem, causa);
    }
}
