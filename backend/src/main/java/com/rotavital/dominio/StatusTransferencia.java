package com.rotavital.dominio;

public enum StatusTransferencia
{
    EM_TRANSITO,
    RECEBIDA,
    // A bolsa foi descartada no caminho (cadeia fria rompida): a transferência termina sem chegada.
    CANCELADA
}
