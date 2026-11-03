package com.rotavital.dominio;

/**
 * Quanto falta do doador até o hospital da campanha (PI3-262): distância de carro, tempo
 * estimado de chegada e de onde vieram esses números.
 */
public record Trajeto(double distanciaKm, int duracaoMinutos, FonteDoTrajeto fonte)
{
    /**
     * As ruas não seguem a linha reta: em cidade, o caminho de carro costuma ser 20% a 40% maior.
     * 1,3 fica no meio dessa faixa.
     */
    public static final double FATOR_DE_DESVIO_DAS_RUAS = 1.3;

    // Velocidade média de carro no trânsito urbano do Recife, com semáforos e horário de pico.
    public static final double VELOCIDADE_MEDIA_KMH = 25.0;

    public Trajeto
    {
        if (distanciaKm < 0 || duracaoMinutos < 0)
        {
            throw new IllegalArgumentException("Distância e duração do trajeto não podem ser negativas.");
        }
        if (fonte == null)
        {
            throw new IllegalArgumentException("Informe a fonte do trajeto.");
        }
    }

    /**
     * Estimativa sem serviço de mapas: linha reta × desvio das ruas, na velocidade média urbana.
     * Arredonda o tempo para cima, para o doador não chegar atrasado.
     */
    public static Trajeto estimadoEntre(Coordenada origem, Coordenada destino)
    {
        double distanciaKm = origem.distanciaEmLinhaRetaKm(destino) * FATOR_DE_DESVIO_DAS_RUAS;
        int minutos = (int) Math.ceil(distanciaKm / VELOCIDADE_MEDIA_KMH * 60);
        return new Trajeto(distanciaKm, minutos, FonteDoTrajeto.ESTIMATIVA);
    }
}
