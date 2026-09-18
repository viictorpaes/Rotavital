import { ArrowRight } from "lucide-react";
import type { PontoDeRede, UrgenciaNecessidade } from "@/types";
import { hospitais } from "@/data/redeMock";
import { conexaoDe } from "@/lib/rede";
import { cn } from "@/lib/utilitarios";

const COR_STATUS: Record<UrgenciaNecessidade, string> =
{
  critico: "bg-rota-red",
  atencao: "bg-amber-500",
  estavel: "bg-emerald-600",
};

interface Props
{
  selecionadoId: string;
  onSelecionar: (hospital: PontoDeRede) => void;
}

/** Hospitais conectados, com distância/tempo e o status do estoque local. */
export function ListaHospitais({ selecionadoId, onSelecionar }: Readonly<Props>)
{
  return (
    <ul className="divide-y divide-rota-border overflow-hidden rounded-xl border border-rota-border bg-white">
      {hospitais.map((hospital) =>
      {
        const conexao = conexaoDe(hospital.id);
        const ativo = hospital.id === selecionadoId;

        return (
          <li key={hospital.id}>
            <button
              type="button"
              onClick={() => onSelecionar(hospital)}
              aria-current={ativo}
              className={cn(
                "flex w-full items-center gap-3 px-4 py-3 text-left transition hover:bg-rota-surface2",
                ativo && "bg-rota-surface2",
              )}
            >
              <span
                className={cn(
                  "h-2 w-2 shrink-0 rounded-full",
                  COR_STATUS[conexao?.status ?? "estavel"],
                )}
              />
              <span className="min-w-0 flex-1">
                <span className="block truncate font-semibold text-gray-900">{hospital.nome}</span>
                <span className="block font-mono text-xs text-gray-500">
                  {conexao ? `${conexao.distanciaKm} km · ${conexao.tempoMin} min` : "sem rota"}
                </span>
              </span>
              {ativo && <ArrowRight className="h-4 w-4 shrink-0 text-rota-red" aria-hidden />}
            </button>
          </li>
        );
      })}
    </ul>
  );
}
