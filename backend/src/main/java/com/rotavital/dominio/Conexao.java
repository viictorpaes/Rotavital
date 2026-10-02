package com.rotavital.dominio;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Aresta dirigida do grafo: ida e volta são duas linhas na tabela.
@Entity
@Table(name = "conexao")
public class Conexao
{
    // Gerado pelo banco (identity).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // O campo continua do tipo da interface; targetEntity diz ao JPA qual entidade carregar.
    @ManyToOne(targetEntity = PontoDeRedeBase.class, optional = false)
    @JoinColumn(name = "origem_id", nullable = false)
    private PontoDeRede origem;

    @ManyToOne(targetEntity = PontoDeRedeBase.class, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private PontoDeRede destino;

    // No banco, as colunas abaixo são numeric; no Java continuam double.
    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "distancia_km", nullable = false)
    private double distanciaKm;

    @JdbcTypeCode(SqlTypes.NUMERIC)
    @Column(name = "tempo_estimado_min", nullable = false)
    private double tempoEstimadoMin;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected Conexao()
    {
    }

    public Conexao(PontoDeRede origem, PontoDeRede destino, 
    double distanciaKm, double tempoEstimadoMin)
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