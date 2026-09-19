import { useMemo, useState } from "react";
import { useDados } from "@/context/ContextoDados";
import {
  montarAvisos,
  montarIndicadores,
  ordenarAvisos,
  type OrdenacaoAvisos,
} from "@/lib/painel";
import { CartaoIndicador } from "@/components/painel/CartaoIndicador";
import { CaixaAvisos } from "@/components/painel/CaixaAvisos";
import { AcoesRapidas } from "@/components/painel/AcoesRapidas";

/**
 * HU-02 — Painel operacional: números rápidos, caixa de avisos ordenável e
 * atalhos, para o médico agir sem percorrer todas as telas. Indicadores e
 * avisos derivam dos mocks de estoque e de pacientes (ver `lib/painel.ts`).
 */
export default function PaginaPainel()
{
  const { lotes, pacientes } = useDados();
  const [ordenacao, setOrdenacao] = useState<OrdenacaoAvisos>("severidade");

  const avisos = useMemo(() => montarAvisos(lotes, pacientes), [lotes, pacientes]);
  const indicadores = useMemo(() => montarIndicadores(lotes, avisos), [lotes, avisos]);
  const avisosOrdenados = useMemo(() => ordenarAvisos(avisos, ordenacao), [avisos, ordenacao]);

  return (
    <div className="space-y-8">
      <div>
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Painel operacional · Rota Vital
        </p>
        <h1 className="mt-1 max-w-2xl text-3xl font-extrabold leading-tight text-gray-900">
          Estoque inteligente de hemocomponentes
        </h1>
        <p className="mt-2 max-w-2xl text-sm text-gray-500">
          Números em tempo real, alertas de desabastecimento e vencimento, e coordenação de envios
          entre a rede de hospitais.
        </p>
      </div>

      <section className="space-y-3 border-t border-rota-border pt-6">
        <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-500">
          Números rápidos
        </h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {indicadores.map((indicador) => (
            <CartaoIndicador key={indicador.id} indicador={indicador} />
          ))}
        </div>
      </section>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <CaixaAvisos
            avisos={avisosOrdenados}
            ordenacao={ordenacao}
            onOrdenacaoChange={setOrdenacao}
          />
        </div>
        <AcoesRapidas />
      </div>
    </div>
  );
}

