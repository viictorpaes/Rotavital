import { useEffect, useMemo, useRef, useState } from "react";
import { AlertCircle, CheckCircle2, Database, RefreshCw } from "lucide-react";
import type { LoteHemocomponente, TipoSanguineo } from "@/types";
import type { StatusBolsaBackend, TipoComponenteBackend } from "@/types/dtos";
import {
  COMPONENTES_FILTRO,
  STATUS_FILTRO,
  TIPOS_SANGUINEOS,
  converterBolsaParaLote,
  paraTipoSanguineoBackend,
  statusGrupo,
} from "@/lib/estoque";
import { listarHemocomponentes } from "@/services/api";
import { FiltroTipoEstoque } from "@/components/estoque/FiltroTipoEstoque";
import { SeletorFiltro } from "@/components/estoque/SeletorFiltro";
import { PaginacaoEstoque } from "@/components/estoque/PaginacaoEstoque";
import { CartaoLote } from "@/components/estoque/CartaoLote";
import { PontoStatus } from "@/components/ui/PontoStatus";

const BANCO_ID = "BS-01";

/** Bolsas por página: 12 enche quatro linhas da grade de três colunas. */
const TAMANHO_PAGINA = 12;

interface GrupoEstoque
{
  tipo: TipoSanguineo;
  lotes: LoteHemocomponente[];
  unidades: number;
}

/**
 * HU-03 — Estoque do banco BS-01, lido de GET /api/v1/hemocomponentes (PI3-180). Filtros e paginação
 * rodam no backend; a página mostra as bolsas que vieram, agrupadas por tipo sanguíneo e na ordem
 * FEFO da API. Se a consulta falhar, a tela mostra o erro: não troca por dados de exemplo.
 */
