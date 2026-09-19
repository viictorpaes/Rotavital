import { useMemo, useState } from "react";
import { CheckCircle2, Search } from "lucide-react";
import type { OrigemAtendimento, PessoaNecessitada, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { CartaoPaciente } from "@/components/pacientes/CartaoPaciente";
import { ModalConcluirProcedimento } from "@/components/pacientes/ModalConcluirProcedimento";
import { contarPorUrgencia, ordenarPorUrgencia, unidadesDisponiveis } from "@/lib/pacientes";
import { cn } from "@/lib/utilitarios";

const RESUMO: { status: UrgenciaNecessidade; rotulo: string; classes: string }[] =
[
  { status: "critico", rotulo: "Críticos", classes: "border-red-200 bg-red-50 text-red-700" },
  { status: "atencao", rotulo: "Atenção", classes: "border-amber-200 bg-amber-50 text-amber-700" },
  { status: "estavel", rotulo: "Estáveis", classes: "border-emerald-200 bg-emerald-50 text-emerald-700" },
];

/**
 * HU-08 — Fila clínica de pacientes com necessidade ativa. A conclusão do
 * procedimento tira o paciente da fila e, quando atendido pelo estoque interno,
 * baixa as unidades por FEFO.
 */
export default function PaginaPacientes()
{
  const { pacientes, lotes, concluirProcedimento, procedimentosConcluidos } = useDados();
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [filtroStatus, setFiltroStatus] = useState<UrgenciaNecessidade | "todos">("todos");
  const [selecionado, setSelecionado] = useState<PessoaNecessitada | null>(null);

  const totais = useMemo(() => contarPorUrgencia(pacientes), [pacientes]);

  const pacientesFiltrados = useMemo(() =>
  {
    const termo = busca.trim().toLowerCase();
    const filtrados = pacientes.filter((p) =>
    {
      const combinaTipo = filtroTipo === "todos" || p.tipoSanguineo === filtroTipo;
      const combinaStatus = filtroStatus === "todos" || p.status === filtroStatus;
      const combinaBusca =
        termo.length === 0 ||
        p.nome.toLowerCase().includes(termo) ||
        p.causa.toLowerCase().includes(termo) ||
        p.componente.toLowerCase().includes(termo);
      return combinaTipo && combinaStatus && combinaBusca;
    });
    return ordenarPorUrgencia(filtrados);
  }, [pacientes, busca, filtroTipo, filtroStatus]);

  function handleConfirmar(origem: OrigemAtendimento)
  {
    if (!selecionado) return undefined;
    return concluirProcedimento(selecionado.id, origem);
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Gestão clínica · Pacientes
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Pacientes</h1>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {procedimentosConcluidos.length > 0 && (
            <span className="flex items-center gap-1.5 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-700">
              <CheckCircle2 className="h-3.5 w-3.5" />
              {procedimentosConcluidos.length} concluídos hoje
            </span>
          )}
          <span className="rounded-full border border-rota-border bg-white px-3 py-1.5 font-mono text-xs font-medium text-gray-600">
            {pacientes.length} ativos
          </span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-3">
        {RESUMO.map(({ status, rotulo, classes }) => (
          <button
            key={status}
            type="button"
            onClick={() => setFiltroStatus(filtroStatus === status ? "todos" : status)}
            aria-pressed={filtroStatus === status}
            className={cn(
              "rounded-xl border px-4 py-3 text-left transition-shadow hover:shadow-card",
              classes,
              filtroStatus === status && "ring-2 ring-rota-red ring-offset-1",
            )}
          >
            <p className="font-mono text-[11px] font-semibold uppercase tracking-widest opacity-80">
              {rotulo}
            </p>
            <p className="mt-1 text-2xl font-extrabold leading-none">{totais[status]}</p>
          </button>
        ))}
      </div>

      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar paciente, diagnóstico ou componente..."
            className="w-full rounded-lg border border-rota-border bg-white py-2.5 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:outline-none"
          />
        </div>
        <FiltroTipoSanguineo value={filtroTipo} onChange={setFiltroTipo} />
      </div>

      {pacientesFiltrados.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          {pacientes.length === 0
            ? "Nenhum paciente na fila — todos os procedimentos foram concluídos."
            : "Nenhum paciente encontrado com esses filtros."}
        </p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {pacientesFiltrados.map((paciente) => (
            <CartaoPaciente
              key={paciente.id}
              paciente={paciente}
              disponiveis={unidadesDisponiveis(lotes, paciente.componente, paciente.tipoSanguineo)}
              onConcluir={setSelecionado}
            />
          ))}
        </div>
      )}

      {selecionado && (
        <ModalConcluirProcedimento
          paciente={selecionado}
          lotes={lotes}
          onConfirmar={handleConfirmar}
          onClose={() => setSelecionado(null)}
        />
      )}
    </div>
  );
}
