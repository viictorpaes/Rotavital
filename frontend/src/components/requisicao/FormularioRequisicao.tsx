import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AlertTriangle, ArrowRight, CheckCircle2, Loader2, MapPin, Megaphone, Network, PackageCheck } from "lucide-react";
import type {
  AlaMedica,
  LoteHemocomponente,
  RequisicaoRegistradaDTO,
  TipoComponente,
  TipoSanguineo,
  UrgenciaNecessidade,
} from "@/types";
import { useDados } from "@/context/ContextoDados";
import { useHospitais } from "@/hooks/useHospitais";
import { registrarRequisicao, sugerirLotes } from "@/services/api";
import {
  TIPOS_SANGUINEOS,
  converterBolsaParaLote,
  converterTipoSanguineo,
  diasAteVencer,
  formatarData,
  paraTipoSanguineoBackend,
} from "@/lib/estoque";
import { componenteParaBackend, mensagemDeErro } from "@/lib/doacoes";
import {
  ALAS_MEDICAS,
  ALA_PARA_BACKEND,
  COMPONENTES,
  URGENCIA_PARA_BACKEND,
  descreverDestino,
  descreverLocalizacao,
  sugerirLotesFefo,
} from "@/lib/requisicao";
import { cn } from "@/lib/utilitarios";
import { CLASSES_CONTROLE, CampoFormulario } from "./CampoFormulario";
import { SeletorUrgencia } from "./SeletorUrgencia";

/** Hemocentro que atende as requisições desta tela (o mesmo do estoque). */
const BANCO_ID = "BS-01";
const UNIDADES_MAXIMAS = 20;
/** Espera entre a última mudança no formulário e a consulta de disponibilidade. */
const ESPERA_CONSULTA_MS = 250;

interface Disponibilidade
{
  /** "contingencia": backend fora do ar; os números vêm do estoque local e o envio fica bloqueado. */
  situacao: "carregando" | "pronto" | "contingencia";
  lotes: LoteHemocomponente[];
  disponiveis: number;
}

/**
 * HU-04 — Requisição de hemocomponente. Antes de enviar, o formulário consulta o FEFO do backend
 * para a quantidade pedida: sem saldo, o envio fica bloqueado e a tela sugere campanha ou remessa
 * inter-hospitalar. O painel lateral mostra onde retirar cada bolsa (câmara, prateleira, nível) e
 * para onde ela vai (hospital · ala · leito) depois de reservada.
 */
