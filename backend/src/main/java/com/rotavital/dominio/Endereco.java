package com.rotavital.dominio;

/**
 * Value object que representa um endereço/localização física.
 * Usado por composição dentro de Hospital e BancoDeSangue.
 */
public class Endereco {

    private String logradouro;
    private double latitude;
    private double longitude;

    public Endereco(String logradouro, double latitude, double longitude) {
        this.logradouro = logradouro;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLogradouro() { return logradouro; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return logradouro;
    }
}
