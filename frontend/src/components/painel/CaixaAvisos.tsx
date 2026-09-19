import { Link } from "react-router-dom";
import { AlertTriangle } from "lucide-react";
import type { AvisoPainel } from "@/types";
import { formatarTempoRelativo, type OrdenacaoAvisos } from "@/lib/painel";
import { PontoStatus } from "@/components/ui/PontoStatus";
import { cn } from "@/lib/utilitarios";

const ORDENACOES: { valor: OrdenacaoAvisos; label: string }[] =
[
  { valor: "severidade", label: "Severidade" },
  { valor: "tempo", label: "Mais recentes" },
];

export function CaixaAvisos({
  avisos,
  ordenacao,
  onOrdenacaoChange,
}: Readonly<{
  avisos: AvisoPainel[];
  ordenacao: OrdenacaoAvisos;
  onOrdenacaoChange: (valor: OrdenacaoAvisos) => void;
}>)
{
  return (
    <section className="space-y-3">
      <div className="flex flex-wrap items-center gap-3">
        <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-500">
          Caixa de avisos
        </h2>
        <p className="text-xs text-gray-400">{avisos.length} ativos</p>

        <div className="ml-auto flex items-center gap-1" role="group" aria-label="Ordenar avisos">
          {ORDENACOES.map(({ valor, label }) => (
            <button
              key={valor}
              type="button"
              onClick={() => onOrdenacaoChange(valor)}
              aria-pressed={ordenacao === valor}
              className={cn(
                "rounded-full px-3 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide transition-colors",
                ordenacao === valor
                  ? "bg-rota-red text-white"
                  : "bg-rota-surface2 text-gray-600 hover:text-gray-900",
              )}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="divide-y divide-rota-border overflow-hidden rounded-xl border border-rota-border bg-white">
        {avisos.map((aviso) => (
          <Link
            key={aviso.id}
            to={aviso.destino}
            className="flex items-start gap-3 p-4 transition-colors hover:bg-rota-surface2"
          >
            <AlertTriangle
              className={cn(
                "mt-0.5 h-4 w-4 shrink-0",
                aviso.severidade === "critico" ? "text-rota-red" : "text-amber-500",
              )}
            />
            <div className="min-w-0 space-y-1">
              <div className="flex flex-wrap items-center gap-2">
                <PontoStatus status={aviso.severidade} />
                <span className="font-mono text-[11px] text-gray-400">
                  {formatarTempoRelativo(aviso.registradoEm)}
                </span>
              </div>
              <p className="text-sm font-semibold text-gray-900">{aviso.titulo}</p>
              <p className="text-xs text-gray-500">{aviso.detalhe}</p>
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
