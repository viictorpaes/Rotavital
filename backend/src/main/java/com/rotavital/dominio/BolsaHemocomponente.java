package com.rotavital.dominio;
import java.time.LocalDate;

/**
 * Representa uma unidade de hemocomponente (bolsa) armazenada
 * em um banco de sangue, com controle de tipo, validade e status.
 */
public class BolsaHemocomponente {

    private String id;
    private TipoComponente tipoComponente;
    private TipoSanguineo tipoSanguineo;
    private LocalDate dataColeta;
    private LocalDate dataValidade;
    private double volumeMl;
    private StatusBolsa status;
    private BancoDeSangue bancoOrigem;

    public BolsaHemocomponente(String id, TipoComponente tipoComponente, TipoSanguineo tipoSanguineo,
                                LocalDate dataColeta, LocalDate dataValidade, double volumeMl,
                                BancoDeSangue bancoOrigem) {
        this.id = id;
        this.tipoComponente = tipoComponente;
        this.tipoSanguineo = tipoSanguineo;
        this.dataColeta = dataColeta;
        this.dataValidade = dataValidade;
        this.volumeMl = volumeMl;
        this.bancoOrigem = bancoOrigem;
        this.status = StatusBolsa.DISPONIVEL;
    }

    public boolean estaVencida(LocalDate dataReferencia) {
        return dataReferencia.isAfter(dataValidade);
    }

    public boolean estaDisponivel() {
        return status == StatusBolsa.DISPONIVEL;
    }

    public void reservar() {
        this.status = StatusBolsa.RESERVADA;
    }

    public void descartar() {
        this.status = StatusBolsa.DESCARTADA;
    }

    // Getters
    public String getId() { return id; }
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public TipoSanguineo getTipoSanguineo() { return tipoSanguineo; }
    public LocalDate getDataColeta() { return dataColeta; }
    public LocalDate getDataValidade() { return dataValidade; }
    public double getVolumeMl() { return volumeMl; }
    public StatusBolsa getStatus() { return status; }
    public BancoDeSangue getBancoOrigem() { return bancoOrigem; }

    @Override
    public String toString() {
        return "Bolsa[" + id + "] " + tipoComponente + " " + tipoSanguineo
                + " válida até " + dataValidade + " - " + status;
    }
}
