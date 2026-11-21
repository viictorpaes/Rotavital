import type { MedidasComponenteDTO, ResumoNumericoDTO } from "@/types/dtos";
import { converterTipoSanguineo } from "@/lib/estoque";

/** Número em pt-BR com casas fixas; medida sem valor (amostra vazia) vira "—". */
export function formatarNumero(valor: number | null | undefined, casas = 1): string
{
  if (valor === null || valor === undefined || Number.isNaN(valor))
  {
    return "—";
  }
  return valor.toLocaleString("pt-BR", { minimumFractionDigits: casas, maximumFractionDigits: casas });
}

export function formatarPercentual(valor: number | null | undefined, casas = 1): string
{
  const numero = formatarNumero(valor, casas);
  return numero === "—" ? numero : `${numero}%`;
}

const ROTULOS_COMPONENTE: Record<string, string> =
{
  HEMACIAS: "Hemácias",
  PLASMA: "Plasma",
  PLAQUETAS: "Plaquetas",
  CRIOPRECIPITADO: "Crioprecipitado",
};

/** Nome curto do componente, para caber em eixos e tabelas. */
export function rotuloComponente(componente: string): string
{
  return ROTULOS_COMPONENTE[componente] ?? componente;
}

/** "O_NEGATIVO" → "O−", com o sinal de menos tipográfico. */
export function rotuloTipoSanguineo(tipo: string): string
{
  return converterTipoSanguineo(tipo).replace("-", "−");
}

/** As três variáveis quantitativas medidas por componente. */
export type VariavelQuantitativa = "diasAteVencer" | "temperaturaCelsius" | "volumeMl";

export const VARIAVEIS: Array<{ valor: VariavelQuantitativa; rotulo: string; unidade: string; casas: number }> =
[
  { valor: "diasAteVencer", rotulo: "Dias até vencer", unidade: "dias", casas: 1 },
  { valor: "temperaturaCelsius", rotulo: "Temperatura", unidade: "°C", casas: 1 },
  { valor: "volumeMl", rotulo: "Volume", unidade: "mL", casas: 0 },
];

export function resumoDa(medidas: MedidasComponenteDTO, variavel: VariavelQuantitativa): ResumoNumericoDTO
{
  return medidas[variavel];
}
