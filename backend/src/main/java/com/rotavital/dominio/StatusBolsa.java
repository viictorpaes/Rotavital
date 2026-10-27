package com.rotavital.dominio;

public enum StatusBolsa
{
    DISPONIVEL,
    RESERVADA,
    EM_TRANSITO,
    ENTREGUE,
    DESCARTADA,
    // Transfundida em um procedimento (ENTREGUE é só a chegada ao hospital) — já aceito pelo banco (docs/DER.md, decisão 4).
    UTILIZADA;

    // Caminho da bolsa no estoque (HU-08). UTILIZADA e DESCARTADA são finais: a bolsa não volta.
    public boolean podePassarPara(StatusBolsa novo)
    {
        return switch (this)
        {
            case DISPONIVEL -> novo == RESERVADA || novo == DESCARTADA;
            // Volta a DISPONIVEL quando a reserva é cancelada; UTILIZADA quando é usada no próprio hospital.
            case RESERVADA -> novo == DISPONIVEL || novo == EM_TRANSITO || novo == UTILIZADA || novo == DESCARTADA;
            // DESCARTADA em trânsito: cadeia fria rompida no caminho.
            case EM_TRANSITO -> novo == ENTREGUE || novo == DESCARTADA;
            case ENTREGUE -> novo == UTILIZADA || novo == DESCARTADA;
            case UTILIZADA, DESCARTADA -> false;
        };
    }
}