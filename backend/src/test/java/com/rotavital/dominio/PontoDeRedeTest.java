package com.rotavital.dominio;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Herança dos pontos de rede (PontoDeRedeBase) e igualdade por id, usada pelo JPA e pelo Dijkstra.
public class PontoDeRedeTest
{
    private final Endereco endereco = new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829);

    @Test
    public void hospitalEBancoDeSangueSaoPontosDeRede()
    {
        PontoDeRede hospital = new Hospital("HOSP-01", "Hospital das Clínicas", endereco);
        PontoDeRede banco = new BancoDeSangue("BS-01", "Hemope Central", endereco);

        Assertions.assertInstanceOf(PontoDeRedeBase.class, hospital);
        Assertions.assertInstanceOf(PontoDeRedeBase.class, banco);
        Assertions.assertEquals(-8.0578, hospital.getLatitude());
        Assertions.assertEquals(-34.8829, banco.getLongitude());
    }

    @Test
    public void pontosComOMesmoIdSaoIguais()
    {
        // Com JPA, a mesma linha do banco pode chegar em dois objetos diferentes.
        Hospital carregadoUmaVez = new Hospital("HOSP-01", "Hospital das Clínicas", endereco);
        Hospital carregadoDeNovo = new Hospital("HOSP-01", "Hospital das Clínicas", endereco);

        Assertions.assertEquals(carregadoUmaVez, carregadoDeNovo);
        Assertions.assertEquals(carregadoUmaVez.hashCode(), carregadoDeNovo.hashCode());

        // O Dijkstra guarda as distâncias em um HashMap com o ponto como chave.
        Map<PontoDeRede, Double> distancias = new HashMap<>();
        distancias.put(carregadoUmaVez, 7.8);
        Assertions.assertEquals(7.8, distancias.get(carregadoDeNovo));
    }

    @Test
    public void pontosComIdsDiferentesSaoDiferentes()
    {
        Hospital hospital = new Hospital("HOSP-01", "Hospital das Clínicas", endereco);
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", endereco);

        Assertions.assertNotEquals(hospital, banco);
        Assertions.assertNotEquals(hospital, new Hospital("HOSP-02", "Hospital das Clínicas", endereco));
    }
}
