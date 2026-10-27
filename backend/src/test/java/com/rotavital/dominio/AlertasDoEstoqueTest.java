package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// HU-02 · T2.1 a T2.4 — Caixa de alertas do painel: bolsa vencida ainda no estoque e bolsa fora da
// faixa de temperatura são críticas; bolsa que vence nos próximos 7 dias pede atenção.
public class AlertasDoEstoqueTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private BancoDeSangue banco;

    @BeforeEach
    public void criarBanco()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
    }

    private BolsaHemocomponente bolsa(String id, int diasParaVencer, double temperatura)
    {
        BolsaHemocomponente bolsa = new BolsaHemocomponente(id, TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(30), hoje.plusDays(diasParaVencer), "LOTE-" + id, 450.0, temperatura,
                "R1 · P1 · N1", banco);
        banco.getEstoque().adicionarBolsa(bolsa);
        return bolsa;
    }

    // "CH-1 VENCIDA CRITICO": lote, tipo e nível de cada alerta, na ordem do painel.
    private List<String> alertas()
    {
        return banco.getEstoque().listarAlertas(hoje).stream()
                .map(a -> a.bolsaId() + " " + a.tipo() + " " + a.nivel())
                .toList();
    }

    @Test
    public void bolsaVencidaAindaNoEstoqueEhCritica()
    {
        bolsa("CH-1", -1, 4.0);

        Assertions.assertEquals(List.of("CH-1 VENCIDA CRITICO"), alertas());
    }

    @ParameterizedTest(name = "vence em {0} dia(s)")
    @ValueSource(ints = { 0, 1, 7 })
    public void bolsaQueVenceEmAte7DiasPedeAtencao(int dias)
    {
        bolsa("CH-1", dias, 4.0);

        Assertions.assertEquals(List.of("CH-1 VENCE_EM_BREVE ATENCAO"), alertas());
    }

    @Test
    public void bolsaQueVenceEm8DiasNaoGeraAlerta()
    {
        bolsa("CH-1", 8, 4.0);

        Assertions.assertEquals(List.of(), alertas());
    }

    @Test
    public void bolsaForaDaFaixaDeTemperaturaEhCritica()
    {
        // Hemácias: 2 °C a 6 °C.
        bolsa("CH-1", 30, 9.0);

        Assertions.assertEquals(List.of("CH-1 FORA_DA_FAIXA CRITICO"), alertas());
    }

    @Test
    public void bolsaVencendoEForaDaFaixaGeraOsDoisAlertas()
    {
        bolsa("CH-1", 3, 9.0);

        Assertions.assertEquals(List.of("CH-1 FORA_DA_FAIXA CRITICO", "CH-1 VENCE_EM_BREVE ATENCAO"), alertas());
    }

    @Test
    public void bolsaReservadaAindaEstaNoEstoqueEGeraAlerta()
    {
        bolsa("CH-1", -2, 4.0).reservar();

        Assertions.assertEquals(List.of("CH-1 VENCIDA CRITICO"), alertas());
    }

    @Test
    public void bolsaDescartadaNaoGeraAlerta()
    {
        bolsa("CH-1", -2, 9.0).descartar();

        Assertions.assertEquals(List.of(), alertas());
    }

    @Test
    public void criticosPrimeiroDepoisQuemVencePrimeiro()
    {
        bolsa("CH-A", 2, 4.0);
        bolsa("CH-B", -3, 4.0);
        bolsa("CH-C", 20, 9.0);
        bolsa("CH-D", 5, 4.0);

        Assertions.assertEquals(List.of(
                "CH-B VENCIDA CRITICO",
                "CH-C FORA_DA_FAIXA CRITICO",
                "CH-A VENCE_EM_BREVE ATENCAO",
                "CH-D VENCE_EM_BREVE ATENCAO"), alertas());
    }

    @Test
    public void mensagemDizQuandoABolsaVence()
    {
        bolsa("CH-HOJE", 0, 4.0);
        bolsa("CH-3D", 3, 4.0);
        bolsa("CH-VENCIDA", -1, 4.0);

        List<String> mensagens = banco.getEstoque().listarAlertas(hoje).stream().map(AlertaDeEstoque::mensagem).toList();

        Assertions.assertEquals(List.of(
                "Bolsa CH-VENCIDA venceu em 30/09/2026: retirar do estoque e descartar.",
                "Bolsa CH-HOJE vence hoje.",
                "Bolsa CH-3D vence em 3 dias (04/10/2026)."), mensagens);
    }
}
