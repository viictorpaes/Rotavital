import { useMemo, useState } from "react";
import type { LoteHemocomponente, TipoSanguineo } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { TIPOS_SANGUINEOS, statusGrupo } from "@/lib/estoque";
import { FiltroTipoEstoque } from "@/components/estoque/FiltroTipoEstoque";
import { CartaoLote } from "@/components/estoque/CartaoLote";
import { PontoStatus } from "@/components/ui/PontoStatus";

interface GrupoEstoque
{
  tipo: TipoSanguineo;
  lotes: LoteHemocomponente[];
  unidades: number;
}

/**
 * HU-03 — Estoque agrupado por tipo sanguíneo, com detalhe de lote (validade,
 * temperatura e localização) para identificar risco de vencimento/temperatura.
 * Dados mockados nesta primeira versão (ver `data/estoqueMock.ts`).
 */
export default function PaginaEstoque()
{
  const { lotes: lotesEstoque } = useDados();
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");

  const grupos = useMemo<GrupoEstoque[]>(() =>
  {
    const tipos = filtroTipo === "todos" ? TIPOS_SANGUINEOS : [filtroTipo];
    return tipos
      .map((tipo) =>
      {
        const lotes = lotesEstoque.filter((lote) => lote.tipoSanguineo === tipo);
        return {
          tipo,
          lotes,
          unidades: lotes.reduce((total, lote) => total + lote.unidades, 0),
        };
      })
      .filter((grupo) => grupo.lotes.length > 0);
  }, [lotesEstoque, filtroTipo]);

  return (
    <div className="space-y-6">
      <div>
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Armazém · Hemocomponentes
        </p>
        <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Estoque por tipo sanguíneo</h1>
      </div>

      <FiltroTipoEstoque value={filtroTipo} onChange={setFiltroTipo} />

      {grupos.length === 0 ? (
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
