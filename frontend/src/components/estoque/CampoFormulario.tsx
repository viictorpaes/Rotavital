import type { ReactNode } from "react";

/** Classes do input/select dos formulários de bolsa (cadastro e edição). */
export const CLASSE_CAMPO =
  "w-full rounded-lg border border-rota-border bg-white px-3 py-2 text-sm text-gray-900 focus:border-rota-red focus:outline-none";

interface Props
{
  id: string;
  rotulo: string;
  dica?: string;
  children: ReactNode;
}

/** Rótulo + campo + dica, ligados pelo id para o leitor de tela anunciar o rótulo certo. */
export function CampoFormulario({ id, rotulo, dica, children }: Readonly<Props>)
{
  return (
    <div className="space-y-1">
      <label htmlFor={id} className="block font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-500">
        {rotulo}
      </label>
      {children}
      {dica && <p className="text-xs text-gray-400">{dica}</p>}
    </div>
  );
}
