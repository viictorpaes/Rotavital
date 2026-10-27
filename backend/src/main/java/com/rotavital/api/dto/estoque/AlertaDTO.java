package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.AlertaDeEstoque;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

public record AlertaDTO(
        String bolsaId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataValidade,
        AlertaDeEstoque.Tipo tipo,
        AlertaDeEstoque.Nivel nivel,
        String mensagem
)

{
        public static AlertaDTO de(AlertaDeEstoque alerta)
        {
                return new AlertaDTO(alerta.bolsaId(), alerta.tipoComponente(), alerta.tipoSanguineo(),
                        alerta.dataValidade(), alerta.tipo(), alerta.nivel(), alerta.mensagem());
        }
}
