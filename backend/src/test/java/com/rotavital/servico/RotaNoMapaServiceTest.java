package com.rotavital.servico;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.PontoDeRede;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Rota de transporte para o mapa (PI3-259), com o banco e o Google falsos.
public class RotaNoMapaServiceTest
{
    private final BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital barao = new Hospital("barao-lucena", "Hospital Barão de Lucena",
            new Endereco("Av. Caxangá, 3860", -8.0432, -34.9331));
    private final Hospital clinicas = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final Hospital portugues = new Hospital("real-portugues", "Real Hospital Português",
            new Endereco("Av. Agamenon Magalhães, 4760", -8.0479, -34.8993));
    private final Hospital isolado = new Hospital("isolado", "Hospital sem conexão",
            new Endereco("Rua Sem Saída, 1", -8.0, -34.9));

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);
    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João Doador", PapelUsuario.DOADOR, null, null);

    private final List<List<Coordenada>> pedidosAoGoogle = new ArrayList<>();
    private Optional<Trajeto> respostaDoGoogle = Optional.empty();
    private RotaNoMapaService servico;

    @BeforeEach
    public void montarServico()
    {
        // Ida e volta, como no banco. O caminho direto Hemope → Clínicas (7,8 km) é mais longo que
        // passar pelo Barão de Lucena (4,3 km). O Português só se liga à rede pelo Hemope.
        List<Conexao> conexoes = List.of(
                new Conexao(hemope, clinicas, 7.8, 18.0),
                new Conexao(clinicas, hemope, 7.8, 18.0),
                new Conexao(hemope, barao, 1.9, 6.0),
                new Conexao(barao, hemope, 1.9, 6.0),
                new Conexao(barao, clinicas, 2.4, 6.5),
                new Conexao(clinicas, barao, 2.4, 6.5),
                new Conexao(hemope, portugues, 2.1, 7.0),
                new Conexao(portugues, hemope, 2.1, 7.0));

        PontoDeRedeRepository pontos = RepositorioFalso.respondendo(PontoDeRedeRepository.class, "findAll",
                argumentos -> List.<PontoDeRedeBase>of(hemope, barao, clinicas, portugues, isolado));
        ConexaoRepository repositorioDeConexoes = RepositorioFalso.respondendo(ConexaoRepository.class,
                "findAllByOrderByIdAsc", argumentos -> conexoes);
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico
                        : doador.getId().equals(argumentos[0]) ? doador : null));

        CalculadoraDeTrajeto google = new CalculadoraDeTrajeto()
        {
            @Override
            public Optional<Trajeto> calcular(Coordenada origem, Coordenada destino)
            {
                throw new AssertionError("A rota do mapa deveria pedir o traçado.");
            }

            @Override
            public Optional<Trajeto> calcularComTracado(List<Coordenada> paradas)
            {
                pedidosAoGoogle.add(paradas);
                return respostaDoGoogle;
            }
        };

        servico = new RotaNoMapaService(new RedeDistribuicaoService(pontos, repositorioDeConexoes), google,
                new IdentificacaoService(usuarios));
    }

    private static List<String> ids(RotaNoMapa rota)
    {
        return rota.caminho().getNos().stream().map(PontoDeRede::getId).toList();
    }

    @Test
    public void oDijkstraEscolheOCaminhoEOGooglePassaPelasMesmasParadas()
    {
        respostaDoGoogle = Optional.of(new Trajeto(9.4, 21, FonteDoTrajeto.GOOGLE_ROUTES, "abc"));

        RotaNoMapa rota = servico.consultar(medico.getId(), "BS-01", "hc-pe");

        Assertions.assertEquals(List.of("BS-01", "barao-lucena", "hc-pe"), ids(rota));
        Assertions.assertEquals(List.of(new Coordenada(-8.0578, -34.8829), new Coordenada(-8.0432, -34.9331),
                new Coordenada(-8.0512, -34.9478)), pedidosAoGoogle.get(0));
        Assertions.assertEquals(new Trajeto(9.4, 21, FonteDoTrajeto.GOOGLE_ROUTES, "abc"), rota.trajeto());
    }

    @Test
    public void semOGoogleValemADistanciaEOTempoDasConexoes()
    {
        RotaNoMapa rota = servico.consultar(medico.getId(), "BS-01", "hc-pe");

        Assertions.assertEquals(4.3, rota.trajeto().distanciaKm(), 0.0001);
        // 12,5 min somando as conexões: arredonda para cima.
        Assertions.assertEquals(13, rota.trajeto().duracaoMinutos());
        Assertions.assertEquals(FonteDoTrajeto.CONEXOES_DA_REDE, rota.trajeto().fonte());
        Assertions.assertNull(rota.trajeto().tracado());
    }

    @Test
    public void rotaEntreDoisHospitaisPassaPeloQueEstaNoCaminho()
    {
        RotaNoMapa rota = servico.consultar(medico.getId(), "hc-pe", "real-portugues");

        Assertions.assertEquals(List.of("hc-pe", "barao-lucena", "BS-01", "real-portugues"), ids(rota));
        Assertions.assertEquals(6.4, rota.trajeto().distanciaKm(), 0.0001);
    }

    @Test
    public void pontoInexistenteRecebe404()
    {
        Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.consultar(medico.getId(), "BS-01", "nao-existe"));
    }

    @Test
    public void pontoSemCaminhoRecebe422()
    {
        Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.consultar(medico.getId(), "BS-01", "isolado"));
    }

    @Test
    public void origemIgualAoDestinoOuAusenteRecebe400()
    {
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.consultar(medico.getId(), "BS-01", "BS-01"));
        Assertions.assertThrows(DadosInvalidosException.class, () -> servico.consultar(medico.getId(), null, "hc-pe"));
    }

    @Test
    public void soQuemUsaATelaDeRedeConsomeACotaDoGoogle()
    {
        Assertions.assertThrows(UsuarioNaoIdentificadoException.class, () -> servico.consultar(null, "BS-01", "hc-pe"));
        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.consultar(doador.getId(), "BS-01", "hc-pe"));
        Assertions.assertTrue(pedidosAoGoogle.isEmpty());
    }
}
