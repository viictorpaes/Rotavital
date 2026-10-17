import { Loader2 } from "lucide-react";
import { cn } from "@/lib/utilitarios";

interface Props
{
  enviando: boolean;
  rotulo: string;
  rotuloEnviando: string;
  /** Desabilita só o botão principal (ex.: nada mudou no formulário). */
  desabilitado?: boolean;
  /** "perigo" deixa o botão principal vermelho-escuro, para ações que não se desfazem. */
  variante?: "padrao" | "perigo";
  onCancelar: () => void;
}

/** Cancelar + botão de envio dos formulários do Estoque. O principal é type="submit". */
export function BotoesFormulario({
  enviando,
  rotulo,
  rotuloEnviando,
  desabilitado = false,
  variante = "padrao",
  onCancelar,
}: Readonly<Props>)
{
  return (
    <div className="flex justify-end gap-3 pt-2">
      <button
        type="button"
        onClick={onCancelar}
        disabled={enviando}
        className="rounded-lg border border-rota-border bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
      >
        Cancelar
      </button>
      <button
        type="submit"
        disabled={enviando || desabilitado}
        className={cn(
          "inline-flex items-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold text-white transition-colors disabled:opacity-60",
          variante === "perigo" ? "bg-rose-700 hover:bg-rose-800" : "bg-rota-red hover:bg-rota-redDark",
        )}
      >
        {enviando && <Loader2 className="h-4 w-4 animate-spin" />}
        {enviando ? rotuloEnviando : rotulo}
      </button>
    </div>
  );
}
