import { useCallback, useEffect, useMemo, useState } from "react";
import { AlertCircle, CheckCircle2, Plus, Search } from "lucide-react";
import type { OrigemAtendimento, PacienteNaFila, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import type { PacienteDTO } from "@/types/dtos";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { useDados } from "@/context/ContextoDados";
import { useNotificacao } from "@/context/ContextoNotificacao";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { CartaoPaciente } from "@/components/pacientes/CartaoPaciente";
import { ModalConcluirProcedimento } from "@/components/pacientes/ModalConcluirProcedimento";
import { ModalPaciente } from "@/components/pacientes/ModalPaciente";
import { ModalRemoverPaciente } from "@/components/pacientes/ModalRemoverPaciente";
import { contarPorUrgencia, ordenarPorUrgencia, pessoaDoPaciente, unidadesDisponiveis } from "@/lib/pacientes";
import { mensagemDeErro } from "@/lib/doacoes";
import { listarPacientes } from "@/services/api";
import { cn } from "@/lib/utilitarios";

const RESUMO: { status: UrgenciaNecessidade; rotulo: string; classes: string }[] =
[
  { status: "critico", rotulo: "Críticos", classes: "border-red-200 bg-red-50 text-red-700" },
  { status: "atencao", rotulo: "Atenção", classes: "border-amber-200 bg-amber-50 text-amber-700" },
  { status: "estavel", rotulo: "Estáveis", classes: "border-emerald-200 bg-emerald-50 text-emerald-700" },
];

/**
 * HU-08 — Fila clínica de pacientes aguardando atendimento, lida de GET /api/v1/pacientes (PI3-195):
 * todos os pacientes, com ou sem campanha de doação. O médico cadastra, edita e remove por aqui.
 * A conclusão do procedimento tira o paciente da fila e, quando atendido pelo estoque interno,
 * baixa as unidades por FEFO (ainda só nesta sessão).
 */
export default function PaginaPacientes()
{
  const { lotes, concluirProcedimento, procedimentosConcluidos, recarregarCampanhas } = useDados();
  const { usuario } = useAutenticacao();
  const { notificarSucesso } = useNotificacao();
  const [lista, setLista] = useState<PacienteDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [emEdicao, setEmEdicao] = useState<PacienteDTO | null>(null);
  const [cadastroAberto, setCadastroAberto] = useState(false);
  const [aRemover, setARemover] = useState<PacienteNaFila | null>(null);
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [filtroStatus, setFiltroStatus] = useState<UrgenciaNecessidade | "todos">("todos");
  const [selecionado, setSelecionado] = useState<PacienteNaFila | null>(null);

  // Só médico grava pacientes (docs/PERFIS_DE_ACESSO.md); o backend também confere.
  const podeGravar = usuario?.papel === "medico";

  const carregar = useCallback(async () =>
  {
    setCarregando(true);
    try
    {
      setLista(await listarPacientes({ status: "AGUARDANDO" }));
      setErro(null);
    }
    catch (falha)
    {
      setErro(mensagemDeErro(falha));
    }
    finally
    {
      setCarregando(false);
    }
  }, []);

  useEffect(() =>
  {
    carregar();
  }, [carregar]);

  // O procedimento ainda não é gravado: quem foi atendido nesta sessão sai da fila só aqui.
  const pacientes = useMemo(() =>
  {
    const atendidos = new Set(procedimentosConcluidos.map((procedimento) => procedimento.pacienteId));
    return lista.filter((paciente) => !atendidos.has(paciente.id)).map(pessoaDoPaciente);
  }, [lista, procedimentosConcluidos]);

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
    return concluirProcedimento(selecionado, origem);
  }

  // Depois de gravar: a lista é lida de novo, e as campanhas também (o paciente pode estar divulgado nelas).
  function aoSalvar(paciente: PacienteDTO, novo: boolean)
  {
    setCadastroAberto(false);
    setEmEdicao(null);
    notificarSucesso(novo ? "Paciente cadastrado" : "Paciente atualizado", `${paciente.nome} · ${paciente.hospitalNome}`);
    carregar();
    recarregarCampanhas();
  }

  function aoRemover(paciente: PacienteNaFila)
  {
    setARemover(null);
    notificarSucesso("Paciente removido", `${paciente.nome} saiu do cadastro.`);
    carregar();
  }

  function editar(pessoa: PacienteNaFila)
  {
    setEmEdicao(lista.find((paciente) => paciente.id === pessoa.id) ?? null);
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
            {pacientes.length} aguardando
          </span>
          {podeGravar && (
            <button
              type="button"
              onClick={() => setCadastroAberto(true)}
              className="flex items-center gap-1.5 rounded-lg bg-rota-red px-3 py-1.5 text-xs font-semibold text-white shadow-sm transition-colors hover:bg-rota-redDark"
            >
              <Plus className="h-3.5 w-3.5" />
              Novo paciente
            </button>
          )}
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

      {erro ? (
        <div role="alert" className="flex flex-wrap items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          <AlertCircle className="h-5 w-5 shrink-0 text-rose-600" />
          <span className="flex-1">
            <strong>Não foi possível carregar os pacientes:</strong> {erro}
          </span>
          <button
            type="button"
            onClick={carregar}
            className="rounded-lg border border-rose-300 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-100"
          >
            Tentar novamente
          </button>
        </div>
      ) : carregando && lista.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Carregando pacientes...
        </p>
      ) : pacientesFiltrados.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          {pacientes.length === 0
            ? "Nenhum paciente aguardando atendimento."
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
              onEditar={podeGravar ? editar : undefined}
              onRemover={podeGravar ? setARemover : undefined}
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

      {(cadastroAberto || emEdicao) && (
        <ModalPaciente
          paciente={emEdicao ?? undefined}
          onClose={() =>
          {
            setCadastroAberto(false);
            setEmEdicao(null);
          }}
          onSalvo={aoSalvar}
        />
      )}

      {aRemover && (
        <ModalRemoverPaciente paciente={aRemover} onClose={() => setARemover(null)} onRemovido={aoRemover} />
      )}
    </div>
  );
}
