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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.TransferenciaDeBolsa;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;
import com.rotavital.repositorio.TransferenciaDeBolsaRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Envio e recebimento pela tela de Rede (PI3-236), com o banco, o Google e o relógio falsos.
public class TransferenciaServiceTest
{
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-04T13:00:00Z"), ZoneId.of("America/Recife"));

    private final BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital clinicas = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final BolsaHemocomponente bolsa = new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS,
            TipoSanguineo.O_NEGATIVO, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 20), "LOTE-1", 450, 4.0,
            "R1 · P3 · N2", hemope);
    private final BolsaHemocomponente quente = new BolsaHemocomponente("CH-2000", TipoComponente.HEMACIAS,
            TipoSanguineo.O_NEGATIVO, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 11, 20), "LOTE-2", 450, 9.0,
            "R1 · P3 · N3", hemope);

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);
    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Equipe Técnica", PapelUsuario.ADMIN, null, null);

    private final List<TransferenciaDeBolsa> gravadas = new ArrayList<>();
    private int pedidosAoGoogle;
    private int transacoes;
    private TransferenciaService servico;

    @BeforeEach
    public void montarServico()
    {
        Map<String, Function<Object[], Object>> repositorioDeTransferencias = Map.of(
                "save", argumentos ->
                {
                    gravadas.add((TransferenciaDeBolsa) argumentos[0]);
                    return argumentos[0];
                },
                "findById", argumentos -> Optional.of(gravadas.get(0)));

        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> List.of(medico, admin).stream().filter(u -> u.getId().equals(argumentos[0])).findFirst());
        IdentificacaoService identificacao = new IdentificacaoService(usuarios);

        RedeDistribuicaoService rede = new RedeDistribuicaoService(
                RepositorioFalso.respondendo(PontoDeRedeRepository.class, "findAll",
                        argumentos -> List.<PontoDeRedeBase>of(hemope, clinicas)),
                RepositorioFalso.respondendo(ConexaoRepository.class, "findAllByOrderByIdAsc",
                        argumentos -> List.of(new Conexao(hemope, clinicas, 7.8, 18.0))));

        CalculadoraDeTrajeto google = new CalculadoraDeTrajeto()
        {
            @Override
            public Optional<Trajeto> calcular(Coordenada origem, Coordenada destino)
            {
                throw new AssertionError("O envio usa o caminho do Dijkstra.");
            }

            @Override
            public Optional<Trajeto> calcularComTracado(List<Coordenada> paradas)
            {
                pedidosAoGoogle++;
                return Optional.of(new Trajeto(9.4, 21, FonteDoTrajeto.GOOGLE_ROUTES, "abc"));
            }
        };

        servico = new TransferenciaService(
                RepositorioFalso.respondendo(TransferenciaDeBolsaRepository.class, repositorioDeTransferencias),
                RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class, "findById",
                        argumentos -> List.of(bolsa, quente).stream().filter(b -> b.getId().equals(argumentos[0])).findFirst()),
                RepositorioFalso.respondendo(HospitalRepository.class, "findById",
                        argumentos -> Optional.ofNullable("hc-pe".equals(argumentos[0]) ? clinicas : null)),
                new RotaNoMapaService(rede, google, identificacao),
                identificacao,
                new TransacaoFalsa(),
                RELOGIO);
    }

    private class TransacaoFalsa implements PlatformTransactionManager
    {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definicao)
        {
            transacoes++;
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status)
        {
        }

        @Override
        public void rollback(TransactionStatus status)
        {
        }
    }

    @Test
    public void despachoUsaOTrajetoDaRedeEGravaNumaTransacao()
    {
        TransferenciaDeBolsa transferencia = servico.despachar(medico.getId(), "CH-1042", "hc-pe");

        Assertions.assertEquals(List.of(transferencia), gravadas);
        Assertions.assertEquals(1, transacoes);
        Assertions.assertEquals(21, transferencia.getDuracaoMinutos());
        Assertions.assertEquals(hemope, transferencia.getOrigem());
        Assertions.assertEquals(StatusBolsa.EM_TRANSITO, bolsa.getStatus());
    }

    @Test
    public void recebimentoPoeABolsaNoHospital()
    {
        TransferenciaDeBolsa transferencia = servico.despachar(medico.getId(), "CH-1042", "hc-pe");

        servico.receber(medico.getId(), UUID.randomUUID());

        Assertions.assertEquals(StatusTransferencia.RECEBIDA, transferencia.getStatus());
        Assertions.assertEquals(clinicas, bolsa.getHospitalAtual());
    }

    @Test
    public void segundoRecebimentoRecebe422()
    {
        servico.despachar(medico.getId(), "CH-1042", "hc-pe");
        servico.receber(medico.getId(), UUID.randomUUID());

        Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.receber(medico.getId(), UUID.randomUUID()));
    }

    @Test
    public void bolsaQueNaoPodeSairNaoGastaACotaDoGoogle()
    {
        Assertions.assertThrows(RegraDeNegocioException.class, () -> servico.despachar(medico.getId(), "CH-2000", "hc-pe"));

        Assertions.assertEquals(0, pedidosAoGoogle);
        Assertions.assertTrue(gravadas.isEmpty());
    }

    @Test
    public void bolsaOuHospitalInexistenteRecebe404()
    {
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> servico.despachar(medico.getId(), "NAO-EXISTE", "hc-pe"));
        // BS-01 é um hemocentro: HospitalRepository não o encontra, a transferência só vai para hospital.
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> servico.despachar(medico.getId(), "CH-1042", "BS-01"));
    }

    @Test
    public void camposAusentesRecebem400()
    {
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.despachar(medico.getId(), " ", "hc-pe"));
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.despachar(medico.getId(), "CH-1042", null));
    }

    @Test
    public void soOMedicoDespachaERecebe()
    {
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class, () -> servico.despachar(null, "CH-1042", "hc-pe"));
        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.despachar(admin.getId(), "CH-1042", "hc-pe"));
        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.receber(admin.getId(), UUID.randomUUID()));
    }
}
