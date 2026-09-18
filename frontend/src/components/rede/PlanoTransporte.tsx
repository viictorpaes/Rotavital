import { MapPin, Route, Thermometer, Truck } from "lucide-react";
import type { PontoDeRede, TipoComponente } from "@/types";
import { conexaoDe, COMPONENTES, formatarFaixa, TRANSPORTE_POR_COMPONENTE } from "@/lib/rede";

interface Props
{
  destino: PontoDeRede;
  componente: TipoComponente;
  onComponenteChange: (componente: TipoComponente) => void;
}

function Linha({
  icone,
  rotulo,
  children,
}: Readonly<{ icone: React.ReactNode; rotulo: string; children: React.ReactNode }>)
{
  return (
    <div className="flex items-start gap-3">
      <span className="mt-0.5 shrink-0 text-gray-400" aria-hidden>{icone}</span>
      <div className="min-w-0 flex-1">
        <p className="font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-400">
          {rotulo}
        </p>
        <p className="text-sm text-gray-900">{children}</p>
      </div>
    </div>
  );
}

/** Plano de transporte na cadeia fria para o hospital selecionado (HU-07). */
export function PlanoTransporte({ destino, componente, onComponenteChange }: Readonly<Props>)
{
  const conexao = conexaoDe(destino.id);

  return (
    <section className="space-y-4 rounded-xl border border-rota-border bg-white p-5">
      <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
        Plano de transporte
      </h2>

      <div className="space-y-3">
        <Linha icone={<MapPin className="h-4 w-4" />} rotulo="Destino">
          {destino.nome}
        </Linha>
        <Linha icone={<MapPin className="h-4 w-4" />} rotulo="Endereço">
          <span className="text-gray-500">{destino.endereco}</span>
        </Linha>
        <Linha icone={<Route className="h-4 w-4" />} rotulo="Melhor rota">
          {conexao ? `${conexao.distanciaKm} km · ${conexao.tempoMin} min estimados` : "—"}
        </Linha>
        <Linha icone={<Truck className="h-4 w-4" />} rotulo="Transporte">
          Veículo refrigerado monitorado
        </Linha>
      </div>

      <div className="space-y-2">
        <label
          htmlFor="hemocomponente"
          className="block font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-400"
        >
          Hemocomponente a enviar
        </label>
        <select
          id="hemocomponente"
          value={componente}
          onChange={(evento) => onComponenteChange(evento.target.value as TipoComponente)}
          className="w-full rounded-lg border border-rota-border bg-white px-3 py-2 text-sm text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
        >
          {COMPONENTES.map((opcao) => (
            <option key={opcao} value={opcao}>{opcao}</option>
          ))}
        </select>
      </div>

      <div className="flex items-start gap-3 rounded-lg bg-blue-50 p-3 text-blue-800">
        <Thermometer className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
        <div>
          <p className="text-sm font-semibold">Temperatura ideal de transporte</p>
          <p className="font-mono text-xs">
            {formatarFaixa(componente)} · {TRANSPORTE_POR_COMPONENTE[componente]}
          </p>
        </div>
      </div>
    </section>
  );
}
