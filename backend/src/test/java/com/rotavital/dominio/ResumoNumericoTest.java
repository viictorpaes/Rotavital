package com.rotavital.dominio;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Medidas de posição e dispersão, conferidas com QUARTIL.INC e DESVPAD.A de uma planilha.
public class ResumoNumericoTest
{
    private static final double TOLERANCIA = 1e-9;

    @Test
    public void calculaAsMedidasDeUmaAmostraConhecida()
    {
        // 2, 4, 4, 4, 5, 5, 7, 9: média 5; desvio amostral √(32/7).
        ResumoNumerico resumo = ResumoNumerico.de(List.of(9.0, 4.0, 2.0, 5.0, 4.0, 7.0, 4.0, 5.0));

        Assertions.assertEquals(8, resumo.n());
        Assertions.assertEquals(5.0, resumo.media(), TOLERANCIA);
        Assertions.assertEquals(4.5, resumo.mediana(), TOLERANCIA);
        Assertions.assertEquals(Math.sqrt(32.0 / 7), resumo.desvioPadrao(), TOLERANCIA);
        Assertions.assertEquals(Math.sqrt(32.0 / 7) / 5 * 100, resumo.coeficienteDeVariacao(), TOLERANCIA);
        Assertions.assertEquals(2.0, resumo.minimo(), TOLERANCIA);
        Assertions.assertEquals(9.0, resumo.maximo(), TOLERANCIA);
        Assertions.assertEquals(7.0, resumo.amplitude(), TOLERANCIA);
    }

    @Test
    public void quartisInterpolamComoOQuartilIncDaPlanilha()
    {
        // Posições (n − 1)·p = 1,75 e 5,25: Q1 = 4 + 0,75·(4 − 4) = 4; Q3 = 5 + 0,25·(7 − 5) = 5,5.
        ResumoNumerico resumo = ResumoNumerico.de(List.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0));

        Assertions.assertEquals(4.0, resumo.primeiroQuartil(), TOLERANCIA);
        Assertions.assertEquals(5.5, resumo.terceiroQuartil(), TOLERANCIA);
    }

    @Test
    public void medianaDeQuantidadeImparEOValorDoMeio()
    {
        ResumoNumerico resumo = ResumoNumerico.de(List.of(30.0, 1.0, 7.0));

        Assertions.assertEquals(7.0, resumo.mediana(), TOLERANCIA);
        Assertions.assertEquals(4.0, resumo.primeiroQuartil(), TOLERANCIA);
        Assertions.assertEquals(18.5, resumo.terceiroQuartil(), TOLERANCIA);
    }

    @Test
    public void umaUnicaObservacaoNaoTemDispersao()
    {
        ResumoNumerico resumo = ResumoNumerico.de(List.of(450.0));

        Assertions.assertEquals(1, resumo.n());
        Assertions.assertEquals(450.0, resumo.media(), TOLERANCIA);
        Assertions.assertEquals(450.0, resumo.mediana(), TOLERANCIA);
        Assertions.assertEquals(0.0, resumo.desvioPadrao(), TOLERANCIA);
        Assertions.assertEquals(0.0, resumo.amplitude(), TOLERANCIA);
    }

    @Test
    public void mediaZeroDeixaOCoeficienteDeVariacaoSemValor()
    {
        ResumoNumerico resumo = ResumoNumerico.de(List.of(-2.0, 2.0));

        Assertions.assertEquals(0.0, resumo.media(), TOLERANCIA);
        Assertions.assertNull(resumo.coeficienteDeVariacao());
    }

    @Test
    public void amostraVaziaNaoTemMedidas()
    {
        ResumoNumerico resumo = ResumoNumerico.de(List.of());

        Assertions.assertEquals(0, resumo.n());
        Assertions.assertNull(resumo.media());
        Assertions.assertNull(resumo.mediana());
        Assertions.assertNull(resumo.desvioPadrao());
    }
}
