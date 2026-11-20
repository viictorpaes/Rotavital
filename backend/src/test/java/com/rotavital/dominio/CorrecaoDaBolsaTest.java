package com.rotavital.dominio;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// HU-03 — só bolsa ainda no estoque (DISPONIVEL) aceita correção dos dados do cadastro.
public class CorrecaoDaBolsaTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private BolsaHemocomponente novaBolsa()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-0001", 450.0, 4.0, "R1 · P3", banco);
    }

    @Test
    public void bolsaDisponivelTemOsDadosCorrigidos()
    {
        BolsaHemocomponente bolsa = novaBolsa();

        bolsa.corrigirCadastro(TipoSanguineo.A_POSITIVO, HOJE.minusDays(4), HOJE.plusDays(31), "LOTE-0002",
                430.0, 3.5, "R2 · P1");

        Assertions.assertEquals(TipoSanguineo.A_POSITIVO, bolsa.getTipoSanguineo());
        Assertions.assertEquals(HOJE.minusDays(4), bolsa.getDataColeta());
        Assertions.assertEquals(HOJE.plusDays(31), bolsa.getDataValidade());
        Assertions.assertEquals("LOTE-0002", bolsa.getLoteSintetico());
        Assertions.assertEquals(430.0, bolsa.getVolumeMl());
        Assertions.assertEquals(3.5, bolsa.getTemperaturaCelsius());
        Assertions.assertEquals("R2 · P1", bolsa.getLocalizacao());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
        Assertions.assertEquals(TipoComponente.HEMACIAS, bolsa.getTipoComponente());
        Assertions.assertEquals("CH-1042", bolsa.getId());
    }

    @Test
    public void bolsaReservadaNaoPodeSerCorrigidaENadaMuda()
    {
        BolsaHemocomponente bolsa = novaBolsa();
        bolsa.alterarStatus(StatusBolsa.RESERVADA, HOJE);

        EdicaoNaoPermitidaException erro = Assertions.assertThrows(EdicaoNaoPermitidaException.class,
                () -> bolsa.corrigirCadastro(TipoSanguineo.A_POSITIVO, HOJE.minusDays(5), HOJE.plusDays(30),
                        "LOTE-0001", 300.0, 4.0, "R1 · P3"));

        Assertions.assertEquals("A bolsa CH-1042 está RESERVADA e não pode mais ter os dados corrigidos: "
                + "só bolsas disponíveis podem ser editadas.", erro.getMessage());
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, bolsa.getTipoSanguineo());
        Assertions.assertEquals(450.0, bolsa.getVolumeMl());
    }

    @Test
    public void bolsaDescartadaTambemNaoPodeSerCorrigida()
    {
        BolsaHemocomponente bolsa = novaBolsa();
        bolsa.alterarStatus(StatusBolsa.DESCARTADA, HOJE);

        Assertions.assertThrows(EdicaoNaoPermitidaException.class, bolsa::conferirSePodeSerCorrigida);
    }
}
