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
        // TDD: só a assinatura, para o teste compilar e falhar. A regra entra no próximo commit.
        return false;
    }
}
