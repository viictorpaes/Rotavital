package com.rotavital.servico;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.PontoDeRedeBase;
import com.rotavital.dominio.RedeDistribuicao;
import com.rotavital.repositorio.ConexaoRepository;
import com.rotavital.repositorio.PontoDeRedeRepository;

@Service
public class RedeDistribuicaoService
{
    // Bancos de sangue primeiro, depois hospitais; dentro de cada grupo, por id.
    // A ordenação é feita aqui, no Java, para não depender da collation do banco.
    private static final Comparator<PontoDeRedeBase> ORDEM_DOS_PONTOS =
            Comparator.comparing((PontoDeRedeBase ponto) -> !(ponto instanceof BancoDeSangue))
                    .thenComparing(PontoDeRedeBase::getId);

    private final PontoDeRedeRepository pontos;
    private final ConexaoRepository conexoes;

    public RedeDistribuicaoService(PontoDeRedeRepository pontos, ConexaoRepository conexoes)
    {
        this.pontos = pontos;
        this.conexoes = conexoes;
    }

    /**
     * Monta a rede de distribuição com os pontos e as conexões gravados no banco.
     * Os pontos são carregados antes das conexões, na mesma transação: assim as conexões
     * encontram os pontos já carregados e não geram uma consulta por ponto.
     */
    @Transactional(readOnly = true)
    public RedeDistribuicao carregarRede()
    {
        RedeDistribuicao rede = new RedeDistribuicao();

        List<PontoDeRedeBase> pontosOrdenados = pontos.findAll().stream()
                .sorted(ORDEM_DOS_PONTOS)
                .toList();
        pontosOrdenados.forEach(rede::adicionarPonto);

        // A tabela já guarda ida e volta como duas linhas: cada uma entra como uma única aresta.
        for (Conexao conexao : conexoes.findAllByOrderByIdAsc())
        {
            rede.adicionarConexaoDirigida(conexao);
        }

        return rede;
    }
}
