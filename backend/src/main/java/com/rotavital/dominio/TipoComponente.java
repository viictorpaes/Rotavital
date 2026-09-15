package com.rotavital.dominio;

public enum TipoComponente
{
    HEMACIAS(2.0, 6.0),
    PLASMA(-30.0, -18.0),
    PLAQUETAS(20.0, 24.0),
    CRIOPRECIPITADO(-30.0, -18.0);

    private final double temperaturaMinima;
    private final double temperaturaMaxima;

    TipoComponente(double temperaturaMinima, double temperaturaMaxima)
    {
        this.temperaturaMinima = temperaturaMinima;
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public double getTemperaturaMinima()
    {
        return temperaturaMinima;
    }

    public double getTemperaturaMaxima()
    {
        return temperaturaMaxima;
    }
}
