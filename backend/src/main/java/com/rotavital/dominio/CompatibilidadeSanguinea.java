package com.rotavital.dominio;

/**
 * Quem pode receber de quem, pelo sistema ABO e pelo fator Rh, para cada hemocomponente.
 * É a regra que impede uma reação transfusional: um erro aqui põe o paciente em risco.
 */
public final class CompatibilidadeSanguinea
{
    private CompatibilidadeSanguinea()
    {
    }

    public static boolean podeTransfundir(TipoComponente componente, TipoSanguineo doador, TipoSanguineo receptor)
    {
        // O receptor tem anticorpos contra os antígenos ABO que não tem, e quem é Rh- pode criar
        // anticorpos contra o Rh: as hemácias do doador não podem trazer nenhum desses antígenos.
        boolean aboCompativel = (!doador.temAntigenoA() || receptor.temAntigenoA())
                && (!doador.temAntigenoB() || receptor.temAntigenoB());
        boolean rhCompativel = !doador.isRhPositivo() || receptor.isRhPositivo();

        return aboCompativel && rhCompativel;
    }
}
