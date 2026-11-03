package com.rotavital.dominio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Coordenada e distância em linha reta (haversine), base da estimativa da rota até o hospital (PI3-262).
public class CoordenadaTest
{
    private static final Coordenada HEMOPE = new Coordenada(-8.0578, -34.8829);
    private static final Coordenada HOSPITAL_DAS_CLINICAS = new Coordenada(-8.0512, -34.9478);

    @Test
    public void umGrauDeLatitudeTemCercaDe111Km()
    {
        double km = new Coordenada(0, 0).distanciaEmLinhaRetaKm(new Coordenada(1, 0));

        Assertions.assertEquals(111.195, km, 0.001);
    }

    @Test
    public void doHemopeAoHospitalDasClinicasSaoUns7KmEmLinhaReta()
    {
        Assertions.assertEquals(7.18, HEMOPE.distanciaEmLinhaRetaKm(HOSPITAL_DAS_CLINICAS), 0.01);
    }

    @Test
    public void distanciaNaoDependeDoSentido()
    {
        Assertions.assertEquals(HEMOPE.distanciaEmLinhaRetaKm(HOSPITAL_DAS_CLINICAS),
                HOSPITAL_DAS_CLINICAS.distanciaEmLinhaRetaKm(HEMOPE), 1e-9);
    }

    @Test
    public void distanciaAteOMesmoPontoEZero()
    {
        Assertions.assertEquals(0.0, HEMOPE.distanciaEmLinhaRetaKm(new Coordenada(-8.0578, -34.8829)), 1e-9);
    }

    @Test
    public void latitudeForaDoIntervaloERecusada()
    {
        IllegalArgumentException erro = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Coordenada(-91, -34.9));

        Assertions.assertTrue(erro.getMessage().contains("Latitude"), erro.getMessage());
    }

    @Test
    public void longitudeForaDoIntervaloERecusada()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Coordenada(-8.05, 181));
    }

    @Test
    public void valorQueNaoEhNumeroERecusado()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Coordenada(Double.NaN, -34.9));
    }
}
