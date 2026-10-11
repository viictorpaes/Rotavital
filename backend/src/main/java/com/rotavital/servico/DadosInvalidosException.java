package com.rotavital.servico;

// Dado ausente ou mal preenchido na requisição. A API responde 400 com a mensagem no campo detail.
public class DadosInvalidosException extends RuntimeException
{
    public DadosInvalidosException(String mensagem)
    {
        super(mensagem);
    }
}
