package com.rotavital.dominio;

public enum StatusBolsa
{
    DISPONIVEL,
    RESERVADA,
    EM_TRANSITO,
    ENTREGUE,
    DESCARTADA,
    // Transfundida em um procedimento (ENTREGUE é só a chegada ao hospital) — já aceito pelo banco (docs/DER.md, decisão 4).
    UTILIZADA
}