import { useState } from "react";
import { CalendarCheck } from "lucide-react";
import type { AgendamentoDTO } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import { ROTULO_STATUS_AGENDAMENTO, agendamentoEmAberto, formatarHorario, mensagemDeErro } from "@/lib/doacoes";
import { mudarStatusAgendamento } from "@/services/api";

interface Props
{
  agendamentos: AgendamentoDTO[];
  onRemarcar: (agendamento: AgendamentoDTO) => void;
  /** Chamado depois de cancelar, para o portal ler a lista de novo. */
  onAlterado: () => void;
}

/** A doação em aberto do doador, com remarcar e cancelar, e o histórico das anteriores (HU-10). */
export function MinhasDoacoes({ agendamentos, onRemarcar, onAlterado }: Readonly<Props>)
{
  const { recarregarCampanhas } = useDados();
  const [cancelando, setCancelando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const emAberto = agendamentoEmAberto(agendamentos);
  const anteriores = agendamentos.filter((agendamento) => agendamento.status !== "AGENDADO");

  if (agendamentos.length === 0) return null;

  async function cancelar(agendamento: AgendamentoDTO)
  {
    setCancelando(true);
    setErro(null);
    try
    {
      await mudarStatusAgendamento(agendamento.id, "CANCELADO");
      await recarregarCampanhas();
      onAlterado();
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
    finally
    {
      setCancelando(false);
    }
  }

  return (
    <section className="space-y-3 rounded-xl border border-rota-border bg-white p-5 shadow-card">
      <h2 className="flex items-center gap-2 font-mono text-xs font-bold uppercase tracking-widest text-gray-500">
        <CalendarCheck className="h-4 w-4 text-rota-red" />
        Minhas doações
      </h2>

      {emAberto && (
        <div className="flex flex-col gap-3 rounded-lg border border-sky-200 bg-sky-50 p-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="font-semibold text-gray-900">{formatarHorario(emAberto.agendadoPara)}</p>
            <p className="text-sm text-gray-600">
              {emAberto.bancoNome}
              {emAberto.pacienteNome ? ` · doação para ${emAberto.pacienteNome}` : ""}
            </p>
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => onRemarcar(emAberto)}
              className="rounded-lg bg-gray-900 px-3 py-1.5 text-sm font-semibold text-white hover:bg-gray-700"
            >
              Remarcar
            </button>
            <button
              type="button"
              onClick={() => cancelar(emAberto)}
              disabled={cancelando}
              className="rounded-lg border border-rota-border bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:border-gray-400 disabled:opacity-60"
            >
              {cancelando ? "Cancelando..." : "Cancelar"}
            </button>
          </div>
        </div>
      )}

      {erro && (
        <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </p>
      )}

      {anteriores.length > 0 && (
        <ul className="divide-y divide-rota-border text-sm">
          {anteriores.map((agendamento) => (
            <li key={agendamento.id} className="flex flex-wrap justify-between gap-2 py-2 text-gray-600">
              <span>
                {formatarHorario(agendamento.agendadoPara)}
                {agendamento.pacienteNome ? ` · ${agendamento.pacienteNome}` : ""}
              </span>
              <span className="font-mono text-xs font-semibold uppercase tracking-wide text-gray-400">
                {ROTULO_STATUS_AGENDAMENTO[agendamento.status]}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
