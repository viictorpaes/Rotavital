package com.rotavital.dominio;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa o pedido de hemocomponentes feito por um Hospital
 * a um banco de sangue.
 */
public class RequisicaoHospitalar {

    private String id;
    private Hospital hospital;
    private TipoComponente tipoComponente;
    private TipoSanguineo tipoSanguineo;
    private int quantidade;
    private LocalDateTime dataSolicitacao;
    private StatusRequisicao status;

    public RequisicaoHospitalar(Hospital hospital, TipoComponente tipoComponente,
                                 TipoSanguineo tipoSanguineo, int quantidade) {
        this.id = UUID.randomUUID().toString();
        this.hospital = hospital;
        this.tipoComponente = tipoComponente;
        this.tipoSanguineo = tipoSanguineo;
        this.quantidade = quantidade;
        this.dataSolicitacao = LocalDateTime.now();
        this.status = StatusRequisicao.PENDENTE;
    }

    public void marcarComoAlocada() {
        this.status = StatusRequisicao.ALOCADA;
    }

    public void cancelar() {
        this.status = StatusRequisicao.CANCELADA;
    }

    // Getters
    public String getId() { return id; }
    public Hospital getHospital() { return hospital; }
    public TipoComponente getTipoComponente() { return tipoComponente; }
    public TipoSanguineo getTipoSanguineo() { return tipoSanguineo; }
    public int getQuantidade() { return quantidade; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public StatusRequisicao getStatus() { return status; }

    @Override
    public String toString() {
        return "Requisicao[" + id + "] " + hospital.getNome() + " pede " + quantidade
                + "x " + tipoComponente + " " + tipoSanguineo + " - " + status;
    }
}
