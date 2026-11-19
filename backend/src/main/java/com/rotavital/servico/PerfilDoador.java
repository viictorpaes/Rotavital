package com.rotavital.servico;

import java.time.OffsetDateTime;

import com.rotavital.dominio.Usuario;

// O doador e o resumo das doações dele (PI3-203). ultimaDoacao e proximaDoacao ficam nulas quando não há.
public record PerfilDoador(
        Usuario doador,
        Integer idade,
        long doacoesRealizadas,
        OffsetDateTime ultimaDoacao,
        OffsetDateTime proximaDoacao
)

{

}
