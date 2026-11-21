package com.rotavital.api.dto.estoque;

import java.time.LocalDate;
import java.util.List;

import com.rotavital.dominio.EstatisticaDescritivaDoEstoque;
import com.rotavital.dominio.TabelaDeFrequencia;

// Resposta de GET /api/v1/bancos/{bancoId}/estoque/estatisticas. Percentuais vêm de 0 a 100, sem arredondar.
public record EstatisticasDoEstoqueDTO(
        String bancoId,
        LocalDate dataDeReferencia,
        long cadastradas,
        long emEstoque,
        long vencidasAindaNoEstoque,
        long descartadas,
        double taxaDePerda,
        DistribuicaoDTO porTipoSanguineo,
        DistribuicaoDTO porComponente,
        DistribuicaoDTO porDiasAteVencer,
        List<EstatisticaDescritivaDoEstoque.PorComponente> componentes)
{
    public record DistribuicaoDTO(long total, List<String> moda, List<TabelaDeFrequencia.Linha> linhas)
    {
        static DistribuicaoDTO de(TabelaDeFrequencia tabela)
        {
            return new DistribuicaoDTO(tabela.total(), tabela.moda(), tabela.linhas());
        }
    }

    public static EstatisticasDoEstoqueDTO de(String bancoId, EstatisticaDescritivaDoEstoque estatistica)
    {
        return new EstatisticasDoEstoqueDTO(
                bancoId,
                estatistica.getDataDeReferencia(),
                estatistica.getCadastradas(),
                estatistica.getEmEstoque(),
                estatistica.getVencidasAindaNoEstoque(),
                estatistica.getDescartadas(),
                estatistica.getTaxaDePerda(),
                DistribuicaoDTO.de(estatistica.getPorTipoSanguineo()),
                DistribuicaoDTO.de(estatistica.getPorComponente()),
                DistribuicaoDTO.de(estatistica.getPorDiasAteVencer()),
                estatistica.getMedidasPorComponente());
    }
}
