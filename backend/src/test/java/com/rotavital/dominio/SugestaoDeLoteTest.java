package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// HU-04 · T4.4 — Ordem da sugestão de lote: o mesmo tipo do paciente primeiro (poupa o O-, que
// serve a todos), dentro de cada grupo quem vence primeiro (FEFO), e nunca uma bolsa incompatível.
public class SugestaoDeLoteTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private BancoDeSangue banco;

    @BeforeEach
    public void criarBanco()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
    }

    private BolsaHemocomponente bolsa(String id, TipoComponente componente, TipoSanguineo tipo,
            int diasParaVencer, double temperatura)
    {
        BolsaHemocomponente bolsa = new BolsaHemocomponente(id, componente, tipo, hoje.minusDays(5),
                hoje.plusDays(diasParaVencer), "LOTE-" + id, 450.0, temperatura, "R1 · P1 · N1", banco);
        banco.getEstoque().adicionarBolsa(bolsa);
        return bolsa;
    }

    private BolsaHemocomponente hemacias(String id, TipoSanguineo tipo, int diasParaVencer)
    {
        return bolsa(id, TipoComponente.HEMACIAS, tipo, diasParaVencer, 4.0);
    }

    private List<String> sugerir(TipoComponente componente, TipoSanguineo paciente, int quantidade)
    {
        return banco.getEstoque().sugerirLotes(componente, paciente, quantidade, hoje).stream()
                .map(BolsaHemocomponente::getId)
                .toList();
    }

    @Test
    public void prefereOMesmoTipoDoPacienteMesmoQueOCompativelVencaAntes()
    {
        hemacias("CH-ONEG", TipoSanguineo.O_NEGATIVO, 3);
        hemacias("CH-ANEG", TipoSanguineo.A_NEGATIVO, 9);

        Assertions.assertEquals(List.of("CH-ANEG"), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.A_NEGATIVO, 1));
    }

    @Test
    public void dentroDoMesmoTipoSugereAQueVencePrimeiro()
    {
        hemacias("CH-20D", TipoSanguineo.O_NEGATIVO, 20);
        hemacias("CH-3D", TipoSanguineo.O_NEGATIVO, 3);
        hemacias("CH-9D", TipoSanguineo.O_NEGATIVO, 9);

        Assertions.assertEquals(List.of("CH-3D", "CH-9D"), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 2));
    }

    @Test
    public void completaComBolsaCompativelQuandoOMesmoTipoNaoBasta()
    {
        hemacias("CH-ONEG", TipoSanguineo.O_NEGATIVO, 3);
        hemacias("CH-ANEG", TipoSanguineo.A_NEGATIVO, 9);

        Assertions.assertEquals(List.of("CH-ANEG", "CH-ONEG"),
                sugerir(TipoComponente.HEMACIAS, TipoSanguineo.A_NEGATIVO, 2));
    }

    @Test
    public void nuncaSugereBolsaIncompativel()
    {
        // Paciente O- só recebe hemácias O-.
        hemacias("CH-OPOS", TipoSanguineo.O_POSITIVO, 2);
        hemacias("CH-ANEG", TipoSanguineo.A_NEGATIVO, 2);

        Assertions.assertEquals(List.of(), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 1));
    }

    @Test
    public void ignoraBolsaVencidaForaDaFaixaOuReservada()
    {
        hemacias("CH-VENCIDA", TipoSanguineo.O_NEGATIVO, -1);
        bolsa("CH-QUENTE", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 5, 9.0);
        hemacias("CH-RESERVADA", TipoSanguineo.O_NEGATIVO, 6).reservar();
        hemacias("CH-APTA", TipoSanguineo.O_NEGATIVO, 30);

        Assertions.assertEquals(List.of("CH-APTA"), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 4));
    }

    @Test
    public void ignoraOutroComponente()
    {
        bolsa("PF-1", TipoComponente.PLASMA, TipoSanguineo.O_NEGATIVO, 3, -25.0);
        hemacias("CH-1", TipoSanguineo.O_NEGATIVO, 10);

        Assertions.assertEquals(List.of("CH-1"), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 2));
    }

    @Test
    public void devolveMenosBolsasQuandoNaoHaOSuficiente()
    {
        hemacias("CH-1", TipoSanguineo.O_NEGATIVO, 10);

        Assertions.assertEquals(List.of("CH-1"), sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 3));
    }

    @Test
    public void quantidadeMenorQueUmEhRecusada()
    {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> sugerir(TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, 0));
    }
}
