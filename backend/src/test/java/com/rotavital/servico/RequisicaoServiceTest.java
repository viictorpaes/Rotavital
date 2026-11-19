package com.rotavital.servico;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.requisicao.NivelUrgencia;
import com.rotavital.api.dto.requisicao.NovaRequisicaoRequest;
import com.rotavital.dominio.AlaHospitalar;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.UsuarioRepository;

// HU-04 — Requisição só passa com estoque suficiente (senão 422) e grava o destino nas bolsas.
public class RequisicaoServiceTest
{
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-04T13:00:00Z"), ZoneId.of("America/Recife"));
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 4);

    private final BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital clinicas = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "Carlos", PapelUsuario.DOADOR, null, null);

    private final List<BolsaHemocomponente> gravadas = new ArrayList<>();
    private final List<String> travas = new ArrayList<>();
    private RequisicaoService servico;

    private BolsaHemocomponente bolsa(String id, TipoSanguineo tipo, int diasParaVencer, String localizacao)
    {
        BolsaHemocomponente bolsa = new BolsaHemocomponente(id, TipoComponente.HEMACIAS, tipo, HOJE.minusDays(20),
                HOJE.plusDays(diasParaVencer), "LOTE-" + id, 450, 4.0, localizacao, hemope);
        hemope.getEstoque().adicionarBolsa(bolsa);
        return bolsa;
    }

    @BeforeEach
    public void montarServico()
    {
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> List.of(medico, doador).stream().filter(u -> u.getId().equals(argumentos[0])).findFirst());

        Map<String, Function<Object[], Object>> repositorioDeBolsas = Map.of(
                "saveAll", argumentos ->
                {
                    for (Object bolsa : (Iterable<?>) argumentos[0])
                    {
                        gravadas.add((BolsaHemocomponente) bolsa);
                    }
                    return argumentos[0];
                },
                "travarDisponiveis", argumentos ->
                {
                    travas.add(argumentos[0] + "/" + argumentos[1]);
                    return hemope.getEstoque().getBolsas().stream()
                            .filter(b -> b.getTipoComponente() == argumentos[1] && b.getStatus() == StatusBolsa.DISPONIVEL)
                            .toList();
                });

        servico = new RequisicaoService(
                RepositorioFalso.respondendo(BancoDeSangueRepository.class, "findById",
                        argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? hemope : null)),
                RepositorioFalso.respondendo(HospitalRepository.class, "findById",
                        argumentos -> Optional.ofNullable("hc-pe".equals(argumentos[0]) ? clinicas : null)),
                RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, repositorioDeBolsas),
                new IdentificacaoService(usuarios),
                RELOGIO);
    }

    private static NovaRequisicaoRequest pedido(TipoSanguineo tipo, int quantidade, String leito)
    {
        return new NovaRequisicaoRequest("BS-01", "hc-pe", AlaHospitalar.UTI_ADULTO, leito, TipoComponente.HEMACIAS,
                tipo, quantidade, NivelUrgencia.ALTA, "João Batista");
    }

    @Test
    public void reservaPorFefoEGravaODestinoNasBolsas()
    {
        BolsaHemocomponente vencePrimeiro = bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");
        BolsaHemocomponente venceDepois = bolsa("CH-2", TipoSanguineo.A_POSITIVO, 20, "R1 · P3 · N3");
        BolsaHemocomponente sobra = bolsa("CH-3", TipoSanguineo.A_POSITIVO, 30, "R2 · P1 · N1");

        RequisicaoRegistrada requisicao = servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 2, "04"));

        Assertions.assertTrue(requisicao.protocolo().startsWith("REQ-"));
        Assertions.assertEquals("Hospital das Clínicas de PE · UTI Adulto · Leito 04", requisicao.destino());
        Assertions.assertEquals("Dra. Ana Lima", requisicao.solicitante());
        Assertions.assertEquals(List.of("CH-1", "CH-2"),
                requisicao.bolsas().stream().map(b -> b.bolsa().getId()).toList());
        Assertions.assertEquals(List.of("R1 · P3 · N2", "R1 · P3 · N3"),
                requisicao.bolsas().stream().map(RequisicaoRegistrada.BolsaAlocada::retirarDe).toList());

        for (BolsaHemocomponente reservada : List.of(vencePrimeiro, venceDepois))
        {
            Assertions.assertEquals(StatusBolsa.RESERVADA, reservada.getStatus());
            Assertions.assertEquals("Hospital das Clínicas de PE · UTI Adulto · Leito 04", reservada.getLocalizacao());
        }
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, sobra.getStatus());
        Assertions.assertEquals("R2 · P1 · N1", sobra.getLocalizacao());
        Assertions.assertEquals(List.of(vencePrimeiro, venceDepois), gravadas);
    }

    @Test
    public void travaAsBolsasDisponiveisDoComponenteAntesDeEscolher()
    {
        bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");

        servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 1, null));

        Assertions.assertEquals(List.of("BS-01/HEMACIAS"), travas);
    }

    @Test
    public void segundaRequisicaoSoVeOQueSobrouDaPrimeira()
    {
        bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");
        bolsa("CH-2", TipoSanguineo.A_POSITIVO, 20, "R1 · P3 · N3");

        servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 2, "04"));
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 1, "07")));

        Assertions.assertTrue(erro.getMessage().contains("não há nenhuma bolsa"), erro.getMessage());
        Assertions.assertEquals(2, gravadas.size(), "Só a primeira requisição reservou.");
    }

    @Test
    public void estoqueInsuficienteResponde422ENaoReservaNada()
    {
        BolsaHemocomponente unica = bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 2, null)));

        Assertions.assertTrue(erro.getMessage().contains("Estoque insuficiente"), erro.getMessage());
        Assertions.assertTrue(erro.getMessage().contains("há só 1 bolsa"), erro.getMessage());
        Assertions.assertTrue(erro.getMessage().contains("tela de Rede"), erro.getMessage());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, unica.getStatus());
        Assertions.assertEquals("R1 · P3 · N2", unica.getLocalizacao());
        Assertions.assertTrue(gravadas.isEmpty());
    }

    @Test
    public void semBolsaCompativelResponde422()
    {
        bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.registrar(medico.getId(), pedido(TipoSanguineo.O_NEGATIVO, 1, null)));

        Assertions.assertTrue(erro.getMessage().contains("não há nenhuma bolsa"), erro.getMessage());
    }

    @Test
    public void bolsasJaReservadasNaoContamComoEstoque()
    {
        BolsaHemocomponente reservada = bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");
        reservada.reservar();

        Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.registrar(medico.getId(), pedido(TipoSanguineo.A_POSITIVO, 1, null)));
    }

    @Test
    public void camposAusentesOuForaDoLimiteRespondem400()
    {
        NovaRequisicaoRequest vazio = new NovaRequisicaoRequest(null, " ", null, null, null, null, 0, null, null);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.registrar(medico.getId(), vazio));

        for (String campo : List.of("bancoId", "hospitalId", "ala", "tipoComponente", "tipoSanguineo", "quantidade"))
        {
            Assertions.assertTrue(erro.getMessage().contains("'" + campo + "'"), erro.getMessage());
        }
    }

    @Test
    public void hospitalInexistenteResponde404()
    {
        NovaRequisicaoRequest pedido = new NovaRequisicaoRequest("BS-01", "nao-existe", AlaHospitalar.EMERGENCIA, null,
                TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO, 1, null, null);

        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> servico.registrar(medico.getId(), pedido));
    }

    @Test
    public void soMedicoFazRequisicao()
    {
        bolsa("CH-1", TipoSanguineo.A_POSITIVO, 3, "R1 · P3 · N2");

        Assertions.assertThrows(UsuarioNaoIdentificadoException.class,
                () -> servico.registrar(null, pedido(TipoSanguineo.A_POSITIVO, 1, null)));
        Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.registrar(doador.getId(), pedido(TipoSanguineo.A_POSITIVO, 1, null)));
    }
}
