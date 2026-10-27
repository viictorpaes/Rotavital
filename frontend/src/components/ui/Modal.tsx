import type { ReactNode } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utilitarios";

interface Props
{
  titulo: string;
  onClose: () => void;
  children: ReactNode;
  className?: string;
}

/** Janela sobre a tela, fechada pelo X ou por um clique fora dela. */
export function Modal({ titulo, onClose, children, className }: Readonly<Props>)
{
  return (
    // !m-0: a página costuma usar space-y-*, que daria margem ao fundo fixo e deixaria uma faixa sem cobrir no topo.
    <div
      className="fixed inset-0 z-50 !m-0 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className={cn(
          "max-h-[90vh] w-full max-w-md overflow-y-auto rounded-2xl border border-rota-border bg-white p-6 shadow-card",
          className,
        )}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
      >
        <div className="mb-4 flex items-start justify-between">
          <h2 className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500">{titulo}</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
