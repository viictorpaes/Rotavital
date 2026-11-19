import { useEffect, useState } from "react";
import type { PontoRedeDTO, RotaCalculadaDTO } from "@/types/dtos";
import { calcularRota } from "@/services/api";

/** Menor caminho por id de hospital. */
export type RotasDaRede = Record<string, RotaCalculadaDTO>;

/**
 * Distância e tempo do menor caminho (GET /rotas, Dijkstra) da origem até cada destino, para a
 * lista de hospitais. Um destino sem caminho fica de fora: a lista mostra "sem rota".
 */
export function useRotasDaRede(origemId: string, destinos: PontoRedeDTO[])
{
  const [rotas, setRotas] = useState<RotasDaRede>({});

  useEffect(() =>
  {
    let ativo = true;
    setRotas({});

    destinos.forEach((destino) =>
    {
      calcularRota(origemId, destino.id)
        .then((rota) =>
        {
          if (ativo) setRotas((anteriores) => ({ ...anteriores, [destino.id]: rota }));
        })
        .catch(() => undefined);
    });

    return () =>
    {
      ativo = false;
    };
  }, [origemId, destinos]);

  return rotas;
}