export function FormularioRequisicao({ onAbrirCampanha }: Readonly<{ onAbrirCampanha?: () => void }>)
{
  const { lotes, recarregarEstoque } = useDados();
  const { hospitais, erro: erroHospitais } = useHospitais();

  const [componente, setComponente] = useState<TipoComponente>(COMPONENTES[0]);
  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>("O-");
  const [hospitalId, setHospitalId] = useState("");
  const [ala, setAla] = useState<AlaMedica>(ALAS_MEDICAS[0]);
  const [leito, setLeito] = useState("");
  const [unidades, setUnidades] = useState(1);
  const [paciente, setPaciente] = useState("");
  const [urgencia, setUrgencia] = useState<UrgenciaNecessidade>("atencao");

  const [disponibilidade, setDisponibilidade] = useState<Disponibilidade>({
    situacao: "carregando",
    lotes: [],
    disponiveis: 0,
  });
  // Muda depois de um envio, para a disponibilidade ser lida de novo (as bolsas reservadas saem dela).
  const [versaoEstoque, setVersaoEstoque] = useState(0);
  const [enviando, setEnviando] = useState(false);
  const [erroEnvio, setErroEnvio] = useState<string | null>(null);
  const [enviada, setEnviada] = useState<RequisicaoRegistradaDTO | null>(null);

  useEffect(() =>
  {
    if (!hospitalId && hospitais.length > 0) setHospitalId(hospitais[0].id);
  }, [hospitais, hospitalId]);

  useEffect(() =>
  {
    let ativo = true;
    setDisponibilidade((atual) => ({ ...atual, situacao: "carregando" }));

    const espera = window.setTimeout(() =>
    {
      sugerirLotes(BANCO_ID, componenteParaBackend(componente), paraTipoSanguineoBackend(tipoSanguineo), unidades)
        .then((sugestao) =>
        {
          if (!ativo) return;
          setDisponibilidade({
            situacao: "pronto",
            lotes: sugestao.lotes.map(converterBolsaParaLote),
            disponiveis: sugestao.quantidadeSugerida,
          });
        })
        .catch(() =>
        {
          if (!ativo) return;
          const local = sugerirLotesFefo(lotes, componente, tipoSanguineo, unidades);
          setDisponibilidade({ situacao: "contingencia", lotes: local.lotes, disponiveis: local.disponiveis });
        });
    }, ESPERA_CONSULTA_MS);

    return () =>
    {
      ativo = false;
      window.clearTimeout(espera);
    };
  }, [componente, tipoSanguineo, unidades, lotes, versaoEstoque]);

  const hospital = hospitais.find((item) => item.id === hospitalId);
  const destino = hospital ? descreverDestino(hospital.nome, ala, leito) : null;
  const faltam = Math.max(0, unidades - disponibilidade.disponiveis);
  const consultando = disponibilidade.situacao === "carregando";
  const semSaldo = !consultando && faltam > 0;
  const offline = disponibilidade.situacao === "contingencia";
  const podeEnviar = disponibilidade.situacao === "pronto" && faltam === 0 && !!hospital && !enviando;

  const motivoBloqueio = useMemo(() =>
  {
    if (enviando) return null;
    if (consultando) return "Conferindo o estoque…";
    if (offline) return "Sem conexão com o servidor: não dá para reservar agora.";
    if (semSaldo) return "Sem estoque compatível suficiente";
    if (!hospital) return erroHospitais ?? "Escolha o hospital de destino.";
    return null;
  }, [enviando, consultando, offline, semSaldo, hospital, erroHospitais]);

  async function handleSubmit(evento: React.FormEvent)
  {
    evento.preventDefault();
    if (!podeEnviar || !hospital) return;

    setEnviando(true);
    setErroEnvio(null);
    try
    {
      const resposta = await registrarRequisicao({
        bancoId: BANCO_ID,
        hospitalId: hospital.id,
        ala: ALA_PARA_BACKEND[ala],
        leito: leito.trim() || undefined,
        tipoComponente: componenteParaBackend(componente),
        tipoSanguineo: paraTipoSanguineoBackend(tipoSanguineo),
        quantidade: unidades,
        urgencia: URGENCIA_PARA_BACKEND[urgencia],
        paciente: paciente.trim() || undefined,
      });
      setEnviada(resposta);
      setPaciente("");
      setLeito("");
    }
    catch (falha)
    {
      setErroEnvio(mensagemDeErro(falha));
    }
    finally
    {
      setEnviando(false);
      setVersaoEstoque((versao) => versao + 1);
      void recarregarEstoque();
    }
  }

  const principal = disponibilidade.lotes[0];
  const dias = principal ? diasAteVencer(principal) : 0;

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

          <CampoFormulario rotulo="Hospital de destino" htmlFor="req-hospital">
            <select
              id="req-hospital"
              value={hospitalId}
              onChange={(e) => setHospitalId(e.target.value)}
              disabled={hospitais.length === 0}
              className={CLASSES_CONTROLE}
            >
              {hospitais.length === 0 && <option value="">{erroHospitais ?? "Carregando hospitais…"}</option>}
              {hospitais.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.nome}
                </option>
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

          <CampoFormulario rotulo="Leito (opcional)" htmlFor="req-leito">
            <input
              id="req-leito"
              type="text"
              maxLength={20}
              value={leito}
              onChange={(e) => setLeito(e.target.value)}
              placeholder="Ex.: 04"
              className={CLASSES_CONTROLE}
            />
          </CampoFormulario>

          <CampoFormulario rotulo="Quantidade (unidades)" htmlFor="req-unidades">
            <input
              id="req-unidades"
              type="number"
              min={1}
              max={UNIDADES_MAXIMAS}
              value={unidades}
              onChange={(e) =>
                setUnidades(Math.min(UNIDADES_MAXIMAS, Math.max(1, Math.trunc(Number(e.target.value)) || 1)))}
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

        {semSaldo && (
          <AlertaSaldoInsuficiente
            componente={componente}
            tipoSanguineo={tipoSanguineo}
            pedidas={unidades}
            disponiveis={disponibilidade.disponiveis}
            onAbrirCampanha={onAbrirCampanha}
          />
        )}

        {erroEnvio && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {erroEnvio}
          </p>
        )}

        <button
          type="submit"
          disabled={!podeEnviar}
          aria-describedby={motivoBloqueio ? "req-motivo-bloqueio" : undefined}
          className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:bg-gray-300 disabled:text-gray-600"
        >
          {enviando && <Loader2 className="h-4 w-4 animate-spin" />}
          {enviando ? "Reservando bolsas…" : semSaldo ? "Sem estoque compatível" : "Enviar requisição"}
        </button>
        {motivoBloqueio && !semSaldo && (
          <p id="req-motivo-bloqueio" className="-mt-3 text-center text-xs text-gray-500">
            {motivoBloqueio}
          </p>
        )}
      </form>

      <aside className="space-y-4">
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
          Lote sugerido (FEFO)
        </p>

        {principal ? (
          <div className="rounded-xl border border-rota-border bg-white p-5 shadow-card">
            <div className="flex items-center justify-between gap-3">
              <span className="font-mono text-sm font-semibold text-gray-900">{principal.codigo}</span>
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
              {principal.tipoSanguineo !== tipoSanguineo && (
                <span className="font-semibold text-gray-700">{principal.tipoSanguineo} compatível · </span>
              )}
              vence em {formatarData(principal.dataValidade)}
            </p>

            <LocalizacaoFisica localizacao={principal.localizacao} />

            {disponibilidade.lotes.length > 1 && (
              <ul className="mt-4 space-y-2 border-t border-rota-border pt-3">
                {disponibilidade.lotes.slice(1).map((lote) => (
                  <li key={lote.id} className="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1 text-xs">
                    <span className="font-mono font-semibold text-gray-800">
                      {lote.codigo}
                      {lote.tipoSanguineo !== tipoSanguineo && (
                        <span className="ml-1 font-normal text-gray-500">({lote.tipoSanguineo})</span>
                      )}
                    </span>
                    <span className="flex items-center gap-1 font-mono text-gray-500">
                      <MapPin className="h-3 w-3 text-rota-red" />
                      {descreverLocalizacao(lote.localizacao).join(" · ") || "Sem posição"}
                    </span>
                  </li>
                ))}
              </ul>
            )}

            {destino && (
              <p className="mt-4 flex items-start gap-1.5 rounded-lg bg-gray-50 px-3 py-2 text-xs text-gray-600">
                <ArrowRight className="mt-0.5 h-3.5 w-3.5 shrink-0 text-rota-red" />
                <span>
                  Após a reserva, a bolsa passa a constar em <strong className="text-gray-900">{destino}</strong>.
                </span>
              </p>
            )}
          </div>
        ) : (
          <p className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-500 shadow-card">
            {consultando ? "Conferindo o estoque…" : `Sem lotes ${tipoSanguineo} de ${componente} aptos no estoque.`}
          </p>
        )}

        <p
          aria-live="polite"
          className={cn(
            "rounded-lg px-3 py-2 font-mono text-xs",
            consultando
              ? "bg-gray-50 text-gray-500"
              : semSaldo
                ? "bg-red-50 text-red-700"
                : "bg-emerald-50 text-emerald-700",
          )}
        >
          {consultando
            ? "Conferindo saldo…"
            : `${disponibilidade.disponiveis} de ${unidades} unidade(s) disponível(is)${offline ? " · estoque local" : ""}`}
        </p>

        {enviada && <ConfirmacaoRequisicao requisicao={enviada} />}
      </aside>
    </div>
  );
}

