import { useCallback, useEffect, useRef, useState } from "react";
import type { LoteHemocomponente, TipoComponente } from "@/types";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { COMPONENTES_FILTRO } from "@/lib/estoque";
import { filaFefo } from "@/lib/rede";
import { listarHemocomponentes } from "@/services/api";

// Página maior que a fila: bolsas vencidas ou fora da faixa vêm na frente e são tiradas em `filaFefo`.
const BOLSAS_POR_CONSULTA = 50;

/**
 * Bolsas que podem sair do hemocentro, por FEFO, lidas do estoque real (PI3-236).
 * Sem hemocentro (a origem da rota é um hospital), a fila fica vazia: envio só sai de hemocentro.
 */
export function useFilaDeEnvio(hemocentroId: string | null, componente: TipoComponente)
{
  const [lotes, setLotes] = useState<LoteHemocomponente[]>([]);
  const [situacao, setSituacao] = useState<SituacaoCarga>("carregando");
  const [erro, setErro] = useState<string | null>(null);
  const ultimaConsulta = useRef(0);

  const recarregar = useCallback(async () =>
  {
    // Vale só a resposta da última consulta: trocando rápido de hemocomponente, uma resposta
    // atrasada mostraria na fila de envio bolsas de outro componente.
    const consulta = ++ultimaConsulta.current;

    if (!hemocentroId)
    {
      setLotes([]);
      setSituacao("pronto");
      return;
    }

    try
    {
      const bolsas = await listarHemocomponentes({
        bancoId: hemocentroId,
        tipoComponente: COMPONENTES_FILTRO.find(({ rotulo }) => rotulo === componente)?.valor,
        status: "DISPONIVEL",
        size: BOLSAS_POR_CONSULTA,
      });
      if (consulta !== ultimaConsulta.current) return;

      setLotes(filaFefo(bolsas));
      setErro(null);
      setSituacao("pronto");
    }
    catch (e)
    {
      if (consulta !== ultimaConsulta.current) return;

      setErro(mensagemDeErro(e));
      setSituacao("erro");
    }
  }, [hemocentroId, componente]);

  useEffect(() =>
  {
    setSituacao("carregando");
    recarregar();
  }, [recarregar]);

  return { lotes, situacao, erro, recarregar };
}
