package com.rotavital.dominio;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RedeDistribuicao
{
    private final List<PontoDeRede> pontos;
    private final List<Conexao> conexoes;

    public RedeDistribuicao()
    {
        this.pontos = new ArrayList<>();
        this.conexoes = new ArrayList<>();
    }

    public void adicionarPonto(PontoDeRede ponto)
    {
        pontos.add(ponto);
    }

    public void adicionarConexao(PontoDeRede origem, PontoDeRede destino, double distanciaKm, double tempoEstimadoMin)
    {
        conexoes.add(new Conexao(origem, destino, distanciaKm, tempoEstimadoMin));
        conexoes.add(new Conexao(destino, origem, distanciaKm, tempoEstimadoMin));
    }

    public List<PontoDeRede> getPontos()
    {
        return pontos;
    }

    public List<Conexao> getConexoes()
    {
        return conexoes;
    }

    public RotaCalculada calcularRotaMinima(String origemId, String destinoId, LocalDateTime janelaEntregaLimite)
    {
        PontoDeRede origem = buscarPonto(origemId);
        PontoDeRede destino = buscarPonto(destinoId);

        Map<PontoDeRede, Double> distancias = new HashMap<>();
        Map<PontoDeRede, Double> temposAcumulados = new HashMap<>();
        Map<PontoDeRede, PontoDeRede> predecessores = new HashMap<>();

        for (PontoDeRede ponto : pontos)
        {
            distancias.put(ponto, Double.MAX_VALUE);
            temposAcumulados.put(ponto, 0.0);
        }

        distancias.put(origem, 0.0);

        List<PontoDeRede> visitados = new ArrayList<>();
        PriorityQueue<PontoDeRede> fila = new PriorityQueue<>(
                (a, b) -> Double.compare(distancias.get(a), distancias.get(b)));
        fila.add(origem);

        while (!fila.isEmpty())
        {
            PontoDeRede atual = fila.poll();

            if (visitados.contains(atual))
            {
                continue;
            }

            visitados.add(atual);

            if (atual.equals(destino))
            {
                break;
            }

            for (Conexao conexao : conexoes)
            {
                if (!conexao.getOrigem().equals(atual))
                {
                    continue;
                }

                PontoDeRede vizinho = conexao.getDestino();

                double novaDistancia = distancias.get(atual) + conexao.getDistanciaKm();

                if (novaDistancia < distancias.get(vizinho))
                {
                    distancias.put(vizinho, novaDistancia);
                    temposAcumulados.put(vizinho, temposAcumulados.get(atual) + conexao.getTempoEstimadoMin());
                    predecessores.put(vizinho, atual);
                    fila.add(vizinho);
                }
            }
        }

        if (distancias.get(destino) == Double.MAX_VALUE)
        {
            throw new IllegalStateException("Nenhuma rota encontrada entre " + origemId + " e " + destinoId);
        }

        List<PontoDeRede> caminho = new ArrayList<>();
        PontoDeRede passo = destino;

        while (passo != null)
        {
            caminho.add(0, passo);
            passo = predecessores.get(passo);
        }

        double tempoTotal = temposAcumulados.get(destino);
        boolean dentroDaJanela = janelaEntregaLimite == null
        || !LocalDateTime.now().plusMinutes((long) tempoTotal).isAfter(janelaEntregaLimite);

        return new RotaCalculada(origem, destino, caminho, distancias.get(destino), tempoTotal, dentroDaJanela);
    }

    private PontoDeRede buscarPonto(String id)
    {
        return pontos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ponto de rede não encontrado: " + id));
    }
}