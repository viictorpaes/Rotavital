package com.rotavital.api.dto.acesso;

import java.util.List;
import java.util.UUID;

// telaInicial é a rota do frontend para onde o perfil vai depois do login (ex.: /painel).
// accessToken é o token do Supabase Auth; a API ainda não o exige (decisão 6 de docs/PERFIS_DE_ACESSO.md).
public record AcessoDTO(
        UUID id,
        String nome,
        TipoAcesso tipoAcesso,
        String telaInicial,
        List<String> menu,
        String accessToken,
        long expiraEmSegundos
)

{

}
