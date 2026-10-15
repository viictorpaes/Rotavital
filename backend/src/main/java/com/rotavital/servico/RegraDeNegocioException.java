package com.rotavital.servico;

// Os dados vieram bem preenchidos, mas quebram uma regra do estoque (ex.: bolsa já vencida).
// A API responde 422 com a mensagem no campo detail.
public class RegraDeNegocioException extends RuntimeException
{
    public RegraDeNegocioException(String mensagem)
    {
        super(mensagem);
    }
}
