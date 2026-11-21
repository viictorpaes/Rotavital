package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Estatística descritiva do estoque de um banco de sangue numa data de referência.
 *
 * Duas populações diferentes aparecem aqui, e cada medida diz qual usa:
 * - "em estoque": bolsas DISPONIVEL e ainda na validade, o que de fato pode ser usado;
 * - "cadastradas": todas as bolsas que o banco já registrou, em qualquer status, base da taxa de perda.
 */
public final class EstatisticaDescritivaDoEstoque
{
    /** Classes de "dias até vencer" para o histograma: [0, 7], [8, 15], [16, 30], [31, 90] e 91 ou mais. */
    public enum ClasseDeValidade
    {
        ATE_7_DIAS("0 a 7 dias", 0, 7),
        DE_8_A_15_DIAS("8 a 15 dias", 8, 15),
        DE_16_A_30_DIAS("16 a 30 dias", 16, 30),
        DE_31_A_90_DIAS("31 a 90 dias", 31, 90),
        MAIS_DE_90_DIAS("91 dias ou mais", 91, Long.MAX_VALUE);

        private final String rotulo;
        private final long minimo;
        private final long maximo;

        ClasseDeValidade(String rotulo, long minimo, long maximo)
        {
            this.rotulo = rotulo;
            this.minimo = minimo;
            this.maximo = maximo;
        }

        public String getRotulo()
        {
            return rotulo;
        }

        static ClasseDeValidade de(long diasAteVencer)
        {
            return Arrays.stream(values())
                    .filter(classe -> diasAteVencer >= classe.minimo && diasAteVencer <= classe.maximo)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Dias até vencer negativos: " + diasAteVencer));
        }
    }

    /** Medidas de um componente: validade, temperatura e volume das bolsas em estoque, e perdas entre as cadastradas. */
    public record PorComponente(
            TipoComponente componente,
            long emEstoque,
            ResumoNumerico diasAteVencer,
            ResumoNumerico temperaturaCelsius,
            long foraDaFaixa,
            double percentualForaDaFaixa,
            ResumoNumerico volumeMl,
            long cadastradas,
            long descartadas,
            double taxaDePerda)
    {
    }

    private final LocalDate dataDeReferencia;
    private final long cadastradas;
    private final long emEstoque;
    private final long vencidasAindaNoEstoque;
    private final long descartadas;
    private final TabelaDeFrequencia porTipoSanguineo;
    private final TabelaDeFrequencia porComponente;
    private final TabelaDeFrequencia porDiasAteVencer;
    private final List<PorComponente> medidasPorComponente;

    private EstatisticaDescritivaDoEstoque(List<BolsaHemocomponente> bolsas, LocalDate hoje)
    {
        this.dataDeReferencia = hoje;

        List<BolsaHemocomponente> noEstoque = bolsas.stream()
                .filter(bolsa -> estaEmEstoque(bolsa, hoje))
                .toList();

        this.cadastradas = bolsas.size();
        this.emEstoque = noEstoque.size();
        this.vencidasAindaNoEstoque = bolsas.stream()
                .filter(bolsa -> bolsa.estaDisponivel() && bolsa.estaVencida(hoje))
                .count();
        this.descartadas = bolsas.stream().filter(EstatisticaDescritivaDoEstoque::foiDescartada).count();

        this.porTipoSanguineo = new TabelaDeFrequencia(nomes(TipoSanguineo.values()));
        this.porComponente = new TabelaDeFrequencia(nomes(TipoComponente.values()));
        this.porDiasAteVencer = new TabelaDeFrequencia(Arrays.stream(ClasseDeValidade.values())
                .map(ClasseDeValidade::getRotulo)
                .toList());

        for (BolsaHemocomponente bolsa : noEstoque)
        {
            porTipoSanguineo.contar(bolsa.getTipoSanguineo().name());
            porComponente.contar(bolsa.getTipoComponente().name());
            porDiasAteVencer.contar(ClasseDeValidade.de(diasAteVencer(bolsa, hoje)).getRotulo());
        }

        this.medidasPorComponente = Arrays.stream(TipoComponente.values())
                .map(componente -> medir(componente, bolsas, noEstoque, hoje))
                .toList();
    }

    public static EstatisticaDescritivaDoEstoque calcular(List<BolsaHemocomponente> bolsas, LocalDate hoje)
    {
        return new EstatisticaDescritivaDoEstoque(bolsas, hoje);
    }

    private static PorComponente medir(TipoComponente componente, List<BolsaHemocomponente> todas,
            List<BolsaHemocomponente> noEstoque, LocalDate hoje)
    {
        List<BolsaHemocomponente> doComponente = noEstoque.stream()
                .filter(bolsa -> bolsa.getTipoComponente() == componente)
                .toList();
        long foraDaFaixa = doComponente.stream().filter(BolsaHemocomponente::estaForaDaFaixa).count();
        long cadastradasDoComponente = todas.stream().filter(bolsa -> bolsa.getTipoComponente() == componente).count();
        long descartadasDoComponente = todas.stream()
                .filter(bolsa -> bolsa.getTipoComponente() == componente && foiDescartada(bolsa))
                .count();

        return new PorComponente(
                componente,
                doComponente.size(),
                ResumoNumerico.de(doComponente.stream().map(bolsa -> (double) diasAteVencer(bolsa, hoje)).toList()),
                ResumoNumerico.de(doComponente.stream().map(BolsaHemocomponente::getTemperaturaCelsius).toList()),
                foraDaFaixa,
                TabelaDeFrequencia.percentual(foraDaFaixa, doComponente.size()),
                ResumoNumerico.de(doComponente.stream().map(BolsaHemocomponente::getVolumeMl).toList()),
                cadastradasDoComponente,
                descartadasDoComponente,
                TabelaDeFrequencia.percentual(descartadasDoComponente, cadastradasDoComponente));
    }

    private static boolean estaEmEstoque(BolsaHemocomponente bolsa, LocalDate hoje)
    {
        return bolsa.estaDisponivel() && !bolsa.estaVencida(hoje);
    }

    private static boolean foiDescartada(BolsaHemocomponente bolsa)
    {
        return bolsa.getStatus() == StatusBolsa.DESCARTADA;
    }

    // Vence hoje = 0 dias: a bolsa ainda vale até o fim do dia.
    private static long diasAteVencer(BolsaHemocomponente bolsa, LocalDate hoje)
    {
        return ChronoUnit.DAYS.between(hoje, bolsa.getDataValidade());
    }

    private static List<String> nomes(Enum<?>[] valores)
    {
        return Arrays.stream(valores).map(Enum::name).toList();
    }

    public LocalDate getDataDeReferencia()
    {
        return dataDeReferencia;
    }

    public long getCadastradas()
    {
        return cadastradas;
    }

    public long getEmEstoque()
    {
        return emEstoque;
    }

    public long getVencidasAindaNoEstoque()
    {
        return vencidasAindaNoEstoque;
    }

    public long getDescartadas()
    {
        return descartadas;
    }

    public double getTaxaDePerda()
    {
        return TabelaDeFrequencia.percentual(descartadas, cadastradas);
    }

    public TabelaDeFrequencia getPorTipoSanguineo()
    {
        return porTipoSanguineo;
    }

    public TabelaDeFrequencia getPorComponente()
    {
        return porComponente;
    }

    public TabelaDeFrequencia getPorDiasAteVencer()
    {
        return porDiasAteVencer;
    }

    public List<PorComponente> getMedidasPorComponente()
    {
        return medidasPorComponente;
    }
}
