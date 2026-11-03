package com.rotavital.servico;

import java.util.UUID;

import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Trajeto;

/**
 * Resultado da rota do doador até o hospital da campanha (PI3-262). O trajeto é nulo quando o
 * doador não informou onde está (negou a localização): a tela ainda abre o Google Maps com o destino.
 */
public record RotaAteHospital(UUID campanhaId, Hospital hospital, Trajeto trajeto)
{
}
