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