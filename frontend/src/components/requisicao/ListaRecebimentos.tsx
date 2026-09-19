import { Check, CheckCircle2 } from "lucide-react";
import { useDados } from "@/context/ContextoDados";

/**
 * HU-05 — Conferência física das remessas vindas de outras instituições. Ao
 * confirmar, as unidades entram no estoque como um novo lote, sem lançamento
 * manual.
 */
export function ListaRecebimentos()
{
  const { recebimentosPendentes, recebimentosConfirmados, confirmarRecebimento } = useDados();

  return (
    <div className="max-w-3xl space-y-4">
      <p className="border-l-2 border-rota-red pl-4 text-sm text-gray-500">
        Registre o recebimento físico dos hemocomponentes. Ao confirmar, as unidades são acrescentadas
        automaticamente ao estoque.
      </p>

      {recebimentosPendentes.length === 0 && (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Nenhuma requisição pendente de conferência.
        </p>
      )}

      {recebimentosPendentes.map((requisicao) => (
        <div
          key={requisicao.id}
          className="flex flex-col gap-4 rounded-xl border border-rota-border bg-white p-5 shadow-card sm:flex-row sm:items-center"
        >
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-mono text-xs uppercase tracking-widest text-gray-400">
                {requisicao.id}
              </span>
              <span className="rounded-full border border-rota-border px-2 py-0.5 font-mono text-[10px] uppercase tracking-wide text-gray-500">
                {requisicao.chegadaEm}
              </span>
            </div>
            <p className="mt-1.5 text-lg font-bold text-gray-900">{requisicao.componente}</p>
            <p className="mt-1 font-mono text-sm">
              <span className="font-bold text-rota-red">{requisicao.tipoSanguineo}</span>{" "}
              <span className="text-gray-500">{requisicao.unidades} unidades</span>
            </p>
            <p className="mt-1 text-xs text-gray-500">{requisicao.origem}</p>
          </div>

          <button
            type="button"
            onClick={() => confirmarRecebimento(requisicao.id)}
            className="inline-flex shrink-0 items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
          >
            <Check className="h-4 w-4" />
            Requisição recebida
          </button>
        </div>
      ))}

      {recebimentosConfirmados.length > 0 && (
        <section className="space-y-2 border-t border-rota-border pt-6">
          <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-500">
            Confirmadas nesta sessão
          </h2>
          {recebimentosConfirmados.map((requisicao) => (
            <p
              key={requisicao.id}
              className="flex flex-wrap items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
            >
              <CheckCircle2 className="h-4 w-4 shrink-0" />
              <span className="font-mono font-semibold">{requisicao.id}</span>
              <span className="flex-1">
                {requisicao.componente} · {requisicao.tipoSanguineo} · {requisicao.unidades} un.
              </span>
              <span className="font-mono text-[11px] uppercase tracking-wide opacity-70">
                somadas ao estoque
              </span>
            </p>
          ))}
        </section>
      )}
    </div>
  );
}
