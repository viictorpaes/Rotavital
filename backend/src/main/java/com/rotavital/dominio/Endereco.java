package com.rotavital.dominio;

public class Endereco
{
    private final String logradouro;
    private final double latitude;
    private final double longitude;

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