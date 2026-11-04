import { PackageCheck } from "lucide-react";
import type { TransferenciaDTO } from "@/types/dtos";
import type { SituacaoCarga } from "@/context/ContextoDados";
import { useAgora } from "@/hooks/useAgora";
import { converterTipoComponente, converterTipoSanguineo } from "@/lib/estoque";
import { LinhaDoTempoTransferencia } from "@/components/rede/LinhaDoTempoTransferencia";

const INTERVALO_DO_PROGRESSO_MS = 5_000;

interface Props
{
  transferencias: TransferenciaDTO[];
  situacao: SituacaoCarga;
  erro: string | null;
  /** Só o médico confirma; a API confere de novo. */
  podeReceber: boolean;
  recebendoId: string | null;
  onReceber: (transferencia: TransferenciaDTO) => void;
}

/** Bolsas a caminho dos hospitais, com a linha do tempo (PI3-260) e a confirmação de chegada (PI3-236). */
export function TransferenciasEmAndamento({
  transferencias,
  situacao,
  erro,
  podeReceber,
  recebendoId,
  onReceber,
}: Readonly<Props>)
{
  const agora = useAgora(INTERVALO_DO_PROGRESSO_MS);

  return (
    <section className="space-y-4 rounded-xl border border-rota-border bg-white p-5">
      <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
        Transferências em andamento
      </h2>

      {situacao === "carregando" && <p className="text-sm text-gray-500">Carregando…</p>}
      {situacao === "erro" && <p className="text-sm text-rota-red">{erro}</p>}
      {situacao === "pronto" && transferencias.length === 0 && (
        <p className="text-sm text-gray-500">Nenhuma bolsa a caminho agora.</p>
      )}

      <ul className="space-y-3">
        {transferencias.map((transferencia) => (
          <li key={transferencia.id} className="space-y-3 rounded-lg border border-rota-border p-4">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-900">
                  <span className="font-mono">{transferencia.bolsaId}</span>
                  {" · "}
                  {converterTipoComponente(transferencia.tipoComponente)}
                  {" · "}
                  <span className="text-rota-red">{converterTipoSanguineo(transferencia.tipoSanguineo)}</span>
                </p>
                <p className="text-sm text-gray-500">
                  {transferencia.origemNome} → {transferencia.destinoNome}
                </p>
              </div>

              {podeReceber && (
                <button
                  type="button"
                  disabled={recebendoId === transferencia.id}
                  onClick={() => onReceber(transferencia)}
                  className="inline-flex shrink-0 items-center gap-2 rounded-lg border border-rota-border bg-white px-3 py-2 text-sm font-semibold text-gray-900 transition hover:bg-rota-surface2 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  <PackageCheck className="h-4 w-4 text-rota-red" aria-hidden />
                  {recebendoId === transferencia.id ? "Confirmando…" : "Confirmar recebimento"}
                </button>
              )}
            </div>

            <LinhaDoTempoTransferencia transferencia={transferencia} agora={agora} />
          </li>
        ))}
      </ul>
    </section>
  );
}
