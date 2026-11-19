package com.rotavital.api.dto.rota;

import java.util.List;

import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.PontoDeRede;
import com.rotavital.dominio.Trajeto;
import com.rotavital.servico.RotaNoMapa;

/**
 * Resposta de GET /rotas/trajeto (PI3-259): os pontos do menor caminho, na ordem, e os números do
 * trajeto. polyline é o traçado do Google pelas ruas; nulo quando a rota não passou por ele.
 */
public record RotaNoMapaDTO(
        String origemId,
        String destinoId,
        List<String> nos,
        double distanciaKm,
        int duracaoMinutos,
        FonteDoTrajeto fonte,
        String polyline
)
{
    public static RotaNoMapaDTO de(RotaNoMapa rota)
    {
        Trajeto trajeto = rota.trajeto();
        List<String> nos = rota.caminho().getNos().stream().map(PontoDeRede::getId).toList();

        return new RotaNoMapaDTO(rota.caminho().getOrigem().getId(), rota.caminho().getDestino().getId(), nos,
                Math.round(trajeto.distanciaKm() * 10) / 10.0, trajeto.duracaoMinutos(), trajeto.fonte(), trajeto.tracado());
    }
}
