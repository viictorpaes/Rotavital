import { useEffect, useMemo, useState } from "react";
import type { PontoRedeDTO } from "@/types/dtos";
import { conexoes } from "@/data/redeMock";
import { calcularRota, rotaDeReferencia, type RotaCalculada } from "@/lib/roteirizacao";

/** Rotas por id de hospital. */
export type RotasDaRede = Record<string, RotaCalculada>;

function rotasDeReferencia(origem: PontoRedeDTO, hospitais: PontoRedeDTO[]): RotasDaRede
{
  return Object.fromEntries(
    hospitais.flatMap((hospital) =>
    {
      const conexao = conexoes.find((c) => c.hospitalId === hospital.id);
      return conexao ? [[hospital.id, rotaDeReferencia(conexao, hospital, origem)]] : [];
    }),
  );
}

/**
 * Roteiriza a rede inteira uma vez (HU-07): começa pelos valores mockados e
 * substitui cada hospital assim que o roteador responde, para que a lista, o
 * mapa e o plano de transporte mostrem sempre os mesmos números.
 */
export function useRotasDaRede(origem: PontoRedeDTO, hospitais: PontoRedeDTO[])
{
  const referencias = useMemo(() => rotasDeReferencia(origem, hospitais), [origem, hospitais]);
  const [rotas, setRotas] = useState<RotasDaRede>(referencias);

  useEffect(() =>
  {
    const controlador = new AbortController();

    hospitais.forEach(async (hospital) =>
    {
      const referencia = referencias[hospital.id];
      if (!referencia) return;

      const rota = await calcularRota(origem, hospital, referencia, controlador.signal);
      if (controlador.signal.aborted) return;
      setRotas((anteriores) => ({ ...anteriores, [hospital.id]: rota }));
    });

    return () => controlador.abort();
  }, [origem, hospitais, referencias]);

  return { rotas };
}
