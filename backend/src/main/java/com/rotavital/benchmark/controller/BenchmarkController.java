package com.rotavital.benchmark.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.benchmark.dto.BenchmarkRequest;
import com.rotavital.benchmark.dto.BenchmarkResponse;
import com.rotavital.benchmark.dto.ItemExecucaoDTO;
import com.rotavital.benchmark.model.RegistroTelemetria;
import com.rotavital.benchmark.model.ResultadoAuditoria;
import com.rotavital.benchmark.service.AuditoriaTelemetriaService;

@RestController
@RequestMapping("/api/v1/benchmarks")
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private final AuditoriaTelemetriaService auditoriaService;

    public BenchmarkController(AuditoriaTelemetriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/auditoria-telemetria")
    public ResponseEntity<BenchmarkResponse> executarViaGet(
            @RequestParam(defaultValue = "100000") int tamanho,
            @RequestParam(defaultValue = "TODOS") String modo) throws Exception {
        return ResponseEntity.ok(processar(tamanho, modo));
    }

    @PostMapping("/auditoria-telemetria")
    public ResponseEntity<BenchmarkResponse> executarViaPost(
            @RequestBody(required = false) BenchmarkRequest request) throws Exception {
        int tamanho = (request != null && request.quantidadeRegistros() != null)
                ? request.quantidadeRegistros()
                : 100000;
        String modo = (request != null && request.modo() != null)
                ? request.modo().toUpperCase()
                : "TODOS";

        return ResponseEntity.ok(processar(tamanho, modo));
    }

    private BenchmarkResponse processar(int tamanho, String modo) throws Exception {
        // 1. Gera o volume de dados em memória
        List<RegistroTelemetria> dados = auditoriaService.gerarMassaDados(tamanho);

        List<ItemExecucaoDTO> execucoes = new ArrayList<>();
        boolean todosIdenticos = true;

        if ("TODOS".equalsIgnoreCase(modo)) {
            // Execução Sequencial (Baseline)
            long t0 = System.nanoTime();
            ResultadoAuditoria resSeq = auditoriaService.processarSequencial(dados);
            long tempoSeqMs = (System.nanoTime() - t0) / 1_000_000;
            execucoes.add(criarItem("SEQUENCIAL", 1, tempoSeqMs, 1.0, resSeq));

            // Execução com 2 Threads
            long t2_0 = System.nanoTime();
            ResultadoAuditoria res2 = auditoriaService.processarComThreads(dados, 2);
            long tempo2Ms = Math.max(1, (System.nanoTime() - t2_0) / 1_000_000);
            double speedup2 = Math.round(((double) tempoSeqMs / tempo2Ms) * 100.0) / 100.0;
            execucoes.add(criarItem("THREADS_2", 2, tempo2Ms, speedup2, res2));
            if (!resSeq.equals(res2)) todosIdenticos = false;

            // Execução com 4 Threads
            long t4_0 = System.nanoTime();
            ResultadoAuditoria res4 = auditoriaService.processarComThreads(dados, 4);
            long tempo4Ms = Math.max(1, (System.nanoTime() - t4_0) / 1_000_000);
            double speedup4 = Math.round(((double) tempoSeqMs / tempo4Ms) * 100.0) / 100.0;
            execucoes.add(criarItem("THREADS_4", 4, tempo4Ms, speedup4, res4));
            if (!resSeq.equals(res4)) todosIdenticos = false;

            // Execução com 8 Threads
            long t8_0 = System.nanoTime();
            ResultadoAuditoria res8 = auditoriaService.processarComThreads(dados, 8);
            long tempo8Ms = Math.max(1, (System.nanoTime() - t8_0) / 1_000_000);
            double speedup8 = Math.round(((double) tempoSeqMs / tempo8Ms) * 100.0) / 100.0;
            execucoes.add(criarItem("THREADS_8", 8, tempo8Ms, speedup8, res8));
            if (!resSeq.equals(res8)) todosIdenticos = false;

            // Execução com Virtual Threads (Java 21)
            long tv_0 = System.nanoTime();
            ResultadoAuditoria resVt = auditoriaService.processarComVirtualThreads(dados, 8);
            long tempoVtMs = Math.max(1, (System.nanoTime() - tv_0) / 1_000_000);
            double speedupVt = Math.round(((double) tempoSeqMs / tempoVtMs) * 100.0) / 100.0;
            execucoes.add(criarItem("VIRTUAL_THREADS_8", 8, tempoVtMs, speedupVt, resVt));
            if (!resSeq.equals(resVt)) todosIdenticos = false;

        } else if ("SEQUENCIAL".equalsIgnoreCase(modo)) {
            long t0 = System.nanoTime();
            ResultadoAuditoria res = auditoriaService.processarSequencial(dados);
            long tempoMs = (System.nanoTime() - t0) / 1_000_000;
            execucoes.add(criarItem("SEQUENCIAL", 1, tempoMs, 1.0, res));

        } else if (modo.startsWith("THREADS_")) {
            int n = Integer.parseInt(modo.replace("THREADS_", ""));
            long t0 = System.nanoTime();
            ResultadoAuditoria res = auditoriaService.processarComThreads(dados, n);
            long tempoMs = (System.nanoTime() - t0) / 1_000_000;
            execucoes.add(criarItem(modo, n, tempoMs, 1.0, res));

        } else if ("VIRTUAL_THREADS".equalsIgnoreCase(modo)) {
            long t0 = System.nanoTime();
            ResultadoAuditoria res = auditoriaService.processarComVirtualThreads(dados, 8);
            long tempoMs = (System.nanoTime() - t0) / 1_000_000;
            execucoes.add(criarItem("VIRTUAL_THREADS_8", 8, tempoMs, 1.0, res));
        }

        String msg = todosIdenticos
                ? "SUCESSO: Todas as versões (sequencial e paralelas) retornaram exatamente o mesmo resultado numérico. Zero race condition!"
                : "ALERTA: Houve divergência numérica entre as execuções.";

        return new BenchmarkResponse(tamanho, todosIdenticos, msg, execucoes);
    }

    private ItemExecucaoDTO criarItem(String modo, int threads, long tempoMs, double speedup, ResultadoAuditoria res) {
        return new ItemExecucaoDTO(
                modo,
                threads,
                tempoMs,
                speedup,
                res.getTotalProcessado(),
                res.getTotalAnomalias(),
                res.getTotalDescartesCriticos(),
                res.getMediaTemperatura(),
                res.getDesvioPadraoTemperatura(),
                res.getIndiceDegradacaoMedio()
        );
    }
}

