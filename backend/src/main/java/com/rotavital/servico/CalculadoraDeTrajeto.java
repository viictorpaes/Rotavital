package com.rotavital.servico;

import java.util.Optional;

import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.Trajeto;

/**
 * Trajeto de carro entre dois pontos, calculado por um serviço de mapas pelas ruas de verdade
 * (hoje, a Routes API do Google: com.rotavital.google.GoogleRoutes).
 *
 * Devolve vazio quando o serviço não está configurado, não responde ou não acha caminho:
 * quem chama decide o plano B, para a tela do doador nunca ficar sem resposta.
 */
public interface CalculadoraDeTrajeto
{
    Optional<Trajeto> calcular(Coordenada origem, Coordenada destino);
}
