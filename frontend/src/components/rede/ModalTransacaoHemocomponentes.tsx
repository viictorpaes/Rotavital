import { useMemo, useState } from "react";
import { ArrowRightLeft, ClipboardList, MapPin, Package, Route, ShieldAlert, ShieldCheck, Thermometer, Truck, X } from "lucide-react";
import type { PontoDeRede, TipoComponente, UrgenciaNecessidade } from "@/types";
import { lotesEstoque } from "@/data/estoqueMock";
import { COMPONENTES, formatarFaixa, TRANSPORTE_POR_COMPONENTE } from "@/lib/rede";
import type { RotaCalculada } from "@/lib/roteirizacao";
import { hemocentro } from "@/data/redeMock";

const URGENCIAS: Array<{ valor: UrgenciaNecessidade; rotulo: string; descricao: string }> = [
  { valor: "critico", rotulo: "Crítico", descricao: "Urgência imediata" },
  { valor: "atencao", rotulo: "Atenção", descricao: "Necessidade prioritária" },
  { valor: "estavel", rotulo: "Estável", descricao: "Planejado" },
];

interface Props {
  hospital: PontoDeRede;
  rota: RotaCalculada;
  onClose: () => void;
}

function LinhaInfo({
  rotulo,
  valor,
}: Readonly<{ rotulo: string; valor: string }>) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-lg border border-rota-border bg-white px-3 py-2">
      <span className="font-mono text-[10px] font-semibold uppercase tracking-wide text-gray-400">
        {rotulo}
      </span>
      <span className="text-sm font-medium text-gray-900">{valor}</span>
    </div>
  );
}

