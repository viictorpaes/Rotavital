package com.rotavital.dominio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabela de distribuição de frequências: absoluta (fi), relativa (fr, em %) e relativa
 * acumulada (Fr, em %), na ordem em que as categorias foram declaradas.
 *
 * Todas as categorias aparecem, inclusive as com frequência zero, para que a tabela de um
 * estoque vazio de AB− continue mostrando a linha AB− com 0.
 */
public final class TabelaDeFrequencia
{
    public record Linha(String categoria, long frequenciaAbsoluta, double frequenciaRelativa,
            double frequenciaRelativaAcumulada)
    {
    }

    private final Map<String, Long> contagens = new LinkedHashMap<>();

    public TabelaDeFrequencia(List<String> categorias)
    {
        for (String categoria : categorias)
        {
            contagens.put(categoria, 0L);
        }
    }

    public void contar(String categoria)
    {
        if (!contagens.containsKey(categoria))
        {
            throw new IllegalArgumentException("Categoria fora da tabela: " + categoria);
        }
        contagens.merge(categoria, 1L, Long::sum);
    }

    public long total()
    {
        return contagens.values().stream().mapToLong(Long::longValue).sum();
    }

    public List<Linha> linhas()
    {
        long total = total();
        List<Linha> linhas = new ArrayList<>();
        long acumulada = 0;

        for (Map.Entry<String, Long> contagem : contagens.entrySet())
        {
            acumulada += contagem.getValue();
            linhas.add(new Linha(contagem.getKey(), contagem.getValue(),
                    percentual(contagem.getValue(), total), percentual(acumulada, total)));
        }
        return linhas;
    }

    // Moda: as categorias com a maior frequência (pode haver empate). Sem observações, não há moda.
    public List<String> moda()
    {
        long maior = contagens.values().stream().mapToLong(Long::longValue).max().orElse(0);
        if (maior == 0)
        {
            return List.of();
        }
        return contagens.entrySet().stream()
                .filter(contagem -> contagem.getValue() == maior)
                .map(Map.Entry::getKey)
                .toList();
    }

    static double percentual(long parte, long total)
    {
        return total == 0 ? 0.0 : parte * 100.0 / total;
    }
}
