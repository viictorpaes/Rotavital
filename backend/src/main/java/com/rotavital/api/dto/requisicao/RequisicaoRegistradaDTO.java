package com.rotavital.api.dto.requisicao;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.rotavital.dominio.AlaHospitalar;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.StatusRequisicao;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.servico.RequisicaoRegistrada;

/**
 * Resposta de POST /requisicoes (HU-04): o pedido aceito, o destino físico e as bolsas reservadas,
 * com o lugar de onde retirar cada uma no hemocentro (retirarDe) e a localização nova (destino).
 */
public record RequisicaoRegistradaDTO(
        String protocolo,
        String bancoId,
        String hospitalId,
        String hospitalNome,
        AlaHospitalar ala,
        String alaRotulo,
        String leito,
        String destino,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        int quantidade,
        NivelUrgencia urgencia,
        String paciente,
        String solicitanteNome,
        StatusRequisicao status,
        OffsetDateTime registradaEm,
        List<BolsaAlocadaDTO> bolsas
)

{
    public record BolsaAlocadaDTO(
            String id,
            String loteSintetico,
            TipoSanguineo tipoSanguineo,
            LocalDate dataValidade,
            String retirarDe,
            String localizacao,
            StatusBolsa status
    )
    {
    }

    public static RequisicaoRegistradaDTO de(RequisicaoRegistrada requisicao)
    {
        List<BolsaAlocadaDTO> bolsas = requisicao.bolsas().stream()
                .map(alocada -> new BolsaAlocadaDTO(
                        alocada.bolsa().getId(),
                        alocada.bolsa().getLoteSintetico(),
                        alocada.bolsa().getTipoSanguineo(),
                        alocada.bolsa().getDataValidade(),
                        alocada.retirarDe(),
                        alocada.bolsa().getLocalizacao(),
                        alocada.bolsa().getStatus()))
                .toList();

        return new RequisicaoRegistradaDTO(
                requisicao.protocolo(),
                requisicao.banco().getId(),
                requisicao.hospital().getId(),
                requisicao.hospital().getNome(),
                requisicao.ala(),
                requisicao.ala().getRotulo(),
                requisicao.leito(),
                requisicao.destino(),
                requisicao.tipoComponente(),
                requisicao.tipoSanguineo(),
                requisicao.quantidade(),
                requisicao.urgencia(),
                requisicao.paciente(),
                requisicao.solicitante(),
                StatusRequisicao.ALOCADA,
                requisicao.registradaEm(),
                bolsas);
    }
}
