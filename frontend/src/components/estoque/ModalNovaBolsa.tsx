import { useState } from "react";
import type { FormEvent } from "react";
import type { TipoSanguineo } from "@/types";
import type { BolsaHemocomponenteDTO, TipoComponenteBackend } from "@/types/dtos";
import {
  COMPONENTES_FILTRO,
  PRAZO_MAXIMO_DIAS,
  TIPOS_SANGUINEOS,
  formatarTemperatura,
  hojeIso,
  lerNumero,
  obterFaixaTemperatura,
  paraTipoSanguineoBackend,
  sugerirValidade,
  temperaturaSugerida,
} from "@/lib/estoque";
import { cadastrarHemocomponente } from "@/services/api";
import { mensagemDoErro } from "@/lib/erros";
import { AlertaErroFormulario } from "./AlertaErroFormulario";
import { BotoesFormulario } from "./BotoesFormulario";
import { CampoFormulario as Campo, CLASSE_CAMPO } from "./CampoFormulario";
import { JanelaModal } from "./JanelaModal";

interface Props
{
  bancoId: string;
  onClose: () => void;
  /** Chamado com a bolsa que a API criou (já com o id gerado pelo backend). */
  onCadastrada: (bolsa: BolsaHemocomponenteDTO) => void;
}

/**
 * PI3-179 — Formulário de bolsa nova da tela de Estoque, enviado para POST /api/v1/hemocomponentes.
 * As regras (validade máxima do componente, volume, temperatura do sensor, lote repetido) ficam na
 * API: o formulário só sugere valores e mostra, dentro dele, a mensagem de erro que voltou.
 * Esc, o X e o clique fora fecham, menos durante o envio (a bolsa pode já ter sido gravada).
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
        // Em branco vai como null: a API responde qual campo é obrigatório.
        dataColeta: dataColeta || null,
        dataValidade: dataValidade || null,
        loteSintetico: lote.trim(),
        volumeMl: lerNumero(volume),
        temperaturaCelsius: lerNumero(temperatura),
        localizacao: localizacao.trim() || undefined,
        bancoOrigemId: bancoId,
      });
      onCadastrada(bolsa);
    }
    catch (falha: unknown)
    {
      setErro(mensagemDoErro(falha, "Não foi possível conectar com o back-end. A bolsa não foi registrada."));
      setEnviando(false);
    }
  }

  return (
    <JanelaModal
      idTitulo="titulo-nova-bolsa"
      titulo="Nova bolsa no estoque"
      subtitulo={`Banco ${bancoId} · o código da bolsa é gerado pelo sistema.`}
      bloqueada={enviando}
      onClose={onClose}
    >
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

        {erro && <AlertaErroFormulario mensagem={erro} />}

        <BotoesFormulario
          enviando={enviando}
          rotulo="Registrar bolsa"
          rotuloEnviando="Registrando..."
          onCancelar={onClose}
        />
      </form>
    </JanelaModal>
  );
}
