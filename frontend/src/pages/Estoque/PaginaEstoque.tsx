import { useEffect, useMemo, useState } from "react";
import { AlertCircle, CheckCircle2, Database, RefreshCw } from "lucide-react";
import type { LoteHemocomponente, TipoSanguineo } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { TIPOS_SANGUINEOS, statusGrupo, converterBolsaParaLote } from "@/lib/estoque";
import { buscarEstoque } from "@/services/api";
import { FiltroTipoEstoque } from "@/components/estoque/FiltroTipoEstoque";
import { CartaoLote } from "@/components/estoque/CartaoLote";
import { PontoStatus } from "@/components/ui/PontoStatus";

interface GrupoEstoque
{
  tipo: TipoSanguineo;
  lotes: LoteHemocomponente[];
  unidades: number;
}

export default function PaginaEstoque()
{
  const { lotes: lotesMock } = useDados();
  const [lotes, setLotes] = useState<LoteHemocomponente[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [fonte, setFonte] = useState<"banco" | "mock">("banco");
  const [erro, setErro] = useState<string | null>(null);
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");

  async function carregarEstoqueDoBanco()
  {
    try
    {
      setCarregando(true);
      const res = await buscarEstoque("BS-01");
      if (res && res.bolsas && res.bolsas.length > 0)
      {
        const convertidos = res.bolsas.map(converterBolsaParaLote);
        setLotes(convertidos);
        setFonte("banco");
        setErro(null);
      }
      else
      {
        setLotes([]);
        setFonte("banco");
        setErro(null);
      }
    }
    catch (err: any)
    {
      console.warn("Falha ao carregar estoque do Supabase, usando mocks locais:", err);
      setLotes(lotesMock);
      setFonte("mock");
      setErro(err.message || "Não foi possível conectar com o back-end.");
    }
    finally
    {
      setCarregando(false);
    }
  }

  useEffect(() =>
  {
    carregarEstoqueDoBanco();
  }, []);

  const listaExibicao = lotes.length > 0 ? lotes : (fonte === "mock" ? lotesMock : []);

  const grupos = useMemo<GrupoEstoque[]>(() =>
  {
    const tipos = filtroTipo === "todos" ? TIPOS_SANGUINEOS : [filtroTipo];
    return tipos
      .map((tipo) =>
      {
        const lotesDoTipo = listaExibicao.filter((lote) => lote.tipoSanguineo === tipo);
        return {
          tipo,
          lotes: lotesDoTipo,
          unidades: lotesDoTipo.reduce((total, lote) => total + lote.unidades, 0),
        };
      })
      .filter((grupo) => grupo.lotes.length > 0);
  }, [listaExibicao, filtroTipo]);

  const totalBolsasGeral = useMemo(() =>
  {
    return listaExibicao.reduce((acc, l) => acc + l.unidades, 0);
  }, [listaExibicao]);

  return (
    <div className="space-y-6">
      {/* Cabeçalho com indicador de conexão real */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Armazém · Hemocomponentes
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Estoque por tipo sanguíneo</h1>
        </div>

        <div className="flex items-center gap-3">
          {fonte === "banco" ? (
            <div className="flex items-center gap-2 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-800 shadow-sm">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" />
              <span>
                <strong>Supabase Conectado:</strong> {totalBolsasGeral} bolsa(s) no Banco BS-01
              </span>
            </div>
          ) : (
            <div className="flex items-center gap-2 rounded-lg border border-amber-200 bg-amber-50 px-3 py-1.5 text-xs font-medium text-amber-800 shadow-sm">
              <AlertCircle className="h-4 w-4 text-amber-600" />
              <span>Modo Demonstração (Mocks locais)</span>
            </div>
          )}

          <button
            type="button"
            onClick={() => carregarEstoqueDoBanco()}
            disabled={carregando}
            className="flex items-center gap-2 rounded-lg border border-rota-border bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 shadow-sm hover:bg-gray-50 active:scale-95 disabled:opacity-50"
            title="Sincronizar estoque do banco de dados agora"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${carregando ? "animate-spin text-rota-red" : ""}`} />
            {carregando ? "Sincronizando..." : "Sincronizar Banco"}
          </button>
        </div>
      </div>

      {erro && (
        <div className="flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 p-3.5 text-xs text-rose-800">
          <AlertCircle className="h-4 w-4 shrink-0 text-rose-600" />
          <span>
            <strong>Aviso de Conexão:</strong> {erro}. Exibindo dados locais de contingência.
          </span>
        </div>
      )}

      <FiltroTipoEstoque value={filtroTipo} onChange={setFiltroTipo} />

      {carregando && listaExibicao.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-rota-border bg-white p-12 text-center">
          <Database className="h-8 w-8 animate-bounce text-rota-red" />
          <p className="mt-3 text-sm font-semibold text-gray-700">Consultando estoque no Supabase PostgreSQL...</p>
          <p className="text-xs text-gray-400">Endpoint: GET /api/v1/bancos/BS-01/estoque</p>
        </div>
      ) : grupos.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Nenhum lote em estoque para este tipo sanguíneo.
        </p>
      ) : (
        grupos.map((grupo) => (
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
        ))
      )}
    </div>
  );
}
