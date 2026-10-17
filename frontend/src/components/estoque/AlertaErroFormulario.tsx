import { AlertCircle } from "lucide-react";

/** Mensagem de erro da API dentro de um formulário do Estoque. */
export function AlertaErroFormulario({ mensagem }: Readonly<{ mensagem: string }>)
{
  return (
    <div role="alert" className="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">
      <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-rose-600" />
      <span>{mensagem}</span>
    </div>
  );
}
