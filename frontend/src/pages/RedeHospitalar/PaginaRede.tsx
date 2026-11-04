import { useMemo, useState } from "react";
import type { TipoComponente } from "@/types";
import type { PontoRedeDTO } from "@/types/dtos";
import { usePontosDaRede } from "@/hooks/usePontosDaRede";
import { useRotaNoMapa } from "@/hooks/useRotaNoMapa";
import { useRotasDaRede } from "@/hooks/useRotasDaRede";
import { COR_DO_DESTAQUE, type DestaqueDoPonto } from "@/lib/marcadoresDaRede";
import { ListaHospitais } from "@/components/rede/ListaHospitais";
import { MapaRede } from "@/components/rede/MapaRede";
import { PlanoTransporte } from "@/components/rede/PlanoTransporte";
import { PrioridadeEnvio } from "@/components/rede/PrioridadeEnvio";

const LEGENDA: Array<{ destaque: DestaqueDoPonto; rotulo: string }> =
[
  { destaque: "origem", rotulo: "Origem e hemocentros" },
  { destaque: "destino", rotulo: "Destino" },
  { destaque: "parada", rotulo: "Parada do caminho" },
  { destaque: "normal", rotulo: "Hospitais conveniados" },
];

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

function Rede({ origemInicial, hospitais, pontos }: Readonly<{
  origemInicial: PontoRedeDTO;
  hospitais: PontoRedeDTO[];
  pontos: PontoRedeDTO[];
}>)
{
  const [origem, setOrigem] = useState(origemInicial);
  const [destino, setDestino] = useState(hospitais[0]);
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");

  const destinos = useMemo(() => hospitais.filter((hospital) => hospital.id !== origem.id), [hospitais, origem.id]);
  const rotas = useRotasDaRede(origem.id, destinos);
  const { rota, caminho, tracado, situacao, erro } = useRotaNoMapa(origem, destino, pontos);

  const destaques = useMemo(() =>
  {
    const papeis: Record<string, DestaqueDoPonto> = {};
    caminho.forEach((ponto) =>
    {
      papeis[ponto.id] = "parada";
    });
    papeis[origem.id] = "origem";
    papeis[destino.id] = "destino";
    return papeis;
  }, [caminho, origem.id, destino.id]);

  function trocarOrigem(id: string)
  {
    const nova = pontos.find((ponto) => ponto.id === id);
    if (!nova) return;

    setOrigem(nova);
    // A origem não pode ser o próprio destino: passa para o primeiro outro hospital.
    if (nova.id === destino.id)
    {
      setDestino(hospitais.find((hospital) => hospital.id !== nova.id) ?? destino);
    }
  }

  function selecionar(ponto: PontoRedeDTO)
  {
    if (ponto.tipo === "HOSPITAL" && ponto.id !== origem.id) setDestino(ponto);
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
      <section className="self-start overflow-hidden rounded-xl border border-rota-border bg-white lg:col-span-3">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-rota-border px-5 py-4">
          <div>
            <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
              Mapa da rede · Recife — PE
            </h2>
            <p className="mt-1 font-mono text-[11px] text-gray-500">
              {situacao === "carregando" ? "Calculando a rota…" : "Clique em um hospital para calcular a rota"}
            </p>
          </div>
          <label className="flex items-center gap-2 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-400">
            Origem
            <select
              value={origem.id}
              onChange={(evento) => trocarOrigem(evento.target.value)}
              className="max-w-[12rem] rounded-lg border border-rota-border bg-white px-2 py-1.5 text-xs normal-case text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
            >
              {pontos.map((ponto) => (
                <option key={ponto.id} value={ponto.id}>{ponto.nome}</option>
              ))}
            </select>
          </label>
        </div>

        <MapaRede pontos={pontos} destaques={destaques} tracado={tracado} onSelecionar={selecionar} />

        <ul className="flex flex-wrap items-center gap-4 border-t border-rota-border px-5 py-3 font-mono text-[11px] text-gray-500">
          {LEGENDA.map(({ destaque, rotulo }) => (
            <li key={destaque} className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full" style={{ background: COR_DO_DESTAQUE[destaque] }} /> {rotulo}
            </li>
          ))}
        </ul>
      </section>

      <div className="space-y-4 lg:col-span-2">
        <ListaHospitais hospitais={destinos} selecionadoId={destino.id} rotas={rotas} onSelecionar={setDestino} />
        <PlanoTransporte
          origem={origem}
          destino={destino}
          rota={rota}
          caminho={caminho}
          semRota={situacao === "erro" ? erro : "calculando…"}
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
 * Os pontos vêm do backend (PI3-258) e a rota, do Dijkstra levado para as ruas (PI3-259).
 * Selecionar um hospital, no mapa ou na lista, recalcula a rota e o plano.
 */
export default function PaginaRede()
{
  const { pontos, situacao, erro } = usePontosDaRede();
  const hospitais = useMemo(() => pontos.filter((ponto) => ponto.tipo === "HOSPITAL"), [pontos]);
  const hemocentro = pontos.find((ponto) => ponto.tipo === "BANCO_DE_SANGUE");

  return (
    <div className="space-y-6">
      <Cabecalho />

      {situacao === "carregando" && <p className="text-sm text-gray-500">Carregando a rede…</p>}
      {situacao === "erro" && <p className="text-sm text-rota-red">{erro}</p>}
      {situacao === "pronto" && (!hemocentro || hospitais.length === 0) && (
        <p className="text-sm text-gray-500">A rede ainda não tem hemocentro e hospitais cadastrados.</p>
      )}
      {situacao === "pronto" && hemocentro && hospitais.length > 0 && (
        <Rede origemInicial={hemocentro} hospitais={hospitais} pontos={pontos} />
      )}
    </div>
  );
}
