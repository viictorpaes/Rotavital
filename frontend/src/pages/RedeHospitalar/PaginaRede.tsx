import { useMemo, useState } from "react";
import type { TipoComponente } from "@/types";
import type { PontoRedeDTO } from "@/types/dtos";
import { usePontosDaRede } from "@/hooks/usePontosDaRede";
import { useRotasDaRede } from "@/hooks/useRotasDaRede";
import { ListaHospitais } from "@/components/rede/ListaHospitais";
import { MapaRede } from "@/components/rede/MapaRede";
import { PlanoTransporte } from "@/components/rede/PlanoTransporte";
import { PrioridadeEnvio } from "@/components/rede/PrioridadeEnvio";

function Cabecalho()
{
  return (
    <header className="border-b border-rota-border pb-6">
      <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
        Rede integrada · Envios inter-hospitalares
      </p>
      <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Hospitais conectados</h1>
    </header>
  );
}

function Rede({ origem, hospitais, pontos }: Readonly<{
  origem: PontoRedeDTO;
  hospitais: PontoRedeDTO[];
  pontos: PontoRedeDTO[];
}>)
{
  const [selecionado, setSelecionado] = useState<PontoRedeDTO>(hospitais[0]);
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");
  const { rotas } = useRotasDaRede(origem, hospitais);
  const rota = rotas[selecionado.id];

  function selecionar(ponto: PontoRedeDTO)
  {
    if (ponto.tipo === "HOSPITAL") setSelecionado(ponto);
  }

  return (
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
          pontos={pontos}
          selecionadoId={selecionado.id}
          tracado={rota?.pontos ?? []}
          onSelecionar={selecionar}
        />

        <ul className="flex flex-wrap items-center gap-4 border-t border-rota-border px-5 py-3 font-mono text-[11px] text-gray-500">
          <li className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 bg-rota-red" /> Hemocentro
          </li>
          <li className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full bg-gray-900" /> Selecionado
          </li>
          <li className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full bg-gray-500" /> Hospitais conveniados
          </li>
        </ul>
      </section>

      <div className="space-y-4 lg:col-span-2">
        <ListaHospitais
          hospitais={hospitais}
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
  );
}

/**
 * HU-07 — Mapa dos hospitais conectados e plano de transporte na cadeia fria.
 * Os pontos vêm do backend (PI3-258). Selecionar um hospital, no mapa ou na lista,
 * recalcula a rota e o plano.
 */
export default function PaginaRede()
{
  const { pontos, situacao, erro } = usePontosDaRede();
  const hospitais = useMemo(() => pontos.filter((ponto) => ponto.tipo === "HOSPITAL"), [pontos]);
  const origem = pontos.find((ponto) => ponto.tipo === "BANCO_DE_SANGUE");

  return (
    <div className="space-y-6">
      <Cabecalho />

      {situacao === "carregando" && <p className="text-sm text-gray-500">Carregando a rede…</p>}
      {situacao === "erro" && <p className="text-sm text-rota-red">{erro}</p>}
      {situacao === "pronto" && (!origem || hospitais.length === 0) && (
        <p className="text-sm text-gray-500">A rede ainda não tem hemocentro e hospitais cadastrados.</p>
      )}
      {situacao === "pronto" && origem && hospitais.length > 0 && (
        <Rede origem={origem} hospitais={hospitais} pontos={pontos} />
      )}
    </div>
  );
}
