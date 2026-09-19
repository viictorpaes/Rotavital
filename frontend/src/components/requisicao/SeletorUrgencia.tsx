import type { UrgenciaNecessidade } from "@/types";
import { cn } from "@/lib/utilitarios";

const OPCOES: Array<{ valor: UrgenciaNecessidade; rotulo: string; ativo: string }> =
[
  { valor: "critico", rotulo: "Emergência", ativo: "border-red-500 bg-red-50 text-red-600" },
  { valor: "atencao", rotulo: "Urgente", ativo: "border-amber-500 bg-amber-50 text-amber-600" },
  { valor: "estavel", rotulo: "Eletiva", ativo: "border-emerald-500 bg-emerald-50 text-emerald-600" },
];

interface Props
{
  rotuloGrupo: string;
  value: UrgenciaNecessidade;
  onChange: (value: UrgenciaNecessidade) => void;
}

/** Emergência · Urgente · Eletiva — usado na requisição (HU-04) e na campanha (HU-06). */
export function SeletorUrgencia({ rotuloGrupo, value, onChange }: Readonly<Props>)
{
  return (
    <div role="group" aria-label={rotuloGrupo} className="flex gap-2">
      {OPCOES.map((opcao) => (
        <button
          key={opcao.valor}
          type="button"
          aria-pressed={value === opcao.valor}
          onClick={() => onChange(opcao.valor)}
          className={cn(
            "flex-1 rounded-lg border px-3 py-2.5 font-mono text-sm font-semibold transition-colors",
            value === opcao.valor
              ? opcao.ativo
              : "border-rota-border bg-white text-gray-600 hover:border-gray-400",
          )}
        >
          {opcao.rotulo}
        </button>
      ))}
    </div>
  );
}
