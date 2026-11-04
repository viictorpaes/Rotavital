import { useEffect, useMemo, useState } from "react";
import type { Coordenada } from "@/types";
import type { PontoRedeDTO, RotaNoMapaDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { decodificarPolyline } from "@/lib/rede";
import { tracadoPelasRuas } from "@/lib/roteirizacao";
import { calcularRotaNoMapa } from "@/services/api";

/**
 * PI3-259 — Rota da origem até o destino para o mapa: o caminho do Dijkstra e o traçado.
 * Com o Google, o traçado vem pronto do backend. Sem ele, o mapa desenha as retas entre as paradas
 * e troca pelas ruas assim que o OSRM responder.
 */
export function useRotaNoMapa(origem: PontoRedeDTO, destino: PontoRedeDTO, pontos: PontoRedeDTO[])
{
  const [rota, setRota] = useState<RotaNoMapaDTO | null>(null);
  const [tracado, setTracado] = useState<Coordenada[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() =>
  {
    const controlador = new AbortController();

    setRota(null);
    setTracado([]);
    setErro(null);
    setSituacao("carregando");

    calcularRotaNoMapa(origem.id, destino.id)
      .then(async (resposta) =>
      {
        if (controlador.signal.aborted) return;
        setRota(resposta);
        setSituacao("pronto");

        if (resposta.polyline)
        {
          setTracado(decodificarPolyline(resposta.polyline));
          return;
        }

        const paradas = resposta.nos.flatMap((id) => pontos.filter((ponto) => ponto.id === id));
        setTracado(paradas);

        const ruas = await tracadoPelasRuas(paradas, controlador.signal);
        if (ruas && !controlador.signal.aborted) setTracado(ruas);
      })
      .catch((e) =>
      {
        if (controlador.signal.aborted) return;
        setErro(mensagemDeErro(e));
        setSituacao("erro");
      });

    return () => controlador.abort();
  }, [origem.id, destino.id, pontos]);

  /** Os pontos do caminho, na ordem, para mostrar por onde a rota passa. */
  const caminho = useMemo(
    () => (rota?.nos ?? []).flatMap((id) => pontos.filter((ponto) => ponto.id === id)),
    [rota, pontos],
  );

  return { rota, caminho, tracado, situacao, erro };
}
