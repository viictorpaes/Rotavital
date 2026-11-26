package com.rotavital.observabilidade;

import java.util.concurrent.TimeUnit;

import com.rotavital.dominio.CategoriaDescarte;
import com.rotavital.dominio.TipoComponente;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

/**
 * Métricas de negócio do Rota Vital (Micrometer), expostas em /actuator/prometheus.
 *
 * Usam o registro global do Micrometer: o Spring Boot liga o registro do Prometheus a ele
 * (management.metrics.use-global-registry, ligado por padrão). Nos testes unitários, sem Spring,
 * o registro global fica vazio e as chamadas não fazem nada, por isso os serviços não precisam
 * receber um MeterRegistry no construtor.
 *
 * Nomes no Prometheus:
 *  - rotavital_bolsas_cadastradas_total{componente}       (counter)
 *  - rotavital_bolsas_descartadas_total{categoria}        (counter)
 *  - rotavital_lotes_sugeridos_total{componente,atendimento} (counter)
 *  - rotavital_rotas_calculo_seconds_*{resultado}         (timer)
 */
public final class MetricasDeNegocio
{
    private MetricasDeNegocio()
    {
    }

    public static void bolsaCadastrada(TipoComponente componente)
    {
        Counter.builder("rotavital.bolsas.cadastradas")
                .description("Bolsas de hemocomponente cadastradas no estoque")
                .tag("componente", nome(componente))
                .register(Metrics.globalRegistry)
                .increment();
    }

    public static void bolsaDescartada(CategoriaDescarte categoria)
    {
        Counter.builder("rotavital.bolsas.descartadas")
                .description("Bolsas descartadas, por motivo do descarte")
                .tag("categoria", nome(categoria))
                .register(Metrics.globalRegistry)
                .increment();
    }

    /** atendimento = "completo" quando o lote tem todas as bolsas pedidas, "parcial" quando faltam. */
    public static void loteSugerido(TipoComponente componente, int pedidas, int encontradas)
    {
        Counter.builder("rotavital.lotes.sugeridos")
                .description("Lotes de bolsas sugeridos para atender uma requisição")
                .tag("componente", nome(componente))
                .tag("atendimento", encontradas >= pedidas ? "completo" : "parcial")
                .register(Metrics.globalRegistry)
                .increment();
    }

    /** resultado = "ok", "fora_da_janela", "nao_encontrado" ou "sem_rota". */
    public static void rotaCalculada(long inicioNanos, String resultado)
    {
        Timer.builder("rotavital.rotas.calculo")
                .description("Tempo para carregar a rede e calcular a rota mínima (Dijkstra)")
                .tag("resultado", resultado)
                .publishPercentileHistogram()
                .register(Metrics.globalRegistry)
                .record(System.nanoTime() - inicioNanos, TimeUnit.NANOSECONDS);
    }

    private static String nome(Enum<?> valor)
    {
        return valor == null ? "desconhecido" : valor.name();
    }
}
