import { useEffect, useState } from "react";
import type { PontoRedeDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { listarPontosRede } from "@/services/api";

/** Hemocentros e hospitais da rede, lidos do backend (PI3-258). */
export function usePontosDaRede()
{
  const [pontos, setPontos] = useState<PontoRedeDTO[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() =>
  {
    let ativo = true;
    listarPontosRede()
      .then((lista) =>
      {
        if (!ativo) return;
        setPontos(lista);
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
  }, []);

  return { pontos, situacao, erro };
}
