package com.rotavital.servico;

import com.rotavital.dominio.Usuario;

// Resultado do cadastro pelo portal: o doador gravado e a sessão, que fica nula enquanto o e-mail não é confirmado.
public record DoadorCadastrado(
        Usuario doador,
        SessaoAutenticada sessao
)

{
    public boolean precisaConfirmarEmail()
    {
        return sessao == null;
    }
}
