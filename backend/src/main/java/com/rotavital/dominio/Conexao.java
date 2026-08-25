package com.rotavital.dominio;

public class Conexao
{
    private final PontoDeRede origem;
    private final PontoDeRede destino;
    private final double distanciaKm;
    private final double tempoEstimadoMin;

    public Conexao(PontoDeRede origem, PontoDeRede destino, double distanciaKm, double tempoEstimadoMin)
    {
        this.origem = origem;
        this.destino = destino;
        this.distanciaKm = distanciaKm;
        this.tempoEstimadoMin = tempoEstimadoMin;
    }

    public PontoDeRede getOrigem()
    {
        return origem;
    }

    public PontoDeRede getDestino()
    {
        return destino;
    }

    public double getDistanciaKm()
    {
        return distanciaKm;
    }

    public double getTempoEstimadoMin()
    {
        return tempoEstimadoMin;
    }

    @Override
    public String toString()
    {
        return origem.getNome() + " -> " + destino.getNome()
                + " (" + distanciaKm + "km, " + tempoEstimadoMin + "min)";
    }
}