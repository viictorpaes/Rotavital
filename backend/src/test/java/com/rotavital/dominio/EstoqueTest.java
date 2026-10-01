package com.rotavital.dominio;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// O estoque não tem lista própria: lê as bolsas do banco de sangue, que é o que o JPA carrega.
public class EstoqueTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private BancoDeSangue banco;

    @BeforeEach
    public void criarBanco()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central",
                new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829));
    }

    private BolsaHemocomponente novaBolsa(String id, TipoComponente componente, TipoSanguineo tipo,
            LocalDate dataValidade)
    {
        return new BolsaHemocomponente(id, componente, tipo, hoje.minusDays(5), dataValidade,
                "LOTE-" + id, 450.0, 4.0, "R1 · P1 · N1", banco);
    }

    @Test
    public void estoqueEnxergaAsBolsasCarregadasNoBancoDeSangue()
    {
        // Simula o Hibernate preenchendo a lista de bolsas do banco de sangue ao ler do banco de dados.
        BolsaHemocomponente carregada = novaBolsa("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(30));
        banco.bolsas().add(carregada);

        Assertions.assertEquals(List.of(carregada), banco.getEstoque().getBolsas());
    }

    @Test
    public void bolsaAdicionadaPeloEstoqueFicaNoBancoDeSangue()
    {
        BolsaHemocomponente bolsa = novaBolsa("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(30));

        banco.getEstoque().adicionarBolsa(bolsa);

        Assertions.assertEquals(List.of(bolsa), banco.bolsas());
    }

    @Test
    public void buscarDisponiveisFiltraComponenteTipoEStatus()
    {
        Estoque estoque = banco.getEstoque();
        BolsaHemocomponente compativel = novaBolsa("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(30));
        BolsaHemocomponente reservada = novaBolsa("CH-1043", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(20));
        reservada.reservar();
        estoque.adicionarBolsa(compativel);
        estoque.adicionarBolsa(reservada);
        estoque.adicionarBolsa(novaBolsa("CH-1061", TipoComponente.HEMACIAS,
                TipoSanguineo.A_NEGATIVO, hoje.plusDays(30)));
        estoque.adicionarBolsa(novaBolsa("PQ-3014", TipoComponente.PLAQUETAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(3)));

        List<BolsaHemocomponente> disponiveis = estoque.buscarDisponiveis(TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO);

        Assertions.assertEquals(List.of(compativel), disponiveis);
    }

    @Test
    public void buscarPorTipoSanguineoEListarVencidas()
    {
        Estoque estoque = banco.getEstoque();
        BolsaHemocomponente vencida = novaBolsa("CH-1080", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.minusDays(1));
        BolsaHemocomponente valida = novaBolsa("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO, hoje.plusDays(30));
        BolsaHemocomponente outroTipo = novaBolsa("CR-4002", TipoComponente.CRIOPRECIPITADO,
                TipoSanguineo.AB_NEGATIVO, hoje.plusDays(300));
        estoque.adicionarBolsa(vencida);
        estoque.adicionarBolsa(valida);
        estoque.adicionarBolsa(outroTipo);

        Assertions.assertEquals(List.of(vencida, valida), estoque.buscarPorTipoSanguineo(TipoSanguineo.O_POSITIVO));
        Assertions.assertEquals(List.of(vencida), estoque.listarVencidas(hoje));
    }
}
