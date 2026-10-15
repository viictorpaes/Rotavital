package com.rotavital.dominio;

public enum TipoComponente
{
    // Prazo máximo entre coleta e validade (RDC ANVISA 34/2014, o mesmo da constraint
    // ck_bolsa_prazo_maximo_por_componente). Cada componente tem uma sigla e uma faixa de mil ids:
    // hemácias vão de CH-1001 a CH-1999, plasma de PF-2001 a PF-2999, e assim por diante.
    HEMACIAS(2.0, 6.0, 42, "CH", 1000),
    PLASMA(-30.0, -18.0, 730, "PF", 2000),
    PLAQUETAS(20.0, 24.0, 7, "PQ", 3000),
    CRIOPRECIPITADO(-30.0, -18.0, 365, "CR", 4000);

    private final double temperaturaMinima;
    private final double temperaturaMaxima;
    private final int prazoMaximoDias;
    private final String sigla;
    private final int inicioDaFaixaDeIds;

    TipoComponente(double temperaturaMinima, double temperaturaMaxima, int prazoMaximoDias,
    String sigla, int inicioDaFaixaDeIds)
    {
        this.temperaturaMinima = temperaturaMinima;
        this.temperaturaMaxima = temperaturaMaxima;
        this.prazoMaximoDias = prazoMaximoDias;
        this.sigla = sigla;
        this.inicioDaFaixaDeIds = inicioDaFaixaDeIds;
    }

    public double getTemperaturaMinima()
    {
        return temperaturaMinima;
    }

    public double getTemperaturaMaxima()
    {
        return temperaturaMaxima;
    }

    public int getPrazoMaximoDias()
    {
        return prazoMaximoDias;
    }

    public String getSigla()
    {
        return sigla;
    }

    public int getInicioDaFaixaDeIds()
    {
        return inicioDaFaixaDeIds;
    }
}
