package com.rotavital.servico;

// E-mail ou senha não conferem. A API responde 401 sempre com a mesma mensagem, para não revelar
// se o e-mail está cadastrado.
public class CredenciaisInvalidasException extends RuntimeException
{
    public CredenciaisInvalidasException(String mensagem)
    {
        super(mensagem);
    }
}
