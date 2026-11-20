import { useEffect, useState } from "react";
import type { FormEvent, ReactNode } from "react";
import { AlertCircle, Loader2, X } from "lucide-react";
import type { TipoSanguineo } from "@/types";
import type {
  BolsaHemocomponenteDTO,
  CorrecaoBolsaHemocomponenteDTO,
  TipoComponenteBackend,
} from "@/types/dtos";
import {
  COMPONENTES_FILTRO,
  PRAZO_MAXIMO_DIAS,
  TIPOS_SANGUINEOS,
  converterTipoSanguineo,
  formatarTemperatura,
  hojeIso,
  obterFaixaTemperatura,
  paraTipoSanguineoBackend,
} from "@/lib/estoque";
import { corrigirHemocomponente, ErroHttp } from "@/services/api";

interface Props
{
  bolsa: BolsaHemocomponenteDTO;
  /** Só o admin corrige o tipo sanguíneo; para os outros perfis o campo aparece travado. */
  podeCorrigirTipoSanguineo: boolean;
  onClose: () => void;
  /** Chamado com a bolsa como a API devolveu depois da correção. */
  onCorrigida: (bolsa: BolsaHemocomponenteDTO) => void;
}

const CLASSE_CAMPO =
  "w-full rounded-lg border border-rota-border bg-white px-3 py-2 text-sm text-gray-900 focus:border-rota-red focus:outline-none disabled:bg-gray-50 disabled:text-gray-500";

/** "" vira null: campo apagado vai para a API, que responde o que está errado. */
function paraNumero(valor: string): number | null
{
  const normalizado = valor.trim().replace(",", ".");
  return normalizado === "" ? null : Number(normalizado);
}

/**
 * HU-03 — Correção dos dados de uma bolsa que ainda está no estoque (DISPONIVEL), por
 * PATCH /api/v1/hemocomponentes/{id}. Só os campos alterados vão para a API, que confere as mesmas
 * regras do cadastro. Componente e código não mudam: o código nasce do componente.
 */
