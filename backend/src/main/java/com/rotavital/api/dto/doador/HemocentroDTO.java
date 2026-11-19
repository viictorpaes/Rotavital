package com.rotavital.api.dto.doador;

// Hemocentro onde o doador marca a doação (PI3-205): endereço, posição no mapa e expediente.
public record HemocentroDTO(
        String id,
        String nome,
        String logradouro,
        String cidade,
        String uf,
        double latitude,
        double longitude,
        String atendimento,
        int vagasPorHorario
)
{
}
