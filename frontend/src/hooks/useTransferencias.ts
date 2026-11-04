import { useCallback, useEffect, useState } from "react";
import type { TransferenciaDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { listarTransferencias } from "@/services/api";

/** Transferências de bolsas em andamento, da que saiu primeiro para a última (PI3-236). */
export function useTransferencias()
{
  const [transferencias, setTransferencias] = useState<TransferenciaDTO[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);

  const recarregar = useCallback(async () =>
  {
    try
    {
      setTransferencias(await listarTransferencias("EM_TRANSITO"));
      setErro(null);
      setSituacao("pronto");
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
      setSituacao("erro");
    }
  }, []);

  useEffect(() =>
  {
    recarregar();
  }, [recarregar]);

  return { transferencias, situacao, erro, recarregar };
}
