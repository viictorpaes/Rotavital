import { useState } from "react";
import type { PontoDeRede, TipoComponente } from "@/types";
import { hospitais } from "@/data/redeMock";
import { useRotasDaRede } from "@/hooks/useRotasDaRede";
import { ListaHospitais } from "@/components/rede/ListaHospitais";
import { MapaRede } from "@/components/rede/MapaRede";
import { PlanoTransporte } from "@/components/rede/PlanoTransporte";
import { PrioridadeEnvio } from "@/components/rede/PrioridadeEnvio";

/**
 * HU-07 — Mapa dos hospitais conectados e plano de transporte na cadeia fria.
 * Selecionar um hospital (no mapa ou na lista) recalcula a rota e o plano.
 * Dados mockados nesta primeira versão (ver `data/redeMock.ts`).
 */
export default function PaginaRede()
{
  const [selecionado, setSelecionado] = useState<PontoDeRede>(hospitais[0]);
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");
  const { rotas, carregando } = useRotasDaRede();
  const rota = rotas[selecionado.id];

  return (
    <div className="space-y-6">
      <header className="border-b border-rota-border pb-6">
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Rede integrada · Envios inter-hospitalares
        </p>
        <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Hospitais conectados</h1>
      </header>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <section className="self-start overflow-hidden rounded-xl border border-rota-border bg-white lg:col-span-3">
          <div className="border-b border-rota-border px-5 py-4">
            <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
              Mapa da rede · Recife — PE
            </h2>
            <p className="mt-1 font-mono text-[11px] text-gray-500">
              Clique em um hospital para calcular a rota
            </p>
          </div>

          <MapaRede
            selecionado={selecionado}
            rota={rota}
            calculando={carregando}
            onSelecionar={setSelecionado}
            altura="420px"
          />

          <ul className="flex flex-wrap items-center gap-4 border-t border-rota-border px-5 py-3 font-mono text-[11px] text-gray-500">
            <li className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 bg-rota-red" /> Cesar Life
            </li>
            <li className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full bg-gray-900" /> Selecionado
            </li>
            <li className="flex items-center gap-1.5">
              <span className="h-2 w-2 rounded-full bg-gray-400" /> Outros hospitais
            </li>
          </ul>
        </section>

        <div className="space-y-4 lg:col-span-2">
          <ListaHospitais
            selecionadoId={selecionado.id}
            rotas={rotas}
            onSelecionar={setSelecionado}
          />
          <PlanoTransporte
            destino={selecionado}
            rota={rota}
            componente={componente}
            onComponenteChange={setComponente}
          />
          <PrioridadeEnvio componente={componente} />
        </div>
      </div>
    </div>
  );
}
