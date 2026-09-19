import type { Conexao, Coordenada, PontoDeRede } from "@/types";

/** Resultado da roteirização entre o hemocentro e um hospital (HU-07). */
export interface RotaCalculada
{
  /** Traçado a desenhar no mapa. */
  pontos: Coordenada[];
  distanciaKm: number;
  tempoMin: number;
  /** `false` quando os números vêm do mock, por falha ou demora do roteador. */
  doRoteador: boolean;
}

const OSRM = "https://router.project-osrm.org/route/v1/driving";
const TEMPO_LIMITE_MS = 8_000;

/** Rota de referência do mock: linha reta e os números previstos na conexão. */
export function rotaDeReferencia(conexao: Conexao, destino: PontoDeRede, origem: Coordenada): RotaCalculada
{
  return {
    pontos: [origem, destino],
    distanciaKm: conexao.distanciaKm,
    tempoMin: conexao.tempoMin,
    doRoteador: false,
  };
}

/**
 * Traçado, distância e tempo reais por ruas via OSRM (serviço público, sem
 * chave). Qualquer falha, demora ou resposta vazia cai na rota de referência
 * mockada — a tela nunca fica sem plano de transporte.
 */
export async function calcularRota(
  origem: Coordenada,
  destino: PontoDeRede,
  referencia: RotaCalculada,
  signal?: AbortSignal,
): Promise<RotaCalculada>
{
  const limite = AbortSignal.timeout(TEMPO_LIMITE_MS);
  const cancelamento = signal ? AbortSignal.any([signal, limite]) : limite;

  try
  {
    const resposta = await fetch(
      `${OSRM}/${origem.longitude},${origem.latitude};${destino.longitude},${destino.latitude}` +
        "?geometries=geojson&overview=full",
      { signal: cancelamento },
    );
    if (!resposta.ok) return referencia;

    const rota = (await resposta.json())?.routes?.[0];
    const coordenadas: Array<[number, number]> | undefined = rota?.geometry?.coordinates;
    if (!coordenadas?.length || typeof rota.distance !== "number" || typeof rota.duration !== "number")
    {
      return referencia;
    }

    return {
      pontos: coordenadas.map(([longitude, latitude]) => ({ latitude, longitude })),
      distanciaKm: Math.round((rota.distance / 1000) * 10) / 10,
      tempoMin: Math.max(1, Math.round(rota.duration / 60)),
      doRoteador: true,
    };
  }
  catch
  {
    return referencia;
  }
}
