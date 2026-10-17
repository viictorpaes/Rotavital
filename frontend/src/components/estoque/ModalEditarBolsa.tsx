import { useState } from "react";
import type { FormEvent } from "react";
import type { LoteHemocomponente } from "@/types";
import type { AtualizarBolsaHemocomponenteDTO, BolsaHemocomponenteDTO } from "@/types/dtos";
import {
  PRAZO_MAXIMO_DIAS,
  formatarData,
  formatarTemperatura,
  lerNumero,
  paraTipoComponenteBackend,
  somarDias,
} from "@/lib/estoque";
import { mensagemDoErro } from "@/lib/erros";
import { atualizarHemocomponente } from "@/services/api";
import { AlertaErroFormulario } from "./AlertaErroFormulario";
import { BotoesFormulario } from "./BotoesFormulario";
import { CampoFormulario as Campo, CLASSE_CAMPO } from "./CampoFormulario";
import { JanelaModal } from "./JanelaModal";

interface Props
{
  lote: LoteHemocomponente;
  onClose: () => void;
  /** Chamado com a bolsa como a API devolveu depois da correção. */
  onSalva: (bolsa: BolsaHemocomponenteDTO) => void;
}

/**
 * PI3-190 — Correção de validade, volume, temperatura e localização de uma bolsa, por
 * PATCH /api/v1/hemocomponentes/{id}. Só os campos alterados vão no corpo (o PATCH deixa o resto como
 * está); as regras são as do cadastro e ficam na API, que devolve a mensagem mostrada no formulário.
 * Componente, tipo, coleta e lote não mudam: se estiverem errados, a bolsa é descartada e recadastrada.
 */
export function ModalEditarBolsa({ lote, onClose, onSalva }: Readonly<Props>)
{
  const inicial = {
    dataValidade: lote.dataValidade,
    volume: String(lote.volumeMl),
    temperatura: String(lote.temperaturaAtual),
    localizacao: lote.localizacao,
  };
  const [dataValidade, setDataValidade] = useState(inicial.dataValidade);
  const [volume, setVolume] = useState(inicial.volume);
  const [temperatura, setTemperatura] = useState(inicial.temperatura);
  const [localizacao, setLocalizacao] = useState(inicial.localizacao);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const prazo = PRAZO_MAXIMO_DIAS[paraTipoComponenteBackend(lote.componente)];
  const dicaValidade = lote.dataColeta
    ? `Coleta em ${formatarData(lote.dataColeta)}: até ${formatarData(somarDias(lote.dataColeta, prazo))}.`
    : `Máximo de ${prazo} dias depois da coleta.`;

  // Campo em branco ou igual ao original não vai no corpo: para o PATCH, ausente quer dizer "não muda".
  const alteracoes: AtualizarBolsaHemocomponenteDTO = {};
  if (dataValidade && dataValidade !== inicial.dataValidade) alteracoes.dataValidade = dataValidade;
  if (volume.trim() && volume !== inicial.volume) alteracoes.volumeMl = lerNumero(volume) ?? undefined;
  if (temperatura.trim() && temperatura !== inicial.temperatura)
  {
    alteracoes.temperaturaCelsius = lerNumero(temperatura) ?? undefined;
  }
  if (localizacao.trim() !== inicial.localizacao.trim()) alteracoes.localizacao = localizacao.trim();
  const nadaMudou = Object.keys(alteracoes).length === 0;

  async function aoEnviar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);

    try
    {
      onSalva(await atualizarHemocomponente(lote.id, alteracoes));
    }
    catch (falha: unknown)
    {
      setErro(mensagemDoErro(falha, "Não foi possível conectar com o back-end. A bolsa não foi alterada."));
      setEnviando(false);
    }
  }

  return (
    <JanelaModal
      idTitulo="titulo-editar-bolsa"
      titulo={`Corrigir bolsa ${lote.id}`}
      subtitulo={`${lote.componente} · ${lote.tipoSanguineo} · lote ${lote.codigo}`}
      bloqueada={enviando}
      onClose={onClose}
    >
      {/* noValidate: quem valida é a API, para a tela mostrar exatamente as regras do backend. */}
      <form onSubmit={aoEnviar} noValidate className="space-y-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Campo id="editar-bolsa-validade" rotulo="Data de validade" dica={dicaValidade}>
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
              value={volume}
              onChange={(e) => setVolume(e.target.value)}
              className={CLASSE_CAMPO}
            />
          </Campo>

          <Campo
            id="editar-bolsa-temperatura"
            rotulo="Temperatura (°C)"
            dica={`Faixa ideal: ${formatarTemperatura(lote.temperaturaIdeal.minima)} a ${formatarTemperatura(lote.temperaturaIdeal.maxima)}.`}
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

          <Campo id="editar-bolsa-localizacao" rotulo="Localização" dica="Em branco apaga a localização.">
            <input
              id="editar-bolsa-localizacao"
              type="text"
              value={localizacao}
              onChange={(e) => setLocalizacao(e.target.value)}
              className={CLASSE_CAMPO}
            />
          </Campo>
        </div>

        {erro && <AlertaErroFormulario mensagem={erro} />}

        <BotoesFormulario
          enviando={enviando}
          rotulo="Salvar correção"
          rotuloEnviando="Salvando..."
          desabilitado={nadaMudou}
          onCancelar={onClose}
        />
      </form>
    </JanelaModal>
  );
}
