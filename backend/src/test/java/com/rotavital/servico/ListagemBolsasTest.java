package com.rotavital.servico;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

// Listagem de bolsas com filtros e paginação (GET /api/v1/hemocomponentes). A consulta em si roda no
// banco de dados; aqui o teste confere o que o serviço repassa ao repositório e o que ele recusa.
public class ListagemBolsasTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private BolsaHemocomponente bolsa;
    private Object[] consultaFeita;
    private EstoqueService servico;

    @BeforeEach
    public void criarServicoComUmaBolsa()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco);

        // Guarda os argumentos da consulta para o teste conferir filtros e página.
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                "buscarComFiltros", argumentos ->
                {
                    consultaFeita = argumentos;
                    return List.of(bolsa);
                });
        // A listagem não consulta os bancos de sangue.
        servico = new EstoqueService(null, bolsasFalsas);
    }

    @Test
    public void repassaOsFiltrosEAPaginaAoRepositorio()
    {
        List<BolsaHemocomponente> bolsas = servico.listarBolsas("BS-01", TipoComponente.HEMACIAS,
                TipoSanguineo.O_NEGATIVO, StatusBolsa.DISPONIVEL, 2, 10);

        Assertions.assertEquals(List.of(bolsa), bolsas);
        Assertions.assertEquals("BS-01", consultaFeita[0]);
        Assertions.assertEquals(TipoComponente.HEMACIAS, consultaFeita[1]);
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, consultaFeita[2]);
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, consultaFeita[3]);

        Pageable pagina = (Pageable) consultaFeita[4];
        Assertions.assertEquals(2, pagina.getPageNumber());
        Assertions.assertEquals(10, pagina.getPageSize());
        Assertions.assertEquals(20, pagina.getOffset());
    }

    @Test
    public void semFiltrosRepassaTudoNulo()
    {
        servico.listarBolsas(null, null, null, null, 0, 20);

        Assertions.assertArrayEquals(new Object[] { null, null, null, null }, Arrays.copyOf(consultaFeita, 4));
    }

    @Test
    public void bancoEmBrancoNaoFiltra()
    {
        servico.listarBolsas("  ", null, null, null, 0, 20);

        Assertions.assertNull(consultaFeita[0]);
    }

    @Test
    public void paginaNegativaDevolveErro()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.listarBolsas(null, null, null, null, -1, 20));

        Assertions.assertEquals("O parâmetro 'page' deve ser maior ou igual a 0.", erro.getMessage());
        Assertions.assertNull(consultaFeita, "Com parâmetro inválido, o banco de dados não deve ser consultado.");
    }

    @Test
    public void tamanhoZeroOuAcimaDeCemDevolveErro()
    {
        for (int tamanho : new int[] { 0, 101 })
        {
            DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                    () -> servico.listarBolsas(null, null, null, null, 0, tamanho));

            Assertions.assertEquals("O parâmetro 'size' deve estar entre 1 e 100.", erro.getMessage());
        }
    }

    @Test
    public void tamanhoNosLimitesEhAceito()
    {
        Assertions.assertDoesNotThrow(() -> servico.listarBolsas(null, null, null, null, 0, 1));
        Assertions.assertDoesNotThrow(() -> servico.listarBolsas(null, null, null, null, 0, 100));
    }

    @Test
    public void paginaETamanhoInvalidosTrazemAsDuasMensagens()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.listarBolsas(null, null, null, null, -1, 0));

        Assertions.assertEquals("O parâmetro 'page' deve ser maior ou igual a 0. "
                + "O parâmetro 'size' deve estar entre 1 e 100.", erro.getMessage());
    }
}
