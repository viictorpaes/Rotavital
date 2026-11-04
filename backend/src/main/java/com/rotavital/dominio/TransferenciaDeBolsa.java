package com.rotavital.dominio;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * PI3-236 — Uma bolsa saindo do hemocentro para um hospital, pela tela de Rede: registra de onde
 * ela veio, para onde foi, quem despachou, quem recebeu e quando. As regras de quais bolsas podem
 * sair e chegar ficam em BolsaHemocomponente.
 */
@Entity
@Table(name = "transferencia_bolsa")
public class TransferenciaDeBolsa
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bolsa_id", nullable = false)
    private BolsaHemocomponente bolsa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origem_id", nullable = false)
    private BancoDeSangue origem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Hospital destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusTransferencia status;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "distancia_km", nullable = false)
    private double distanciaKm;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "fonte_trajeto", nullable = false)
    private FonteDoTrajeto fonteDoTrajeto;

    @Column(name = "despachada_em", nullable = false, updatable = false)
    private OffsetDateTime despachadaEm;

    @Column(name = "chegada_prevista_em", nullable = false, updatable = false)
    private OffsetDateTime chegadaPrevistaEm;

    @Column(name = "recebida_em")
    private OffsetDateTime recebidaEm;

    @ManyToOne
    @JoinColumn(name = "despachada_por")
    private Usuario despachadaPor;

    @ManyToOne
    @JoinColumn(name = "recebida_por")
    private Usuario recebidaPor;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use despachar.
    protected TransferenciaDeBolsa()
    {
    }

    private TransferenciaDeBolsa(BolsaHemocomponente bolsa, Hospital destino, Trajeto trajeto, Usuario responsavel,
            OffsetDateTime agora)
    {
        this.bolsa = bolsa;
        this.origem = bolsa.getBancoOrigem();
        this.destino = destino;
        this.status = StatusTransferencia.EM_TRANSITO;
        this.distanciaKm = trajeto.distanciaKm();
        this.duracaoMinutos = trajeto.duracaoMinutos();
        this.fonteDoTrajeto = trajeto.fonte();
        this.despachadaEm = agora;
        this.chegadaPrevistaEm = agora.plusMinutes(trajeto.duracaoMinutos());
        this.despachadaPor = responsavel;
    }

    /** Tira a bolsa do estoque e registra a saída; bolsa que não pode sair lança MovimentacaoInvalidaException. */
    public static TransferenciaDeBolsa despachar(BolsaHemocomponente bolsa, Hospital destino, Trajeto trajeto,
            Usuario responsavel, OffsetDateTime agora)
    {
        bolsa.despacharPara(destino, agora.toLocalDate());
        return new TransferenciaDeBolsa(bolsa, destino, trajeto, responsavel, agora);
    }

    /** Confirma a chegada: a partir daqui, a bolsa está no hospital de destino. */
    public void receber(Usuario responsavel, OffsetDateTime agora)
    {
        if (status != StatusTransferencia.EM_TRANSITO)
        {
            throw new MovimentacaoInvalidaException("A transferência da bolsa " + bolsa.getId() + " já foi encerrada ("
                    + status + ").");
        }

        bolsa.receberEm(destino, agora.toLocalDate());
        this.status = StatusTransferencia.RECEBIDA;
        this.recebidaEm = agora;
        this.recebidaPor = responsavel;
    }

    // A bolsa foi descartada no caminho: não há mais o que receber.
    public void cancelar()
    {
        this.status = StatusTransferencia.CANCELADA;
    }

    public UUID getId()
    {
        return id;
    }

    public BolsaHemocomponente getBolsa()
    {
        return bolsa;
    }

    public BancoDeSangue getOrigem()
    {
        return origem;
    }

    public Hospital getDestino()
    {
        return destino;
    }

    public StatusTransferencia getStatus()
    {
        return status;
    }

    public double getDistanciaKm()
    {
        return distanciaKm;
    }

    public int getDuracaoMinutos()
    {
        return duracaoMinutos;
    }

    public FonteDoTrajeto getFonteDoTrajeto()
    {
        return fonteDoTrajeto;
    }

    public OffsetDateTime getDespachadaEm()
    {
        return despachadaEm;
    }

    public OffsetDateTime getChegadaPrevistaEm()
    {
        return chegadaPrevistaEm;
    }

    public OffsetDateTime getRecebidaEm()
    {
        return recebidaEm;
    }

    public Usuario getDespachadaPor()
    {
        return despachadaPor;
    }

    public Usuario getRecebidaPor()
    {
        return recebidaPor;
    }
}
