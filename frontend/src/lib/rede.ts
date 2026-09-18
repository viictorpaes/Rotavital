import type { Conexao, Coordenada, LoteHemocomponente, PontoDeRede, TipoComponente } from "@/types";
import { lotesEstoque, FAIXAS_IDEAIS } from "@/data/estoqueMock";
import { conexoes, hemocentro, hospitais } from "@/data/redeMock";
import { diasAteVencer } from "@/lib/estoque";

export const COMPONENTES: TipoComponente[] = [
  "Concentrado de Hemácias",
  "Plasma Fresco Congelado",
  "Concentrado de Plaquetas",
  "Crioprecipitado",
];

/** Embalagem recomendada para manter a cadeia fria de cada hemocomponente. */
export const TRANSPORTE_POR_COMPONENTE: Record<TipoComponente, string> =
{
  "Concentrado de Hemácias": "caixa térmica refrigerada",
  "Plasma Fresco Congelado": "caixa térmica com gelo seco",
  "Concentrado de Plaquetas": "caixa térmica em temperatura ambiente controlada",
  "Crioprecipitado": "caixa térmica com gelo seco",
};

export function conexaoDe(hospitalId: string)
{
  return conexoes.find((conexao) => conexao.hospitalId === hospitalId);
}

export function hospitalDe(hospitalId: string)
{
  return hospitais.find((hospital) => hospital.id === hospitalId);
}

/** Todos os pontos da rede — hemocentro de origem + hospitais conectados. */
export const pontosDaRede: PontoDeRede[] = [hemocentro, ...hospitais];

export function formatarFaixa(componente: TipoComponente)
{
  const faixa = FAIXAS_IDEAIS[componente];
  return `${faixa.minima} a ${faixa.maxima} °C`;
}

/**
 * Prioridade de envio por FEFO (*first expired, first out*): entre os lotes do
 * hemocomponente escolhido, saem primeiro os mais próximos do vencimento.
 */
export function prioridadeFefo(componente: TipoComponente, limite = 3): LoteHemocomponente[]
{
  return lotesEstoque
    .filter((lote) => lote.componente === componente)
    .sort((a, b) => diasAteVencer(a) - diasAteVencer(b))
    .slice(0, limite);
}

/** Trajeto completo: hemocentro → vértices intermediários → hospital. */
export function tracadoDaRota(conexao: Conexao, destino: PontoDeRede): Coordenada[]
{
  return [hemocentro, ...conexao.trajeto, destino];
}
