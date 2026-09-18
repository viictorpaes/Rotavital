import type { TipoComponente } from "@/types";
import { diasAteVencer, statusLote } from "@/lib/estoque";
import { prioridadeFefo } from "@/lib/rede";
import { cn } from "@/lib/utilitarios";

const COR_PRAZO: Record<string, string> =
{
  critico: "bg-red-50 text-red-600",
  atencao: "bg-amber-50 text-amber-600",
  estavel: "bg-emerald-50 text-emerald-600",
};

/** Fila de envio ordenada por FEFO — primeiro a vencer, primeiro a sair (HU-07). */
export function PrioridadeEnvio({ componente }: Readonly<{ componente: TipoComponente }>)
{
  const lotes = prioridadeFefo(componente);

  return (
    <section className="space-y-3 rounded-xl border border-rota-border bg-white p-5">
      <header className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Prioridade de envio (FEFO)
        </h2>
        <p className="font-mono text-[11px] text-gray-400">mais próximos do vencimento</p>
      </header>

      {lotes.length === 0 ? (
        <p className="text-sm text-gray-500">Nenhum lote deste hemocomponente em estoque.</p>
      ) : (
        <ol className="space-y-2">
          {lotes.map((lote, indice) => (
            <li key={lote.id} className="flex items-center gap-3">
              <span className="font-mono text-xs text-gray-400">{indice + 1}</span>
              <span className="font-mono text-sm text-gray-900">{lote.codigo}</span>
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
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
