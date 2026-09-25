package com.rotavital.benchmark.dto;

import java.util.List;

public record BenchmarkResponse(
        int tamanhoMassa,
        boolean todosResultadosIdenticos,
        String mensagemConsistencia,
        List<ItemExecucaoDTO> execucoes
) {}

