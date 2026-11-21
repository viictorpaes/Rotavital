package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.estoque.AtualizarBolsaRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.UsuarioRepository;

// HU-03 — correção dos dados de uma bolsa: quem pode, quando pode e com quais regras.
public class CorrecaoDeBolsaServiceTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana", PapelUsuario.MEDICO, "BS-01", null);
    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Admin", PapelUsuario.ADMIN, null, null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João", PapelUsuario.DOADOR, null, TipoSanguineo.O_POSITIVO);

    private BolsaHemocomponente bolsa;
    private CorrecaoDeBolsaService servico;

    @BeforeEach
    public void criarServico()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
        bolsa = new BolsaHemocomponente("PQ-3012", TipoComponente.PLAQUETAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(1), HOJE.plusDays(4), "LOTE-0001", 250.0, 22.0, "Agitador 1", banco);

        // Só a PQ-3012 existe; o lote LOTE-0099 de plaquetas já é de outra bolsa.
        BolsaHemocomponenteRepository bolsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, Map.of(
                "findById", argumentos -> Optional.ofNullable("PQ-3012".equals(argumentos[0]) ? bolsa : null),
                "existsByLoteSinteticoAndTipoComponenteAndIdNot", argumentos ->
                        "LOTE-0099".equals(argumentos[0]) && argumentos[1] == TipoComponente.PLAQUETAS));
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico
                        : admin.getId().equals(argumentos[0]) ? admin
                        : doador.getId().equals(argumentos[0]) ? doador : null));
        BancoDeSangueRepository bancos = RepositorioFalso.respondendo(BancoDeSangueRepository.class, Map.of());
        Clock relogio = Clock.fixed(Instant.parse("2026-10-01T15:00:00Z"), ZoneId.of("America/Recife"));

        servico = new CorrecaoDeBolsaService(bolsas, new EstoqueService(bancos, bolsas, relogio),
                new IdentificacaoService(usuarios), relogio);
    }

    private static AtualizarBolsaRequest correcao(TipoSanguineo tipo, LocalDate coleta, LocalDate validade, String lote,
            Double volume, Double temperatura, String localizacao)
    {
        return new AtualizarBolsaRequest(null, null, null, tipo, coleta, validade, lote, volume, temperatura, localizacao);
    }

    @Test
    public void medicoCorrigeVolumeELocalizacaoEOResteFicaComoEstava()
    {
        BolsaHemocomponente corrigida = servico.corrigir(medico.getId(), "PQ-3012",
                correcao(null, null, null, null, 230.0, null, "  Agitador 2  "));

        Assertions.assertEquals(230.0, corrigida.getVolumeMl());
        Assertions.assertEquals("Agitador 2", corrigida.getLocalizacao());
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, corrigida.getTipoSanguineo());
        Assertions.assertEquals(HOJE.plusDays(4), corrigida.getDataValidade());
        Assertions.assertEquals("LOTE-0001", corrigida.getLoteSintetico());
        Assertions.assertEquals(22.0, corrigida.getTemperaturaCelsius());
    }

    @Test
    public void localizacaoEmBrancoApagaALocalizacao()
    {
        servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, null, null, null, "   "));

        Assertions.assertNull(bolsa.getLocalizacao());
    }

    @Test
    public void medicoNaoCorrigeOTipoSanguineo()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012",
                        correcao(TipoSanguineo.A_POSITIVO, null, null, null, 200.0, null, null)));

        Assertions.assertEquals("Só admin pode corrigir o tipo sanguíneo de uma bolsa.", erro.getMessage());
        Assertions.assertEquals(TipoSanguineo.O_NEGATIVO, bolsa.getTipoSanguineo());
        Assertions.assertEquals(250.0, bolsa.getVolumeMl());
    }

    @Test
    public void mandarOMesmoTipoSanguineoNaoContaComoCorrecaoDele()
    {
        servico.corrigir(medico.getId(), "PQ-3012", correcao(TipoSanguineo.O_NEGATIVO, null, null, null, 240.0, null, null));

        Assertions.assertEquals(240.0, bolsa.getVolumeMl());
    }

    @Test
    public void adminCorrigeOTipoSanguineo()
    {
        servico.corrigir(admin.getId(), "PQ-3012", correcao(TipoSanguineo.A_POSITIVO, null, null, null, null, null, null));

        Assertions.assertEquals(TipoSanguineo.A_POSITIVO, bolsa.getTipoSanguineo());
    }

    @Test
    public void semUsuarioOuComDoadorNaoCorrige()
    {
        AtualizarBolsaRequest dados = correcao(null, null, null, null, 200.0, null, null);

        Assertions.assertThrows(UsuarioNaoIdentificadoException.class, () -> servico.corrigir(null, "PQ-3012", dados));
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.corrigir(doador.getId(), "PQ-3012", dados));
        Assertions.assertEquals("Só médico ou admin pode corrigir os dados de uma bolsa.", erro.getMessage());
        Assertions.assertEquals(250.0, bolsa.getVolumeMl());
    }

    @Test
    public void bolsaInexistenteDa404()
    {
        RecursoNaoEncontradoException erro = Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.corrigir(medico.getId(), "PQ-9999", correcao(null, null, null, null, 200.0, null, null)));

        Assertions.assertEquals("Nenhuma bolsa encontrada com id PQ-9999.", erro.getMessage());
    }

    @Test
    public void bolsaQueJaSaiuDoEstoqueNaoECorrigida()
    {
        bolsa.alterarStatus(StatusBolsa.RESERVADA, HOJE);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, null, 200.0, null, null)));

        Assertions.assertEquals("A bolsa PQ-3012 está RESERVADA e não pode mais ter os dados corrigidos: "
                + "só bolsas disponíveis podem ser editadas.", erro.getMessage());
        Assertions.assertEquals(250.0, bolsa.getVolumeMl());
    }

    @Test
    public void validadeAlemDoPrazoDoComponenteERecusadaComAMesmaMensagemDoCadastro()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012",
                        correcao(null, null, HOJE.plusDays(29), null, null, null, null)));

        Assertions.assertEquals("A validade de PLAQUETAS é de no máximo 7 dias depois da coleta, "
                + "e foram informados 30 dias.", erro.getMessage());
        Assertions.assertEquals(HOJE.plusDays(4), bolsa.getDataValidade());
    }

    @Test
    public void novaValidadeQueJaPassouERecusada()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012",
                        correcao(null, HOJE.minusDays(5), HOJE.minusDays(1), null, null, null, null)));

        Assertions.assertEquals("A nova validade (30/09/2026) já passou.", erro.getMessage());
    }

    @Test
    public void bolsaJaVencidaAindaTemALocalizacaoCorrigida()
    {
        // Vencida ontem e ainda DISPONIVEL (o descarte vem depois): a validade não foi mexida.
        BancoDeSangue banco = bolsa.getBancoOrigem();
        bolsa = new BolsaHemocomponente("PQ-3012", TipoComponente.PLAQUETAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(6), HOJE.minusDays(1), "LOTE-0001", 250.0, 22.0, "Agitador 1", banco);

        servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, null, null, null, "Quarentena"));

        Assertions.assertEquals("Quarentena", bolsa.getLocalizacao());
    }

    @Test
    public void loteDeOutraBolsaDoMesmoComponenteERecusado()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, "LOTE-0099", null, null, null)));

        Assertions.assertEquals("Já existe uma bolsa de PLAQUETAS do lote LOTE-0099: "
                + "cada doação gera no máximo uma bolsa de cada componente.", erro.getMessage());
        Assertions.assertEquals("LOTE-0001", bolsa.getLoteSintetico());
    }

    @Test
    public void manterOProprioLoteNaoContaComoRepetido()
    {
        servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, " LOTE-0001 ", 240.0, null, null));

        Assertions.assertEquals("LOTE-0001", bolsa.getLoteSintetico());
        Assertions.assertEquals(240.0, bolsa.getVolumeMl());
    }

    @Test
    public void loteEmBrancoEVolumeZeroDao400ComOsDoisProblemas()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, "  ", 0.0, null, null)));

        Assertions.assertEquals("O lote não pode ficar em branco. O volume deve ser maior que zero.", erro.getMessage());
    }

    @Test
    public void volumeAcimaDoMaximoERecusado()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.corrigir(medico.getId(), "PQ-3012", correcao(null, null, null, null, 800.0, null, null)));

        Assertions.assertEquals("O volume (800.0 ml) passa do máximo de 700.0 ml por bolsa.", erro.getMessage());
    }
}
