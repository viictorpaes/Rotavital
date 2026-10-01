package com.rotavital.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstoqueTest
{
    private Estoque estoque;
    private BancoDeSangue banco;

    @BeforeEach
    void setUp()
    {
        estoque = new Estoque();
        banco = new BancoDeSangue(
                "BS-01",
                "Hemope Central",
                new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829));
    }

    @Test
    void adicionaBolsaAoEstoque()
    {
        BolsaHemocomponente bolsa = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));

        estoque.adicionarBolsa(bolsa);

        assertEquals(List.of(bolsa), estoque.getBolsas());
    }

    @Test
    void estoquePodeSerUsadoIndependentementeDoEstoqueDoBancoDeOrigem()
    {
        BolsaHemocomponente bolsa = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));

        estoque.adicionarBolsa(bolsa);

        assertEquals(List.of(bolsa), estoque.getBolsas());
        assertTrue(banco.getEstoque().getBolsas().isEmpty());
    }

    @Test
    void buscaSomenteBolsasDisponiveisDoComponenteETipoSanguineoInformados()
    {
        BolsaHemocomponente compativel = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));
        BolsaHemocomponente reservada = criarBolsa(
                "CH-002", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));
        BolsaHemocomponente outroTipo = criarBolsa(
                "PQ-001", TipoComponente.PLAQUETAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));

        estoque.adicionarBolsa(compativel);
        estoque.adicionarBolsa(reservada);
        estoque.adicionarBolsa(outroTipo);
        reservada.reservar();

        assertEquals(List.of(compativel), estoque.buscarDisponiveis(
                TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO));
    }

    @Test
    void buscaPorTipoSanguineoSemFiltrarStatus()
    {
        BolsaHemocomponente disponivel = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));
        BolsaHemocomponente reservada = criarBolsa(
                "PQ-001", TipoComponente.PLAQUETAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));
        BolsaHemocomponente outroTipoSanguineo = criarBolsa(
                "CH-002", TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO,
                LocalDate.now().plusDays(10));

        estoque.adicionarBolsa(disponivel);
        estoque.adicionarBolsa(reservada);
        estoque.adicionarBolsa(outroTipoSanguineo);
        reservada.reservar();

        assertEquals(List.of(disponivel, reservada),
                estoque.buscarPorTipoSanguineo(TipoSanguineo.O_POSITIVO));
    }

    @Test
    void listaBolsasVencidasNaDataDeReferencia()
    {
        BolsaHemocomponente vencida = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.of(2026, 1, 1));
        BolsaHemocomponente valida = criarBolsa(
                "CH-002", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.of(2026, 1, 3));

        estoque.adicionarBolsa(vencida);
        estoque.adicionarBolsa(valida);

        assertEquals(List.of(vencida), estoque.listarVencidas(LocalDate.of(2026, 1, 2)));
    }

    @Test
    void consultasNaoPermitemAlterarColecaoInterna()
    {
        BolsaHemocomponente bolsa = criarBolsa(
                "CH-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10));
        estoque.adicionarBolsa(bolsa);

        List<List<BolsaHemocomponente>> resultados = List.of(
                estoque.getBolsas(),
                estoque.buscarDisponiveis(TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO),
                estoque.buscarPorTipoSanguineo(TipoSanguineo.O_POSITIVO),
                estoque.listarVencidas(LocalDate.now()));

        for (List<BolsaHemocomponente> resultado : resultados)
        {
            assertThrows(UnsupportedOperationException.class, resultado::clear);
        }

        assertEquals(List.of(bolsa), estoque.getBolsas());
    }

    @Test
    void permiteConsultarEstoqueVazio()
    {
        assertTrue(estoque.getBolsas().isEmpty());
        assertTrue(estoque.buscarDisponiveis(
                TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO).isEmpty());
        assertTrue(estoque.buscarPorTipoSanguineo(TipoSanguineo.O_POSITIVO).isEmpty());
        assertTrue(estoque.listarVencidas(LocalDate.now()).isEmpty());
    }

    private BolsaHemocomponente criarBolsa(
            String id,
            TipoComponente componente,
            TipoSanguineo tipoSanguineo,
            LocalDate validade)
    {
        return new BolsaHemocomponente(
                id,
                componente,
                tipoSanguineo,
                validade.minusDays(10),
                validade,
                450.0,
                4.0,
                "R1 · P1 · N1",
                banco);
    }
}
