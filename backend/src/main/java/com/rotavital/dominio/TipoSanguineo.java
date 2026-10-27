package com.rotavital.dominio;

public enum TipoSanguineo
{
    // antígeno A, antígeno B, fator Rh — o que a compatibilidade ABO/Rh compara.
    A_POSITIVO(true, false, true),
    A_NEGATIVO(true, false, false),
    B_POSITIVO(false, true, true),
    B_NEGATIVO(false, true, false),
    AB_POSITIVO(true, true, true),
    AB_NEGATIVO(true, true, false),
    O_POSITIVO(false, false, true),
    O_NEGATIVO(false, false, false);

    private final boolean antigenoA;
    private final boolean antigenoB;
    private final boolean rhPositivo;

    TipoSanguineo(boolean antigenoA, boolean antigenoB, boolean rhPositivo)
    {
        this.antigenoA = antigenoA;
        this.antigenoB = antigenoB;
        this.rhPositivo = rhPositivo;
    }

    public boolean temAntigenoA()
    {
        return antigenoA;
    }

    public boolean temAntigenoB()
    {
        return antigenoB;
    }

    public boolean isRhPositivo()
    {
        return rhPositivo;
    }
}