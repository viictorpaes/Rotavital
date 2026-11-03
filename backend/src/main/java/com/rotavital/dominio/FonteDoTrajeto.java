package com.rotavital.dominio;

// De onde vieram a distância e o tempo da rota até o hospital (PI3-262).
public enum FonteDoTrajeto
{
    // Routes API do Google: caminho pelas ruas, com o trânsito do momento.
    GOOGLE_ROUTES,
    // Plano B, sem o Google: linha reta corrigida pelo desvio das ruas e velocidade média urbana.
    ESTIMATIVA
}
