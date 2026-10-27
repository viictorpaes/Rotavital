package com.rotavital.servico;

// A requisição não diz quem está agindo (header X-Usuario-Id ausente ou de um usuário que não existe).
// A API responde 401 com a mensagem no campo detail.
public class UsuarioNaoIdentificadoException extends RuntimeException
{
    public UsuarioNaoIdentificadoException(String mensagem)
    {
        super(mensagem);
    }
}
