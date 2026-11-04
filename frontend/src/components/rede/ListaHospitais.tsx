import { ArrowRight } from "lucide-react";
import type { PontoRedeDTO } from "@/types/dtos";
import type { RotasDaRede } from "@/hooks/useRotasDaRede";
import { formatarDistancia, formatarDuracao } from "@/lib/rotaAteHospital";
import { cn } from "@/lib/utilitarios";

interface Props
{
  hospitais: PontoRedeDTO[];
  selecionadoId: string;
  rotas: RotasDaRede;
  onSelecionar: (hospital: PontoRedeDTO) => void;
}

/** Hospitais conectados, com a distância e o tempo a partir da origem. */
export function ListaHospitais({ hospitais, selecionadoId, rotas, onSelecionar }: Readonly<Props>)
{
  return (
    <ul className="divide-y divide-rota-border overflow-hidden rounded-xl border border-rota-border bg-white">
      {hospitais.map((hospital) =>
      {
        const rota = rotas[hospital.id];
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
              <span className="min-w-0 flex-1">
                <span className="block truncate font-semibold text-gray-900">{hospital.nome}</span>
                <span className="block font-mono text-xs text-gray-500">
                  {rota
                    ? `${formatarDistancia(rota.distanciaTotalKm)} · ${formatarDuracao(Math.ceil(rota.tempoEstimadoMin))}`
                    : "sem rota"}
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
