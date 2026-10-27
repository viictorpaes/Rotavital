package com.rotavital.dominio;

// AGENDADO é o único status em aberto: os outros três encerram o agendamento.
public enum StatusAgendamento
{
    AGENDADO,
    REALIZADO,
    CANCELADO,
    FALTOU
}
