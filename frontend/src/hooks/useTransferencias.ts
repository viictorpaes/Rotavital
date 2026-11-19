import { useCallback, useEffect, useState } from "react";
import type { TransferenciaDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { listarTransferencias } from "@/services/api";

// O bastante para outra pessoa ver um envio ou uma chegada sem recarregar a tela.
const INTERVALO_DE_ATUALIZACAO_MS = 15_000;

/**
 * Transferências de bolsas em andamento, da que saiu primeiro para a última (PI3-236).
 * Consulta a API de tempos em tempos (PI3-260), menos com a aba em segundo plano.
 */
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

    const consultaPeriodica = setInterval(() =>
    {
      if (!document.hidden) recarregar();
    }, INTERVALO_DE_ATUALIZACAO_MS);

    return () => clearInterval(consultaPeriodica);
  }, [recarregar]);

  return { transferencias, situacao, erro, recarregar };
}
