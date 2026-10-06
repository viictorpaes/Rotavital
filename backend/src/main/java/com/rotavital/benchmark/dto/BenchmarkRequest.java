package com.rotavital.benchmark.dto;

public record BenchmarkRequest(
        Integer quantidadeRegistros, // Ex: 100000 ou 1000000
        String modo                 // "TODOS", "SEQUENCIAL", "THREADS_2", "THREADS_4", "THREADS_8", "VIRTUAL_THREADS"
) {}

