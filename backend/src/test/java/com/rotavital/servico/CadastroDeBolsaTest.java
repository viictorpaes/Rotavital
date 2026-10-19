package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

// Cadastro de uma bolsa nova (PI3-179): o id gerado pela sigla do componente e o que é gravado.
public class CadastroDeBolsaTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private final List<BolsaHemocomponente> gravadas = new ArrayList<>();
    private BancoDeSangue banco;
    private EstoqueService servico;

    @BeforeEach
    public void criarServicoComOBancoBS01()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));

        // Ids que já existem, por sigla: CH-9999 é a bolsa de teste, fora da faixa das hemácias,
        // e CH-TESTE não tem número. Plasma (PF) ainda não tem nenhuma bolsa.
        Map<String, List<String>> idsPorSigla = Map.of(
                "CH", List.of("CH-1042", "CH-9999", "CH-1090", "CH-TESTE"),
                "PF", List.of());

        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                Map.of(
                        "existsByLoteSinteticoAndTipoComponente", argumentos -> false,
                        "buscarIdsComSigla", argumentos -> idsPorSigla.get((String) argumentos[0]),
                        "save", argumentos ->
                        {
                            gravadas.add((BolsaHemocomponente) argumentos[0]);
                            return argumentos[0];
                        }));

        // Relógio parado no meio do dia 01/10/2026 em Recife.
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));
        servico = new EstoqueService(bancosFalsos, bolsasFalsas, relogio);
    }

    private NovaBolsaHemocomponenteRequest bolsa(TipoComponente componente, LocalDate dataValidade, String lote,
    String localizacao)
    {
        return new NovaBolsaHemocomponenteRequest(componente, TipoSanguineo.O_NEGATIVO, hoje.minusDays(3), dataValidade,
                lote, 450.0, 4.0, localizacao, "BS-01");
    }

    @Test
    public void idSegueOMaiorNumeroDaFaixaDoComponente()
    {
        BolsaHemocomponente bolsa = servico.cadastrarBolsa(
                bolsa(TipoComponente.HEMACIAS, hoje.plusDays(30), "LOTE-SIM-0100", "R1 · P1 · N1"));

        // CH-9999 e CH-TESTE ficam fora da conta: o maior da faixa é CH-1090.
        Assertions.assertEquals("CH-1091", bolsa.getId());
    }

    @Test
    public void primeiraBolsaDoComponenteComecaNaFaixaDele()
    {
        BolsaHemocomponente bolsa = servico.cadastrarBolsa(
                bolsa(TipoComponente.PLASMA, hoje.plusDays(300), "LOTE-SIM-0100", null));

        Assertions.assertEquals("PF-2001", bolsa.getId());
    }

    @Test
    public void gravaABolsaDisponivelComOsDadosInformados()
    {
        BolsaHemocomponente bolsa = servico.cadastrarBolsa(
                bolsa(TipoComponente.HEMACIAS, hoje.plusDays(30), "  LOTE-SIM-0100 ", " R1 · P1 · N1 "));

        Assertions.assertEquals(List.of(bolsa), gravadas);
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
        Assertions.assertEquals(TipoComponente.HEMACIAS, bolsa.getTipoComponente());
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, bolsa.getTipoSanguineo());
        Assertions.assertEquals(hoje.minusDays(3), bolsa.getDataColeta());
        Assertions.assertEquals(hoje.plusDays(30), bolsa.getDataValidade());
        Assertions.assertEquals(450.0, bolsa.getVolumeMl());
        Assertions.assertEquals(4.0, bolsa.getTemperaturaCelsius());
        Assertions.assertSame(banco, bolsa.getBancoOrigem());
        // Espaços nas pontas saem antes de gravar.
        Assertions.assertEquals("LOTE-SIM-0100", bolsa.getLoteSintetico());
        Assertions.assertEquals("R1 · P1 · N1", bolsa.getLocalizacao());
        // Bolsa criada no cadastro vira INSERT no save(), nunca a atualização de outra bolsa com o mesmo id.
        Assertions.assertTrue(bolsa.isNew());
    }

    @Test
    public void localizacaoEmBrancoFicaVazia()
    {
        BolsaHemocomponente bolsa = servico.cadastrarBolsa(
                bolsa(TipoComponente.HEMACIAS, hoje.plusDays(30), "LOTE-SIM-0100", "   "));

        Assertions.assertNull(bolsa.getLocalizacao());
    }

    @Test
    public void dadosInvalidosNaoGravamNada()
    {
        // Validade 45 dias depois da coleta: passa do prazo das hemácias (42).
        Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.cadastrarBolsa(
                bolsa(TipoComponente.HEMACIAS, hoje.plusDays(42), "LOTE-SIM-0100", null)));

        Assertions.assertTrue(gravadas.isEmpty());
    }

    @Test
    public void usaADataDeHojeDoRelogio()
    {
        // Coleta amanhã (02/10/2026), para o relógio parado em 01/10/2026.
        NovaBolsaHemocomponenteRequest dados = new NovaBolsaHemocomponenteRequest(TipoComponente.HEMACIAS,
                TipoSanguineo.O_NEGATIVO, hoje.plusDays(1), hoje.plusDays(30), "LOTE-SIM-0100", 450.0, 4.0, null, "BS-01");

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.cadastrarBolsa(dados));

        Assertions.assertEquals("A data de coleta (02/10/2026) não pode ser depois de hoje (01/10/2026).", erro.getMessage());
    }
}
