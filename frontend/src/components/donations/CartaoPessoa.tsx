import { Droplet, Navigation } from "lucide-react";
import type { PessoaNecessitada } from "@/types";
import { SeloTipoSanguineo } from "@/components/ui/SeloTipoSanguineo";
import { PontoStatus } from "@/components/ui/PontoStatus";

interface Props
{
  pessoa: PessoaNecessitada;
  onDoar: (pessoa: PessoaNecessitada) => void;
  /** Abre a rota até o hospital do paciente (PI3-237). */
  onComoChegar: (pessoa: PessoaNecessitada) => void;
  /** O doador já tem uma doação em aberto: o backend aceita só uma por vez. */
  desabilitado?: boolean;
}

export function CartaoPessoa({ pessoa, onDoar, onComoChegar, desabilitado = false }: Readonly<Props>)
{
  return (
    <div className="flex flex-col gap-3 rounded-xl border border-rota-border bg-white p-5 shadow-card">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-bold text-gray-900">{pessoa.nome}</p>
          <p className="text-sm text-gray-500">
            {pessoa.sexo} · {pessoa.idade} anos · {pessoa.hospital}
          </p>
        </div>
        <SeloTipoSanguineo tipo={pessoa.tipoSanguineo} />
      </div>

      <p className="border-l-2 border-rota-border pl-2 text-sm text-gray-600">{pessoa.componente}</p>

      <PontoStatus status={pessoa.status} />

      <p className="text-sm text-gray-600">{pessoa.causa}</p>

      <div className="mt-1 flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => onDoar(pessoa)}
          disabled={desabilitado}
          className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Droplet className="h-4 w-4" fill="currentColor" />
          Fazer doação
        </button>
        {/* Fica ativo mesmo com doação em aberto: quem já agendou é quem mais precisa da rota. */}
        <button
          type="button"
          onClick={() => onComoChegar(pessoa)}
          aria-label={`Como chegar ao ${pessoa.hospital}`}
          className="flex flex-1 items-center justify-center gap-2 rounded-lg border border-gray-900 px-4 py-2.5 text-sm font-semibold text-gray-900 transition-colors hover:bg-gray-900 hover:text-white"
        >
          <Navigation className="h-4 w-4" />
          Como chegar
        </button>
      </div>
    </div>
  );
}
