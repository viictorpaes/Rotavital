package com.rotavital.dominio;

// A bolsa não pode passar do status atual para o pedido (ex.: usar de novo uma bolsa já UTILIZADA).
// O serviço converte em RegraDeNegocioException, e a API responde 422.
public class MovimentacaoInvalidaException extends RuntimeException
{
    public MovimentacaoInvalidaException(String mensagem)
    {
        super(mensagem);
    }
}
