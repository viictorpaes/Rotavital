import { useState } from "react";
import { Clock, MapPin, X } from "lucide-react";
import type { PessoaNecessitada } from "@/types";

interface Props
{
  pessoa: PessoaNecessitada;
  onClose: () => void;
}

export function ModalAgendarDoacao({ pessoa, onClose }: Readonly<Props>)
{
  const [confirmado, setConfirmado] = useState(false);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="w-full max-w-md rounded-2xl border border-rota-border bg-white p-6 shadow-card"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="schedule-modal-title"
      >
        <div className="mb-4 flex items-start justify-between">
          <h2
            id="schedule-modal-title"
            className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500"
          >
            Agende sua doação
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-4">
          <p className="text-sm text-gray-700">
            Você vai doar para <span className="font-bold text-gray-900">{pessoa.nome}</span> — tipo{" "}
            <span className="font-bold text-rota-red">{pessoa.tipoSanguineo}</span> · {pessoa.componente}.
          </p>

          <EsbocoMapaRota />

          <div className="flex items-start gap-2 text-sm text-gray-700">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
            <div>
              <p className="font-semibold text-gray-900">Rota Vital — Hemocentro Central</p>
              <p className="text-gray-500">R. Arnóbio Marques, 310 — Santo Amaro, Recife — PE</p>
            </div>
          </div>

          <div className="flex items-start gap-2 text-sm text-gray-700">
            <Clock className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
            <p className="font-semibold text-gray-900">Seg a Sáb · 07h às 19h</p>
          </div>

          <p className="border-l-2 border-rota-border pl-2 text-sm italic text-gray-500">{pessoa.causa}</p>

          <button
            type="button"
            onClick={() => setConfirmado(true)}
            disabled={confirmado}
            className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-default disabled:bg-emerald-600"
          >
            {confirmado ? "Rota traçada — até logo!" : "Confirmar e traçar rota no celular"}
          </button>
        </div>
      </div>
    </div>
  );
}

function EsbocoMapaRota()
{
  return (
    <div className="relative h-28 overflow-hidden rounded-lg border border-rota-border bg-rota-surface2">
      <span className="absolute right-2 top-1.5 font-mono text-[10px] uppercase tracking-widest text-gray-400">
        Recife
      </span>
      <svg viewBox="0 0 300 100" className="h-full w-full" aria-hidden="true">
        <path
          d="M 30 75 Q 100 20 150 50 T 260 25"
          fill="none"
          stroke="#c1272d"
          strokeWidth="2"
          strokeDasharray="6 5"
        />
        <circle cx="30" cy="75" r="5" fill="#c1272d" />
        <rect x="252" y="18" width="14" height="14" rx="2" fill="#c1272d" />
      </svg>
    </div>
  );
}
