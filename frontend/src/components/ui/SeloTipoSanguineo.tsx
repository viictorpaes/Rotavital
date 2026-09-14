import type { TipoSanguineo } from "@/types";
import { cn } from "@/lib/utilitarios";

export function SeloTipoSanguineo({
  tipo,
  className,
}: Readonly<{ tipo: TipoSanguineo; className?: string }>)
{
  return (
    <div className={cn("flex flex-col items-end shrink-0", className)}>
      <span className="text-2xl font-extrabold leading-none text-rota-red">{tipo}</span>
      <span className="mt-1 font-mono text-[10px] uppercase tracking-widest text-gray-400">Tipo</span>
    </div>
  );
}
