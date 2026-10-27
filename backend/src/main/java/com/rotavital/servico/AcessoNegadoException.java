package com.rotavital.servico;

// O usuário foi identificado, mas o perfil dele não pode fazer esta ação (docs/PERFIS_DE_ACESSO.md, seção 5).
// A API responde 403 com a mensagem no campo detail.
public class AcessoNegadoException extends RuntimeException
{
    public AcessoNegadoException(String mensagem)
    {
        super(mensagem);
    }
}
