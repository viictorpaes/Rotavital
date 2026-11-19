package com.rotavital.dominio;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Value object: não tem tabela própria, vira colunas da tabela do ponto (ponto_rede).
@Embeddable
public class Endereco
{
    @Column(name = "logradouro", nullable = false)
    private String logradouro;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    // Opcionais no banco; o Portal do Doador mostra "logradouro — cidade/UF" (PI3-205).
    @Column(name = "cidade")
    private String cidade;

    // char(2) no banco: sem o tipo CHAR, o ddl-auto=validate esperaria varchar.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf", length = 2)
    private String uf;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected Endereco()
    {
    }

    public Endereco(String logradouro, double latitude, double longitude)
    {
        this.logradouro = logradouro;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLogradouro()
    {
        return logradouro;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public String getCidade()
    {
        return cidade;
    }

    public String getUf()
    {
        return uf;
    }

    @Override
    public String toString()
    {
        return logradouro;
    }
}
