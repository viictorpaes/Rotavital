import { cn } from "@/lib/utilitarios";

/** Estilo comum dos controles (input/select/textarea) dos formulários de requisição. */
export const CLASSES_CONTROLE =
  "w-full rounded-lg border border-rota-border bg-white px-3 py-2.5 text-sm text-gray-900 " +
  "placeholder:text-gray-400 focus:border-rota-red focus:outline-none";

interface Props
{
  rotulo: string;
  htmlFor?: string;
  className?: string;
  children: React.ReactNode;
}

export function CampoFormulario({ rotulo, htmlFor, className, children }: Readonly<Props>)
{
  return (
    <div className={cn("block", className)}>
      <label
        htmlFor={htmlFor}
        className="mb-1.5 block font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-400"
      >
        {rotulo}
      </label>
      {children}
    </div>
  );
}
