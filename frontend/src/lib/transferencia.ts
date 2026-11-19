import type { TransferenciaDTO } from "@/types/dtos";
import { formatarDuracao, horarioDeChegada } from "@/lib/rotaAteHospital";

/** PI3-260 — Como a tela acompanha uma transferência em andamento. */

export type EstadoDaEtapa = "feita" | "atual" | "futura";

export interface EtapaDaTransferencia
{
  rotulo: string;
  detalhe: string;
  estado: EstadoDaEtapa;
}

const MS_POR_MINUTO = 60_000;

function horaDe(iso: string)
{
  return horarioDeChegada(0, new Date(iso));
}

/**
 * Quanto do tempo previsto já passou, de 0 a 1. As maletas não têm GPS: o progresso é uma
 * estimativa pelo relógio, e a bolsa só chega de verdade quando alguém confirma o recebimento.
 */
export function progressoDaTransferencia(transferencia: TransferenciaDTO, agora: number): number
{
  const saida = Date.parse(transferencia.despachadaEm);
  const chegada = Date.parse(transferencia.chegadaPrevistaEm);
  if (chegada <= saida) return 1;

  return Math.min(1, Math.max(0, (agora - saida) / (chegada - saida)));
}

/** As quatro etapas da linha do tempo: Origem, Em trânsito, Chegada estimada e Destino. */
export function etapasDaTransferencia(transferencia: TransferenciaDTO, agora: number): EtapaDaTransferencia[]
{
  const progresso = progressoDaTransferencia(transferencia, agora);
  const emTransito = progresso < 1;

  return [
    { rotulo: "Origem", detalhe: `${transferencia.origemNome} · ${horaDe(transferencia.despachadaEm)}`, estado: "feita" },
    { rotulo: "Em trânsito", detalhe: `${Math.round(progresso * 100)}% do tempo previsto`, estado: emTransito ? "atual" : "feita" },
    { rotulo: "Chegada estimada", detalhe: horaDe(transferencia.chegadaPrevistaEm), estado: emTransito ? "futura" : "atual" },
    { rotulo: "Destino", detalhe: transferencia.destinoNome, estado: "futura" },
  ];
}

/** "Chega em cerca de 12 min", ou há quanto tempo passou da hora sem ninguém confirmar a chegada. */
export function previsaoDaChegada(transferencia: TransferenciaDTO, agora: number): { texto: string; atrasada: boolean }
{
  const minutos = Math.round((Date.parse(transferencia.chegadaPrevistaEm) - agora) / MS_POR_MINUTO);

  if (minutos > 0) return { texto: `Chega em cerca de ${formatarDuracao(minutos)}`, atrasada: false };
  if (minutos === 0) return { texto: "Chegando agora", atrasada: false };
  return { texto: `Passou ${formatarDuracao(-minutos)} da hora prevista: confirme a chegada`, atrasada: true };
}
