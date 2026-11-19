package com.rotavital.api.dto.rota;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.TransferenciaDeBolsa;
import com.rotavital.dominio.Usuario;

/** Resposta de /transferencias (PI3-236): a bolsa, de onde saiu, para onde vai e em que pé está. */
public record TransferenciaDTO(
        UUID id,
        String bolsaId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        String origemId,
        String origemNome,
        String destinoId,
        String destinoNome,
        StatusTransferencia status,
        double distanciaKm,
        int duracaoMinutos,
        FonteDoTrajeto fonte,
        OffsetDateTime despachadaEm,
        OffsetDateTime chegadaPrevistaEm,
        OffsetDateTime recebidaEm,
        String despachadaPorNome,
        String recebidaPorNome,
        String localizacaoDaBolsa
)
{
    public static TransferenciaDTO de(TransferenciaDeBolsa transferencia)
    {
        BolsaHemocomponente bolsa = transferencia.getBolsa();

        return new TransferenciaDTO(
                transferencia.getId(),
                bolsa.getId(),
                bolsa.getTipoComponente(),
                bolsa.getTipoSanguineo(),
                transferencia.getOrigem().getId(),
                transferencia.getOrigem().getNome(),
                transferencia.getDestino().getId(),
                transferencia.getDestino().getNome(),
                transferencia.getStatus(),
                Math.round(transferencia.getDistanciaKm() * 10) / 10.0,
                transferencia.getDuracaoMinutos(),
                transferencia.getFonteDoTrajeto(),
                transferencia.getDespachadaEm(),
                transferencia.getChegadaPrevistaEm(),
                transferencia.getRecebidaEm(),
                nomeDe(transferencia.getDespachadaPor()),
                nomeDe(transferencia.getRecebidaPor()),
                bolsa.getLocalizacao());
    }

    private static String nomeDe(Usuario usuario)
    {
        return usuario == null ? null : usuario.getNome();
    }
}
