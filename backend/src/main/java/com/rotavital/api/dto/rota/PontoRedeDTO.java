package com.rotavital.api.dto.rota;

public record PontoRedeDTO(
        String id,
        String nome,
        TipoPontoRede tipo,
        double latitude,
        double longitude,
        String logradouro,
        String cidade,
        String uf
)

{
        
}