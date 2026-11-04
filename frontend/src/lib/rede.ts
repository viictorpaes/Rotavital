import type { Coordenada, LoteHemocomponente, TipoComponente } from "@/types";
import type { PontoRedeDTO } from "@/types/dtos";
import { lotesEstoque, FAIXAS_IDEAIS } from "@/data/estoqueMock";
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

/** "Av. Prof. Moraes Rego, 1235 — Recife/PE": cidade e UF são opcionais no cadastro. */
export function enderecoDoPonto(ponto: PontoRedeDTO)
{
  const local = [ponto.cidade, ponto.uf].filter(Boolean).join("/");
  return local ? `${ponto.logradouro} — ${local}` : ponto.logradouro;
}

export function formatarFaixa(componente: TipoComponente)
{
  const faixa = FAIXAS_IDEAIS[componente];
  return `${faixa.minima} a ${faixa.maxima} °C`;
}

/**
 * Traçado do Google em coordenadas: o formato "encoded polyline" guarda cada ponto como a diferença
 * para o anterior, em blocos de 5 bits (developers.google.com/maps/documentation/utilities/polylinealgorithm).
 */
export function decodificarPolyline(polyline: string): Coordenada[]
{
  const pontos: Coordenada[] = [];
  let indice = 0;
  let latitude = 0;
  let longitude = 0;

  function proximaDiferenca()
  {
    let valor = 0;
    let deslocamento = 0;
    let bloco: number;

    do
    {
      bloco = polyline.charCodeAt(indice++) - 63;
      valor |= (bloco & 0x1f) << deslocamento;
      deslocamento += 5;
    }
    while (bloco >= 0x20);

    return valor & 1 ? ~(valor >> 1) : valor >> 1;
  }

  while (indice < polyline.length)
  {
    latitude += proximaDiferenca();
    longitude += proximaDiferenca();
    pontos.push({ latitude: latitude / 1e5, longitude: longitude / 1e5 });
  }

  return pontos;
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
