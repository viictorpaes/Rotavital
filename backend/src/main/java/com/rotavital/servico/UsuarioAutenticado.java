package com.rotavital.servico;

import com.rotavital.dominio.Usuario;

// Resultado de um login aceito: quem entrou (tabela usuario) e a sessão do Supabase Auth.
public record UsuarioAutenticado(
        Usuario usuario,
        SessaoAutenticada sessao
)

{

}
