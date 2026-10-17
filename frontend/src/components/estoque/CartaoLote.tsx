import { Lock, LockOpen, MapPin, Pencil, Thermometer, Trash2 } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import { cn } from "@/lib/utilitarios";
import {
  bolsaEditavel,
  diasAteVencer,
  formatarData,
  formatarTemperatura,
  rotuloStatusBolsa,
  statusLote,
  temperaturaForaDaFaixa,
} from "@/lib/estoque";

const CLASSES_STATUS =
{
  critico: "text-red-600",
  atencao: "text-amber-600",
  estavel: "text-emerald-600",
} as const;

const ROTULOS_STATUS = { critico: "Crítico", atencao: "Atenção", estavel: "Estável" } as const;

function Campo({ rotulo, children }: Readonly<{ rotulo: string; children: React.ReactNode }>)
{
  return (
    <div className="bg-white p-3">
      <p className="font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">{rotulo}</p>
      <p className="mt-1 font-mono text-sm text-gray-900">{children}</p>
    </div>
  );
}

/** Ações da bolsa (PI3-190). Sem elas, o cartão só mostra os dados (ex.: dados de exemplo). */
export interface AcoesBolsa
{
  onEditar: (lote: LoteHemocomponente) => void;
  onAlternarReserva: (lote: LoteHemocomponente) => void;
  onDescartar: (lote: LoteHemocomponente) => void;
  /** true enquanto a reserva desta bolsa está sendo enviada. */
  ocupada?: boolean;
}

const CLASSE_ACAO =
  "inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-semibold transition-colors disabled:opacity-50";

export function CartaoLote({ lote, acoes }: Readonly<{ lote: LoteHemocomponente; acoes?: AcoesBolsa }>)
{
  const status = statusLote(lote);
  const dias = diasAteVencer(lote);
  const foraDaFaixa = temperaturaForaDaFaixa(lote);

  return (
    <div className="flex flex-col rounded-xl border border-rota-border bg-white p-5 shadow-card">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-mono text-xs uppercase tracking-widest text-gray-400">{lote.codigo}</p>
          <p className="mt-1 text-lg font-bold text-gray-900">{lote.componente}</p>
        </div>
        <span className="shrink-0 text-2xl font-extrabold leading-none text-rota-red">
          {lote.tipoSanguineo}
        </span>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-px overflow-hidden rounded-lg border border-rota-border bg-rota-border">
        <Campo rotulo="Unidades">
          <span className="text-base font-semibold">{lote.unidades}</span>{" "}
          <span className={cn("text-xs", CLASSES_STATUS[status])}>{ROTULOS_STATUS[status]}</span>
        </Campo>
        <Campo rotulo="Volume">
          <span className="text-base font-semibold">{lote.volumeMl}</span>{" "}
          <span className="text-xs text-gray-400">mL</span>
        </Campo>
        <Campo rotulo="Validade">
          <span className={cn(dias <= 3 ? "text-red-600" : dias <= 15 ? "text-amber-600" : "text-emerald-600")}>
            {formatarData(lote.dataValidade)}
          </span>{" "}
          <span className="text-xs text-gray-400">
            · {dias < 0 ? "vencido" : `${dias}d`}
          </span>
        </Campo>
        <Campo rotulo="🌡 Temperatura">
          <span className={cn(foraDaFaixa ? "text-red-600" : "text-blue-600")}>
            {formatarTemperatura(lote.temperaturaAtual)}
          </span>{" "}
          <span className="text-xs text-gray-400">
            / ideal {formatarTemperatura(lote.temperaturaIdeal.minima)}…
            {formatarTemperatura(lote.temperaturaIdeal.maxima)}
          </span>
        </Campo>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-2">
        <p className="flex items-center gap-1.5 font-mono text-xs text-gray-500">
          <MapPin className="h-3.5 w-3.5 text-rota-red" />
          {lote.localizacao}
        </p>
        {lote.statusBolsa && lote.statusBolsa !== "DISPONIVEL" && (
          <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-600">
            {rotuloStatusBolsa(lote.statusBolsa)}
          </span>
        )}
        {foraDaFaixa && (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-red-50 px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide text-red-600">
            <Thermometer className="h-3 w-3" />
            Fora da faixa
          </span>
        )}
      </div>

      {acoes && bolsaEditavel(lote) && (
        <div className="mt-4 flex flex-wrap gap-2 border-t border-rota-border pt-4">
          <button
            type="button"
            onClick={() => acoes.onEditar(lote)}
            disabled={acoes.ocupada}
            className={cn(CLASSE_ACAO, "border-rota-border text-gray-700 hover:bg-gray-50")}
            aria-label={`Editar bolsa ${lote.id}`}
          >
            <Pencil className="h-3.5 w-3.5" />
            Editar
          </button>
          <button
            type="button"
            onClick={() => acoes.onAlternarReserva(lote)}
            disabled={acoes.ocupada}
            className={cn(CLASSE_ACAO, "border-rota-border text-gray-700 hover:bg-gray-50")}
            aria-label={`${lote.statusBolsa === "RESERVADA" ? "Liberar reserva da" : "Reservar"} bolsa ${lote.id}`}
          >
            {lote.statusBolsa === "RESERVADA"
              ? <><LockOpen className="h-3.5 w-3.5" />Liberar reserva</>
              : <><Lock className="h-3.5 w-3.5" />Reservar</>}
          </button>
          <button
            type="button"
            onClick={() => acoes.onDescartar(lote)}
            disabled={acoes.ocupada}
            className={cn(CLASSE_ACAO, "ml-auto border-rose-200 text-rose-700 hover:bg-rose-50")}
            aria-label={`Descartar bolsa ${lote.id}`}
          >
            <Trash2 className="h-3.5 w-3.5" />
            Descartar
          </button>
        </div>
      )}
    </div>
  );
}
