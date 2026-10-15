package com.rotavital.diagnostico;

import java.util.List;

public record DiagnosticoResponse(
        BackendInfo backend,
        BancoInfo bancoDeDados,
        List<LogHttpItem> logsRecentes
)
{
    public record BackendInfo(
            String status,
            long uptimeSegundos,
            String versaoJava,
            String versaoSpringBoot,
            String dataHora
    ) {}

    public record BancoInfo(
            String status,
            long latenciaMs,
            String produto,
            String catalogo,
            String schema,
            String urlMascarada,
            long totalPontosRede,
            long totalBolsasEstoque,
            long totalConexoes,
            String estadoTabelas,
            String mensagemErro
    ) {}
}
