import { useMemo, useState } from "react";
import { AlertTriangle, Clock } from "lucide-react";
import { useDados } from "@/context/ContextoDados";
import { diasAteVencer, statusLote } from "@/lib/estoque";
import { cn } from "@/lib/utilitarios";
import { FormularioRequisicao } from "@/components/requisicao/FormularioRequisicao";
import { ListaRecebimentos } from "@/components/requisicao/ListaRecebimentos";
import { FormularioCampanha } from "@/components/requisicao/FormularioCampanha";

type Aba = "solicitar" | "recebidas" | "campanha";

/** Janela em que um lote já conta como "próximo do vencimento" nesta tela. */
const JANELA_VENCIMENTO_DIAS = 4;

/**
 * HU-04, HU-05 e HU-06 — Solicitação clínica em três abas: solicitar um
 * hemocomponente (com sugestão FEFO), conferir remessas recebidas e publicar
 * uma campanha de doação. Dados ainda mockados (ver `context/ContextoDados.tsx`).
 */
export default function PaginaRequisicao()
{
  const { lotes, recebimentosPendentes } = useDados();
  const [aba, setAba] = useState<Aba>("solicitar");

  const vencendo = useMemo(
    () => lotes.filter((lote) => diasAteVencer(lote) <= JANELA_VENCIMENTO_DIAS).length,
    [lotes],
  );

  const emRisco = useMemo(() => lotes.filter((lote) => statusLote(lote) === "critico").length, [lotes]);

  const ABAS: Array<{ valor: Aba; rotulo: string; contador?: number }> =
  [
    { valor: "solicitar", rotulo: "Solicitar" },
    { valor: "recebidas", rotulo: "Recebidas", contador: recebimentosPendentes.length },
    { valor: "campanha", rotulo: "Campanha" },
  ];

  return (
    <div className="space-y-6">
      <div className="border-b border-rota-border pb-6">
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Solicitação clínica
        </p>
        <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Requisição de hemocomponente</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="flex items-center gap-4 rounded-xl border border-rota-border bg-white p-5 shadow-card">
          <Clock className="h-7 w-7 shrink-0 text-amber-500" />
          <div>
            <p className="text-3xl font-extrabold text-gray-900">{vencendo}</p>
            <p className="text-sm text-gray-500">hemocomponentes próximos do vencimento</p>
          </div>
        </div>
        <div className="flex items-center gap-4 rounded-xl border border-rota-border bg-white p-5 shadow-card">
          <AlertTriangle className="h-7 w-7 shrink-0 text-rota-red" />
          <div>
            <p className="text-3xl font-extrabold text-gray-900">{emRisco}</p>
            <p className="text-sm text-gray-500">lotes em situação crítica</p>
          </div>
        </div>
      </div>

      <div role="tablist" aria-label="Abas da requisição" className="flex flex-wrap gap-2">
        {ABAS.map((item) => (
          <button
            key={item.valor}
            type="button"
            role="tab"
            aria-selected={aba === item.valor}
            onClick={() => setAba(item.valor)}
            className={cn(
              "inline-flex items-center gap-2 rounded-md border px-4 py-2 font-mono text-sm font-semibold transition-colors",
              aba === item.valor
                ? "border-rota-red bg-rota-red text-white"
                : "border-rota-border bg-white text-gray-700 hover:border-gray-400",
            )}
          >
            {item.rotulo}
            {item.contador != null && item.contador > 0 && (
              <span
                className={cn(
                  "rounded-full px-1.5 py-0.5 text-[10px]",
                  aba === item.valor ? "bg-white/20 text-white" : "bg-red-50 text-rota-red",
                )}
              >
                {item.contador}
              </span>
            )}
          </button>
        ))}
      </div>

      {aba === "solicitar" && <FormularioRequisicao />}
      {aba === "recebidas" && <ListaRecebimentos />}
      {aba === "campanha" && <FormularioCampanha />}
    </div>
  );
}
