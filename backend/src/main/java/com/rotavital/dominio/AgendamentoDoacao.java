package com.rotavital.dominio;

import java.time.OffsetDateTime;
import java.util.UUID;

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
 * Doação marcada por um doador em um banco de sangue (HU-09, HU-10, tabela agendamento_doacao).
 * doador_papel e banco_tipo ficam com o default do banco ('DOADOR' e 'BANCO_DE_SANGUE').
 */
@Entity
@Table(name = "agendamento_doacao")
public class AgendamentoDoacao
{
    // Gerado pelo Hibernate ao gravar; assim o save() do Spring Data insere direto, sem consultar antes.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doador_id", nullable = false)
    private Usuario doador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private BancoDeSangue banco;

    @ManyToOne
    @JoinColumn(name = "campanha_id")
    private CampanhaDoacao campanha;

    @Column(name = "agendado_para", nullable = false)
    private OffsetDateTime agendadoPara;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAgendamento status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected AgendamentoDoacao()
    {
    }

    public AgendamentoDoacao(Usuario doador, BancoDeSangue banco, CampanhaDoacao campanha,
            OffsetDateTime agendadoPara, OffsetDateTime criadoEm)
    {
        this.doador = doador;
        this.banco = banco;
        this.campanha = campanha;
        this.agendadoPara = agendadoPara;
        this.status = StatusAgendamento.AGENDADO;
        this.criadoEm = criadoEm;
    }

    public boolean estaEmAberto()
    {
        return status == StatusAgendamento.AGENDADO;
    }

    public void remarcar(OffsetDateTime novoHorario)
    {
        this.agendadoPara = novoHorario;
    }

    public void mudarStatus(StatusAgendamento novo)
    {
        this.status = novo;
    }

    public UUID getId()
    {
        return id;
    }

    public Usuario getDoador()
    {
        return doador;
    }

    public BancoDeSangue getBanco()
    {
        return banco;
    }

    public CampanhaDoacao getCampanha()
    {
        return campanha;
    }

    public OffsetDateTime getAgendadoPara()
    {
        return agendadoPara;
    }

    public StatusAgendamento getStatus()
    {
        return status;
    }

    public OffsetDateTime getCriadoEm()
    {
        return criadoEm;
    }
}
