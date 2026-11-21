import { useState } from "react";
import { Ban, Eye, MapPin, Pencil, Thermometer, Trash2 } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import type { CategoriaDescarteBackend } from "@/types/dtos";
import { cn } from "@/lib/utilitarios";
import {
  diasAteVencer,
  formatarData,
  formatarTemperatura,
  rotuloCategoriaDescarte,
  rotuloStatusBolsa,
  statusLote,
  temperaturaForaDaFaixa,
} from "@/lib/estoque";
import { ModalDescartarBolsa } from "./ModalDescartarBolsa";

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

// Status a partir dos quais a bolsa ainda pode ser descartada pela tela de Estoque.
const STATUS_DESCARTAVEIS = ["DISPONIVEL", "RESERVADA"];

export function CartaoLote({ lote, onDescarte, onEditar, onDetalhes }: Readonly<{
  lote: LoteHemocomponente;
  /** Só chega para o admin (PI3-243): sem ele, o botão "Descartar bolsa" não aparece. */
  onDescarte?: (id: string, categoria: CategoriaDescarteBackend, observacao: string) => Promise<void>;
  /** Abre a correção dos dados (só para bolsa disponível). */
  onEditar?: (id: string) => void;
  /** Abre os detalhes da bolsa em modo consulta. */
  onDetalhes?: (id: string) => void;
}>)
{
  const status = statusLote(lote);
  const dias = diasAteVencer(lote);
  const foraDaFaixa = temperaturaForaDaFaixa(lote);
  const descartada = lote.statusBolsa === "DESCARTADA";
  const [modalAberto, setModalAberto] = useState(false);

  async function descartar(categoria: CategoriaDescarteBackend, observacao: string)
  {
    if (!onDescarte) return;
    await onDescarte(lote.id, categoria, observacao);
    setModalAberto(false);
  }

  return (
    <div className={cn(
      "flex flex-col rounded-xl border bg-white p-5 shadow-card",
      descartada ? "border-red-200 bg-red-50/30" : "border-rota-border",
    )}
    >
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
        {descartada && (
          <p className="flex w-full items-start gap-2 rounded-lg bg-rose-50 px-3 py-2 text-xs text-rose-800">
            <Ban className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            <span>
              <strong className="font-mono uppercase tracking-wide">Inutilizável</strong>
              {lote.categoriaDescarte && <> · {rotuloCategoriaDescarte(lote.categoriaDescarte)}</>}
              {lote.motivoDescarte && <> · {lote.motivoDescarte}</>}
            </span>
          </p>
        )}
        {foraDaFaixa && (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-red-50 px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide text-red-600">
            <Thermometer className="h-3 w-3" />
            Fora da faixa
          </span>
        )}
      </div>

      <div className="mt-4 flex flex-wrap justify-end gap-2">
        {onDetalhes && (
          <button
            type="button"
            onClick={() => onDetalhes(lote.id)}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-rota-border px-3 py-2 text-xs font-semibold text-gray-700 transition-colors hover:bg-gray-50"
          >
            <Eye className="h-3.5 w-3.5" />
            Ver detalhes
          </button>
        )}
        {lote.statusBolsa === "DISPONIVEL" && onEditar && (
          <button
            type="button"
            onClick={() => onEditar(lote.id)}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-rota-border px-3 py-2 text-xs font-semibold text-gray-700 transition-colors hover:bg-gray-50"
          >
            <Pencil className="h-3.5 w-3.5" />
            Editar dados
          </button>
        )}
        {onDescarte && lote.statusBolsa && STATUS_DESCARTAVEIS.includes(lote.statusBolsa) && (
          <button
            type="button"
            onClick={() => setModalAberto(true)}
            className="inline-flex items-center justify-center gap-2 rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-50"
          >
            <Trash2 className="h-3.5 w-3.5" />
            Descartar bolsa
          </button>
        )}
      </div>

      {modalAberto && (
        <ModalDescartarBolsa lote={lote} onClose={() => setModalAberto(false)} onDescartar={descartar} />
      )}
    </div>
  );
}
