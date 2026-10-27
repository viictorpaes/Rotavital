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
 * Paciente divulgado para doação (HU-06, tabela campanha_doacao). Só um médico publica
 * (publicada_por_papel fica com o default 'MEDICO' do banco) e o paciente tem no máximo
 * uma campanha ativa.
 */
@Entity
@Table(name = "campanha_doacao")
public class CampanhaDoacao
{
    // Gerado pelo Hibernate ao gravar; assim o save() do Spring Data insere direto, sem consultar antes.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "publicada_por")
    private Usuario publicadaPor;

    @Column(name = "publicada_em", nullable = false, updatable = false)
    private OffsetDateTime publicadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusCampanha status;

    @Column(name = "encerrada_em")
    private OffsetDateTime encerradaEm;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected CampanhaDoacao()
    {
    }

    public CampanhaDoacao(Paciente paciente, Usuario publicadaPor, OffsetDateTime publicadaEm)
    {
        this.paciente = paciente;
        this.publicadaPor = publicadaPor;
        this.publicadaEm = publicadaEm;
        this.status = StatusCampanha.ATIVA;
    }

    public boolean estaAtiva()
    {
        return status == StatusCampanha.ATIVA;
    }

    public void encerrar(OffsetDateTime quando)
    {
        this.status = StatusCampanha.ENCERRADA;
        this.encerradaEm = quando;
    }

    public UUID getId()
    {
        return id;
    }

    public Paciente getPaciente()
    {
        return paciente;
    }

    public Usuario getPublicadaPor()
    {
        return publicadaPor;
    }

    public OffsetDateTime getPublicadaEm()
    {
        return publicadaEm;
    }

    public StatusCampanha getStatus()
    {
        return status;
    }

    public OffsetDateTime getEncerradaEm()
    {
        return encerradaEm;
    }
}
