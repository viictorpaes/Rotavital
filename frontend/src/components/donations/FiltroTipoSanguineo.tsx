import type { TipoSanguineo } from "@/types";
import { cn } from "@/lib/utilitarios";

const TIPOS: TipoSanguineo[] = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];

interface Props
{
  value: TipoSanguineo | "todos";
  onChange: (value: TipoSanguineo | "todos") => void;
  className?: string;
}

export function FiltroTipoSanguineo({ value, onChange, className }: Readonly<Props>)
{
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value as TipoSanguineo | "todos")}
      aria-label="Filtrar por tipo sanguíneo"
      className={cn(
        "rounded-lg border border-rota-border bg-white px-3 py-2.5 text-sm font-medium text-gray-700 focus:border-rota-red focus:outline-none",
        className,
      )}
    >
      <option value="todos">Todos</option>
      {TIPOS.map((tipo) => (
        <option key={tipo} value={tipo}>
          {tipo}
        </option>
      ))}
    </select>
  );
}
