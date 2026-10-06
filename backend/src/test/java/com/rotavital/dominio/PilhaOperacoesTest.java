package com.rotavital.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PilhaOperacoesTest
{
    @Test
    void registraConsultaERemoveOperacoesEmOrdemLifo()
    {
        PilhaOperacoes<String> pilha = new PilhaOperacoes<>();
        String primeira = "criar requisição";
        String segunda = "alocar bolsa";

        pilha.registrar(primeira);
        pilha.registrar(segunda);

        assertSame(segunda, pilha.consultarMaisRecente().orElseThrow());
        assertSame(segunda, pilha.removerMaisRecente().orElseThrow());
        assertSame(primeira, pilha.consultarMaisRecente().orElseThrow());
        assertSame(primeira, pilha.removerMaisRecente().orElseThrow());
    }

    @Test
    void consultaERemocaoEmPilhaVaziaRetornamOptionalVazio()
    {
        PilhaOperacoes<String> pilha = new PilhaOperacoes<>();

        assertTrue(pilha.consultarMaisRecente().isEmpty());
        assertTrue(pilha.removerMaisRecente().isEmpty());
        assertTrue(pilha.listar().isEmpty());
    }

    @Test
    void listagemNaoPermiteAlterarPilha()
    {
        PilhaOperacoes<String> pilha = new PilhaOperacoes<>();
        pilha.registrar("criar requisição");
        pilha.registrar("alocar bolsa");

        List<String> historico = pilha.listar();

        assertEquals(List.of("alocar bolsa", "criar requisição"), historico);
        assertThrows(UnsupportedOperationException.class, historico::clear);
        assertEquals(List.of("alocar bolsa", "criar requisição"), pilha.listar());
    }

    @Test
    void naoPermiteRegistrarOperacaoNula()
    {
        PilhaOperacoes<String> pilha = new PilhaOperacoes<>();

        assertThrows(NullPointerException.class, () -> pilha.registrar(null));
        assertTrue(pilha.listar().isEmpty());
    }
}
