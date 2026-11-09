import { useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { MapPin, Route, Thermometer, Truck, X } from "lucide-react";
import type { PontoDeRede, TipoComponente } from "@/types";
import { hospitais, hemocentro } from "@/data/redeMock";
import { COMPONENTES, formatarFaixa, TRANSPORTE_POR_COMPONENTE } from "@/lib/rede";
import type { RotaCalculada } from "@/lib/roteirizacao";
import estoqueRede from "@/data/estoqueRedeHospitalar.json";

type EstoqueTipoSanguineo = Record<string, number>;
type EstoqueRedeHospitalar = Record<string, Record<TipoComponente, EstoqueTipoSanguineo>>;

const estoqueHospitais = estoqueRede as EstoqueRedeHospitalar;

interface Props
{
  hospital: PontoDeRede;
  rota: RotaCalculada;
  onClose: () => void;
}

function Linha({
  icone,
  rotulo,
  children,
}: Readonly<{ icone: ReactNode; rotulo: string; children: ReactNode }>)
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

export function ModalTransacaoHemocomponentes({ hospital, rota, onClose }: Readonly<Props>)
{
  const navigate = useNavigate();
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");
  const [doadorId, setDoadorId] = useState<string | null>(null);
  const [transferenciaAtiva, setTransferenciaAtiva] = useState(false);
  const [progressoTransferencia, setProgressoTransferencia] = useState(0);
  const [transferenciaConcluida, setTransferenciaConcluida] = useState(false);
  const timerRef = useRef<number | null>(null);

  function totalPorComponente(hospitalId: string, componenteAtual: TipoComponente)
  {
    return Object.values(estoqueHospitais[hospitalId]?.[componenteAtual] ?? {}).reduce(
      (total, quantidade) => total + quantidade,
      0,
    );
  }

  const hospitaisDisponiveis = useMemo(
    () =>
      hospitais.filter(
        (hospitalAtual) =>
          hospitalAtual.id !== hospital.id && totalPorComponente(hospitalAtual.id, componente) > 0,
      ),
    [componente, hospital.id],
  );

  const hospitaisComEstoque = useMemo(
    () =>
      hospitais.filter(
        (hospitalAtual) => totalPorComponente(hospitalAtual.id, componente) > 0,
      ),
    [componente],
  );

  const melhorDoador = useMemo(() =>
  {
    if (hospitaisDisponiveis.length === 0)
    {
      return null;
    }

    return hospitaisDisponiveis.reduce((melhor, hospitalAtual) =>
      totalPorComponente(hospitalAtual.id, componente) > totalPorComponente(melhor.id, componente)
        ? hospitalAtual
        : melhor,
    );
  }, [componente, hospitaisDisponiveis]);

  const doador = hospitais.find((hospitalAtual) => hospitalAtual.id === doadorId) ?? null;
  const totalDoador = doador ? totalPorComponente(doador.id, componente) : 0;

  useEffect(() =>
  {
    if (!melhorDoador)
    {
      setDoadorId(null);
      return;
    }

    setDoadorId(melhorDoador.id);
  }, [melhorDoador]);

  useEffect(() =>
  {
    const limparTimer = () =>
    {
      if (timerRef.current != null)
      {
        window.clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };

    if (!transferenciaAtiva)
    {
      limparTimer();
      return;
    }

    const duracaoTotalMs = 30_000;
    const inicio = Date.now();

    timerRef.current = window.setInterval(() =>
    {
      const progresso = Math.min(((Date.now() - inicio) / duracaoTotalMs) * 100, 100);
      setProgressoTransferencia(progresso);

      if (progresso >= 100)
      {
        limparTimer();
        setTransferenciaAtiva(false);
        setTransferenciaConcluida(true);
      }
    }, 250);

    return () =>
    {
      limparTimer();
    };
  }, [transferenciaAtiva]);

  function iniciarTransferencia()
  {
    if (!doador)
    {
      return;
    }

    setTransferenciaConcluida(false);
    setProgressoTransferencia(0);
    setTransferenciaAtiva(true);
  }

  function abrirCampanhaDeArrecadacao()
  {
    navigate("/requisicao", { state: { aba: "campanha" } });
    onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-rota-border bg-white p-6 shadow-card"
        onClick={(evento) => evento.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="titulo-transacao-hemocomponentes"
      >
        <div className="mb-4 flex items-start justify-between gap-3">
          <div>
            <h2
              id="titulo-transacao-hemocomponentes"
              className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500"
            >
              Transferência de sangue
            </h2>
            <p className="mt-1 text-lg font-bold text-gray-900">{hospital.nome}</p>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-4">
          <div className="rounded-xl border border-rota-border bg-rota-surface2 p-3">
            <p className="font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              Origem da rede
            </p>
            <p className="mt-1 text-sm font-semibold text-gray-900">{hemocentro.nome}</p>
            <p className="text-xs text-gray-500">Hemocentro central · rede logística ativa</p>
          </div>

          <div className="space-y-3">
            <Linha icone={<MapPin className="h-4 w-4" />} rotulo="Destino">
              {hospital.nome}
            </Linha>
            <Linha icone={<Route className="h-4 w-4" />} rotulo="Melhor rota">
              {rota.distanciaKm} km · {rota.tempoMin} min
            </Linha>
            <Linha icone={<Truck className="h-4 w-4" />} rotulo="Cadeia fria">
              Veículo refrigerado monitorado
            </Linha>
          </div>

          <div className="space-y-2">
            <label
              htmlFor="modal-hemocomponente"
              className="block font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-400"
            >
              Hemocomponente
            </label>
            <select
              id="modal-hemocomponente"
              aria-label="Hemocomponente"
              value={componente}
              onChange={(evento) => setComponente(evento.target.value as TipoComponente)}
              className="w-full rounded-lg border border-rota-border bg-white px-3 py-2 text-sm text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
            >
              {COMPONENTES.map((opcao) => (
                <option key={opcao} value={opcao}>{opcao}</option>
              ))}
            </select>
          </div>

          <div className="rounded-xl border border-rota-border bg-white p-3">
            <p className="font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-400">
              Hospitais da rede com estoque
            </p>
            {hospitaisComEstoque.length === 0 ? (
              <p className="mt-2 text-sm text-gray-500">
                Nenhum hospital da rede possui {componente} no momento.
              </p>
            ) : (
              <ul className="mt-2 space-y-1 text-sm text-gray-700">
                {hospitaisComEstoque.map((hospitalAtual) => (
                  <li key={hospitalAtual.id} className="flex items-center justify-between gap-2">
                    <span>{hospitalAtual.nome}</span>
                    <span className="font-semibold text-rota-red">
                      {totalPorComponente(hospitalAtual.id, componente)} un.
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="rounded-xl border border-rota-red/20 bg-red-50/60 p-3">
            <p className="font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              Doação sugerida
            </p>
            {!doador ? (
              <div className="mt-3 space-y-3">
                <p className="text-sm text-gray-700">
                  Lamentamos, mas esse hemocomponente está em falta na rede nesse momento. Deseja
                  iniciar uma campanha de arrecadação?
                </p>
                <button
                  type="button"
                  onClick={abrirCampanhaDeArrecadacao}
                  className="rounded-lg bg-rota-red px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
                >
                  Sim
                </button>
              </div>
            ) : (
              <>
                <p className="mt-2 text-base font-bold text-gray-900">{doador.nome}</p>
                <p className="text-sm text-gray-600">{doador.endereco}</p>
                <p className="mt-2 text-sm text-rota-red">
                  {totalDoador} unidades disponíveis para {componente}.
                </p>

                {!transferenciaAtiva && !transferenciaConcluida && (
                  <button
                    type="button"
                    onClick={iniciarTransferencia}
                    className="mt-3 inline-flex w-full items-center justify-center rounded-lg bg-rota-red px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
                  >
                    Iniciar transferência
                  </button>
                )}

                {(transferenciaAtiva || transferenciaConcluida) && (
                  <div className="mt-3 rounded-lg border border-blue-200 bg-blue-50 p-3">
                    <div className="mb-2 flex items-center justify-between gap-3 text-xs font-semibold text-blue-700">
                      <span>{transferenciaConcluida ? "Transferência concluída" : "Transferência em andamento"}</span>
                      <span>{Math.round(progressoTransferencia)}%</span>
                    </div>
                    <div className="h-2.5 overflow-hidden rounded-full bg-blue-100">
                      <div
                        className="h-full rounded-full bg-blue-600 transition-all duration-300"
                        style={{ width: `${progressoTransferencia}%` }}
                      />
                    </div>
                    <p className="mt-2 text-xs text-blue-700">
                      {doador.nome} → {hospital.nome} · {componente}
                    </p>
                  </div>
                )}
              </>
            )}
          </div>

          <div className="flex items-start gap-3 rounded-lg bg-blue-50 p-3 text-blue-800">
            <Thermometer className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
            <div>
              <p className="text-sm font-semibold">Temperatura ideal</p>
              <p className="font-mono text-xs">
                {formatarFaixa(componente)} · {TRANSPORTE_POR_COMPONENTE[componente]}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
