package com.rotavital.servico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;

// Leitura do estoque de um banco de sangue a partir do repositório (no app, a tabela ponto_rede).
public class EstoqueServiceTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private BancoDeSangue banco;
    private BolsaHemocomponente oPositivo;
    private BolsaHemocomponente abNegativo;
    private EstoqueService servico;

    @BeforeEach
    public void criarBancoComBolsas()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        oPositivo = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P1 · N1", banco);
        abNegativo = new BolsaHemocomponente("CR-4002", TipoComponente.CRIOPRECIPITADO, TipoSanguineo.AB_NEGATIVO,
                hoje.minusDays(5), hoje.plusDays(300), "LOTE-SIM-0002", 50.0, -25.0, "F1 · P1 · N1", banco);
        banco.getEstoque().adicionarBolsa(oPositivo);
        banco.getEstoque().adicionarBolsa(abNegativo);

        // Só o BS-01 existe; outro id devolve vazio, como o findById do Spring Data.
        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        servico = new EstoqueService(bancosFalsos);
    }

    @Test
    public void bancoInexistenteDevolveVazio()
    {
        Assertions.assertTrue(servico.buscarBolsas("BS-99", null).isEmpty());
    }

    @Test
    public void semFiltroDevolveTodasAsBolsasDoBanco()
    {
        Optional<List<BolsaHemocomponente>> bolsas = servico.buscarBolsas("BS-01", null);

        Assertions.assertEquals(Optional.of(List.of(oPositivo, abNegativo)), bolsas);
    }

    @Test
    public void comFiltroDevolveSoOTipoSanguineoPedido()
    {
        Optional<List<BolsaHemocomponente>> bolsas = servico.buscarBolsas("BS-01", TipoSanguineo.AB_NEGATIVO);

        Assertions.assertEquals(Optional.of(List.of(abNegativo)), bolsas);
    }
}
