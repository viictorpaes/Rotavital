package com.rotavital.api.dto.estoque;

import java.time.LocalDate;
import java.util.List;

import com.rotavital.dominio.ResumoDePerdas;

/**
 * Resposta de GET /api/v1/bancos/{bancoId}/estoque/perdas. Os quatro primeiros campos são os de
 * antes do PI3-243; os demais trazem o período, a taxa de perda, o tipo com mais descartes e os
 * motivos. Rankings ordenados por quantidade de bolsas (maior primeiro).
 */
public record ResumoPerdasDTO(
        long totalBolsas,
        double totalVolumeMl,
        List<TotalPerdaDTO> porTipoSanguineo,
        List<TotalPerdaDTO> porComponente,
        Integer periodoDias,
        LocalDate desde,
        List<TotalPerdaDTO> porMotivo,
        List<String> tiposComMaisDescartes,
        long coletadasNoPeriodo,
        long descartadasDasColetadas,
        double taxaDePerda)
{
    public static ResumoPerdasDTO de(ResumoDePerdas resumo, Integer periodoDias)
    {
        return new ResumoPerdasDTO(
                resumo.getTotalBolsas(),
                resumo.getTotalVolumeMl(),
                converter(resumo.getPorTipoSanguineo()),
                converter(resumo.getPorComponente()),
                periodoDias,
                resumo.getDesde(),
                converter(resumo.getPorMotivo()),
                resumo.getTiposComMaisDescartes(),
                resumo.getColetadasNoPeriodo(),
                resumo.getDescartadasDasColetadas(),
                resumo.getTaxaDePerda());
    }

    private static List<TotalPerdaDTO> converter(List<ResumoDePerdas.Total> totais)
    {
        return totais.stream()
                .map(total -> new TotalPerdaDTO(total.categoria(), total.bolsas(), total.volumeMl()))
                .toList();
    }
}
