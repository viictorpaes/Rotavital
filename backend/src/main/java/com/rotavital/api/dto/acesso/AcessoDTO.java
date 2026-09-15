package com.rotavital.api.dto.acesso;

import java.util.List;

public record AcessoDTO(
        String nome,
        TipoAcesso tipoAcesso,
        String telaInicial,
        List<String> menu
)

{

}
