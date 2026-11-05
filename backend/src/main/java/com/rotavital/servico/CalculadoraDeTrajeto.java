package com.rotavital.servico;

import java.util.List;
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

    /**
     * Trajeto passando pelas paradas, na ordem, com o traçado para desenhar no mapa (PI3-259).
     * Quem não sabe traçar não responde: quem chama usa o plano B.
     */
    default Optional<Trajeto> calcularComTracado(List<Coordenada> paradas)
    {
        return Optional.empty();
    }
}
