package com.rotavital.dominio;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Regras da bolsa que precisam continuar valendo depois do mapeamento JPA.
public class BolsaHemocomponenteTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829));

    private BolsaHemocomponente novaBolsa(double temperaturaCelsius)
    {
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, temperaturaCelsius,
                "R1 · P1 · N1", banco);
    }

    @Test
    public void bolsaNasceDisponivel()
    {
        BolsaHemocomponente bolsa = novaBolsa(4.0);

        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
        Assertions.assertTrue(bolsa.estaDisponivel());
        Assertions.assertSame(banco, bolsa.getBancoOrigem());
    }

    @Test
    public void reservarEDescartarMudamOStatus()
    {
        BolsaHemocomponente reservada = novaBolsa(4.0);
        reservada.reservar();
        Assertions.assertEquals(StatusBolsa.RESERVADA, reservada.getStatus());
        Assertions.assertFalse(reservada.estaDisponivel());

        BolsaHemocomponente descartada = novaBolsa(4.0);
        descartada.descartar();
        Assertions.assertEquals(StatusBolsa.DESCARTADA, descartada.getStatus());
        Assertions.assertFalse(descartada.estaDisponivel());
    }

    @Test
    public void bolsaSoVenceDepoisDaDataDeValidade()
    {
        BolsaHemocomponente bolsa = novaBolsa(4.0);

        Assertions.assertFalse(bolsa.estaVencida(hoje.plusDays(30)));
        Assertions.assertTrue(bolsa.estaVencida(hoje.plusDays(31)));
    }

    @Test
    public void temperaturaForaDaFaixaDoComponente()
    {
        // Hemácias: faixa de 2 °C a 6 °C.
        Assertions.assertFalse(novaBolsa(4.0).estaForaDaFaixa());
        Assertions.assertTrue(novaBolsa(8.0).estaForaDaFaixa());
    }
}
