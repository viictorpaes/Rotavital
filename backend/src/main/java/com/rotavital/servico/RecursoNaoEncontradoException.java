package com.rotavital.servico;

// O id pedido não existe no banco. A API responde 404 com a mensagem no campo detail.
public class RecursoNaoEncontradoException extends RuntimeException
{
    public RecursoNaoEncontradoException(String mensagem)
    {
        super(mensagem);
    }
}
