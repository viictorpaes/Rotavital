import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, RefreshCw, Search } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import type { TipoSanguineo } from "@/types";
import type { CampanhaDTO, StatusCampanhaBackend } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import { pessoaDaCampanha } from "@/lib/doacoes";
import { cn } from "@/lib/utilitarios";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { CartaoCampanha } from "@/components/donations/CartaoCampanha";
import { ModalAgendamentosCampanha } from "@/components/donations/ModalAgendamentosCampanha";
import { ModalEncerrarCampanha } from "@/components/donations/ModalEncerrarCampanha";
import { FormularioCampanha } from "@/components/requisicao/FormularioCampanha";
import { Modal } from "@/components/ui/Modal";

type FiltroStatus = StatusCampanhaBackend | "TODAS";

const ABAS: { valor: FiltroStatus; rotulo: string }[] =
[
  { valor: "ATIVA", rotulo: "Ativas" },
  { valor: "ENCERRADA", rotulo: "Encerradas" },
  { valor: "TODAS", rotulo: "Todas" },
];

/**
 * HU-09 · PI3-193 — Campanhas de doação gravadas no backend: o médico acompanha os
 * agendamentos de cada campanha, edita os dados do paciente e encerra a campanha.
 * O admin vê tudo, sem gravar (decisão 2 de docs/PERFIS_DE_ACESSO.md).
 */
export default function PaginaDoacoes()
{
  const { usuario } = useAutenticacao();
  const { campanhas, situacaoCampanhas, erroCampanhas, recarregarCampanhas } = useDados();
  const navigate = useNavigate();
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [filtroStatus, setFiltroStatus] = useState<FiltroStatus>("ATIVA");
  const [editando, setEditando] = useState<CampanhaDTO | null>(null);
  const [encerrando, setEncerrando] = useState<CampanhaDTO | null>(null);
  const [vendoAgendamentos, setVendoAgendamentos] = useState<CampanhaDTO | null>(null);

  const ehMedico = usuario?.papel === "medico";

  const campanhasFiltradas = useMemo(() =>
  {
    const termo = busca.trim().toLowerCase();
    return campanhas.filter((campanha) =>
    {
      const pessoa = pessoaDaCampanha(campanha);
      const combinaStatus = filtroStatus === "TODAS" || campanha.status === filtroStatus;
      const combinaTipo = filtroTipo === "todos" || pessoa.tipoSanguineo === filtroTipo;
      const combinaBusca =
        termo.length === 0 ||
        pessoa.nome.toLowerCase().includes(termo) ||
        pessoa.causa.toLowerCase().includes(termo) ||
        pessoa.componente.toLowerCase().includes(termo) ||
        pessoa.hospital.toLowerCase().includes(termo);
      return combinaStatus && combinaTipo && combinaBusca;
    });
  }, [campanhas, busca, filtroTipo, filtroStatus]);

  const agendamentosEmAberto = campanhas
    .filter((campanha) => campanha.status === "ATIVA")
    .reduce((total, campanha) => total + campanha.agendamentosEmAberto, 0);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Doações · Campanhas e agendamentos
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Olá, {usuario?.nome}</h1>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <span className="rounded-full border border-rota-border bg-white px-3 py-1.5 text-xs font-medium text-gray-600">
            {agendamentosEmAberto} {agendamentosEmAberto === 1 ? "doação agendada" : "doações agendadas"}
          </span>
          {ehMedico && (
            <button
              type="button"
              onClick={() => navigate("/requisicao?aba=campanha")}
              className="inline-flex items-center gap-1.5 rounded-lg bg-rota-red px-3 py-1.5 text-sm font-semibold text-white hover:bg-rota-redDark"
            >
              <Plus className="h-4 w-4" />
              Nova campanha
            </button>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-3 lg:flex-row">
        <div role="tablist" aria-label="Situação da campanha" className="flex gap-1 rounded-lg border border-rota-border bg-white p-1">
          {ABAS.map((aba) => (
            <button
              key={aba.valor}
              type="button"
              role="tab"
              aria-selected={filtroStatus === aba.valor}
              onClick={() => setFiltroStatus(aba.valor)}
              className={cn(
                "rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                filtroStatus === aba.valor ? "bg-gray-900 text-white" : "text-gray-600 hover:bg-rota-surface2",
              )}
            >
              {aba.rotulo}
            </button>
          ))}
        </div>
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por paciente, causa, necessidade ou hospital..."
            className="w-full rounded-lg border border-rota-border bg-white py-2.5 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:outline-none"
          />
        </div>
        <FiltroTipoSanguineo value={filtroTipo} onChange={setFiltroTipo} />
      </div>

      {situacaoCampanhas === "carregando" && (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Carregando campanhas...
        </p>
      )}

      {situacaoCampanhas === "erro" && (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          <p>Não foi possível carregar as campanhas: {erroCampanhas}</p>
          <button
            type="button"
            onClick={() => recarregarCampanhas()}
            className="inline-flex items-center gap-1.5 rounded-lg border border-red-300 bg-white px-3 py-1.5 font-medium hover:bg-red-100"
          >
            <RefreshCw className="h-4 w-4" />
            Tentar de novo
          </button>
        </div>
      )}

      {situacaoCampanhas === "pronto" && campanhasFiltradas.length === 0 && (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          {campanhas.length === 0
            ? "Nenhuma campanha publicada ainda. Publique uma em Requisição › Campanha."
            : "Nenhuma campanha encontrada com esses filtros."}
        </p>
      )}

      {situacaoCampanhas === "pronto" && campanhasFiltradas.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {campanhasFiltradas.map((campanha) => (
            <CartaoCampanha
              key={campanha.id}
              campanha={campanha}
              podeGerenciar={ehMedico}
              onEditar={setEditando}
              onEncerrar={setEncerrando}
              onVerAgendamentos={setVendoAgendamentos}
            />
          ))}
        </div>
      )}

      {editando && (
        <Modal titulo="Editar campanha" onClose={() => setEditando(null)} className="max-w-3xl">
          <FormularioCampanha
            campanha={editando}
            onSalvo={() => setEditando(null)}
            onCancelar={() => setEditando(null)}
          />
        </Modal>
      )}

      {encerrando && <ModalEncerrarCampanha campanha={encerrando} onClose={() => setEncerrando(null)} />}

      {vendoAgendamentos && (
        <ModalAgendamentosCampanha
          campanha={vendoAgendamentos}
          podeGerenciar={ehMedico}
          onClose={() => setVendoAgendamentos(null)}
        />
      )}
    </div>
  );
}
