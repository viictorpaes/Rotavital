import { CheckCircle2, Package, Pencil, Trash2, TriangleAlert } from "lucide-react";
import type { PacienteNaFila } from "@/types";
import { SeloTipoSanguineo } from "@/components/ui/SeloTipoSanguineo";
import { PontoStatus } from "@/components/ui/PontoStatus";
import { cn } from "@/lib/utilitarios";

interface Props
{
  paciente: PacienteNaFila;
  /** Unidades compatíveis em estoque — define o aviso de cobertura do cartão. */
  disponiveis: number;
  onConcluir: (paciente: PacienteNaFila) => void;
  /** Editar e remover (PI3-195); sem eles, o cartão não mostra esses botões (ex.: perfil sem permissão). */
  onEditar?: (paciente: PacienteNaFila) => void;
  onRemover?: (paciente: PacienteNaFila) => void;
}

export function CartaoPaciente({ paciente, disponiveis, onConcluir, onEditar, onRemover }: Readonly<Props>)
{
  const cobreOPedido = disponiveis >= paciente.unidadesNecessarias;

  return (
    <div
      className={cn(
        "flex flex-col gap-3 rounded-xl border bg-white p-5 shadow-card",
        paciente.status === "critico" ? "border-red-200" : "border-rota-border",
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-bold text-gray-900">{paciente.nome}</p>
          <p className="font-mono text-xs text-gray-500">
            {paciente.sexo} · {paciente.idade} anos · {paciente.hospital}
          </p>
        </div>
        <SeloTipoSanguineo tipo={paciente.tipoSanguineo} />
      </div>

      <p className="border-l-2 border-rota-border pl-2 font-mono text-xs text-gray-600">
        {paciente.componente} · {paciente.unidadesNecessarias}{" "}
        {paciente.unidadesNecessarias === 1 ? "unidade" : "unidades"}
      </p>

      <PontoStatus status={paciente.status} />

      <p className="text-sm text-gray-600">{paciente.causa}</p>

      <p
        className={cn(
          "flex items-center gap-1.5 text-xs font-medium",
          cobreOPedido ? "text-emerald-600" : "text-amber-600",
        )}
      >
        {cobreOPedido ? (
          <Package className="h-3.5 w-3.5" />
        ) : (
          <TriangleAlert className="h-3.5 w-3.5" />
        )}
        {disponiveis} un. compatíveis em estoque
      </p>

      <button
        type="button"
        onClick={() => onConcluir(paciente)}
        className="mt-1 flex w-full items-center justify-center gap-2 rounded-lg border border-rota-border px-4 py-2.5 text-sm font-semibold text-gray-700 transition-colors hover:border-rota-red hover:bg-rota-red hover:text-white"
      >
        <CheckCircle2 className="h-4 w-4" />
        Concluir procedimento
      </button>

      {(onEditar || onRemover) && (
        <div className="flex gap-2">
          {onEditar && (
            <button
              type="button"
              onClick={() => onEditar(paciente)}
              aria-label={`Editar ${paciente.nome}`}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-rota-border px-3 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-50"
            >
              <Pencil className="h-3.5 w-3.5" />
              Editar
            </button>
          )}
          {onRemover && (
            <button
              type="button"
              onClick={() => onRemover(paciente)}
              aria-label={`Remover ${paciente.nome}`}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700 hover:bg-rose-50"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Remover
            </button>
          )}
        </div>
      )}
    </div>
  );
}
