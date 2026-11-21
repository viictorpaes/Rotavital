package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Perdas de um banco de sangue (PI3-243): as bolsas descartadas, agrupadas por tipo sanguíneo,
 * componente e motivo, e a taxa de perda.
 *
 * Período: com "desde", só contam os descartes registrados a partir dessa data; sem ele, todo o
 * histórico. A taxa de perda olha as bolsas coletadas no período e diz quantas delas acabaram
 * descartadas, por isso nunca passa de 100%, mesmo numa janela curta.
 */
public final class ResumoDePerdas
{
    /** Uma linha do ranking: quantas bolsas e quantos mL se perderam na categoria. */
    public record Total(String categoria, long bolsas, double volumeMl)
    {
    }

    // Mais bolsas primeiro; empate pelo volume e, por fim, pelo nome, para a ordem não variar.
    private static final Comparator<Total> MAIOR_PERDA_PRIMEIRO = Comparator.comparingLong(Total::bolsas).reversed()
            .thenComparing(Comparator.comparingDouble(Total::volumeMl).reversed())
            .thenComparing(Total::categoria);

    private final LocalDate desde;
    private final List<BolsaHemocomponente> descartadas;
    private final long coletadasNoPeriodo;
    private final long descartadasDasColetadas;

    private ResumoDePerdas(List<BolsaHemocomponente> bolsas, LocalDate desde)
    {
        this.desde = desde;
        this.descartadas = bolsas.stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DESCARTADA && descartadaNoPeriodo(bolsa, desde))
                .toList();

        List<BolsaHemocomponente> coletadas = bolsas.stream()
                .filter(bolsa -> desde == null || !bolsa.getDataColeta().isBefore(desde))
                .toList();
        this.coletadasNoPeriodo = coletadas.size();
        this.descartadasDasColetadas = coletadas.stream()
                .filter(bolsa -> bolsa.getStatus() == StatusBolsa.DESCARTADA)
                .count();
    }

    public static ResumoDePerdas calcular(List<BolsaHemocomponente> bolsas, LocalDate desde)
    {
        return new ResumoDePerdas(bolsas, desde);
    }

    // Descarte anterior à data de descarte existir no banco (sem data) só entra no histórico completo.
    private static boolean descartadaNoPeriodo(BolsaHemocomponente bolsa, LocalDate desde)
    {
        if (desde == null)
        {
            return true;
        }
        return bolsa.getDataDescarte() != null && !bolsa.getDataDescarte().toLocalDate().isBefore(desde);
    }

    private List<Total> agrupar(Function<BolsaHemocomponente, String> categoria)
    {
        Map<String, long[]> bolsasPorCategoria = new LinkedHashMap<>();
        Map<String, Double> volumePorCategoria = new LinkedHashMap<>();

        for (BolsaHemocomponente bolsa : descartadas)
        {
            String chave = categoria.apply(bolsa);
            bolsasPorCategoria.computeIfAbsent(chave, ignorada -> new long[1])[0]++;
            volumePorCategoria.merge(chave, bolsa.getVolumeMl(), Double::sum);
        }

        List<Total> totais = new ArrayList<>();
        bolsasPorCategoria.forEach((chave, contagem) -> totais.add(new Total(chave, contagem[0], volumePorCategoria.get(chave))));
        totais.sort(MAIOR_PERDA_PRIMEIRO);
        return totais;
    }

    public LocalDate getDesde()
    {
        return desde;
    }

    public long getTotalBolsas()
    {
        return descartadas.size();
    }

    public double getTotalVolumeMl()
    {
        return descartadas.stream().mapToDouble(BolsaHemocomponente::getVolumeMl).sum();
    }

    public List<Total> getPorTipoSanguineo()
    {
        return agrupar(bolsa -> bolsa.getTipoSanguineo().name());
    }

    public List<Total> getPorComponente()
    {
        return agrupar(bolsa -> bolsa.getTipoComponente().name());
    }

    // Descarte sem motivo padronizado (feito antes do PI3-243) conta como OUTRO.
    public List<Total> getPorMotivo()
    {
        return agrupar(bolsa -> bolsa.getCategoriaDescarte() == null
                ? CategoriaDescarte.OUTRO.name()
                : bolsa.getCategoriaDescarte().name());
    }

    /** Tipo(s) sanguíneo(s) com mais bolsas descartadas; mais de um quando há empate. */
    public List<String> getTiposComMaisDescartes()
    {
        List<Total> porTipo = getPorTipoSanguineo();
        if (porTipo.isEmpty())
        {
            return List.of();
        }
        long maior = porTipo.get(0).bolsas();
        return porTipo.stream().filter(total -> total.bolsas() == maior).map(Total::categoria).toList();
    }

    public long getColetadasNoPeriodo()
    {
        return coletadasNoPeriodo;
    }

    public long getDescartadasDasColetadas()
    {
        return descartadasDasColetadas;
    }

    /** Das bolsas coletadas no período, a porcentagem que foi descartada (0 a 100). */
    public double getTaxaDePerda()
    {
        return TabelaDeFrequencia.percentual(descartadasDasColetadas, coletadasNoPeriodo);
    }
}
