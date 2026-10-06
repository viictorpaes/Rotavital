package com.rotavital.servico;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PontoDeRede;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.dominio.RedeDistribuicao;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;

// Montagem da rede a partir do que os repositórios devolvem (no app, as tabelas ponto_rede e conexao).
public class RedeDistribuicaoServiceTest
{
    private final BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));
    private final Hospital hospital01 = new Hospital("HOSP-01", "Hospital das Clínicas", new Endereco("Recife/PE", -8.0476, -34.8770));
    private final Hospital hcPe = new Hospital("hc-pe", "HC-UFPE", new Endereco("Recife/PE", -8.0490, -34.9470));

    private RedeDistribuicaoService servico(List<PontoDeRedeBase> pontos, List<Conexao> conexoes)
    {
        PontoDeRedeRepository pontosFalsos = RepositorioFalso.respondendo(
                PontoDeRedeRepository.class, "findAll", argumentos -> pontos);
        ConexaoRepository conexoesFalsas = RepositorioFalso.respondendo(
                ConexaoRepository.class, "findAllByOrderByIdAsc", argumentos -> conexoes);

        return new RedeDistribuicaoService(pontosFalsos, conexoesFalsas);
    }

    @Test
    public void pontosFicamComOsBancosDeSangueNaFrenteEDepoisPorId()
    {
        RedeDistribuicao rede = servico(List.of(hcPe, banco, hospital01), List.of()).carregarRede();

        List<String> ids = rede.getPontos().stream().map(PontoDeRede::getId).toList();
        Assertions.assertEquals(List.of("BS-01", "HOSP-01", "hc-pe"), ids);
    }

    @Test
    public void cadaLinhaDeConexaoViraUmaUnicaAresta()
    {
        // Duas ligações, cada uma gravada como ida e volta: 4 linhas na tabela.
        List<Conexao> linhas = List.of(
                new Conexao(banco, hospital01, 3.1, 9.0),
                new Conexao(hospital01, banco, 3.1, 9.0),
                new Conexao(banco, hcPe, 7.8, 18.0),
                new Conexao(hcPe, banco, 7.8, 18.0));

        RedeDistribuicao rede = servico(List.of(banco, hospital01, hcPe), linhas).carregarRede();

        Assertions.assertEquals(4, rede.getConexoes().size());
        Assertions.assertEquals(7.8, rede.calcularRotaMinima("BS-01", "hc-pe", null).getDistanciaTotalKm());
        Assertions.assertEquals(18.0, rede.calcularRotaMinima("hc-pe", "BS-01", null).getTempoEstimadoMin());
    }
}
