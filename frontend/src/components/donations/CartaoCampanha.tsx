import { CalendarClock, Pencil, Square, Users } from "lucide-react";
import type { CampanhaDTO } from "@/types/dtos";
import { pessoaDaCampanha } from "@/lib/doacoes";
import { SeloTipoSanguineo } from "@/components/ui/SeloTipoSanguineo";
import { PontoStatus } from "@/components/ui/PontoStatus";
import { cn } from "@/lib/utilitarios";

interface Props
{
  campanha: CampanhaDTO;
  /** Médico: mostra editar e encerrar nas campanhas ativas. */
  podeGerenciar: boolean;
  onEditar: (campanha: CampanhaDTO) => void;
  onEncerrar: (campanha: CampanhaDTO) => void;
  onVerAgendamentos: (campanha: CampanhaDTO) => void;
}

/** Campanha de doação na tela de Doações (PI3-191): paciente, situação e agendamentos em aberto. */
export function CartaoCampanha({ campanha, podeGerenciar, onEditar, onEncerrar, onVerAgendamentos }: Readonly<Props>)
{
  const pessoa = pessoaDaCampanha(campanha);
  const ativa = campanha.status === "ATIVA";
  const publicadaEm = new Date(campanha.publicadaEm).toLocaleDateString("pt-BR");

  return (
    <div
      className={cn(
        "flex flex-col gap-3 rounded-xl border bg-white p-5 shadow-card",
        ativa ? "border-rota-border" : "border-dashed border-gray-300 opacity-80",
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-bold text-gray-900">{pessoa.nome}</p>
          <p className="text-sm text-gray-500">
            {pessoa.sexo} · {pessoa.idade} anos · {pessoa.hospital}
          </p>
        </div>
        <SeloTipoSanguineo tipo={pessoa.tipoSanguineo} />
      </div>

      <p className="border-l-2 border-rota-border pl-2 text-sm text-gray-600">
        {pessoa.componente} · {pessoa.unidadesNecessarias} {pessoa.unidadesNecessarias === 1 ? "unidade" : "unidades"}
      </p>

      <div className="flex flex-wrap items-center gap-2">
        <PontoStatus status={pessoa.status} />
        {!ativa && (
          <span className="rounded-full bg-gray-100 px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
            Encerrada
          </span>
        )}
      </div>

      <p className="text-sm text-gray-600">{pessoa.causa}</p>

      <p className="flex items-center gap-1.5 font-mono text-xs text-gray-400">
        <CalendarClock className="h-3.5 w-3.5" />
        Publicada em {publicadaEm}
        {campanha.publicadaPorNome ? ` por ${campanha.publicadaPorNome}` : ""}
      </p>

      <div className="mt-1 flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => onVerAgendamentos(campanha)}
          className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-gray-900 px-3 py-2 text-sm font-semibold text-white transition-colors hover:bg-gray-700"
        >
          <Users className="h-4 w-4" />
          Agendamentos ({campanha.agendamentosEmAberto})
        </button>
        {podeGerenciar && ativa && (
          <>
            <button
              type="button"
              onClick={() => onEditar(campanha)}
              aria-label={`Editar campanha de ${pessoa.nome}`}
              className="inline-flex items-center gap-1.5 rounded-lg border border-rota-border px-3 py-2 text-sm font-medium text-gray-700 hover:border-gray-400"
            >
              <Pencil className="h-4 w-4" />
              Editar
            </button>
            <button
              type="button"
              onClick={() => onEncerrar(campanha)}
              aria-label={`Encerrar campanha de ${pessoa.nome}`}
              className="inline-flex items-center gap-1.5 rounded-lg border border-red-200 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
            >
              <Square className="h-4 w-4" />
              Encerrar
            </button>
          </>
        )}
      </div>
    </div>
  );
}
