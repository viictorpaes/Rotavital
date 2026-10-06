package com.rotavital.diagnostico;

public record LogHttpItem(
        String id,
        String horario,
        String metodo,
        String rota,
        int status,
        long duracaoMs,
        String statusTag
) {}
