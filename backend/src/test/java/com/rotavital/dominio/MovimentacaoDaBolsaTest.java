package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// HU-08 · T8.1 e T8.2 — Para onde a bolsa pode ir a partir de cada status. A baixa no procedimento
// é a passagem para UTILIZADA; UTILIZADA e DESCARTADA são finais (a bolsa não volta ao estoque).
public class MovimentacaoDaBolsaTest
{
    private static final Map<StatusBolsa, Set<StatusBolsa>> PERMITIDAS = Map.of(
            StatusBolsa.DISPONIVEL, EnumSet.of(StatusBolsa.RESERVADA, StatusBolsa.DESCARTADA),
            StatusBolsa.RESERVADA, EnumSet.of(StatusBolsa.DISPONIVEL, StatusBolsa.EM_TRANSITO,
                    StatusBolsa.UTILIZADA, StatusBolsa.DESCARTADA),
            StatusBolsa.EM_TRANSITO, EnumSet.of(StatusBolsa.ENTREGUE, StatusBolsa.DESCARTADA),
            StatusBolsa.ENTREGUE, EnumSet.of(StatusBolsa.UTILIZADA, StatusBolsa.DESCARTADA),
            StatusBolsa.UTILIZADA, EnumSet.noneOf(StatusBolsa.class),
            StatusBolsa.DESCARTADA, EnumSet.noneOf(StatusBolsa.class));

    private final LocalDate hoje = LocalDate.of(2026, 10, 1);

    private BolsaHemocomponente bolsa(int diasParaVencer)
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(30), hoje.plusDays(diasParaVencer), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco);
    }

    // Leva a bolsa até o status pedido pelo caminho normal do estoque.
    private BolsaHemocomponente bolsaNoStatus(StatusBolsa status)
    {
        BolsaHemocomponente bolsa = bolsa(10);
        switch (status)
        {
            case DISPONIVEL -> { }
            case RESERVADA -> bolsa.alterarStatus(StatusBolsa.RESERVADA, hoje);
            case EM_TRANSITO ->
            {
                bolsa.alterarStatus(StatusBolsa.RESERVADA, hoje);
                bolsa.alterarStatus(StatusBolsa.EM_TRANSITO, hoje);
            }
            case ENTREGUE ->
            {
                bolsa.alterarStatus(StatusBolsa.RESERVADA, hoje);
                bolsa.alterarStatus(StatusBolsa.EM_TRANSITO, hoje);
                bolsa.alterarStatus(StatusBolsa.ENTREGUE, hoje);
            }
            case UTILIZADA ->
            {
                bolsa.alterarStatus(StatusBolsa.RESERVADA, hoje);
                bolsa.alterarStatus(StatusBolsa.UTILIZADA, hoje);
            }
            case DESCARTADA -> bolsa.descartar();
        }
        return bolsa;
    }

    static Stream<Arguments> todasAsTransicoes()
    {
        return Stream.of(StatusBolsa.values())
                .flatMap(de -> Stream.of(StatusBolsa.values()).map(para -> Arguments.of(de, para)));
    }

    // As 36 combinações de status: as da tabela PERMITIDAS mudam o status; todas as outras são recusadas.
    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("todasAsTransicoes")
    public void transicaoDeStatus(StatusBolsa de, StatusBolsa para)
    {
        BolsaHemocomponente bolsa = bolsaNoStatus(de);

        if (PERMITIDAS.get(de).contains(para))
        {
            bolsa.alterarStatus(para, hoje);
            Assertions.assertEquals(para, bolsa.getStatus());
        }
        else
        {
            Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> bolsa.alterarStatus(para, hoje));
            Assertions.assertEquals(de, bolsa.getStatus(), "A transição recusada não pode mudar o status.");
        }
    }

    @Test
    public void bolsaVencidaNaoPodeSerReservada()
    {
        BolsaHemocomponente vencida = bolsa(-1);

        Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> vencida.alterarStatus(StatusBolsa.RESERVADA, hoje));
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, vencida.getStatus());
    }

    @Test
    public void bolsaQueVenceuDepoisDeReservadaNaoPodeSerUtilizada()
    {
        BolsaHemocomponente bolsa = bolsa(2);
        bolsa.alterarStatus(StatusBolsa.RESERVADA, hoje);

        Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> bolsa.alterarStatus(StatusBolsa.UTILIZADA, hoje.plusDays(3)));
    }

    @Test
    public void bolsaVencidaPodeSerDescartada()
    {
        BolsaHemocomponente vencida = bolsa(-1);

        vencida.alterarStatus(StatusBolsa.DESCARTADA, hoje);

        Assertions.assertEquals(StatusBolsa.DESCARTADA, vencida.getStatus());
    }

    @Test
    public void mensagemDizDeOndeParaOndeFoiRecusado()
    {
        BolsaHemocomponente utilizada = bolsaNoStatus(StatusBolsa.UTILIZADA);

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> utilizada.alterarStatus(StatusBolsa.RESERVADA, hoje));

        Assertions.assertEquals("A bolsa CH-1042 está UTILIZADA e não pode passar para RESERVADA.", erro.getMessage());
    }
}
