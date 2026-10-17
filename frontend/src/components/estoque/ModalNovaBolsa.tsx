import { useEffect, useState } from "react";
import type { FormEvent, ReactNode } from "react";
import { AlertCircle, Loader2, X } from "lucide-react";
import type { TipoSanguineo } from "@/types";
import type { BolsaHemocomponenteDTO, TipoComponenteBackend } from "@/types/dtos";
import {
  COMPONENTES_FILTRO,
  PRAZO_MAXIMO_DIAS,
  TIPOS_SANGUINEOS,
  formatarTemperatura,
  hojeIso,
  obterFaixaTemperatura,
  paraTipoSanguineoBackend,
  sugerirValidade,
  temperaturaSugerida,
} from "@/lib/estoque";
import { cadastrarHemocomponente, ErroHttp } from "@/services/api";

interface Props
{
  bancoId: string;
  onClose: () => void;
  /** Chamado com a bolsa que a API criou (já com o id gerado pelo backend). */
  onCadastrada: (bolsa: BolsaHemocomponenteDTO) => void;
}

const CLASSE_CAMPO =
  "w-full rounded-lg border border-rota-border bg-white px-3 py-2 text-sm text-gray-900 focus:border-rota-red focus:outline-none";

/** "" vira null: campo em branco vai para a API, que responde qual campo é obrigatório. */
function paraNumero(valor: string): number | null
{
  const normalizado = valor.trim().replace(",", ".");
  return normalizado === "" ? null : Number(normalizado);
}

/**
 * PI3-179 — Formulário de bolsa nova da tela de Estoque, enviado para POST /api/v1/hemocomponentes.
 * As regras (validade máxima do componente, volume, temperatura do sensor, lote repetido) ficam na
 * API: o formulário só sugere valores e mostra, dentro dele, a mensagem de erro que voltou.
 */
