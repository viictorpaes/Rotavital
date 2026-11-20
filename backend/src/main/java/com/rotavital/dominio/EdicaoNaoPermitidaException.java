package com.rotavital.dominio;

// Correção de dados recusada porque a bolsa já saiu do estoque (reservada, enviada, usada ou descartada).
public class EdicaoNaoPermitidaException extends RuntimeException
{
    public EdicaoNaoPermitidaException(String mensagem)
    {
        super(mensagem);
    }
}
