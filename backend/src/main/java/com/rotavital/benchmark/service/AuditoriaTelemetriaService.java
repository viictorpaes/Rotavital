package com.rotavital.benchmark.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import com.rotavital.benchmark.model.RegistroTelemetria;
import com.rotavital.benchmark.model.ResultadoAuditoria;

@Service
public class AuditoriaTelemetriaService {

    // Coordenada de referência (Hemope Central - Recife/PE)
    private static final double LAT_BASE = -8.0578;
    private static final double LON_BASE = -34.8829;
    private static final double RAIO_TERRA_KM = 6371.0;

    /**
     * Gera uma massa sintética determinística de N registros para garantir
     * que os testes sejam exatamente reproduzíveis.
     */
    public List<RegistroTelemetria> gerarMassaDados(int quantidade) {
        List<RegistroTelemetria> lista = new ArrayList<>(quantidade);
        // Semente fixa para garantir igualdade estrita entre as execuções
        Random random = new Random(42L);

        String[] tipos = {"HEMACIAS", "PLAQUETAS", "PLASMA"};

        for (int i = 0; i < quantidade; i++) {
            String tipo = tipos[random.nextInt(tipos.length)];
            double tempBase;
            if ("HEMACIAS".equals(tipo)) {
                tempBase = 2.0 + (random.nextDouble() * 5.0); // 2 a 7 °C
            } else if ("PLAQUETAS".equals(tipo)) {
                tempBase = 19.0 + (random.nextDouble() * 6.0); // 19 a 25 °C
            } else {
                tempBase = -25.0 + (random.nextDouble() * 10.0); // -25 a -15 °C
            }

            // Simula pequenas coordenadas ao redor de PE e estados vizinhos
            double lat = LAT_BASE + ((random.nextDouble() - 0.5) * 3.0);
            double lon = LON_BASE + ((random.nextDouble() - 0.5) * 3.0);
            int minutos = 10 + random.nextInt(300);

            lista.add(new RegistroTelemetria(
                    "LEIT-" + i,
                    "BOLSA-" + (i % 10000),
                    tipo,
                    tempBase,
                    lat,
                    lon,
                    minutos
            ));
        }
        return lista;
    }

    /**
     * Processa uma única leitura aplicando regras térmicas e cálculo Haversine (CPU-bound).
     */
    private void processarRegistro(RegistroTelemetria reg, ResultadoAuditoria resultado) {
        double minPermitido;
        double maxPermitido;

        switch (reg.tipoComponente()) {
            case "HEMACIAS" -> {
                minPermitido = 2.0;
                maxPermitido = 6.0;
            }
            case "PLAQUETAS" -> {
                minPermitido = 20.0;
                maxPermitido = 24.0;
            }
            case "PLASMA" -> {
                minPermitido = -30.0;
                maxPermitido = -18.0;
            }
            default -> {
                minPermitido = 0.0;
                maxPermitido = 10.0;
            }
        }

        // Cálculo de distância por Haversine (intensivo em CPU: sen, cos, atan2, sqrt)
        double dLat = Math.toRadians(reg.latitude() - LAT_BASE);
        double dLon = Math.toRadians(reg.longitude() - LON_BASE);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(LAT_BASE)) * Math.cos(Math.toRadians(reg.latitude())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanciaKm = RAIO_TERRA_KM * c;

        // Desvio da temperatura ideal
        double desvio = 0.0;
        if (reg.temperatura() < minPermitido) {
            desvio = minPermitido - reg.temperatura();
        } else if (reg.temperatura() > maxPermitido) {
            desvio = reg.temperatura() - maxPermitido;
        }

        boolean anomalia = desvio > 0.0;

        // Índice ponderado de degradação térmica
        double horas = reg.minutosEmTransito() / 60.0;
        double indiceDegradacao = (distanciaKm * 0.05) + (horas * desvio * 15.0);

        boolean descarteCritico = desvio > 2.5 || indiceDegradacao > 120.0;

        resultado.acumular(anomalia, descarteCritico, reg.temperatura(), indiceDegradacao);
    }

    /**
     * Versão 1: Sequencial (1 única Thread)
     */
    public ResultadoAuditoria processarSequencial(List<RegistroTelemetria> dados) {
        ResultadoAuditoria resultado = new ResultadoAuditoria();
        for (RegistroTelemetria reg : dados) {
            processarRegistro(reg, resultado);
        }
        return resultado;
    }

    /**
     * Versão 2: Com Threads de Plataforma (ExecutorService: 2, 4 ou 8 threads)
     */
    public ResultadoAuditoria processarComThreads(List<RegistroTelemetria> dados, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int total = dados.size();
        int tamanhoFatia = total / numThreads;

        List<Callable<ResultadoAuditoria>> tarefas = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            int inicio = i * tamanhoFatia;
            int fim = (i == numThreads - 1) ? total : inicio + tamanhoFatia;

            tarefas.add(() -> {
                ResultadoAuditoria parcial = new ResultadoAuditoria();
                for (int j = inicio; j < fim; j++) {
                    processarRegistro(dados.get(j), parcial);
                }
                return parcial;
            });
        }

        List<Future<ResultadoAuditoria>> futuros = executor.invokeAll(tarefas);
        executor.shutdown();

        // Agregação final (Reduce)
        ResultadoAuditoria consolidado = new ResultadoAuditoria();
        for (Future<ResultadoAuditoria> f : futuros) {
            consolidado = ResultadoAuditoria.combinar(consolidado, f.get());
        }

        return consolidado;
    }

    /**
     * Versão 3: Com Virtual Threads (Java 21) - Opcional de destaque
     */
    public ResultadoAuditoria processarComVirtualThreads(List<RegistroTelemetria> dados, int numFatias) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            int total = dados.size();
            int tamanhoFatia = total / numFatias;

            List<Callable<ResultadoAuditoria>> tarefas = new ArrayList<>(numFatias);

            for (int i = 0; i < numFatias; i++) {
                int inicio = i * tamanhoFatia;
                int fim = (i == numFatias - 1) ? total : inicio + tamanhoFatia;

                tarefas.add(() -> {
                    ResultadoAuditoria parcial = new ResultadoAuditoria();
                    for (int j = inicio; j < fim; j++) {
                        processarRegistro(dados.get(j), parcial);
                    }
                    return parcial;
                });
            }

            List<Future<ResultadoAuditoria>> futuros = executor.invokeAll(tarefas);

            ResultadoAuditoria consolidado = new ResultadoAuditoria();
            for (Future<ResultadoAuditoria> f : futuros) {
                consolidado = ResultadoAuditoria.combinar(consolidado, f.get());
            }

            return consolidado;
        }
    }
}

