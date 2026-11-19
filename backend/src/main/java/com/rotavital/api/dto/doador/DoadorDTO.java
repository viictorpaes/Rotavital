package com.rotavital.api.dto.doador;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.rotavital.dominio.TipoSanguineo;

// Perfil do doador (PI3-203): os dados do cadastro e o resumo das doações dele.
public record DoadorDTO(
        UUID id,
        String nome,
        String email,
        LocalDate dataNascimento,
        Integer idade,
        TipoSanguineo tipoSanguineo,
        String telefone,
        long doacoesRealizadas,
        OffsetDateTime ultimaDoacao,
        OffsetDateTime proximaDoacao
)
{
}
