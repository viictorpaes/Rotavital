import { Check } from "lucide-react";
import type { TransferenciaDTO } from "@/types/dtos";
import { formatarDistancia } from "@/lib/rotaAteHospital";
import {
  etapasDaTransferencia,
  previsaoDaChegada,
  progressoDaTransferencia,
  type EstadoDaEtapa,
} from "@/lib/transferencia";
import { cn } from "@/lib/utilitarios";

const COR_DA_ETAPA: Record<EstadoDaEtapa, string> =
{
  feita: "border-rota-red bg-rota-red text-white",
  atual: "border-rota-red bg-white text-rota-red animate-pulse",
  futura: "border-rota-border bg-white text-gray-300",
};

interface Props
{
  transferencia: TransferenciaDTO;
  /** Hora atual em ms: quem chama decide de quanto em quanto tempo o progresso anda. */
  agora: number;
}

/** Linha do tempo de uma transferência ativa, com o progresso e a previsão de chegada (PI3-260). */
export function LinhaDoTempoTransferencia({ transferencia, agora }: Readonly<Props>)
{
  const etapas = etapasDaTransferencia(transferencia, agora);
  const progresso = Math.round(progressoDaTransferencia(transferencia, agora) * 100);
  const previsao = previsaoDaChegada(transferencia, agora);

  return (
    <div className="space-y-3">
      <ol className="grid grid-cols-4 gap-2">
        {etapas.map((etapa) => (
          <li key={etapa.rotulo} aria-current={etapa.estado === "atual" ? "step" : undefined} className="min-w-0">
            <span
              className={cn(
                "flex h-5 w-5 items-center justify-center rounded-full border-2",
                COR_DA_ETAPA[etapa.estado],
              )}
            >
              {etapa.estado === "feita"
                ? <Check className="h-3 w-3" aria-hidden />
                : <span className="h-1.5 w-1.5 rounded-full bg-current" />}
            </span>
            <p className="mt-1.5 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              {etapa.rotulo}
            </p>
            <p className="truncate text-xs text-gray-900" title={etapa.detalhe}>{etapa.detalhe}</p>
          </li>
        ))}
      </ol>

      <div
        role="progressbar"
        aria-label={`Progresso estimado da bolsa ${transferencia.bolsaId}`}
        aria-valuenow={progresso}
        aria-valuemin={0}
        aria-valuemax={100}
        className="h-1.5 overflow-hidden rounded-full bg-rota-surface2"
      >
        <div className="h-full rounded-full bg-rota-red transition-all duration-500" style={{ width: `${progresso}%` }} />
      </div>

      <p className="flex flex-wrap items-center justify-between gap-2 font-mono text-[11px] text-gray-500">
        <span className={cn(previsao.atrasada && "font-semibold text-amber-600")}>{previsao.texto}</span>
        <span>{formatarDistancia(transferencia.distanciaKm)} · estimado pelo tempo da rota</span>
      </p>
    </div>
  );
}
