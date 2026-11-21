package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.EstatisticaDescritivaDoEstoque;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;

// O serviço só busca o banco e usa a data do relógio; o cálculo é testado no domínio.
public class EstatisticaDescritivaServiceTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private EstatisticaDescritivaService servico;

    @BeforeEach
    public void criarServico()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        banco.getEstoque().adicionarBolsa(new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS,
                TipoSanguineo.O_NEGATIVO, HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-0001", 450.0, 4.0, null, banco));

        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, "findById",
                argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        servico = new EstatisticaDescritivaService(bancos, relogio);
    }

    @Test
    public void calculaComAsBolsasDoBancoNaDataDoRelogio()
    {
        EstatisticaDescritivaDoEstoque estatistica = servico.calcular("BS-01");

        Assertions.assertEquals(HOJE, estatistica.getDataDeReferencia());
        Assertions.assertEquals(1, estatistica.getEmEstoque());
        Assertions.assertEquals(30.0, estatistica.getMedidasPorComponente().get(0).diasAteVencer().media());
    }

    @Test
    public void bancoInexistenteDa404()
    {
        RecursoNaoEncontradoException erro = Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.calcular("BS-99"));

        Assertions.assertEquals("Nenhum banco de sangue encontrado com id BS-99.", erro.getMessage());
    }
}
