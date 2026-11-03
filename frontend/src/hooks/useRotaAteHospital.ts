import { useEffect, useState } from "react";
import type { RotaAteHospitalDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import type { PosicaoDoador } from "@/lib/geolocalizacao";
import { mensagemDeErro } from "@/lib/doacoes";
import { calcularRotaAteHospital } from "@/services/api";

/**
 * PI3-262 — Rota até o hospital do paciente da campanha, lida do backend (Routes API do Google).
 * Sem posição, busca só o destino; quando a posição chega, busca de novo com distância e tempo,
 * mantendo o destino na tela enquanto calcula.
 */
export function useRotaAteHospital(campanhaId: string, posicao: PosicaoDoador | null)
{
  const [rota, setRota] = useState<RotaAteHospitalDTO | null>(null);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);

  // Depende do objeto da posição, não só dos números: "Tentar de novo" lê a posição outra vez e,
  // mesmo que o GPS devolva as mesmas coordenadas, a rota é pedida de novo.
  useEffect(() =>
  {
    let ativo = true;
    const origem = posicao ? { latitude: posicao.latitude, longitude: posicao.longitude } : undefined;

    setSituacao("carregando");
    setErro(null);
    calcularRotaAteHospital(campanhaId, origem)
      .then((resposta) =>
      {
        if (!ativo) return;
        setRota(resposta);
        setSituacao("pronto");
      })
      .catch((e) =>
      {
        if (!ativo) return;
        setErro(mensagemDeErro(e));
        setSituacao("erro");
      });

    return () =>
    {
      ativo = false;
    };
  }, [campanhaId, posicao]);

  return { rota, situacao, erro };
}
