package com.rotavital.dominio;

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

    @Override
    public String toString()
    {
        return logradouro;
    }
}
