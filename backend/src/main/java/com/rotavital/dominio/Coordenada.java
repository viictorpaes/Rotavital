package com.rotavital.dominio;

/**
 * Ponto na superfície da Terra em graus decimais (WGS 84, o mesmo do GPS, do navegador e do
 * Google Maps). Usado na rota do doador até o hospital da campanha (PI3-262).
 */
public record Coordenada(double latitude, double longitude)
{
    // Raio médio da Terra (IUGG), o valor usual na fórmula de haversine.
    private static final double RAIO_DA_TERRA_KM = 6371.0088;

    public Coordenada
    {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90)
        {
            throw new IllegalArgumentException("Latitude fora do intervalo de -90 a 90: " + latitude);
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180)
        {
            throw new IllegalArgumentException("Longitude fora do intervalo de -180 a 180: " + longitude);
        }
    }

    /** Distância em linha reta pela superfície da Terra (fórmula de haversine), em km. */
    public double distanciaEmLinhaRetaKm(Coordenada outra)
    {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(outra.latitude);
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(outra.longitude - longitude);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * RAIO_DA_TERRA_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    @Override
    public String toString()
    {
        return latitude + "," + longitude;
    }
}
