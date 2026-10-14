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
        return switch (componente)
        {
            // Plaquetas têm hemácias residuais: seguem a regra mais conservadora, a das hemácias.
            case HEMACIAS, PLAQUETAS -> hemaciasCompativeis(doador, receptor);
            case PLASMA, CRIOPRECIPITADO -> plasmaCompativel(doador, receptor);
        };
    }

    // O receptor tem anticorpos contra os antígenos ABO que não tem, e quem é Rh- pode criar
    // anticorpos contra o Rh: as hemácias do doador não podem trazer nenhum desses antígenos.
    private static boolean hemaciasCompativeis(TipoSanguineo doador, TipoSanguineo receptor)
    {
        boolean aboCompativel = (!doador.temAntigenoA() || receptor.temAntigenoA())
                && (!doador.temAntigenoB() || receptor.temAntigenoB());
        boolean rhCompativel = !doador.isRhPositivo() || receptor.isRhPositivo();

        return aboCompativel && rhCompativel;
    }

    // O plasma traz os anticorpos do doador: ele não pode ter anticorpo contra antígeno do receptor,
    // ou seja, o doador precisa ter todos os antígenos ABO do receptor. O Rh não conta.
    private static boolean plasmaCompativel(TipoSanguineo doador, TipoSanguineo receptor)
    {
        return (!receptor.temAntigenoA() || doador.temAntigenoA())
                && (!receptor.temAntigenoB() || doador.temAntigenoB());
    }
}
