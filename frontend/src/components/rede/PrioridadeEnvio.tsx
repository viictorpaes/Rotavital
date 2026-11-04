import { useEffect, useState } from "react";
import { Send } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { diasAteVencer, statusLote } from "@/lib/estoque";
import { cn } from "@/lib/utilitarios";

const COR_PRAZO: Record<string, string> =
{
  critico: "bg-red-50 text-red-600",
  atencao: "bg-amber-50 text-amber-600",
  estavel: "bg-emerald-50 text-emerald-600",
};

interface Props
{
  lotes: LoteHemocomponente[];
  situacao: SituacaoCarga;
  erro: string | null;
  /** Envio só sai de hemocentro: com um hospital de origem, a fila fica vazia. */
  origemEhHemocentro: boolean;
  destinoNome: string;
  /** Sem rota calculada até o destino, não há como confirmar o envio. */
  rotaPronta: boolean;
  /** Só o médico despacha; a API confere de novo. */
  podeEnviar: boolean;
  enviando: boolean;
  onEnviar: (lote: LoteHemocomponente) => void;
}

/**
 * Fila de envio do estoque real, ordenada por FEFO: primeiro a vencer, primeiro a sair (PI3-236).
 * A primeira bolsa já vem marcada; dá para trocar antes de confirmar o envio.
 */
export function PrioridadeEnvio({
  lotes,
  situacao,
  erro,
  origemEhHemocentro,
  destinoNome,
  rotaPronta,
  podeEnviar,
  enviando,
  onEnviar,
}: Readonly<Props>)
{
  const [marcadoId, setMarcadoId] = useState<string | null>(null);
  const marcado = lotes.find((lote) => lote.id === marcadoId) ?? lotes[0];

  useEffect(() =>
  {
    setMarcadoId(null);
  }, [lotes]);

  return (
    <section className="space-y-3 rounded-xl border border-rota-border bg-white p-5">
      <header className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Prioridade de envio (FEFO)
        </h2>
        <p className="font-mono text-[11px] text-gray-400">mais próximos do vencimento</p>
      </header>

      {situacao === "carregando" && <p className="text-sm text-gray-500">Carregando o estoque…</p>}
      {situacao === "erro" && <p className="text-sm text-rota-red">{erro}</p>}
      {situacao === "pronto" && !origemEhHemocentro && (
        <p className="text-sm text-gray-500">Os envios saem de um hemocentro: escolha um como origem da rota.</p>
      )}
      {situacao === "pronto" && origemEhHemocentro && lotes.length === 0 && (
        <p className="text-sm text-gray-500">Nenhuma bolsa deste hemocomponente pode sair agora.</p>
      )}

      {situacao === "pronto" && lotes.length > 0 && (
        <>
          <ol className="space-y-1">
            {lotes.map((lote, indice) => (
              <li key={lote.id}>
                <label
                  className={cn(
                    "flex cursor-pointer items-center gap-3 rounded-lg px-2 py-1.5 transition hover:bg-rota-surface2",
                    lote.id === marcado?.id && "bg-rota-surface2",
                  )}
                >
                  <input
                    type="radio"
                    name="bolsa-para-envio"
                    checked={lote.id === marcado?.id}
                    onChange={() => setMarcadoId(lote.id)}
                    className="accent-rota-red"
                  />
                  <span className="font-mono text-xs text-gray-400">{indice + 1}</span>
                  <span className="font-mono text-sm text-gray-900">{lote.id}</span>
                  <span className="font-semibold text-rota-red">{lote.tipoSanguineo}</span>
                  <span
                    className={cn(
                      "ml-auto inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-[11px] font-semibold",
                      COR_PRAZO[statusLote(lote)],
                    )}
                  >
                    <span className="h-1.5 w-1.5 rounded-full bg-current" />
                    {diasAteVencer(lote)}D
                  </span>
                </label>
              </li>
            ))}
          </ol>

          {podeEnviar
            ? (
              <button
                type="button"
                disabled={enviando || !marcado || !rotaPronta}
                onClick={() => marcado && onEnviar(marcado)}
                className="flex w-full items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-50"
              >
                <Send className="h-4 w-4" aria-hidden />
                {enviando ? "Enviando…" : `Confirmar envio para ${destinoNome}`}
              </button>
            )
            : <p className="font-mono text-[11px] text-gray-400">Só o médico confirma o envio.</p>}
        </>
      )}
    </section>
  );
}
