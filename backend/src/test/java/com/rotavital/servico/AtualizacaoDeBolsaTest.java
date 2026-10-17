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

import com.rotavital.api.dto.estoque.AtualizarBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

// Edição e descarte de bolsas (PI3-190): o que pode mudar, em que status e com quais regras.
public class AtualizacaoDeBolsaTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private final List<BolsaHemocomponente> gravadas = new ArrayList<>();
    private BolsaHemocomponente bolsa;
    private EstoqueService servico;

    @BeforeEach
    public void criarServicoComUmaBolsaDeHemacias()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        // Coletada há 5 dias: a validade de hemácias pode ir até a coleta + 42 dias (06/11/2026).
        bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", banco);

        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.of(banco));
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                Map.of(
                        "findById", argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null),
                        "save", argumentos ->
                        {
                            gravadas.add((BolsaHemocomponente) argumentos[0]);
                            return argumentos[0];
                        }));

        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));
        servico = new EstoqueService(bancosFalsos, bolsasFalsas, relogio);
    }

    private static AtualizarBolsaHemocomponenteRequest status(StatusBolsa status)
    {
        return new AtualizarBolsaHemocomponenteRequest(status, null, null, null, null);
    }

    private String mensagemDaRegra(AtualizarBolsaHemocomponenteRequest dados)
    {
        return Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.atualizarBolsa("CH-1042", dados))
                .getMessage();
    }

    @Test
    public void reservarDisponivelGravaComoReservada()
    {
        BolsaHemocomponente atualizada = servico.atualizarBolsa("CH-1042", status(StatusBolsa.RESERVADA));

        Assertions.assertEquals(StatusBolsa.RESERVADA, atualizada.getStatus());
        Assertions.assertEquals(List.of(bolsa), gravadas);
    }

    @Test
    public void liberarReservaVoltaParaDisponivel()
    {
        bolsa.reservar();

        Assertions.assertEquals(StatusBolsa.DISPONIVEL,
                servico.atualizarBolsa("CH-1042", status(StatusBolsa.DISPONIVEL)).getStatus());
    }

    @Test
    public void descartarReservadaGravaComoDescartada()
    {
        bolsa.reservar();

        Assertions.assertEquals(StatusBolsa.DESCARTADA,
                servico.atualizarBolsa("CH-1042", status(StatusBolsa.DESCARTADA)).getStatus());
    }

    @Test
    public void corrigeValidadeVolumeTemperaturaELocalizacaoSemMexerNoResto()
    {
        BolsaHemocomponente atualizada = servico.atualizarBolsa("CH-1042", new AtualizarBolsaHemocomponenteRequest(
                null, hoje.plusDays(20), 400.0, 5.5, "  R2 · P1 · N1  "));

        Assertions.assertEquals(hoje.plusDays(20), atualizada.getDataValidade());
        Assertions.assertEquals(400.0, atualizada.getVolumeMl());
        Assertions.assertEquals(5.5, atualizada.getTemperaturaCelsius());
        Assertions.assertEquals("R2 · P1 · N1", atualizada.getLocalizacao());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, atualizada.getStatus());
        Assertions.assertEquals("LOTE-SIM-0001", atualizada.getLoteSintetico());
    }

    @Test
    public void campoAusenteFicaComoEstava()
    {
        BolsaHemocomponente atualizada = servico.atualizarBolsa("CH-1042",
                new AtualizarBolsaHemocomponenteRequest(null, null, 380.0, null, null));

        Assertions.assertEquals(380.0, atualizada.getVolumeMl());
        Assertions.assertEquals(hoje.plusDays(30), atualizada.getDataValidade());
        Assertions.assertEquals(4.0, atualizada.getTemperaturaCelsius());
        Assertions.assertEquals("R1 · P3 · N2", atualizada.getLocalizacao());
    }

    @Test
    public void localizacaoEmBrancoApagaALocalizacao()
    {
        Assertions.assertNull(servico.atualizarBolsa("CH-1042",
                new AtualizarBolsaHemocomponenteRequest(null, null, null, null, "   ")).getLocalizacao());
    }

    @Test
    public void bolsaInexistenteLanca404ComAMesmaMensagemDaConsulta()
    {
        RecursoNaoEncontradoException erro = Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.atualizarBolsa("BOLSA-999", status(StatusBolsa.RESERVADA)));

        Assertions.assertEquals("Nenhuma bolsa encontrada com id BOLSA-999.", erro.getMessage());
    }

    @Test
    public void corpoSemNenhumCampoLanca400()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.atualizarBolsa("CH-1042", new AtualizarBolsaHemocomponenteRequest(null, null, null, null, null)));

        Assertions.assertEquals("Informe ao menos um campo para alterar: status, dataValidade, volumeMl, "
                + "temperaturaCelsius ou localizacao.", erro.getMessage());
        Assertions.assertTrue(gravadas.isEmpty());
    }

    @Test
    public void volumeZeroLanca400()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.atualizarBolsa("CH-1042", new AtualizarBolsaHemocomponenteRequest(null, null, 0.0, null, null)));

        Assertions.assertEquals("O campo 'volumeMl' deve ser maior que zero.", erro.getMessage());
    }

    @Test
    public void descartadaNaoPodeMaisSerAlterada()
    {
        bolsa.descartar();

        Assertions.assertEquals("A bolsa CH-1042 está DESCARTADA e não pode mais ser alterada: só bolsas "
                + "DISPONIVEL ou RESERVADA podem.", mensagemDaRegra(status(StatusBolsa.DISPONIVEL)));
        Assertions.assertTrue(gravadas.isEmpty());
    }

    @Test
    public void statusDoTransporteNaoPodeSerEscolhido()
    {
        Assertions.assertEquals("O status EM_TRANSITO não pode ser escolhido aqui: use DISPONIVEL, RESERVADA ou "
                + "DESCARTADA.", mensagemDaRegra(status(StatusBolsa.EM_TRANSITO)));
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }

    @Test
    public void validadeAlemDoPrazoDoComponenteLanca422()
    {
        // Coleta em 26/09/2026: 50 dias passa dos 42 das hemácias.
        Assertions.assertEquals("A validade de HEMACIAS é de no máximo 42 dias depois da coleta, e foram informados "
                + "50 dias.", mensagemDaRegra(new AtualizarBolsaHemocomponenteRequest(
                        null, hoje.minusDays(5).plusDays(50), null, null, null)));
    }

    @Test
    public void validadeNoPassadoLanca422()
    {
        Assertions.assertEquals("A bolsa venceu em 30/09/2026 e não pode entrar no estoque.",
                mensagemDaRegra(new AtualizarBolsaHemocomponenteRequest(null, hoje.minusDays(1), null, null, null)));
    }

    @Test
    public void juntaTodosOsProblemasNumaMensagemSoENaoGravaNada()
    {
        String mensagem = mensagemDaRegra(new AtualizarBolsaHemocomponenteRequest(
                StatusBolsa.UTILIZADA, hoje.minusDays(5), 800.0, -90.0, null));

        Assertions.assertTrue(mensagem.contains("O status UTILIZADA não pode ser escolhido aqui"), mensagem);
        Assertions.assertTrue(mensagem.contains("precisa ser depois da data de coleta"), mensagem);
        Assertions.assertTrue(mensagem.contains("passa do máximo de 700.0 ml"), mensagem);
        Assertions.assertTrue(mensagem.contains("fora do que o sensor mede"), mensagem);
        Assertions.assertTrue(gravadas.isEmpty());
        Assertions.assertEquals(450.0, bolsa.getVolumeMl());
    }
}