/** Posição física da bolsa no hemocentro, por extenso (Refrigerador 1 · Prateleira 3 · Nível 2). */
function LocalizacaoFisica({ localizacao }: Readonly<{ localizacao: string }>)
{
  const partes = descreverLocalizacao(localizacao);

  return (
    <div className="mt-4 rounded-lg border border-rota-border p-3">
      <p className="flex items-center gap-1.5 font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">
        <MapPin className="h-3.5 w-3.5 text-rota-red" />
        Retirar de
      </p>
      {partes.length > 0 ? (
        <dl className="mt-2 flex flex-wrap gap-1.5">
          {partes.map((parte) => (
            <dd
              key={parte}
              className="rounded-md bg-red-50 px-2 py-1 font-mono text-xs font-semibold text-rota-red"
            >
              {parte}
            </dd>
          ))}
        </dl>
      ) : (
        <p className="mt-2 text-xs text-gray-500">Posição não cadastrada.</p>
      )}
      {partes.length > 0 && partes.join(" · ") !== localizacao && (
        <p className="mt-1.5 font-mono text-[11px] text-gray-400">Código: {localizacao}</p>
      )}
    </div>
  );
}

function AlertaSaldoInsuficiente({ componente, tipoSanguineo, pedidas, disponiveis, onAbrirCampanha }: Readonly<{
  componente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  pedidas: number;
  disponiveis: number;
  onAbrirCampanha?: () => void;
}>)
{
  return (
    <div id="req-motivo-bloqueio" role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4">
      <p className="flex items-center gap-2 text-sm font-semibold text-red-700">
        <AlertTriangle className="h-4 w-4 shrink-0" />
        Saldo insuficiente no estoque
      </p>
      <p className="mt-1.5 text-sm text-red-800">
        {disponiveis === 0
          ? `Não há bolsas aptas e compatíveis de ${componente} para ${tipoSanguineo}.`
          : `Só ${disponiveis} de ${pedidas} unidades de ${componente} para ${tipoSanguineo} estão disponíveis.`}{" "}
        A requisição fica bloqueada até haver saldo.
      </p>
      <div className="mt-3 flex flex-wrap gap-2">
        <Link
          to="/rede"
          className="inline-flex items-center gap-1.5 rounded-lg bg-rota-red px-3 py-2 text-xs font-semibold text-white transition-colors hover:bg-rota-redDark"
        >
          <Network className="h-3.5 w-3.5" />
          Solicitar remessa inter-hospitalar
        </Link>
        {onAbrirCampanha && (
          <button
            type="button"
            onClick={onAbrirCampanha}
            className="inline-flex items-center gap-1.5 rounded-lg border border-red-300 bg-white px-3 py-2 text-xs font-semibold text-red-700 transition-colors hover:bg-red-100"
          >
            <Megaphone className="h-3.5 w-3.5" />
            Abrir campanha de doação
          </button>
        )}
      </div>
    </div>
  );
}