export default function PaginaEstoque()
{
  const [lotes, setLotes] = useState<LoteHemocomponente[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [filtroComponente, setFiltroComponente] = useState<TipoComponenteBackend | "todos">("todos");
  const [filtroStatus, setFiltroStatus] = useState<StatusBolsaBackend | "todos">("todos");
  const [pagina, setPagina] = useState(0);
  const [recarga, setRecarga] = useState(0);
  // Só a resposta da consulta mais recente atualiza a tela (filtros trocados rápido não embaralham a lista).
  const ultimaConsulta = useRef(0);

  useEffect(() =>
  {
    const consulta = ++ultimaConsulta.current;
    setCarregando(true);

    listarHemocomponentes({
      bancoId: BANCO_ID,
      tipoSanguineo: filtroTipo === "todos" ? undefined : paraTipoSanguineoBackend(filtroTipo),
      tipoComponente: filtroComponente === "todos" ? undefined : filtroComponente,
      status: filtroStatus === "todos" ? undefined : filtroStatus,
      page: pagina,
      size: TAMANHO_PAGINA,
    })
      .then((bolsas) =>
      {
        if (consulta !== ultimaConsulta.current) return;
        setLotes(bolsas.map(converterBolsaParaLote));
        setErro(null);
      })
      .catch((falha: unknown) =>
      {
        if (consulta !== ultimaConsulta.current) return;
        setLotes([]);
        setErro(falha instanceof Error && falha.message ? falha.message : "Não foi possível conectar com o back-end");
      })
      .finally(() =>
      {
        if (consulta === ultimaConsulta.current) setCarregando(false);
      });
  }, [filtroTipo, filtroComponente, filtroStatus, pagina, recarga]);

  // Trocar um filtro volta para a primeira página, para não abrir uma página que não existe mais.
  function aoMudarFiltro<T>(definir: (valor: T) => void)
  {
    return (valor: T) =>
    {
      definir(valor);
      setPagina(0);
    };
  }

  const grupos = useMemo<GrupoEstoque[]>(() =>
  {
    return TIPOS_SANGUINEOS
      .map((tipo) =>
      {
        const lotesDoTipo = lotes.filter((lote) => lote.tipoSanguineo === tipo);
        return {
          tipo,
          lotes: lotesDoTipo,
          unidades: lotesDoTipo.reduce((total, lote) => total + lote.unidades, 0),
        };
      })
      .filter((grupo) => grupo.lotes.length > 0);
  }, [lotes]);

  const temProxima = lotes.length === TAMANHO_PAGINA;
  const primeiraCarga = carregando && lotes.length === 0 && !erro;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Armazém · Hemocomponentes
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Estoque por tipo sanguíneo</h1>
        </div>

        <div className="flex items-center gap-3">
          {!erro && !primeiraCarga && (
            <div className="flex items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-800 shadow-sm">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" />
              <span>
                <strong>Supabase conectado:</strong> {lotes.length} bolsa(s) nesta página · Banco {BANCO_ID}
              </span>
            </div>
          )}

          <button
            type="button"
            onClick={() => setRecarga((valor) => valor + 1)}
            disabled={carregando}
            className="flex items-center gap-2 rounded-lg border border-rota-border bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 shadow-sm hover:bg-gray-50 active:scale-95 disabled:opacity-50"
            title="Consultar o estoque no banco de dados de novo"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${carregando ? "animate-spin text-rota-red" : ""}`} />
            {carregando ? "Sincronizando..." : "Sincronizar Banco"}
          </button>
        </div>
      </div>

      <div className="space-y-4">
        <FiltroTipoEstoque value={filtroTipo} onChange={aoMudarFiltro(setFiltroTipo)} />
        <div className="flex flex-wrap gap-4">
          <SeletorFiltro
            id="filtro-componente"
            rotulo="Componente"
            valor={filtroComponente}
            opcoes={COMPONENTES_FILTRO}
            onChange={aoMudarFiltro(setFiltroComponente)}
          />
          <SeletorFiltro
            id="filtro-status"
            rotulo="Status"
            valor={filtroStatus}
            opcoes={STATUS_FILTRO}
            onChange={aoMudarFiltro(setFiltroStatus)}
          />
        </div>
      </div>

      {erro ? (
        <div role="alert" className="flex flex-wrap items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          <AlertCircle className="h-5 w-5 shrink-0 text-rose-600" />
          <span className="flex-1">
            <strong>Não foi possível carregar o estoque:</strong> {erro}.
          </span>
          <button
            type="button"
            onClick={() => setRecarga((valor) => valor + 1)}
            className="rounded-lg border border-rose-300 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-100"
          >
            Tentar novamente
          </button>
        </div>
      ) : primeiraCarga ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-rota-border bg-white p-12 text-center">
          <Database className="h-8 w-8 animate-bounce text-rota-red" />
          <p className="mt-3 text-sm font-semibold text-gray-700">Consultando estoque no Supabase PostgreSQL...</p>
          <p className="text-xs text-gray-400">Endpoint: GET /api/v1/hemocomponentes</p>
        </div>
      ) : grupos.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          {pagina > 0 ? "Não há mais bolsas nesta página." : "Nenhuma bolsa em estoque com esses filtros."}
        </p>
      ) : (
        <div className={carregando ? "space-y-6 opacity-60 transition-opacity" : "space-y-6 transition-opacity"}>
          {grupos.map((grupo) => (
            <section key={grupo.tipo} className="space-y-4 border-t border-rota-border pt-6">
              <div className="flex flex-wrap items-center gap-3">
                <h2 className="text-2xl font-extrabold text-rota-red">{grupo.tipo}</h2>
                <p className="text-sm text-gray-500">
                  {grupo.lotes.length} {grupo.lotes.length === 1 ? "lote" : "lotes"} ·{" "}
                  {grupo.unidades} {grupo.unidades === 1 ? "unidade" : "unidades"}
                </p>
                <PontoStatus status={statusGrupo(grupo.lotes)} className="ml-auto" />
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                {grupo.lotes.map((lote) => (
                  <CartaoLote key={lote.id} lote={lote} />
                ))}
              </div>
            </section>
          ))}
        </div>
      )}

      {!erro && (pagina > 0 || temProxima) && (
        <PaginacaoEstoque pagina={pagina} temProxima={temProxima} carregando={carregando} onMudar={setPagina} />
      )}
    </div>
  );
}
