import { useEffect } from "react";
import type { ReactNode } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utilitarios";

interface Props
{
  /** id do título, ligado ao aria-labelledby do diálogo. */
  idTitulo: string;
  titulo: string;
  subtitulo?: string;
  /** Enquanto true (ex.: requisição em andamento), Esc, o X e o clique fora não fecham. */
  bloqueada?: boolean;
  larguraMaxima?: "max-w-md" | "max-w-lg";
  onClose: () => void;
  children: ReactNode;
}

/** Moldura dos modais do Estoque: fundo escuro, cartão central, título e botão de fechar. */
export function JanelaModal({
  idTitulo,
  titulo,
  subtitulo,
  bloqueada = false,
  larguraMaxima = "max-w-lg",
  onClose,
  children,
}: Readonly<Props>)
{
  useEffect(() =>
  {
    function aoTeclar(evento: KeyboardEvent)
    {
      if (evento.key === "Escape" && !bloqueada) onClose();
    }
    window.addEventListener("keydown", aoTeclar);
    return () => window.removeEventListener("keydown", aoTeclar);
  }, [bloqueada, onClose]);

  function fechar()
  {
    if (!bloqueada) onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={fechar}
      role="presentation"
    >
      <div
        className={cn("max-h-[90vh] w-full overflow-y-auto rounded-2xl border border-rota-border bg-white p-6 shadow-card", larguraMaxima)}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
      >
        <div className="mb-4 flex items-start justify-between gap-3">
          <div>
            <h2 id={idTitulo} className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500">
              {titulo}
            </h2>
            {subtitulo && <p className="mt-1 text-sm text-gray-500">{subtitulo}</p>}
          </div>
          <button
            type="button"
            onClick={fechar}
            disabled={bloqueada}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700 disabled:opacity-50"
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
