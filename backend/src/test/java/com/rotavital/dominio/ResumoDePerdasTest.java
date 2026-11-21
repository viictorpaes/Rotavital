package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// PI3-243: perdas por tipo, componente e motivo, com período e taxa de perda.
public class ResumoDePerdasTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final double TOLERANCIA = 1e-9;

    private BancoDeSangue banco;
    private final List<BolsaHemocomponente> bolsas = new ArrayList<>();

    private BolsaHemocomponente bolsa(String id, TipoComponente componente, TipoSanguineo tipo, int diasDesdeAColeta,
            double volume)
    {
        LocalDate coleta = HOJE.minusDays(diasDesdeAColeta);
        BolsaHemocomponente bolsa = new BolsaHemocomponente(id, componente, tipo, coleta, coleta.plusDays(5),
                "LOTE-" + id, volume, 4.0, null, banco);
        bolsas.add(bolsa);
        return bolsa;
    }

    private static OffsetDateTime diasAtras(int dias)
    {
        return HOJE.minusDays(dias).atTime(10, 0).atOffset(ZoneOffset.ofHours(-3));
    }

    @BeforeEach
    public void montarBolsas()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));

        // Descartadas recentes, coletadas há poucos dias.
        bolsa("CH-1", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 3, 300)
                .descartar(CategoriaDescarte.BOLSA_DANIFICADA, null, diasAtras(1));
        bolsa("CH-2", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 4, 280)
                .descartar(CategoriaDescarte.TEMPERATURA_FORA_DO_PADRAO, null, diasAtras(2));
        bolsa("PQ-1", TipoComponente.PLAQUETAS, TipoSanguineo.A_POSITIVO, 5, 60)
                .descartar(CategoriaDescarte.TEMPERATURA_FORA_DO_PADRAO, null, diasAtras(0));
        // Descarte antigo, de 40 dias atrás.
        bolsa("CH-3", TipoComponente.HEMACIAS, TipoSanguineo.B_POSITIVO, 50, 310)
                .descartar(CategoriaDescarte.VALIDADE_EXPIRADA, null, diasAtras(40));
        // Descarte de antes do PI3-243: sem motivo padronizado nem data.
        bolsa("CH-4", TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO, 60, 290).descartar();
        // Bolsas que não se perderam.
        bolsa("CH-5", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 2, 300);
        bolsa("PQ-2", TipoComponente.PLAQUETAS, TipoSanguineo.A_POSITIVO, 1, 55).reservar();
    }

    private static long bolsasDa(List<ResumoDePerdas.Total> totais, String categoria)
    {
        return totais.stream().filter(total -> total.categoria().equals(categoria)).findFirst().orElseThrow().bolsas();
    }

    @Test
    public void historicoCompletoContaTodosOsDescartes()
    {
        ResumoDePerdas resumo = ResumoDePerdas.calcular(bolsas, null);

        Assertions.assertNull(resumo.getDesde());
        Assertions.assertEquals(5, resumo.getTotalBolsas());
        Assertions.assertEquals(300 + 280 + 60 + 310 + 290, resumo.getTotalVolumeMl(), TOLERANCIA);
        Assertions.assertEquals(2, bolsasDa(resumo.getPorTipoSanguineo(), "O_POSITIVO"));
        Assertions.assertEquals(2, bolsasDa(resumo.getPorTipoSanguineo(), "A_POSITIVO"));
        Assertions.assertEquals(4, bolsasDa(resumo.getPorComponente(), "HEMACIAS"));
    }

    @Test
    public void periodoSoContaDescartesRegistradosDesdeAData()
    {
        ResumoDePerdas resumo = ResumoDePerdas.calcular(bolsas, HOJE.minusDays(6));

        Assertions.assertEquals(HOJE.minusDays(6), resumo.getDesde());
        Assertions.assertEquals(3, resumo.getTotalBolsas());
        Assertions.assertEquals(List.of("O_POSITIVO"), resumo.getTiposComMaisDescartes());
        Assertions.assertEquals(2, bolsasDa(resumo.getPorMotivo(), "TEMPERATURA_FORA_DO_PADRAO"));
        Assertions.assertEquals(1, bolsasDa(resumo.getPorMotivo(), "BOLSA_DANIFICADA"));
    }

    @Test
    public void rankingVemDaMaiorParaAMenorQuantidade()
    {
        ResumoDePerdas resumo = ResumoDePerdas.calcular(bolsas, HOJE.minusDays(6));

        Assertions.assertEquals("O_POSITIVO", resumo.getPorTipoSanguineo().get(0).categoria());
        Assertions.assertEquals("TEMPERATURA_FORA_DO_PADRAO", resumo.getPorMotivo().get(0).categoria());
        Assertions.assertEquals("HEMACIAS", resumo.getPorComponente().get(0).categoria());
    }

    @Test
    public void descarteAntigoSemMotivoContaComoOutroSoNoHistorico()
    {
        Assertions.assertEquals(1, bolsasDa(ResumoDePerdas.calcular(bolsas, null).getPorMotivo(), "OUTRO"));
        Assertions.assertTrue(ResumoDePerdas.calcular(bolsas, HOJE.minusDays(29)).getPorMotivo().stream()
                .noneMatch(total -> total.categoria().equals("OUTRO")));
    }

    @Test
    public void empateNoTipoComMaisDescartesListaOsDois()
    {
        Assertions.assertEquals(List.of("O_POSITIVO", "A_POSITIVO"),
                ResumoDePerdas.calcular(bolsas, null).getTiposComMaisDescartes());
    }

    @Test
    public void taxaDePerdaOlhaAsBolsasColetadasNoPeriodo()
    {
        // Coletadas nos últimos 7 dias: CH-1, CH-2, PQ-1, CH-5 e PQ-2; três delas descartadas.
        ResumoDePerdas semana = ResumoDePerdas.calcular(bolsas, HOJE.minusDays(6));
        Assertions.assertEquals(5, semana.getColetadasNoPeriodo());
        Assertions.assertEquals(3, semana.getDescartadasDasColetadas());
        Assertions.assertEquals(60.0, semana.getTaxaDePerda(), TOLERANCIA);

        ResumoDePerdas tudo = ResumoDePerdas.calcular(bolsas, null);
        Assertions.assertEquals(7, tudo.getColetadasNoPeriodo());
        Assertions.assertEquals(500.0 / 7, tudo.getTaxaDePerda(), TOLERANCIA);
    }

    @Test
    public void semDescartesNaoTemDestaqueNemDividePorZero()
    {
        ResumoDePerdas vazio = ResumoDePerdas.calcular(List.of(), HOJE.minusDays(29));

        Assertions.assertEquals(0, vazio.getTotalBolsas());
        Assertions.assertEquals(List.of(), vazio.getTiposComMaisDescartes());
        Assertions.assertEquals(0.0, vazio.getTaxaDePerda(), TOLERANCIA);
    }
}
