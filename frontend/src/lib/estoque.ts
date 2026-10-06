import type { LoteHemocomponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";

export const TIPOS_SANGUINEOS: TipoSanguineo[] = 
["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];

/** Dias restantes até a validade (negativo quando o lote já venceu). */
export function diasAteVencer(lote: LoteHemocomponente)
{
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const validade = new Date(`${lote.dataValidade}T00:00:00`);
  return Math.round((validade.getTime() - hoje.getTime()) / 86_400_000);
}

/** Temperatura do lote fora da faixa ideal do hemocomponente (HU-03). */
export function temperaturaForaDaFaixa(lote: LoteHemocomponente)
{
  return(
    lote.temperaturaAtual < lote.temperaturaIdeal.minima ||
    lote.temperaturaAtual > lote.temperaturaIdeal.maxima
  );
}

/** Risco do lote: temperatura fora da faixa ou validade próxima. */
export function statusLote(lote: LoteHemocomponente): UrgenciaNecessidade
{
  const dias = diasAteVencer(lote);
  if (temperaturaForaDaFaixa(lote) || dias <= 3) return "critico";
  
  if (dias <= 15) 
  {
    return "atencao";
  }
  return "estavel";
}

const PESO_STATUS: Record<UrgenciaNecessidade, number> = { critico: 2, atencao: 1, estavel: 0 };

/** Status do tipo sanguíneo = pior status entre seus lotes (HU-03). */
export function statusGrupo(lotes: LoteHemocomponente[]): UrgenciaNecessidade
{
  return lotes.reduce<UrgenciaNecessidade>((pior, lote) =>
  {
    const atual = statusLote(lote);
    return PESO_STATUS[atual] > PESO_STATUS[pior] ? atual : pior;
  }, "estavel");
}

export function formatarData(iso: string)
{
  return new Date(`${iso}T00:00:00`).toLocaleDateString("pt-BR");
}

export function formatarTemperatura(valor: number)
{
  return `${valor.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}°C`;
}

export function converterTipoSanguineo(tipo: string): TipoSanguineo
{
  switch (tipo)
  {
    case "A_POSITIVO":
      return "A+";
    case "A_NEGATIVO":
      return "A-";
    case "B_POSITIVO":
      return "B+";
    case "B_NEGATIVO":
      return "B-";
    case "AB_POSITIVO":
      return "AB+";
    case "AB_NEGATIVO":
      return "AB-";
    case "O_POSITIVO":
      return "O+";
    case "O_NEGATIVO":
      return "O-";
    default:
      return (tipo as TipoSanguineo) || "O+";
  }
}

export function converterTipoComponente(tipo: string): any
{
  switch (tipo)
  {
    case "HEMACIAS":
      return "Concentrado de Hemácias";
    case "PLASMA":
      return "Plasma Fresco Congelado";
    case "PLAQUETAS":
      return "Concentrado de Plaquetas";
    case "CRIOPRECIPITADO":
      return "Crioprecipitado";
    default:
      return tipo || "Concentrado de Hemácias";
  }
}

export function obterFaixaTemperatura(tipo: string): { minima: number; maxima: number }
{
  switch (tipo)
  {
    case "HEMACIAS":
    case "Concentrado de Hemácias":
      return { minima: 2.0, maxima: 6.0 };
    case "PLASMA":
    case "Plasma Fresco Congelado":
      return { minima: -30.0, maxima: -18.0 };
    case "PLAQUETAS":
    case "Concentrado de Plaquetas":
      return { minima: 20.0, maxima: 24.0 };
    case "CRIOPRECIPITADO":
    case "Crioprecipitado":
      return { minima: -30.0, maxima: -18.0 };
    default:
      return { minima: 2.0, maxima: 6.0 };
  }
}

export function converterBolsaParaLote(bolsa: any): LoteHemocomponente
{
  const componente = converterTipoComponente(bolsa.tipoComponente);
  const tipoSanguineo = converterTipoSanguineo(bolsa.tipoSanguineo);

  return {
    id: bolsa.id,
    codigo: bolsa.loteSintetico || bolsa.id,
    componente,
    tipoSanguineo,
    unidades: 1,
    volumeMl: bolsa.volumeMl,
    dataValidade: bolsa.dataValidade,
    temperaturaAtual: bolsa.temperaturaCelsius ?? bolsa.temperaturaAtual ?? 4.0,
    temperaturaIdeal: obterFaixaTemperatura(bolsa.tipoComponente),
    localizacao: bolsa.localizacao || bolsa.localizacaoFisica || "Câmara Fria",
  };
}