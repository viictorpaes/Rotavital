package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.estoque.ResumoPerdasDTO;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Endereco;
import com.rotavital.repositorio.BancoDeSangueRepository;

// PI3-243: o serviço transforma "últimos N dias" numa data e valida o período; o cálculo é do domínio.
public class ResumoPerdasServiceTest
{
    private ResumoPerdasService servico;

    @BeforeEach
    public void criarServico()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, "findById",
                argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        servico = new ResumoPerdasService(bancos, relogio);
    }

    @Test
    public void ultimosSeteDiasContamHojeEOsSeisAnteriores()
    {
        ResumoPerdasDTO resumo = servico.resumir("BS-01", 7).orElseThrow();

        Assertions.assertEquals(7, resumo.periodoDias());
        Assertions.assertEquals(LocalDate.of(2026, 9, 25), resumo.desde());
    }

    @Test
    public void semPeriodoUsaTodoOHistorico()
    {
        ResumoPerdasDTO resumo = servico.resumir("BS-01", null).orElseThrow();

        Assertions.assertNull(resumo.periodoDias());
        Assertions.assertNull(resumo.desde());
        Assertions.assertEquals(0, resumo.totalBolsas());
    }

    @Test
    public void periodoForaDaFaixaDa400()
    {
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.resumir("BS-01", 0));
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.resumir("BS-01", 3651));
    }

    @Test
    public void bancoInexistenteVoltaVazio()
    {
        Assertions.assertTrue(servico.resumir("BS-99", 30).isEmpty());
    }
}
