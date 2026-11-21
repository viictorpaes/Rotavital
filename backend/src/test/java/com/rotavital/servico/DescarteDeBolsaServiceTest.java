package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.CategoriaDescarte;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.TransferenciaDeBolsaRepository;
import com.rotavital.repositorio.UsuarioRepository;

// PI3-243: só o admin descarta, sempre com motivo padronizado, e a data vem do relógio.
public class DescarteDeBolsaServiceTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Admin Rota Vital", PapelUsuario.ADMIN, null, null);
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana", PapelUsuario.MEDICO, "BS-01", null);

    private BolsaHemocomponente bolsa;
    private MovimentacaoDeBolsaService servico;

    @BeforeEach
    public void criarServico()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(5), HOJE.plusDays(30), "LOTE-0001", 450.0, 4.0, "R1", banco);

        BolsaHemocomponenteRepository bolsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, "findById",
                argumentos -> Optional.ofNullable("CH-1042".equals(argumentos[0]) ? bolsa : null));
        TransferenciaDeBolsaRepository transferencias = RepositorioFalso.respondendo(TransferenciaDeBolsaRepository.class,
                "findByBolsaIdAndStatus", argumentos -> Optional.empty());
        Map<UUID, Usuario> pessoas = Map.of(admin.getId(), admin, medico.getId(), medico);
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(pessoas.get(argumentos[0])));

        servico = new MovimentacaoDeBolsaService(bolsas, transferencias, new IdentificacaoService(usuarios), RELOGIO);
    }

    @Test
    public void adminDescartaComMotivoObservacaoEDataDoRelogio()
    {
        BolsaHemocomponente descartada = servico.descartar(admin.getId(), "CH-1042",
                CategoriaDescarte.TEMPERATURA_FORA_DO_PADRAO, "  Geladeira R1 marcou 9 °C  ");

        Assertions.assertEquals(StatusBolsa.DESCARTADA, descartada.getStatus());
        Assertions.assertEquals(CategoriaDescarte.TEMPERATURA_FORA_DO_PADRAO, descartada.getCategoriaDescarte());
        Assertions.assertEquals("Geladeira R1 marcou 9 °C", descartada.getMotivoDescarte());
        Assertions.assertEquals(OffsetDateTime.now(RELOGIO), descartada.getDataDescarte());
    }

    @Test
    public void observacaoEmBrancoFicaVazia()
    {
        servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.BOLSA_DANIFICADA, "   ");

        Assertions.assertNull(bolsa.getMotivoDescarte());
    }

    @Test
    public void medicoNaoDescarta()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.descartar(medico.getId(), "CH-1042", CategoriaDescarte.BOLSA_DANIFICADA, null));

        Assertions.assertEquals("Só admin pode descartar uma bolsa.", erro.getMessage());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
    }

    @Test
    public void semUsuarioNaoDescarta()
    {
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class,
                () -> servico.descartar(null, "CH-1042", CategoriaDescarte.BOLSA_DANIFICADA, null));
    }

    @Test
    public void motivoEObrigatorio()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.descartar(admin.getId(), "CH-1042", null, "Rompeu"));

        Assertions.assertEquals("Escolha o motivo do descarte (categoriaDescarte): "
                + "VALIDADE_EXPIRADA, TEMPERATURA_FORA_DO_PADRAO, BOLSA_DANIFICADA ou OUTRO.", erro.getMessage());
    }

    @Test
    public void outroExigeObservacao()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.OUTRO, " "));

        Assertions.assertEquals("Descreva o motivo do descarte quando ele for \"Outro\".", erro.getMessage());
    }

    @Test
    public void observacaoTemNoMaximo500Caracteres()
    {
        Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.OUTRO, "x".repeat(501)));
        Assertions.assertDoesNotThrow(
                () -> servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.OUTRO, "x".repeat(500)));
    }

    @Test
    public void validadeExpiradaEmBolsaNaValidadeDa422()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.VALIDADE_EXPIRADA, null));

        Assertions.assertEquals("A bolsa CH-1042 vence em 31/10/2026: o motivo \"Validade expirada\" só vale para bolsa vencida.",
                erro.getMessage());
    }

    @Test
    public void bolsaInexistenteDa404()
    {
        Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.descartar(admin.getId(), "CH-9999", CategoriaDescarte.BOLSA_DANIFICADA, null));
    }

    @Test
    public void bolsaJaDescartadaNaoEDescartadaDeNovo()
    {
        servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.BOLSA_DANIFICADA, null);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.OUTRO, "De novo"));

        Assertions.assertEquals("A bolsa CH-1042 está DESCARTADA e não pode passar para DESCARTADA.", erro.getMessage());
        Assertions.assertEquals(CategoriaDescarte.BOLSA_DANIFICADA, bolsa.getCategoriaDescarte());
    }

    @Test
    public void bolsaDescartadaNaoPodeSerReservada()
    {
        servico.descartar(admin.getId(), "CH-1042", CategoriaDescarte.BOLSA_DANIFICADA, null);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.alterarStatus("CH-1042", StatusBolsa.RESERVADA));

        Assertions.assertEquals("A bolsa CH-1042 está DESCARTADA e não pode passar para RESERVADA.", erro.getMessage());
    }

    @Test
    public void mudarStatusParaDescartadaSemOFluxoDeDescarteDa400()
    {
        Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.alterarStatus("CH-1042", StatusBolsa.DESCARTADA));
    }
}