export function ModalEditarBolsa({ bolsa, podeCorrigirTipoSanguineo, onClose, onCorrigida }: Readonly<Props>)
{
  const componente = bolsa.tipoComponente as TipoComponenteBackend;
  const original =
  {
    tipoSanguineo: converterTipoSanguineo(bolsa.tipoSanguineo),
    dataColeta: bolsa.dataColeta ?? "",
    dataValidade: bolsa.dataValidade,
    volume: String(bolsa.volumeMl),
    temperatura: String(bolsa.temperaturaCelsius ?? ""),
    lote: bolsa.loteSintetico ?? "",
    localizacao: bolsa.localizacao ?? "",
  };

  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>(original.tipoSanguineo);
  const [dataColeta, setDataColeta] = useState(original.dataColeta);
  const [dataValidade, setDataValidade] = useState(original.dataValidade);
  const [volume, setVolume] = useState(original.volume);
  const [temperatura, setTemperatura] = useState(original.temperatura);
  const [lote, setLote] = useState(original.lote);
  const [localizacao, setLocalizacao] = useState(original.localizacao);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const faixa = obterFaixaTemperatura(componente);
  const rotuloComponente = COMPONENTES_FILTRO.find((opcao) => opcao.valor === componente)?.rotulo ?? componente;

  // Esc fecha o modal, menos durante o envio.
  useEffect(() =>
  {
    function aoTeclar(evento: KeyboardEvent)
    {
      if (evento.key === "Escape" && !enviando) onClose();
    }
    window.addEventListener("keydown", aoTeclar);
    return () => window.removeEventListener("keydown", aoTeclar);
  }, [enviando, onClose]);

  // Monta o corpo só com o que mudou: o que não foi mexido fica como está no backend.
  function camposAlterados(): CorrecaoBolsaHemocomponenteDTO
  {
    const dados: CorrecaoBolsaHemocomponenteDTO = {};

    if (tipoSanguineo !== original.tipoSanguineo) dados.tipoSanguineo = paraTipoSanguineoBackend(tipoSanguineo);
    if (dataColeta !== original.dataColeta) dados.dataColeta = dataColeta;
    if (dataValidade !== original.dataValidade) dados.dataValidade = dataValidade;
    if (lote.trim() !== original.lote) dados.loteSintetico = lote.trim();
    if (localizacao.trim() !== original.localizacao) dados.localizacao = localizacao.trim();
    if (volume.trim() !== original.volume) dados.volumeMl = paraNumero(volume) ?? 0;
    if (temperatura.trim() !== original.temperatura)
    {
      const valor = paraNumero(temperatura);
      if (valor !== null) dados.temperaturaCelsius = valor;
    }

    return dados;
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    const dados = camposAlterados();

    if (Object.keys(dados).length === 0)
    {
      setErro("Nenhum dado foi alterado.");
      return;
    }
    if (temperatura.trim() === "")
    {
      setErro("Informe a temperatura da bolsa.");
      return;
    }

    setEnviando(true);
    setErro(null);

    try
    {
      onCorrigida(await corrigirHemocomponente(bolsa.id, dados));
    }
    catch (falha: unknown)
    {
      setErro(falha instanceof ErroHttp
        ? falha.detalhe || falha.message
        : "Não foi possível conectar com o back-end. Nada foi alterado.");
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
        aria-labelledby="titulo-editar-bolsa"
      >
        <div className="mb-4 flex items-start justify-between">
          <div>
            <h2
              id="titulo-editar-bolsa"
              className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500"
            >
              Editar bolsa {bolsa.id}
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Corrija o que foi digitado errado. Só bolsas disponíveis podem ser editadas.
            </p>
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

        {/* noValidate: quem valida é a API, com as mesmas regras do cadastro. */}
        <form onSubmit={aoEnviar} noValidate className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo id="editar-bolsa-componente" rotulo="Componente" dica="Não muda: o código da bolsa nasce dele.">
              <input id="editar-bolsa-componente" type="text" value={rotuloComponente} disabled className={CLASSE_CAMPO} />
            </Campo>

            <Campo
              id="editar-bolsa-tipo"
              rotulo="Tipo sanguíneo"
              dica={podeCorrigirTipoSanguineo ? undefined : "Só o admin corrige o tipo sanguíneo."}
            >
              <select
                id="editar-bolsa-tipo"
                value={tipoSanguineo}
                disabled={!podeCorrigirTipoSanguineo}
                onChange={(e) => setTipoSanguineo(e.target.value as TipoSanguineo)}
                className={CLASSE_CAMPO}
              >
                {TIPOS_SANGUINEOS.map((tipo) => (
                  <option key={tipo} value={tipo}>{tipo}</option>
                ))}
              </select>
            </Campo>

            <Campo id="editar-bolsa-coleta" rotulo="Data de coleta">
              <input
                id="editar-bolsa-coleta"
                type="date"
                value={dataColeta}
                max={hojeIso()}
                onChange={(e) => setDataColeta(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo
              id="editar-bolsa-validade"
              rotulo="Data de validade"
              dica={`Máximo de ${PRAZO_MAXIMO_DIAS[componente]} dias depois da coleta.`}
            >
              <input
                id="editar-bolsa-validade"
                type="date"
                value={dataValidade}
                onChange={(e) => setDataValidade(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="editar-bolsa-volume" rotulo="Volume (mL)" dica="Até 700 mL por bolsa.">
              <input
                id="editar-bolsa-volume"
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
              id="editar-bolsa-temperatura"
              rotulo="Temperatura (°C)"
              dica={`Faixa ideal: ${formatarTemperatura(faixa.minima)} a ${formatarTemperatura(faixa.maxima)}.`}
            >
              <input
                id="editar-bolsa-temperatura"
                type="number"
                inputMode="decimal"
                step={0.1}
                value={temperatura}
                onChange={(e) => setTemperatura(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="editar-bolsa-lote" rotulo="Lote da doação">
              <input
                id="editar-bolsa-lote"
                type="text"
                value={lote}
                onChange={(e) => setLote(e.target.value)}
                className={CLASSE_CAMPO}
              />
            </Campo>

            <Campo id="editar-bolsa-localizacao" rotulo="Localização" dica="Em branco: sem localização definida.">
              <input
                id="editar-bolsa-localizacao"
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
              {enviando ? "Salvando..." : "Salvar correção"}
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
