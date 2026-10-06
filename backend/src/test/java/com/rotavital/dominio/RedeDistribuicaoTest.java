package com.rotavital.dominio;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Montagem do grafo com as conexões do banco (uma linha por sentido) e o Dijkstra sobre ele.
public class RedeDistribuicaoTest
{
    private BancoDeSangue banco;
    private Hospital hospitalA;
    private Hospital hospitalB;
    private RedeDistribuicao rede;

    @BeforeEach
    public void criarPontos()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
        hospitalA = new Hospital("HOSP-A", "Hospital A", new Endereco("Recife/PE", -8.0476, -34.8770));
        hospitalB = new Hospital("HOSP-B", "Hospital B", new Endereco("Recife/PE", -8.0300, -34.9000));

        rede = new RedeDistribuicao();
        rede.adicionarPonto(banco);
        rede.adicionarPonto(hospitalA);
        rede.adicionarPonto(hospitalB);
    }

    // Ida e volta, como o seed grava na tabela conexao.
    private void conectarNosDoisSentidos(PontoDeRede origem, PontoDeRede destino, double km, double min)
    {
        rede.adicionarConexaoDirigida(new Conexao(origem, destino, km, min));
        rede.adicionarConexaoDirigida(new Conexao(destino, origem, km, min));
    }

    @Test
    public void conexaoDirigidaCriaUmaArestaEConexaoComumCriaDuas()
    {
        rede.adicionarConexaoDirigida(new Conexao(banco, hospitalA, 5.0, 10.0));
        Assertions.assertEquals(1, rede.getConexoes().size());

        rede.adicionarConexao(banco, hospitalB, 6.0, 12.0);
        Assertions.assertEquals(3, rede.getConexoes().size());
    }

    @Test
    public void calculaARotaMinimaComAsConexoesDoBanco()
    {
        conectarNosDoisSentidos(banco, hospitalA, 5.0, 10.0);
        conectarNosDoisSentidos(hospitalA, hospitalB, 2.0, 5.0);
        conectarNosDoisSentidos(banco, hospitalB, 9.0, 15.0);

        RotaCalculada rota = rede.calcularRotaMinima("BS-01", "HOSP-B", null);

        Assertions.assertEquals(List.of(banco, hospitalA, hospitalB), rota.getNos());
        Assertions.assertEquals(7.0, rota.getDistanciaTotalKm());
        Assertions.assertEquals(15.0, rota.getTempoEstimadoMin());
        Assertions.assertTrue(rota.isDentroDaJanela());

        // A volta usa as linhas do sentido contrário.
        RotaCalculada volta = rede.calcularRotaMinima("HOSP-B", "BS-01", null);
        Assertions.assertEquals(List.of(hospitalB, hospitalA, banco), volta.getNos());
    }

    @Test
    public void conexaoSoDeIdaNaoPermiteAVolta()
    {
        rede.adicionarConexaoDirigida(new Conexao(banco, hospitalA, 5.0, 10.0));

        Assertions.assertEquals(5.0, rede.calcularRotaMinima("BS-01", "HOSP-A", null).getDistanciaTotalKm());
        Assertions.assertThrows(IllegalStateException.class,
                () -> rede.calcularRotaMinima("HOSP-A", "BS-01", null));
    }

    @Test
    public void reconheceOMesmoPontoCarregadoEmOutroObjeto()
    {
        // As conexões apontam para cópias dos pontos (mesmo id, outro objeto), como pode acontecer com JPA.
        Hospital copiaDoHospitalA = new Hospital("HOSP-A", "Hospital A", new Endereco("Recife/PE", -8.0476, -34.8770));
        conectarNosDoisSentidos(banco, copiaDoHospitalA, 5.0, 10.0);

        RotaCalculada rota = rede.calcularRotaMinima("BS-01", "HOSP-A", null);

        Assertions.assertEquals(5.0, rota.getDistanciaTotalKm());
        Assertions.assertEquals(hospitalA, rota.getDestino());
    }

    @Test
    public void pontoInexistenteOuSemCaminhoLancaErro()
    {
        conectarNosDoisSentidos(banco, hospitalA, 5.0, 10.0);

        // A API responde 404 para IllegalArgumentException e 422 para IllegalStateException.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> rede.calcularRotaMinima("BS-01", "NAO-EXISTE", null));
        Assertions.assertThrows(IllegalStateException.class,
                () -> rede.calcularRotaMinima("BS-01", "HOSP-B", null));
    }
}
