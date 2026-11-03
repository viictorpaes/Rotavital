package com.rotavital.api.dto.rota;

import java.util.UUID;

import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Trajeto;
import com.rotavital.servico.RotaAteHospital;

/**
 * Resposta do POST /campanhas/{id}/rota (PI3-262): o hospital do paciente, com as coordenadas
 * cadastradas (o destino do Google Maps e do Waze), e o trajeto a partir do doador.
 * trajeto é nulo quando o doador não mandou a localização.
 */
public record RotaAteHospitalDTO(UUID campanhaId, DestinoDTO destino, TrajetoDTO trajeto)
{
    public static RotaAteHospitalDTO de(RotaAteHospital rota)
    {
        return new RotaAteHospitalDTO(rota.campanhaId(), DestinoDTO.de(rota.hospital()), TrajetoDTO.de(rota.trajeto()));
    }

    public record DestinoDTO(
            String hospitalId,
            String nome,
            String logradouro,
            String cidade,
            String uf,
            double latitude,
            double longitude
    )
    {
        static DestinoDTO de(Hospital hospital)
        {
            Endereco endereco = hospital.getEndereco();
            return new DestinoDTO(hospital.getId(), hospital.getNome(), endereco.getLogradouro(), endereco.getCidade(),
                    endereco.getUf(), hospital.getLatitude(), hospital.getLongitude());
        }
    }

    // distanciaKm com uma casa decimal (100 m): mais precisão que isso não ajuda o doador.
    public record TrajetoDTO(double distanciaKm, int duracaoMinutos, FonteDoTrajeto fonte)
    {
        static TrajetoDTO de(Trajeto trajeto)
        {
            if (trajeto == null)
            {
                return null;
            }
            return new TrajetoDTO(Math.round(trajeto.distanciaKm() * 10) / 10.0, trajeto.duracaoMinutos(), trajeto.fonte());
        }
    }
}
