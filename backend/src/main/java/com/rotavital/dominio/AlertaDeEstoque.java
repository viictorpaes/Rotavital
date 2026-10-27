package com.rotavital.dominio;

import java.time.LocalDate;

/**
 * Aviso da caixa de alertas do painel (HU-02) sobre uma bolsa do estoque.
 * Uma bolsa pode gerar dois alertas (ex.: vence em 3 dias e está fora da faixa).
 */
public record AlertaDeEstoque(
        String bolsaId,
        TipoComponente tipoComponente,
        TipoSanguineo tipoSanguineo,
        LocalDate dataValidade,
        Tipo tipo,
        Nivel nivel,
        String mensagem)
{
    public enum Tipo
    {
        VENCIDA,
        VENCE_EM_BREVE,
        FORA_DA_FAIXA
    }

    // Na ordem de prioridade: os críticos aparecem primeiro no painel.
    public enum Nivel
    {
        CRITICO,
        ATENCAO
    }
}
