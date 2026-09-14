import type { UrgenciaNecessidade } from "@/types";
import { cn } from "@/lib/utilitarios";

const CONFIGURACAO_STATUS: Record<UrgenciaNecessidade, { label: string; classes: string }> =
{
  critico: { label: "Crítico", classes: "bg-red-50 text-red-600" },
  atencao: { label: "Atenção", classes: "bg-amber-50 text-amber-600" },
  estavel: { label: "Estável", classes: "bg-emerald-50 text-emerald-600" },
};

export function PontoStatus({
  status,
  className,
}: Readonly<{ status: UrgenciaNecessidade; className?: string }>)
{
  const cfg = CONFIGURACAO_STATUS[status];
  return (
    <span
      className={cn(
        "inline-flex w-fit items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide",
        cfg.classes,
        className,
      )}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {cfg.label}
    </span>
  );
}
