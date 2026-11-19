import type { Coordenada } from "@/types";

const OSRM = "https://router.project-osrm.org/route/v1/driving";
const TEMPO_LIMITE_MS = 8_000;

/**
 * Plano B do traçado (PI3-259): ruas do caminho pelo OSRM, serviço público sem chave, quando o
 * backend não trouxe o traçado do Google. Só desenha a linha: distância e tempo continuam sendo
 * os do backend, os mesmos que a transferência usa. Qualquer falha devolve null, e o mapa fica
 * com as retas entre as paradas.
 */
export async function tracadoPelasRuas(paradas: Coordenada[], signal: AbortSignal): Promise<Coordenada[] | null>
{
  const caminho = paradas.map(({ latitude, longitude }) => `${longitude},${latitude}`).join(";");

  try
  {
    const resposta = await fetch(`${OSRM}/${caminho}?geometries=geojson&overview=full`, {
      signal: AbortSignal.any([signal, AbortSignal.timeout(TEMPO_LIMITE_MS)]),
    });
    if (!resposta.ok) return null;

    const coordenadas: Array<[number, number]> | undefined = (await resposta.json())?.routes?.[0]?.geometry?.coordinates;
    return coordenadas?.length ? coordenadas.map(([longitude, latitude]) => ({ latitude, longitude })) : null;
  }
  catch
  {
    return null;
  }
}
