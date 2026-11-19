package com.rotavital.servico;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.RedeDistribuicao;
import com.rotavital.dominio.RotaCalculada;
import com.rotavital.dominio.Trajeto;

/**
 * PI3-259 — Rota de transporte entre dois pontos da rede, para desenhar no mapa.
 *
 * Quem decide por onde passar é o Dijkstra, sobre as conexões cadastradas. O Google só leva esse
 * caminho para as ruas, com o trânsito do momento; sem ele, valem a distância e o tempo das conexões.
 *
 * Sem @Transactional de propósito, como em RotaAteHospitalService: a rede é lida numa transação
 * curta e a chamada ao Google acontece sem prender uma conexão do banco.
 */
@Service
public class RotaNoMapaService
{
    private final RedeDistribuicaoService rede;
    private final CalculadoraDeTrajeto calculadora;
    private final IdentificacaoService identificacao;

    public RotaNoMapaService(RedeDistribuicaoService rede, CalculadoraDeTrajeto calculadora,
            IdentificacaoService identificacao)
    {
        this.rede = rede;
        this.calculadora = calculadora;
        this.identificacao = identificacao;
    }

    // Exige alguém logado na Rede: cada cálculo consome a cota da Routes API, como em RotaAteHospitalService.
    public RotaNoMapa consultar(UUID usuarioId, String origemId, String destinoId)
    {
        identificacao.exigir(usuarioId, "calcular a rota de transporte", PapelUsuario.MEDICO, PapelUsuario.ADMIN);
        return calcular(origemId, destinoId);
    }

    public RotaNoMapa calcular(String origemId, String destinoId)
    {
        if (origemId == null || origemId.isBlank() || destinoId == null || destinoId.isBlank())
        {
            throw new DadosInvalidosException("Informe origemId e destinoId na query string.");
        }
        if (origemId.equals(destinoId))
        {
            throw new DadosInvalidosException("A origem e o destino da rota são o mesmo ponto.");
        }

        RotaCalculada caminho = menorCaminho(origemId, destinoId);
        List<Coordenada> paradas = caminho.getNos().stream()
                .map(ponto -> new Coordenada(ponto.getLatitude(), ponto.getLongitude()))
                .toList();

        Trajeto trajeto = calculadora.calcularComTracado(paradas)
                .orElseGet(() -> new Trajeto(caminho.getDistanciaTotalKm(), (int) Math.ceil(caminho.getTempoEstimadoMin()),
                        FonteDoTrajeto.CONEXOES_DA_REDE));

        return new RotaNoMapa(caminho, trajeto);
    }

    private RotaCalculada menorCaminho(String origemId, String destinoId)
    {
        RedeDistribuicao grafo = rede.carregarRede();

        try
        {
            return grafo.calcularRotaMinima(origemId, destinoId, null);
        }
        catch (IllegalArgumentException e)
        {
            throw new RecursoNaoEncontradoException(e.getMessage() + ".");
        }
        catch (IllegalStateException e)
        {
            throw new RegraDeNegocioException(e.getMessage() + ".");
        }
    }
}
