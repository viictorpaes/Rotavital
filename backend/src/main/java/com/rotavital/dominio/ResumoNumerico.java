package com.rotavital.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Medidas de posição e de dispersão de uma amostra (Estatística descritiva).
 *
 * Os quartis usam interpolação linear na posição (n − 1) · p da amostra ordenada, o mesmo
 * método da função QUARTIL.INC do Excel e do LibreOffice, para que o resultado possa ser
 * conferido numa planilha. O desvio padrão é o amostral (divide por n − 1).
 *
 * Amostra vazia: n = 0 e as medidas ficam nulas, porque não existe média de nada.
 */
public record ResumoNumerico(
        int n,
        Double media,
        Double mediana,
        Double desvioPadrao,
        Double coeficienteDeVariacao,
        Double minimo,
        Double primeiroQuartil,
        Double terceiroQuartil,
        Double maximo,
        Double amplitude)
{
    public static ResumoNumerico de(List<Double> valores)
    {
        if (valores.isEmpty())
        {
            return new ResumoNumerico(0, null, null, null, null, null, null, null, null, null);
        }

        List<Double> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);

        int n = ordenados.size();
        double media = ordenados.stream().mapToDouble(Double::doubleValue).sum() / n;
        double desvioPadrao = desvioPadraoAmostral(ordenados, media);
        // CV em %: só faz sentido com média diferente de zero.
        Double coeficienteDeVariacao = media == 0 ? null : desvioPadrao / Math.abs(media) * 100;
        double minimo = ordenados.get(0);
        double maximo = ordenados.get(n - 1);

        return new ResumoNumerico(n, media, quantil(ordenados, 0.5), desvioPadrao, coeficienteDeVariacao,
                minimo, quantil(ordenados, 0.25), quantil(ordenados, 0.75), maximo, maximo - minimo);
    }

    // Uma única observação não tem dispersão amostral: o desvio fica 0 em vez de dividir por zero.
    private static double desvioPadraoAmostral(List<Double> ordenados, double media)
    {
        int n = ordenados.size();
        if (n < 2)
        {
            return 0.0;
        }

        double somaDosQuadrados = 0.0;
        for (double valor : ordenados)
        {
            somaDosQuadrados += (valor - media) * (valor - media);
        }
        return Math.sqrt(somaDosQuadrados / (n - 1));
    }

    // QUARTIL.INC: posição h = (n − 1) · p; entre dois valores, interpola em linha reta.
    static double quantil(List<Double> ordenados, double p)
    {
        double h = (ordenados.size() - 1) * p;
        int abaixo = (int) Math.floor(h);
        int acima = (int) Math.ceil(h);
        double fracao = h - abaixo;
        return ordenados.get(abaixo) + fracao * (ordenados.get(acima) - ordenados.get(abaixo));
    }
}
