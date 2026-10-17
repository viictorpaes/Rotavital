package com.rotavital.dominio;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "bolsa_hemocomponente")
public class BolsaHemocomponente implements Persistable<String>
{
    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false)
    private TipoComponente tipoComponente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo", nullable = false)
    private TipoSanguineo tipoSanguineo;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Column(name = "data_validade", nullable = false)
    private LocalDate dataValidade;

    @Column(name = "lote_sintetico", nullable = false)
    private String loteSintetico;

    // No banco, as colunas abaixo são numeric; no Java continuam double.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "volume_ml", nullable = false)
    private double volumeMl;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "temperatura_celsius")
    private double temperaturaCelsius;

    @Column(name = "localizacao")
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusBolsa status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banco_origem_id", nullable = false)
    private BancoDeSangue bancoOrigem;

    // O id é preenchido pelo sistema, então o save() não sabe sozinho se a bolsa é nova e faria um merge,
    // que sobrescreve a bolsa que já tiver esse id. Com isto, bolsa criada com new vira sempre INSERT
    // (id repetido dá erro), e bolsa lida do banco deixa de ser nova.
    @Transient
    private boolean nova = true;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected BolsaHemocomponente()
    {
    }

    public BolsaHemocomponente(String id, TipoComponente tipoComponente, 
    TipoSanguineo tipoSanguineo, LocalDate dataColeta, LocalDate dataValidade, 
    String loteSintetico, double volumeMl, double temperaturaCelsius, 
    String localizacao, BancoDeSangue bancoOrigem)
    {
        this.id = id;
        this.tipoComponente = tipoComponente;
        this.tipoSanguineo = tipoSanguineo;
        this.dataColeta = dataColeta;
        this.dataValidade = dataValidade;
        this.loteSintetico = loteSintetico;
        this.volumeMl = volumeMl;
        this.temperaturaCelsius = temperaturaCelsius;
        this.localizacao = localizacao;
        this.bancoOrigem = bancoOrigem;
        this.status = StatusBolsa.DISPONIVEL;
    }

    public boolean estaVencida(LocalDate dataReferencia)
    {
        return dataReferencia.isAfter(dataValidade);
    }

    public boolean estaForaDaFaixa()
    {
        return temperaturaCelsius < tipoComponente.getTemperaturaMinima()
        || temperaturaCelsius > tipoComponente.getTemperaturaMaxima();
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

    @PostLoad
    @PostPersist
    void marcarComoGravada()
    {
        this.nova = false;
    }

    @Override
    public boolean isNew()
    {
        return nova;
    }

    @Override
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

    public String getLoteSintetico()
    {
        return loteSintetico;
    }

    public double getVolumeMl()
    {
        return volumeMl;
    }

    public double getTemperaturaCelsius()
    {
        return temperaturaCelsius;
    }

    public String getLocalizacao()
    {
        return localizacao;
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