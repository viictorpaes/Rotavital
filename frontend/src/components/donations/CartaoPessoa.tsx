import { Droplet } from "lucide-react";
import type { PessoaNecessitada } from "@/types";
import { SeloTipoSanguineo } from "@/components/ui/SeloTipoSanguineo";
import { PontoStatus } from "@/components/ui/PontoStatus";

interface Props
{
  pessoa: PessoaNecessitada;
  onDoar: (pessoa: PessoaNecessitada) => void;
}

export function CartaoPessoa({ pessoa, onDoar }: Readonly<Props>)
{
  return (
    <div className="flex flex-col gap-3 rounded-xl border border-rota-border bg-white p-5 shadow-card">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-bold text-gray-900">{pessoa.nome}</p>
          <p className="text-sm text-gray-500">
            {pessoa.sexo} · {pessoa.idade} anos · {pessoa.distanciaKm.toLocaleString("pt-BR", { minimumFractionDigits: 1 })} km
          </p>
        </div>
        <SeloTipoSanguineo tipo={pessoa.tipoSanguineo} />
      </div>

      <p className="border-l-2 border-rota-border pl-2 text-sm text-gray-600">{pessoa.componente}</p>

      <PontoStatus status={pessoa.status} />

      <p className="text-sm text-gray-600">{pessoa.causa}</p>

      <button
        type="button"
        onClick={() => onDoar(pessoa)}
        className="mt-1 flex w-full items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
      >
        <Droplet className="h-4 w-4" fill="currentColor" />
        Fazer doação
      </button>
    </div>
  );
}
