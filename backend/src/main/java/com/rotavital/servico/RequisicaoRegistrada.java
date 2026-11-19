package com.rotavital.servico;

import java.time.OffsetDateTime;
import java.util.List;

import com.rotavital.api.dto.requisicao.NivelUrgencia;
import com.rotavital.dominio.AlaHospitalar;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Resultado de uma requisição atendida (HU-04): as bolsas reservadas pelo FEFO, cada uma com o lugar
 * de onde deve ser retirada no hemocentro e a localização nova (o destino do pedido).
 */
public record RequisicaoRegistrada(
        String protocolo,
        BancoDeSangue banco,
        Hospital hospital,
        AlaHospitalar ala,
        String leito,
        String destino,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        int quantidade,
        NivelUrgencia urgencia,
        String paciente,
        String solicitante,
        OffsetDateTime registradaEm,
        List<BolsaAlocada> bolsas
)
{
    /** Bolsa reservada e onde ela estava no hemocentro antes da reserva (câmara, prateleira, nível). */
    public record BolsaAlocada(BolsaHemocomponente bolsa, String retirarDe)
    {
    }
}
