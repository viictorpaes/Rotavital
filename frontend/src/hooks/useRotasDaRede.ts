import { useEffect, useState } from "react";
import { conexoes, hemocentro, hospitais } from "@/data/redeMock";
import { calcularRota, rotaDeReferencia, type RotaCalculada } from "@/lib/roteirizacao";

/** Rotas por id de hospital. */
export type RotasDaRede = Record<string, RotaCalculada>;

function rotasDeReferencia(): RotasDaRede
{
  return Object.fromEntries(
    hospitais.flatMap((hospital) =>
    {
      const conexao = conexoes.find((c) => c.hospitalId === hospital.id);
      return conexao ? [[hospital.id, rotaDeReferencia(conexao, hospital, hemocentro)]] : [];
    }),
  );
}

/**
 * Roteiriza a rede inteira uma vez (HU-07): começa pelos valores mockados e
 * substitui cada hospital assim que o roteador responde, para que a lista, o
 * mapa e o plano de transporte mostrem sempre os mesmos números.
 */
export function useRotasDaRede()
{
  const [rotas, setRotas] = useState<RotasDaRede>(rotasDeReferencia);
  const [carregando, setCarregando] = useState(true);

  useEffect(() =>
  {
    const controlador = new AbortController();
    const referencias = rotasDeReferencia();

    Promise.all(
      hospitais.map(async (hospital) =>
      {
        const referencia = referencias[hospital.id];
        
        if (!referencia) 
        {
          return;
        }

        const rota = await calcularRota(hemocentro, hospital, referencia, controlador.signal);
        if (controlador.signal.aborted) return;
        setRotas((anteriores) => ({ ...anteriores, [hospital.id]: rota }));
      }),

    ).finally(() =>
    {
      if (!controlador.signal.aborted) 
      {
          setCarregando(false)
      };
    });

    return () => controlador.abort();
  }, []);

  return { rotas, carregando };
}
