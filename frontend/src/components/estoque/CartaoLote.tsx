import { useState } from "react";
import { MapPin, Thermometer, Trash2, X } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import { cn } from "@/lib/utilitarios";
import {
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

export function CartaoLote({ lote, onDescarte }: Readonly<{
  lote: LoteHemocomponente;
  onDescarte: (id: string, motivo: string) => Promise<void>;
}>)
{
  const status = statusLote(lote);
  const dias = diasAteVencer(lote);
  const foraDaFaixa = temperaturaForaDaFaixa(lote);
  const [modalAberto, setModalAberto] = useState(false);
  const [motivo, setMotivo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erroDescarte, setErroDescarte] = useState<string | null>(null);

  async function confirmarDescarte()
  {
    const justificativa = motivo.trim();
    if (!justificativa)
    {
      setErroDescarte("Informe o motivo do descarte.");
      return;
    }

    setEnviando(true);
    setErroDescarte(null);
    try
    {
      await onDescarte(lote.id, justificativa);
      setModalAberto(false);
      setMotivo("");
    }
    catch (falha)
    {
      setErroDescarte(falha instanceof Error ? falha.message : "Não foi possível registrar o descarte.");
    }
    finally
    {
      setEnviando(false);
    }
  }

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
        {lote.statusBolsa === "DESCARTADA" && lote.motivoDescarte && (
          <p className="w-full rounded-lg bg-rose-50 px-3 py-2 text-xs text-rose-800">
            <strong>Motivo do descarte:</strong> {lote.motivoDescarte}
          </p>
        )}
        {foraDaFaixa && (
          <span className="inline-flex items-center gap-1.5 rounded-full bg-red-50 px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide text-red-600">
            <Thermometer className="h-3 w-3" />
            Fora da faixa
          </span>
        )}
      </div>

      {lote.statusBolsa === "DISPONIVEL" && (
        <button
          type="button"
          onClick={() =>
          {
            setErroDescarte(null);
            setModalAberto(true);
          }}
          className="mt-4 inline-flex items-center justify-center gap-2 self-end rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700 transition-colors hover:bg-rose-50"
        >
          <Trash2 className="h-3.5 w-3.5" />
          Descartar bolsa
        </button>
      )}

      {modalAberto && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-950/50 p-4">
          <section
            role="dialog"
            aria-modal="true"
            aria-labelledby={`titulo-descarte-${lote.id}`}
            className="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 id={`titulo-descarte-${lote.id}`} className="text-lg font-bold text-gray-900">
                  Descartar bolsa {lote.id}
                </h2>
                <p className="mt-1 text-sm text-gray-600">
                  Esta ação é definitiva. Informe a justificativa para registrar o descarte.
                </p>
              </div>
              <button
                type="button"
                aria-label="Fechar"
                disabled={enviando}
                onClick={() => setModalAberto(false)}
                className="rounded-md p-1 text-gray-500 hover:bg-gray-100 disabled:opacity-50"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <label htmlFor={`motivo-descarte-${lote.id}`} className="mt-5 block text-sm font-medium text-gray-700">
              Motivo do descarte
            </label>
            <textarea
              id={`motivo-descarte-${lote.id}`}
              value={motivo}
              onChange={(evento) => setMotivo(evento.target.value)}
              maxLength={500}
              required
              rows={4}
              disabled={enviando}
              className="mt-2 w-full resize-y rounded-lg border border-gray-300 p-3 text-sm text-gray-900 outline-none focus:border-rota-red focus:ring-2 focus:ring-red-100 disabled:bg-gray-50"
              placeholder="Ex.: bolsa fora da faixa de temperatura"
            />
            <p className="mt-1 text-right text-xs text-gray-400">{motivo.length}/500</p>

            {erroDescarte && (
              <p role="alert" className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-800">
                {erroDescarte}
              </p>
            )}

            <div className="mt-5 flex justify-end gap-3">
              <button
                type="button"
                disabled={enviando}
                onClick={() => setModalAberto(false)}
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              >
                Cancelar
              </button>
              <button
                type="button"
                disabled={enviando || !motivo.trim() || motivo.length > 500}
                onClick={confirmarDescarte}
                className="rounded-lg bg-rose-700 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-800 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {enviando ? "Registrando..." : "Confirmar descarte"}
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  );
}
