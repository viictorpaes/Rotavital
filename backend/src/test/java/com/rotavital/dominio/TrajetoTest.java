package com.rotavital.dominio;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Estimativa da rota quando o Google não responde (PI3-262): linha reta × 1,3 a 25 km/h.
public class TrajetoTest
{
    private static final Coordenada HEMOPE = new Coordenada(-8.0578, -34.8829);
    private static final Coordenada HOSPITAL_DAS_CLINICAS = new Coordenada(-8.0512, -34.9478);

    @Test
    public void estimativaCorrigeALinhaRetaPeloDesvioDasRuas()
    {
        Trajeto trajeto = Trajeto.estimadoEntre(HEMOPE, HOSPITAL_DAS_CLINICAS);

        // 7,18 km em linha reta × 1,3 = 9,34 km pelas ruas.
        Assertions.assertEquals(9.34, trajeto.distanciaKm(), 0.01);
        Assertions.assertEquals(FonteDoTrajeto.ESTIMATIVA, trajeto.fonte());
    }

    @Test
    public void tempoDaEstimativaArredondaParaCima()
    {
        // 9,34 km a 25 km/h = 22,4 min: o doador vê 23, para não chegar atrasado.
        Assertions.assertEquals(23, Trajeto.estimadoEntre(HEMOPE, HOSPITAL_DAS_CLINICAS).duracaoMinutos());
    }

    @Test
    public void quemJaEstaNoHospitalNaoTemTrajeto()
    {
        Trajeto trajeto = Trajeto.estimadoEntre(HOSPITAL_DAS_CLINICAS, HOSPITAL_DAS_CLINICAS);

        Assertions.assertEquals(0.0, trajeto.distanciaKm(), 1e-9);
        Assertions.assertEquals(0, trajeto.duracaoMinutos());
    }

    @Test
    public void distanciaNegativaERecusada()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Trajeto(-1, 5, FonteDoTrajeto.GOOGLE_ROUTES));
    }

    @Test
    public void trajetoSemFonteERecusado()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Trajeto(3, 5, null));
    }
}