function ConfirmacaoRequisicao({ requisicao }: Readonly<{ requisicao: RequisicaoRegistradaDTO }>)
{
  return (
    <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-5">
      <p className="flex items-center gap-2 text-sm font-semibold text-emerald-700">
        <CheckCircle2 className="h-4 w-4" />
        Requisição registrada
      </p>
      <p className="mt-2 text-sm text-emerald-800">
        Protocolo <span className="font-mono font-semibold">{requisicao.protocolo}</span> —{" "}
        {requisicao.quantidade}× {converterTipoSanguineo(requisicao.tipoSanguineo)} para{" "}
        <strong>{requisicao.destino}</strong>
        {requisicao.paciente && `, paciente ${requisicao.paciente}`}.
      </p>
      <ul className="mt-3 space-y-2">
        {requisicao.bolsas.map((bolsa) => (
          <li key={bolsa.id} className="rounded-lg bg-white/70 px-3 py-2 text-xs text-emerald-900">
            <p className="flex items-center gap-1.5 font-mono font-semibold">
              <PackageCheck className="h-3.5 w-3.5" />
              {bolsa.id} reservada
            </p>
            <p className="mt-1">
              Retirar de: <span className="font-mono">{descreverLocalizacao(bolsa.retirarDe).join(" · ") || "—"}</span>
            </p>
            <p>
              Nova localização: <span className="font-mono">{bolsa.localizacao}</span>
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}
