package com.rotavital.api.dto.doador;

import java.time.LocalDate;

import com.rotavital.dominio.TipoSanguineo;

// Corpo do PUT /doadores/eu. E-mail e senha ficam de fora: são o login, no Supabase Auth.
public record AtualizarPerfilDoadorRequest(
        String nome,
        LocalDate dataNascimento,
        TipoSanguineo tipoSanguineo,
        String telefone
)
{
}
