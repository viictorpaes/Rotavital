package com.rotavital.dominio;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Distribuição de frequências: fi, fr e Fr, na ordem das categorias, com as zeradas incluídas.
public class TabelaDeFrequenciaTest
{
    private static final double TOLERANCIA = 1e-9;

    @Test
    public void calculaFrequenciasAbsolutaRelativaEAcumulada()
    {
        TabelaDeFrequencia tabela = new TabelaDeFrequencia(List.of("O-", "A+", "B+", "AB-"));
        for (String tipo : List.of("A+", "O-", "A+", "B+"))
        {
            tabela.contar(tipo);
        }

        List<TabelaDeFrequencia.Linha> linhas = tabela.linhas();

        Assertions.assertEquals(4, tabela.total());
        Assertions.assertEquals(List.of("O-", "A+", "B+", "AB-"), linhas.stream().map(TabelaDeFrequencia.Linha::categoria).toList());
        Assertions.assertEquals(List.of(1L, 2L, 1L, 0L), linhas.stream().map(TabelaDeFrequencia.Linha::frequenciaAbsoluta).toList());
        Assertions.assertEquals(50.0, linhas.get(1).frequenciaRelativa(), TOLERANCIA);
        Assertions.assertEquals(0.0, linhas.get(3).frequenciaRelativa(), TOLERANCIA);
        Assertions.assertEquals(75.0, linhas.get(1).frequenciaRelativaAcumulada(), TOLERANCIA);
        Assertions.assertEquals(100.0, linhas.get(3).frequenciaRelativaAcumulada(), TOLERANCIA);
    }

    @Test
    public void modaListaTodasAsCategoriasEmpatadas()
    {
        TabelaDeFrequencia tabela = new TabelaDeFrequencia(List.of("O-", "A+", "B+"));
        for (String tipo : List.of("A+", "O-", "A+", "O-", "B+"))
        {
            tabela.contar(tipo);
        }

        Assertions.assertEquals(List.of("O-", "A+"), tabela.moda());
    }

    @Test
    public void tabelaVaziaNaoTemModaNemDivideporZero()
    {
        TabelaDeFrequencia tabela = new TabelaDeFrequencia(List.of("O-", "A+"));

        Assertions.assertEquals(List.of(), tabela.moda());
        Assertions.assertEquals(0.0, tabela.linhas().get(0).frequenciaRelativa(), TOLERANCIA);
        Assertions.assertEquals(0.0, tabela.linhas().get(1).frequenciaRelativaAcumulada(), TOLERANCIA);
    }

    @Test
    public void categoriaForaDaTabelaERecusada()
    {
        TabelaDeFrequencia tabela = new TabelaDeFrequencia(List.of("O-"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> tabela.contar("Z+"));
    }
}
