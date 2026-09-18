import { Link } from "react-router-dom";
import type { UrgenciaNecessidade } from "@/types";
import type { IndicadorPainel } from "@/lib/painel";
import { cn } from "@/lib/utilitarios";

const COR_STATUS: Record<UrgenciaNecessidade, string> =
{
  critico: "bg-red-500",
  atencao: "bg-amber-500",
  estavel: "bg-emerald-500",
};

const ROTULO_STATUS: Record<UrgenciaNecessidade, string> =
{
  critico: "crítico",
  atencao: "atenção",
  estavel: "estável",
};

export function CartaoIndicador({ indicador }: Readonly<{ indicador: IndicadorPainel }>)
{
  return (
    <Link
      to={indicador.destino}
      className="flex flex-col gap-1 rounded-xl border border-rota-border bg-white p-5 transition-colors hover:border-rota-red"
    >
      <div className="flex items-start justify-between gap-2">
        <p className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-500">
          {indicador.rotulo}
        </p>
        <span
          className={cn("mt-1 h-2 w-2 shrink-0 rounded-full", COR_STATUS[indicador.status])}
          aria-label={`Status ${ROTULO_STATUS[indicador.status]}`}
          role="img"
        />
      </div>
      <p className="text-4xl font-extrabold leading-tight text-gray-900">{indicador.valor}</p>
      <p className="text-xs text-gray-500">{indicador.descricao}</p>
    </Link>
  );
}
