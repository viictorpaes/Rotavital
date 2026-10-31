package com.rotavital.api.dto.doador;

import java.time.LocalDate;

import com.rotavital.dominio.TipoSanguineo;

// Corpo do POST /doadores (PI3-203). tipoSanguineo e telefone são opcionais: muita gente só
// descobre o tipo na primeira doação.
public record NovoDoadorRequest(
        String nome,
        String email,
        String senha,
        LocalDate dataNascimento,
        TipoSanguineo tipoSanguineo,
        String telefone
)
{
}
