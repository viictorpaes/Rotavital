package com.rotavital.dominio;
import java.time.LocalDate;

public class BolsaHemocomponente
{
    private final String id;
    private final TipoComponente tipoComponente;
    private final TipoSanguineo tipoSanguineo;
    private final LocalDate dataColeta;
    private final LocalDate dataValidade;
    private final double volumeMl;
    private StatusBolsa status;
    private final BancoDeSangue bancoOrigem;

    public BolsaHemocomponente(String id, TipoComponente tipoComponente, TipoSanguineo tipoSanguineo,
                                LocalDate dataColeta, LocalDate dataValidade, double volumeMl,
                                BancoDeSangue bancoOrigem)
    {
        this.id = id;
        this.tipoComponente = tipoComponente;
        this.tipoSanguineo = tipoSanguineo;
        this.dataColeta = dataColeta;
        this.dataValidade = dataValidade;
        this.volumeMl = volumeMl;
        this.bancoOrigem = bancoOrigem;
        this.status = StatusBolsa.DISPONIVEL;
    }

    public boolean estaVencida(LocalDate dataReferencia)
    {
        return dataReferencia.isAfter(dataValidade);
    }

    public boolean estaDisponivel()
    {
        return status == StatusBolsa.DISPONIVEL;
    }

    public void reservar()
    {
        this.status = StatusBolsa.RESERVADA;
    }

    public void descartar()
    {
        this.status = StatusBolsa.DESCARTADA;
    }

    public String getId()
    {
        return id;
    }

    public TipoComponente getTipoComponente()
    {
        return tipoComponente;
    }

    public TipoSanguineo getTipoSanguineo()
    {
        return tipoSanguineo;
    }

    public LocalDate getDataColeta()
    {
        return dataColeta;
    }

    public LocalDate getDataValidade()
    {
        return dataValidade;
    }

    public double getVolumeMl()
    {
        return volumeMl;
    }

    public StatusBolsa getStatus()
    {
        return status;
    }

    public BancoDeSangue getBancoOrigem()
    {
        return bancoOrigem;
    }

    @Override
    public String toString()
    {
        return "Bolsa[" + id + "] " + tipoComponente + " " + tipoSanguineo
                + " válida até " + dataValidade + " - " + status;
    }
}