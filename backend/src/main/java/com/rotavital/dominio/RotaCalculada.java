package com.rotavital.dominio;
import java.util.List;

public class RotaCalculada
{
    private final PontoDeRede origem;
    private final PontoDeRede destino;
    private final List<PontoDeRede> nos;
    private final double distanciaTotalKm;
    private final double tempoEstimadoMin;
    private final boolean dentroDaJanela;

    public RotaCalculada(PontoDeRede origem, PontoDeRede destino, List<PontoDeRede> nos,
    double distanciaTotalKm, double tempoEstimadoMin, boolean dentroDaJanela)

    {
        this.origem = origem;
        this.destino = destino;
        this.nos = nos;
        this.distanciaTotalKm = distanciaTotalKm;
        this.tempoEstimadoMin = tempoEstimadoMin;
        this.dentroDaJanela = dentroDaJanela;
    }

    public PontoDeRede getOrigem()
    {
        return origem;
    }

    public PontoDeRede getDestino()
    {
        return destino;
    }

    public List<PontoDeRede> getNos()
    {
        return nos;
    }

    public double getDistanciaTotalKm()
    {
        return distanciaTotalKm;
    }

    public double getTempoEstimadoMin()
    {
        return tempoEstimadoMin;
    }

    public boolean isDentroDaJanela()
    {
        return dentroDaJanela;
    }

    @Override
    public String toString()
    {
        return "Rota " + origem.getNome() + " -> " + destino.getNome()
                + " (" + distanciaTotalKm + "km, " + tempoEstimadoMin + "min) - "
                + (dentroDaJanela ? "dentro da janela" : "fora da janela");
    }
}