import type { Coordenada, LoteHemocomponente, TipoComponente } from "@/types";
import type { BolsaHemocomponenteDTO, PontoRedeDTO } from "@/types/dtos";
import { converterBolsaParaLote, diasAteVencer, obterFaixaTemperatura, temperaturaForaDaFaixa } from "@/lib/estoque";

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
  const faixa = obterFaixaTemperatura(componente);
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
 * Fila de envio por FEFO (*first expired, first out*): a API devolve as bolsas da que vence primeiro
 * para a última, e aqui só ficam as que podem sair, com a mesma regra do backend: na validade e com
 * a cadeia fria intacta.
 */
export function filaFefo(bolsas: BolsaHemocomponenteDTO[], limite = 3): LoteHemocomponente[]
{
  return bolsas
    .map(converterBolsaParaLote)
    .filter((lote) => diasAteVencer(lote) >= 0 && !temperaturaForaDaFaixa(lote))
    .slice(0, limite);
}
