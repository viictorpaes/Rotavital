package com.rotavital.dominio;

/**
 * Motivo padronizado do descarte (PI3-243), para contar as perdas por motivo. Uma observação em
 * texto livre pode acompanhar qualquer um deles e é obrigatória em OUTRO.
 */
public enum CategoriaDescarte
{
    VALIDADE_EXPIRADA("Validade expirada"),
    TEMPERATURA_FORA_DO_PADRAO("Temperatura fora do padrão"),
    BOLSA_DANIFICADA("Bolsa danificada"),
    OUTRO("Outro");

    private final String rotulo;

    CategoriaDescarte(String rotulo)
    {
        this.rotulo = rotulo;
    }

    public String getRotulo()
    {
        return rotulo;
    }
}