export function ModalTransacaoHemocomponentes({ hospital, rota, onClose }: Readonly<Props>) {
  const [modo, setModo] = useState<"enviar" | "solicitar">("enviar");
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");
  const [quantidade, setQuantidade] = useState<number>(2);
  const [urgencia, setUrgencia] = useState<UrgenciaNecessidade>("atencao");
  const [selecionado, setSelecionado] = useState<string>("");

  const lotesDisponiveis = useMemo(
    () => lotesEstoque.filter((lote) => lote.componente === componente),
    [componente],
  );

  const minimoSolicitacao = useMemo(() => {
    if (componente === "Concentrado de Plaquetas") return 1;
    if (componente === "Crioprecipitado") return 2;
    if (componente === "Plasma Fresco Congelado") return 3;
    return 2;
  }, [componente]);

  const totalDisponivel = lotesDisponiveis.reduce((soma, lote) => soma + lote.unidades, 0);
  const confirmarTexto = modo === "enviar" ? "Confirmar envio" : "Enviar solicitação";
  const faixa = formatarFaixa(componente);
  const transporte = TRANSPORTE_POR_COMPONENTE[componente];

  const handleTipoComponenteChange = (valor: string) => {
    const novo = valor as TipoComponente;
    setComponente(novo);
    setSelecionado("");
    setQuantidade(Math.max(1, minimoSolicitacao));
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl border border-rota-border bg-white shadow-card"
        onClick={(evento) => evento.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="transacao-title"
      >
        <div className="flex items-start justify-between border-b border-rota-border px-6 py-4">
          <div>
            <p className="font-mono text-[11px] font-bold uppercase tracking-widest text-gray-400">
              Hemocomponentes · {hospital.nome}
            </p>
            <h2 id="transacao-title" className="mt-1 text-xl font-bold text-gray-900">
              Enviar ou solicitar componente
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-2 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700"
            aria-label="Fechar modal"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="space-y-6 px-6 py-5">
          <div className="overflow-hidden rounded-xl border border-rota-border bg-rota-surface2 p-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-mono text-[10px] font-bold uppercase tracking-[0.14em] text-gray-400">
                  Melhor rota
                </p>
                <p className="mt-1 text-base font-semibold text-gray-900">
                  {hemocentro.nome} → {hospital.nome}
                </p>
              </div>
              <span className="rounded-full bg-white px-2.5 py-1 font-mono text-[10px] font-semibold text-rota-red">
                {rota.distanciaKm} km · {rota.tempoMin} min
              </span>
            </div>

            <div className="mt-4 grid gap-2 sm:grid-cols-3">
              <LinhaInfo rotulo="Destino" valor={hospital.nome} />
              <LinhaInfo rotulo="Transporte" valor="Refrigerado" />
              <LinhaInfo rotulo="Conservação" valor={faixa} />
            </div>
          </div>

          <div className="grid gap-4 lg:grid-cols-[1.3fr_0.7fr]">
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-2 rounded-xl bg-rota-surface2 p-1">
                {(["enviar", "solicitar"] as const).map((opcao) => (
                  <button
                    key={opcao}
                    type="button"
                    onClick={() => setModo(opcao)}
                    className={[
                      "flex items-center justify-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold transition",
                      modo === opcao ? "bg-white text-rota-red shadow-sm" : "text-gray-600",
                    ].join(" ")}
                  >
                    {opcao === "enviar" ? <ArrowRightLeft className="h-4 w-4" /> : <ClipboardList className="h-4 w-4" />}
                    {opcao === "enviar" ? "Enviar" : "Solicitar"}
                  </button>
                ))}
              </div>

              <div className="space-y-4">
                <div className="space-y-2">
                  <label
                    htmlFor="tipo-componente"
                    className="block font-mono text-[11px] font-semibold uppercase tracking-[0.12em] text-gray-400"
                  >
                    Hemocomponente
                  </label>
                  <select
                    id="tipo-componente"
                    value={componente}
                    onChange={(evento) => handleTipoComponenteChange(evento.target.value)}
                    className="w-full rounded-lg border border-rota-border bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
                  >
                    {COMPONENTES.map((opcao) => (
                      <option key={opcao} value={opcao}>
                        {opcao}
                      </option>
                    ))}
                  </select>
                </div>

                {modo === "enviar" ? (
                  <div className="space-y-3 rounded-xl border border-rota-border bg-white p-4">
                    <div className="flex items-center gap-2 text-sm font-semibold text-gray-900">
                      <Package className="h-4 w-4 text-rota-red" />
                      Estoque disponível
                    </div>
                    <p className="text-sm text-gray-600">
                      Há <span className="font-bold text-gray-900">{totalDisponivel}</span> unidades de {componente} em estoque.
                    </p>

                    <div className="space-y-2">
                      {lotesDisponiveis.length === 0 ? (
                        <p className="text-sm text-gray-500">Nenhum lote disponível para este componente.</p>
                      ) : (
                        lotesDisponiveis.map((lote) => (
                          <button
                            key={lote.id}
                            type="button"
                            onClick={() => setSelecionado(lote.id)}
                            className={[
                              "flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left transition",
                              selecionado === lote.id
                                ? "border-rota-red bg-rota-surface2"
                                : "border-rota-border bg-white hover:bg-rota-surface2",
                            ].join(" ")}
                          >
                            <div>
                              <p className="text-sm font-semibold text-gray-900">{lote.codigo}</p>
                              <p className="font-mono text-[11px] text-gray-500">
                                {lote.unidades} un. · {lote.tipoSanguineo}
                              </p>
                            </div>
                            <span className="font-mono text-[11px] text-gray-500">{lote.dataValidade}</span>
                          </button>
                        ))
                      )}
                    </div>

                    <div className="space-y-2 pt-2">
                      <label
                        htmlFor="qtd-envio"
                        className="block font-mono text-[11px] font-semibold uppercase tracking-[0.12em] text-gray-400"
                      >
                        Quantidade a enviar
                      </label>
                      <input
                        id="qtd-envio"
                        type="number"
                        min={1}
                        max={Math.max(1, totalDisponivel)}
                        value={quantidade}
                        onChange={(evento) => setQuantidade(Math.max(1, Number(evento.target.value || 1)))}
                        className="w-full rounded-lg border border-rota-border bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
                      />
                    </div>
                  </div>
                ) : (
                  <div className="space-y-4 rounded-xl border border-rota-border bg-white p-4">
                    <div className="space-y-2">
                      <label
                        htmlFor="qtd-solicitacao"
                        className="block font-mono text-[11px] font-semibold uppercase tracking-[0.12em] text-gray-400"
                      >
                        Quantidade mínima solicitada
                      </label>
                      <input
                        id="qtd-solicitacao"
                        type="number"
                        min={minimoSolicitacao}
                        value={quantidade}
                        onChange={(evento) =>
                          setQuantidade(Math.max(minimoSolicitacao, Number(evento.target.value || minimoSolicitacao)))
                        }
                        className="w-full rounded-lg border border-rota-border bg-white px-3 py-2.5 text-sm text-gray-900 focus:border-rota-red focus:outline-none focus:ring-1 focus:ring-rota-red"
                      />
                      <p className="font-mono text-[11px] text-gray-500">
                        Mínimo recomendado: {minimoSolicitacao} unidade(s)
                      </p>
                    </div>

                    <div className="space-y-3">
                      <p className="font-mono text-[11px] font-semibold uppercase tracking-[0.12em] text-gray-400">
                        Nível de urgência da necessidade
                      </p>
                      <div className="space-y-2">
                        {URGENCIAS.map((item) => (
                          <label
                            key={item.valor}
                            className={[
                              "flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition",
                              urgencia === item.valor ? "border-rota-red bg-rota-surface2" : "border-rota-border bg-white",
                            ].join(" ")}
                          >
                            <input
                              type="radio"
                              name="urgencia-necessidade"
                              value={item.valor}
                              checked={urgencia === item.valor}
                              onChange={() => setUrgencia(item.valor)}
                              className="mt-1 h-4 w-4 accent-rota-red"
                            />
                            <div className="min-w-0">
                              <span className="block text-sm font-semibold text-gray-900">{item.rotulo}</span>
                              <span className="block text-xs text-gray-500">{item.descricao}</span>
                            </div>
                          </label>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>

            <aside className="space-y-4 rounded-xl border border-rota-border bg-rota-surface2 p-4">
              <div className="flex items-center gap-2 text-sm font-semibold text-gray-900">
                <Thermometer className="h-4 w-4 text-rota-red" />
                Cadeia fria
              </div>

              <div className="space-y-3">
                <div className="rounded-lg border border-blue-200 bg-blue-50 p-3">
                  <p className="font-mono text-[10px] font-semibold uppercase tracking-wide text-blue-700">
                    Temperatura ideal
                  </p>
                  <p className="mt-1 text-sm font-semibold text-blue-900">{faixa}</p>
                </div>

                <div className="rounded-lg border border-amber-200 bg-amber-50 p-3">
                  <p className="font-mono text-[10px] font-semibold uppercase tracking-wide text-amber-700">
                    Embalagem
                  </p>
                  <p className="mt-1 text-sm font-semibold text-amber-900">{transporte}</p>
                </div>

                <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-3">
                  <p className="font-mono text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
                    Logística
                  </p>
                  <p className="mt-1 text-sm font-semibold text-emerald-900">
                    {rota.tempoMin} min até o destino com rastreio em tempo real.
                  </p>
                </div>
              </div>

              <div className="space-y-2 rounded-lg border border-rota-border bg-white p-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-gray-900">
                  <Route className="h-4 w-4 text-rota-red" />
                  Rota
                </div>
                <p className="flex items-center gap-2 text-sm text-gray-600">
                  <MapPin className="h-3.5 w-3.5 text-rota-red" />
                  {hospital.endereco}
                </p>
                <p className="flex items-center gap-2 text-sm text-gray-600">
                  <Truck className="h-3.5 w-3.5 text-rota-red" />
                  Veículo refrigerado monitorado
                </p>
              </div>
            </aside>
          </div>

          <div className="rounded-xl border border-rota-border bg-rota-surface2 p-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-gray-900">
              <ShieldAlert className="h-4 w-4 text-rota-red" />
              Recomendação do sistema
            </div>
            <p className="mt-2 text-sm text-gray-600">
              {modo === "enviar"
                ? `Enviar ${quantidade} unidade(s) de ${componente} para ${hospital.nome} no próximo turno logístico.`
                : `Solicitar ${quantidade} unidade(s) de ${componente} com urgência ${urgencia} para ${hospital.nome}.`}
            </p>
          </div>

          <div className="flex items-start gap-3 rounded-lg border border-emerald-100 bg-emerald-50 p-3 text-emerald-800">
            <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
            <div>
              <p className="text-sm font-semibold">Critério de validação</p>
              <p className="text-xs text-emerald-700">
                A viagem deve ser concluída em até {Math.max(rota.tempoMin, 20)} minutos e sem variação superior a {faixa}.
              </p>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-rota-border bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 hover:bg-rota-surface2"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white hover:bg-rota-redDark"
            >
              {confirmarTexto}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
