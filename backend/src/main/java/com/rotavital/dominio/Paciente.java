package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
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
 * Pessoa que precisa de hemocomponente (tabela paciente). Na campanha de doação (HU-06),
 * é quem aparece para os doadores na tela de Doações e no portal.
 * hospital_tipo e atualizado_em ficam com o default e o gatilho do banco.
 */
@Entity
@Table(name = "paciente")
public class Paciente
{
    // Gerado pelo Hibernate ao gravar; assim o save() do Spring Data insere direto, sem consultar antes.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false)
    private TipoComponente tipoComponente;

    @Column(name = "unidades_necessarias", nullable = false)
    private int unidadesNecessarias;

    @Column(name = "causa", nullable = false)
    private String causa;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade", nullable = false)
    private Gravidade gravidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusPaciente status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected Paciente()
    {
    }

    public Paciente(Hospital hospital, String nome, Sexo sexo, LocalDate dataNascimento, TipoSanguineo tipoSanguineo,
            TipoComponente tipoComponente, int unidadesNecessarias, String causa, Gravidade gravidade,
            OffsetDateTime criadoEm)
    {
        this.status = StatusPaciente.AGUARDANDO;
        this.criadoEm = criadoEm;
        atualizar(hospital, nome, sexo, dataNascimento, tipoSanguineo, tipoComponente, unidadesNecessarias, causa,
                gravidade);
    }

    // Edição da campanha: troca os dados clínicos divulgados, mantendo id, status e data de criação.
    public void atualizar(Hospital hospital, String nome, Sexo sexo, LocalDate dataNascimento,
            TipoSanguineo tipoSanguineo, TipoComponente tipoComponente, int unidadesNecessarias, String causa,
            Gravidade gravidade)
    {
        this.hospital = hospital;
        this.nome = nome.trim();
        this.sexo = sexo;
        this.dataNascimento = dataNascimento;
        this.tipoSanguineo = tipoSanguineo;
        this.tipoComponente = tipoComponente;
        this.unidadesNecessarias = unidadesNecessarias;
        this.causa = causa.trim();
        this.gravidade = gravidade;
    }

    public int idadeEm(LocalDate data)
    {
        return Period.between(dataNascimento, data).getYears();
    }

    public UUID getId()
    {
        return id;
    }

    public Hospital getHospital()
    {
        return hospital;
    }

    public String getNome()
    {
        return nome;
    }

    public Sexo getSexo()
    {
        return sexo;
    }

    public LocalDate getDataNascimento()
    {
        return dataNascimento;
    }

    public TipoSanguineo getTipoSanguineo()
    {
        return tipoSanguineo;
    }

    public TipoComponente getTipoComponente()
    {
        return tipoComponente;
    }

    public int getUnidadesNecessarias()
    {
        return unidadesNecessarias;
    }

    public String getCausa()
    {
        return causa;
    }

    public Gravidade getGravidade()
    {
        return gravidade;
    }

    public StatusPaciente getStatus()
    {
        return status;
    }

    public OffsetDateTime getCriadoEm()
    {
        return criadoEm;
    }
}
