package com.rotavital.dominio;

/**
 * Ala do hospital para onde vai a bolsa de uma requisição (HU-04). Os valores batem com a
 * constraint ck_requisicao_ala da tabela requisicao_hospitalar.
 */
public enum AlaHospitalar
{
    UTI_ADULTO("UTI Adulto"),
    UTI_NEONATAL("UTI Neonatal"),
    CENTRO_CIRURGICO("Centro Cirúrgico"),
    EMERGENCIA("Emergência"),
    ONCOLOGIA("Oncologia"),
    HEMODIALISE("Hemodiálise"),
    MATERNIDADE("Maternidade");

    /** Separa hospital, ala e leito na localização da bolsa ("Hospital X · UTI Adulto · Leito 04"). */
    public static final String SEPARADOR = " · ";

    private final String rotulo;

    AlaHospitalar(String rotulo)
    {
        this.rotulo = rotulo;
    }

    public String getRotulo()
    {
        return rotulo;
    }

    /**
     * Onde a bolsa vai ficar no hospital, no formato que a tela de Estoque mostra no cartão do lote:
     * "Hospital das Clínicas · UTI Adulto · Leito 04". O leito é opcional.
     */
    public String localizacaoNo(Hospital hospital, String leito)
    {
        String destino = hospital.getNome() + SEPARADOR + rotulo;

        if (leito == null || leito.isBlank())
        {
            return destino;
        }

        return destino + SEPARADOR + "Leito " + leito.strip();
    }
}
