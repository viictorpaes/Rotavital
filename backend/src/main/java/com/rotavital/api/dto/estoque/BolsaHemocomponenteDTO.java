package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record BolsaHemocomponenteDTO(
        String id,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataColeta,
        LocalDate dataValidade,
        String loteSintetico,
        double volumeMl,
        double temperaturaCelsius,
        String localizacao,
        boolean foraDaFaixa,
        StatusBolsa status,
        String bancoOrigemId
)

{
        // Mesmo formato em toda resposta que traz bolsas (estoque, consulta, sugestão de lote).
        public static BolsaHemocomponenteDTO de(BolsaHemocomponente bolsa)
        {
                return new BolsaHemocomponenteDTO(
                        bolsa.getId(),
                        bolsa.getTipoComponente(),
                        bolsa.getTipoSanguineo(),
                        bolsa.getDataColeta(),
                        bolsa.getDataValidade(),
                        bolsa.getLoteSintetico(),
                        bolsa.getVolumeMl(),
                        bolsa.getTemperaturaCelsius(),
                        bolsa.getLocalizacao(),
                        bolsa.estaForaDaFaixa(),
                        bolsa.getStatus(),
                        bolsa.getBancoOrigem().getId()
                );
        }
}