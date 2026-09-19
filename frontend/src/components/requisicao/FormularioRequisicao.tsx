import { useMemo, useState } from "react";
import { CheckCircle2, MapPin } from "lucide-react";
import type { AlaMedica, RequisicaoHemocomponente, TipoComponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { TIPOS_SANGUINEOS, diasAteVencer, formatarData } from "@/lib/estoque";
import { ALAS_MEDICAS, COMPONENTES, gerarProtocolo, sugerirLoteFefo } from "@/lib/requisicao";
import { cn } from "@/lib/utilitarios";
import { CLASSES_CONTROLE, CampoFormulario } from "./CampoFormulario";
import { SeletorUrgencia } from "./SeletorUrgencia";

/**
 * HU-04 — Requisição de hemocomponente. O painel lateral acompanha o formulário
 * em tempo real e mostra o lote que a regra FEFO sugere para o pedido atual.
 */
export function FormularioRequisicao()
{
  const { lotes } = useDados();

  const [componente, setComponente] = useState<TipoComponente>(COMPONENTES[0]);
  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>("O-");
  const [ala, setAla] = useState<AlaMedica>(ALAS_MEDICAS[0]);
  const [unidades, setUnidades] = useState(1);
  const [paciente, setPaciente] = useState("");
  const [urgencia, setUrgencia] = useState<UrgenciaNecessidade>("atencao");
  const [enviada, setEnviada] = useState<RequisicaoHemocomponente | null>(null);

  const sugestao = useMemo(
    () => sugerirLoteFefo(lotes, componente, tipoSanguineo),
    [lotes, componente, tipoSanguineo],
  );

  function handleSubmit(evento: React.FormEvent)
  {
    evento.preventDefault();
    setEnviada({
      protocolo: gerarProtocolo("REQ"),
      componente,
      tipoSanguineo,
      ala,
      unidades,
      paciente: paciente.trim(),
      urgencia,
      loteSugerido: sugestao?.codigo,
    });
  }

  const dias = sugestao ? diasAteVencer(sugestao) : 0;

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <form
        onSubmit={handleSubmit}
        className="space-y-5 rounded-xl border border-rota-border bg-white p-6 shadow-card lg:col-span-2"
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <CampoFormulario rotulo="Hemocomponente" htmlFor="req-componente">
            <select
              id="req-componente"
              value={componente}
              onChange={(e) => setComponente(e.target.value as TipoComponente)}
              className={CLASSES_CONTROLE}
            >
              {COMPONENTES.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </CampoFormulario>

          <CampoFormulario rotulo="Tipo sanguíneo" htmlFor="req-tipo">
            <select
              id="req-tipo"
              value={tipoSanguineo}
              onChange={(e) => setTipoSanguineo(e.target.value as TipoSanguineo)}
              className={CLASSES_CONTROLE}
            >
              {TIPOS_SANGUINEOS.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </CampoFormulario>

          <CampoFormulario rotulo="Ala médica de destino" htmlFor="req-ala">
            <select
              id="req-ala"
              value={ala}
              onChange={(e) => setAla(e.target.value as AlaMedica)}
              className={CLASSES_CONTROLE}
            >
              {ALAS_MEDICAS.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </CampoFormulario>

          <CampoFormulario rotulo="Quantidade (unidades)" htmlFor="req-unidades">
            <input
              id="req-unidades"
              type="number"
              min={1}
              max={20}
              value={unidades}
              onChange={(e) => setUnidades(Math.max(1, Number(e.target.value)))}
              className={CLASSES_CONTROLE}
            />
          </CampoFormulario>
        </div>

        <CampoFormulario rotulo="Nome do paciente" htmlFor="req-paciente">
          <input
            id="req-paciente"
            type="text"
            required
            value={paciente}
            onChange={(e) => setPaciente(e.target.value)}
            placeholder="Ex.: João Batista Ribeiro"
            className={CLASSES_CONTROLE}
          />
        </CampoFormulario>

        <CampoFormulario rotulo="Nível de urgência">
          <SeletorUrgencia rotuloGrupo="Nível de urgência" value={urgencia} onChange={setUrgencia} />
        </CampoFormulario>

        <button
          type="submit"
          className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
        >
          Enviar requisição
        </button>
      </form>

      <aside className="space-y-4">
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Lote sugerido (FEFO)
        </p>

        {sugestao ? (
          <div className="rounded-xl border border-rota-border bg-white p-5 shadow-card">
            <div className="flex items-center justify-between gap-3">
              <span className="font-mono text-sm font-semibold text-gray-900">{sugestao.codigo}</span>
              <span
                className={cn(
                  "rounded-full px-2.5 py-1 font-mono text-[11px] font-semibold uppercase tracking-wide",
                  dias <= 3
                    ? "bg-red-50 text-red-600"
                    : dias <= 15
                      ? "bg-amber-50 text-amber-600"
                      : "bg-emerald-50 text-emerald-600",
                )}
              >
                {dias}d p/ vencer
              </span>
            </div>
            <p className="mt-2 text-sm text-gray-500">
              Priorizamos o lote mais próximo do vencimento para reduzir descarte.
            </p>
            <p className="mt-3 text-xs text-gray-500">
              {sugestao.unidades} unidades disponíveis · vence em {formatarData(sugestao.dataValidade)}
            </p>
            <p className="mt-2 flex items-center gap-1.5 font-mono text-xs text-gray-500">
              <MapPin className="h-3.5 w-3.5 text-rota-red" />
              {sugestao.localizacao}
            </p>
          </div>
        ) : (
          <p className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-500 shadow-card">
            Sem lotes {tipoSanguineo} de {componente} disponíveis — considere uma solicitação
            inter-hospitalar pela tela de Rede.
          </p>
        )}

        {enviada && (
          <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-5">
            <p className="flex items-center gap-2 text-sm font-semibold text-emerald-700">
              <CheckCircle2 className="h-4 w-4" />
              Requisição registrada
            </p>
            <p className="mt-2 text-sm text-emerald-800">
              Protocolo <span className="font-mono font-semibold">{enviada.protocolo}</span> —{" "}
              {enviada.unidades}× {enviada.componente} {enviada.tipoSanguineo} para {enviada.ala}
              {enviada.paciente && `, paciente ${enviada.paciente}`}.
            </p>
            {enviada.loteSugerido && (
              <p className="mt-1 text-sm text-emerald-800">
                Lote reservado: <span className="font-mono">{enviada.loteSugerido}</span>.
              </p>
            )}
          </div>
        )}
      </aside>
    </div>
  );
}