export function ModalNovaBolsa({ bancoId, onClose, onCadastrada }: Readonly<Props>)
{
  const hoje = hojeIso();
  const [componente, setComponente] = useState<TipoComponenteBackend>("HEMACIAS");
  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>("O+");
  const [dataColeta, setDataColeta] = useState(hoje);
  const [dataValidade, setDataValidade] = useState(sugerirValidade("HEMACIAS", hoje));
  const [volume, setVolume] = useState("450");
  const [temperatura, setTemperatura] = useState(String(temperaturaSugerida("HEMACIAS")));
  const [lote, setLote] = useState("");
  const [localizacao, setLocalizacao] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const faixa = obterFaixaTemperatura(componente);

  // Esc fecha o modal, menos durante o envio (a bolsa pode já ter sido gravada).
  useEffect(() =>
  {
    function aoTeclar(evento: KeyboardEvent)
    {
      if (evento.key === "Escape" && !enviando) onClose();
    }
    window.addEventListener("keydown", aoTeclar);
    return () => window.removeEventListener("keydown", aoTeclar);
  }, [enviando, onClose]);

  // Trocar o componente ou a coleta refaz as sugestões: cada componente tem validade e faixa próprias.
  function aoMudarComponente(novo: TipoComponenteBackend)
  {
    setComponente(novo);
    setDataValidade(sugerirValidade(novo, dataColeta));
    setTemperatura(String(temperaturaSugerida(novo)));
  }

  function aoMudarColeta(nova: string)
  {
    setDataColeta(nova);
    setDataValidade(sugerirValidade(componente, nova));
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);

    try
    {
      const bolsa = await cadastrarHemocomponente({
        tipoComponente: componente,
        tipoSanguineo: paraTipoSanguineoBackend(tipoSanguineo),
        dataColeta: dataColeta || null,
        dataValidade: dataValidade || null,
        loteSintetico: lote.trim(),
        volumeMl: paraNumero(volume),
        temperaturaCelsius: paraNumero(temperatura),
        localizacao: localizacao.trim() || undefined,
        bancoOrigemId: bancoId,
      });
      onCadastrada(bolsa);
    }
    catch (falha: unknown)
    {
      if (falha instanceof ErroHttp)
      {
        setErro(falha.detalhe || falha.message);
      }
      else
      {
        setErro("Não foi possível conectar com o back-end. A bolsa não foi registrada.");
      }
      setEnviando(false);
    }
  }

  function fechar()
  {
    if (!enviando) onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={fechar}
      role="presentation"
    >
      <div
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-rota-border bg-white p-6 shadow-card"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="titulo-nova-bolsa"
      >
        <div className="mb-4 flex items-start justify-between">
          <div>
            <h2
              id="titulo-nova-bolsa"
              className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500"
            >
              Nova bolsa no estoque
            </h2>
            <p className="mt-1 text-sm text-gray-500">Banco {bancoId} · o código da bolsa é gerado pelo sistema.</p>
          </div>
          <button
            type="button"
            onClick={fechar}
            disabled={enviando}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700 disabled:opacity-50"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* noValidate: quem valida é a API, para a tela mostrar exatamente as regras do backend. */}
        <form onSubmit={aoEnviar} noValidate className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo id="nova-bolsa-componente" rotulo="Componente">
              <select
                id="nova-bolsa-componente"
                value={componente}
                onChange={(e) => aoMudarComponente(e.target.value as TipoComponenteBackend)}
                className={CLASSE_CAMPO}
              >
                {COMPONENTES_FILTRO.map((opcao) => (
                  <option key={opcao.valor} value={opcao.valor}>{opcao.rotulo}</option>
                ))}
              </select>
            </Campo>

            <Campo id="nova-bolsa-tipo" rotulo="Tipo sanguíneo">
              <select
                id="nova-bolsa-tipo"
                value={tipoSanguineo}
                onChange={(e) => setTipoSanguineo(e.target.value as TipoSanguineo)}
                className={CLASSE_CAMPO}
              >
                {TIPOS_SANGUINEOS.map((tipo) => (
                  <option key={tipo} value={tipo}>{tipo}</option>
                ))}
              </select>
            </Campo>

            <Campo id="nova-bolsa-coleta" rotulo="Data de coleta">
              <input
                id="nova-bolsa-coleta"
                type="date"
                value={dataColeta}
                max={hoje}
                onChange={(e) => aoMudarColeta(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo
              id="nova-bolsa-validade"
              rotulo="Data de validade"
              dica={`Máximo de ${PRAZO_MAXIMO_DIAS[componente]} dias depois da coleta.`}
            >
              <input
                id="nova-bolsa-validade"
                type="date"
                value={dataValidade}
                onChange={(e) => setDataValidade(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="nova-bolsa-volume" rotulo="Volume (mL)" dica="Até 700 mL por bolsa.">
              <input
                id="nova-bolsa-volume"
                type="number"
                inputMode="decimal"
                min={1}
                max={700}
                value={volume}
                onChange={(e) => setVolume(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo
              id="nova-bolsa-temperatura"
              rotulo="Temperatura (°C)"
              dica={`Faixa ideal: ${formatarTemperatura(faixa.minima)} a ${formatarTemperatura(faixa.maxima)}.`}
            >
              <input
                id="nova-bolsa-temperatura"
                type="number"
                inputMode="decimal"
                step={0.1}
                value={temperatura}
                onChange={(e) => setTemperatura(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="nova-bolsa-lote" rotulo="Lote da doação" dica="Uma bolsa de cada componente por lote.">
              <input
                id="nova-bolsa-lote"
                type="text"
                value={lote}
                onChange={(e) => setLote(e.target.value)}
                placeholder="Ex.: LOTE-2026-0420"
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="nova-bolsa-localizacao" rotulo="Localização (opcional)">
              <input
                id="nova-bolsa-localizacao"
                type="text"
                value={localizacao}
                onChange={(e) => setLocalizacao(e.target.value)}
                placeholder="Ex.: Refrigerador 2 / Prateleira B"
                className={CLASSE_CAMPO}
              />
            </Campo>
          </div>

          {erro && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800"
            >
              <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-rose-600" />
              <span>{erro}</span>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={fechar}
              disabled={enviando}
              className="rounded-lg border border-rota-border bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={enviando}
              className="inline-flex items-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:opacity-60"
            >
              {enviando && <Loader2 className="h-4 w-4 animate-spin" />}
              {enviando ? "Registrando..." : "Registrar bolsa"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Campo({ id, rotulo, dica, children }: Readonly<{ id: string; rotulo: string; dica?: string; children: ReactNode }>)
{
  return (
    <div className="space-y-1">
      <label htmlFor={id} className="block font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-500">
        {rotulo}
      </label>
      {children}
      {dica && <p className="text-xs text-gray-400">{dica}</p>}
    </div>
  );
}
