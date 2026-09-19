import type { TipoSanguineo } from "@/types";
import { cn } from "@/lib/utilitarios";
import { TIPOS_SANGUINEOS } from "@/lib/estoque";

interface Props
{
  value: TipoSanguineo | "todos";
  onChange: (value: TipoSanguineo | "todos") => void;
  className?: string;
}

const OPCOES: Array<{ valor: TipoSanguineo | "todos"; rotulo: string }> =
[
  { valor: "todos", rotulo: "Todos" },
  ...TIPOS_SANGUINEOS.map((tipo) => ({ valor: tipo, rotulo: tipo })),
];

/** Filtro em chips (Todos · A+ · A- · ... · O-) da tela de Estoque (HU-03). */
export function FiltroTipoEstoque({ value, onChange, className }: Readonly<Props>)
{
  return (
    <div role="group" aria-label="Filtrar por tipo sanguíneo" className={cn("flex flex-wrap gap-2", className)}>
      {OPCOES.map(({ valor, rotulo }) => (
        <button
          key={valor}
          type="button"
          aria-pressed={value === valor}
          onClick={() => onChange(valor)}
          className={cn(
            "rounded-md border px-3.5 py-1.5 font-mono text-sm font-semibold transition-colors",
            value === valor
              ? "border-gray-900 bg-gray-900 text-white"
              : "border-rota-border bg-white text-gray-700 hover:border-gray-400",
          )}
        >
          {rotulo}
        </button>
      ))}
    </div>
  );
}
