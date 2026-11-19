package com.rotavital.servico;

import com.rotavital.dominio.RotaCalculada;
import com.rotavital.dominio.Trajeto;

/**
 * Rota de transporte entre dois pontos da rede (PI3-259): o menor caminho pelo Dijkstra, que diz por
 * onde passar, e o trajeto, que diz a distância, o tempo e como desenhar esse caminho no mapa.
 */
public record RotaNoMapa(RotaCalculada caminho, Trajeto trajeto)
{
}
