package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Estatística descritiva do estoque: quem entra em cada medida e o que cada número quer dizer.
public class EstatisticaDescritivaDoEstoqueTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final double TOLERANCIA = 1e-9;

    private EstatisticaDescritivaDoEstoque estatistica;

    private static BolsaHemocomponente bolsa(String id, TipoComponente componente, TipoSanguineo tipo,
            int diasAteVencer, double volume, double temperatura, BancoDeSangue banco)
    {
        return new BolsaHemocomponente(id, componente, tipo, HOJE.minusDays(2), HOJE.plusDays(diasAteVencer),
                "LOTE-" + id, volume, temperatura, null, banco);
    }

    @BeforeEach
    public void montarEstoque()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));

        BolsaHemocomponente vencida = bolsa("CH-4", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, -1, 450, 4.0, banco);
        BolsaHemocomponente descartada = bolsa("CH-5", TipoComponente.HEMACIAS, TipoSanguineo.B_POSITIVO, 15, 450, 4.0, banco);
        descartada.descartar();
        BolsaHemocomponente reservada = bolsa("CR-1", TipoComponente.CRIOPRECIPITADO, TipoSanguineo.O_NEGATIVO, 200, 30, -25.0, banco);
        reservada.reservar();

        List<BolsaHemocomponente> bolsas = List.of(
                bolsa("CH-1", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 3, 450, 4.0, banco),
                // Temperatura acima da faixa ideal das hemácias (2 a 6 °C).
                bolsa("CH-2", TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO, 10, 400, 8.5, banco),
                bolsa("CH-3", TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO, 20, 350, 4.0, banco),
                vencida,
                descartada,
                // Vence hoje: ainda vale e conta 0 dias.
                bolsa("PQ-1", TipoComponente.PLAQUETAS, TipoSanguineo.O_POSITIVO, 0, 250, 22.0, banco),
                bolsa("PF-1", TipoComponente.PLASMA, TipoSanguineo.AB_POSITIVO, 365, 200, -25.0, banco),
                reservada);

        estatistica = EstatisticaDescritivaDoEstoque.calcular(bolsas, HOJE);
    }

    private static long frequencia(TabelaDeFrequencia tabela, String categoria)
    {
        return tabela.linhas().stream()
                .filter(linha -> linha.categoria().equals(categoria))
                .findFirst()
                .orElseThrow()
                .frequenciaAbsoluta();
    }

    private EstatisticaDescritivaDoEstoque.PorComponente medidas(TipoComponente componente)
    {
        return estatistica.getMedidasPorComponente().stream()
                .filter(medida -> medida.componente() == componente)
                .findFirst()
                .orElseThrow();
    }

    @Test
    public void emEstoqueSoContaBolsaDisponivelENaValidade()
    {
        Assertions.assertEquals(8, estatistica.getCadastradas());
        Assertions.assertEquals(5, estatistica.getEmEstoque());
        Assertions.assertEquals(1, estatistica.getVencidasAindaNoEstoque());
        Assertions.assertEquals(HOJE, estatistica.getDataDeReferencia());
    }

    @Test
    public void distribuiOEstoquePorTipoSanguineoComTodosOsTiposEModa()
    {
        TabelaDeFrequencia porTipo = estatistica.getPorTipoSanguineo();

        Assertions.assertEquals(8, porTipo.linhas().size());
        Assertions.assertEquals(5, porTipo.total());
        Assertions.assertEquals(2, frequencia(porTipo, "A_POSITIVO"));
        Assertions.assertEquals(1, frequencia(porTipo, "O_NEGATIVO"));
        Assertions.assertEquals(0, frequencia(porTipo, "AB_NEGATIVO"));
        Assertions.assertEquals(List.of("A_POSITIVO"), porTipo.moda());
    }

    @Test
    public void distribuiOEstoquePorComponente()
    {
        TabelaDeFrequencia porComponente = estatistica.getPorComponente();

        Assertions.assertEquals(3, frequencia(porComponente, "HEMACIAS"));
        Assertions.assertEquals(1, frequencia(porComponente, "PLAQUETAS"));
        Assertions.assertEquals(1, frequencia(porComponente, "PLASMA"));
        Assertions.assertEquals(0, frequencia(porComponente, "CRIOPRECIPITADO"));
    }

    @Test
    public void agrupaOsDiasAteVencerEmClasses()
    {
        TabelaDeFrequencia classes = estatistica.getPorDiasAteVencer();

        Assertions.assertEquals(2, frequencia(classes, "0 a 7 dias"));
        Assertions.assertEquals(1, frequencia(classes, "8 a 15 dias"));
        Assertions.assertEquals(1, frequencia(classes, "16 a 30 dias"));
        Assertions.assertEquals(0, frequencia(classes, "31 a 90 dias"));
        Assertions.assertEquals(1, frequencia(classes, "91 dias ou mais"));
        Assertions.assertEquals(80.0, classes.linhas().get(3).frequenciaRelativaAcumulada(), TOLERANCIA);
    }

    @Test
    public void mediasDasHemaciasEmEstoque()
    {
        EstatisticaDescritivaDoEstoque.PorComponente hemacias = medidas(TipoComponente.HEMACIAS);

        Assertions.assertEquals(3, hemacias.emEstoque());
        Assertions.assertEquals(11.0, hemacias.diasAteVencer().media(), TOLERANCIA);
        Assertions.assertEquals(10.0, hemacias.diasAteVencer().mediana(), TOLERANCIA);
        Assertions.assertEquals(400.0, hemacias.volumeMl().media(), TOLERANCIA);
        Assertions.assertEquals(1, hemacias.foraDaFaixa());
        Assertions.assertEquals(100.0 / 3, hemacias.percentualForaDaFaixa(), TOLERANCIA);
    }

    @Test
    public void taxaDePerdaUsaTodasAsBolsasCadastradas()
    {
        Assertions.assertEquals(1, estatistica.getDescartadas());
        Assertions.assertEquals(12.5, estatistica.getTaxaDePerda(), TOLERANCIA);

        EstatisticaDescritivaDoEstoque.PorComponente hemacias = medidas(TipoComponente.HEMACIAS);
        Assertions.assertEquals(5, hemacias.cadastradas());
        Assertions.assertEquals(1, hemacias.descartadas());
        Assertions.assertEquals(20.0, hemacias.taxaDePerda(), TOLERANCIA);
    }

    @Test
    public void componenteSemBolsaEmEstoqueFicaSemMedidas()
    {
        EstatisticaDescritivaDoEstoque.PorComponente crio = medidas(TipoComponente.CRIOPRECIPITADO);

        Assertions.assertEquals(0, crio.emEstoque());
        Assertions.assertEquals(0, crio.diasAteVencer().n());
        Assertions.assertNull(crio.temperaturaCelsius().media());
        Assertions.assertEquals(0.0, crio.percentualForaDaFaixa(), TOLERANCIA);
        Assertions.assertEquals(1, crio.cadastradas());
        Assertions.assertEquals(0.0, crio.taxaDePerda(), TOLERANCIA);
    }

    @Test
    public void estoqueVazioNaoDivideporZero()
    {
        EstatisticaDescritivaDoEstoque vazio = EstatisticaDescritivaDoEstoque.calcular(List.of(), HOJE);

        Assertions.assertEquals(0, vazio.getCadastradas());
        Assertions.assertEquals(0.0, vazio.getTaxaDePerda(), TOLERANCIA);
        Assertions.assertEquals(List.of(), vazio.getPorTipoSanguineo().moda());
    }
}
