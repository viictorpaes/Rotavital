import { useEffect, useState } from "react";
import type { HemocentroDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { listarHemocentros } from "@/services/api";

/** Hemocentros onde o doador pode marcar a doação, lidos do backend (PI3-205). */
export function useHemocentros()
{
  const [hemocentros, setHemocentros] = useState<HemocentroDTO[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() =>
  {
    let ativo = true;
    listarHemocentros()
      .then((lista) =>
      {
        if (!ativo) return;
        setHemocentros(lista);
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

  return { hemocentros, situacao, erro };
}
