package com.rotavital.benchmark.model;

import java.util.Objects;

/**
 * Agregador estatístico da auditoria de telemetria.
 * Suporta redução/merge seguro entre threads parciais.
 */
public class ResultadoAuditoria {
    private long totalProcessado;
    private long totalAnomalias;
    private long totalDescartesCriticos;
    private double somaTemperaturas;
    private double somaQuadradosTemperaturas;
    private double somaIndiceDegradacao;

    public ResultadoAuditoria() {
        this(0, 0, 0, 0.0, 0.0, 0.0);
    }

    public ResultadoAuditoria(long totalProcessado, long totalAnomalias, long totalDescartesCriticos,
                              double somaTemperaturas, double somaQuadradosTemperaturas, double somaIndiceDegradacao) {
        this.totalProcessado = totalProcessado;
        this.totalAnomalias = totalAnomalias;
        this.totalDescartesCriticos = totalDescartesCriticos;
        this.somaTemperaturas = somaTemperaturas;
        this.somaQuadradosTemperaturas = somaQuadradosTemperaturas;
        this.somaIndiceDegradacao = somaIndiceDegradacao;
    }

    public void acumular(boolean anomalia, boolean descarteCritico, double temp, double indiceDegradacao) {
        this.totalProcessado++;
        if (anomalia) this.totalAnomalias++;
        if (descarteCritico) this.totalDescartesCriticos++;
        this.somaTemperaturas += temp;
        this.somaQuadradosTemperaturas += (temp * temp);
        this.somaIndiceDegradacao += indiceDegradacao;
    }

    public static ResultadoAuditoria combinar(ResultadoAuditoria a, ResultadoAuditoria b) {
        return new ResultadoAuditoria(
                a.totalProcessado + b.totalProcessado,
                a.totalAnomalias + b.totalAnomalias,
                a.totalDescartesCriticos + b.totalDescartesCriticos,
                a.somaTemperaturas + b.somaTemperaturas,
                a.somaQuadradosTemperaturas + b.somaQuadradosTemperaturas,
                a.somaIndiceDegradacao + b.somaIndiceDegradacao
        );
    }

    public long getTotalProcessado() {
        return totalProcessado;
    }

    public long getTotalAnomalias() {
        return totalAnomalias;
    }

    public long getTotalDescartesCriticos() {
        return totalDescartesCriticos;
    }

    public double getMediaTemperatura() {
        if (totalProcessado == 0) return 0.0;
        return Math.round((somaTemperaturas / totalProcessado) * 1000.0) / 1000.0;
    }

    public double getDesvioPadraoTemperatura() {
        if (totalProcessado == 0) return 0.0;
        double media = somaTemperaturas / totalProcessado;
        double variancia = (somaQuadradosTemperaturas / totalProcessado) - (media * media);
        return Math.round(Math.sqrt(Math.max(0, variancia)) * 1000.0) / 1000.0;
    }

    public double getIndiceDegradacaoMedio() {
        if (totalProcessado == 0) return 0.0;
        return Math.round((somaIndiceDegradacao / totalProcessado) * 1000.0) / 1000.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultadoAuditoria that = (ResultadoAuditoria) o;
        return totalProcessado == that.totalProcessado &&
               totalAnomalias == that.totalAnomalias &&
               totalDescartesCriticos == that.totalDescartesCriticos &&
               Math.abs(somaTemperaturas - that.somaTemperaturas) < 0.001 &&
               Math.abs(somaQuadradosTemperaturas - that.somaQuadradosTemperaturas) < 0.001;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalProcessado, totalAnomalias, totalDescartesCriticos);
    }
}

