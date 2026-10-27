package com.rotavital.api.dto.estoque;

import java.util.List;

import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Resposta de GET /bancos/{bancoId}/sugestao-lote (HU-04). "faltam" > 0 quando o estoque
// não tem bolsas aptas e compatíveis suficientes: a tela avisa em vez de sugerir menos calada.
public record SugestaoDeLoteDTO(
        String bancoDeSangueId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineoPaciente,
        int quantidadeSolicitada,
        int quantidadeSugerida,
        int faltam,
        List<BolsaHemocomponenteDTO> lotes
)

{
        
}
