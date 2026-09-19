import type {
  ConsumoDeLote,
  LoteHemocomponente,
  PessoaNecessitada,
  TipoComponente,
  TipoSanguineo,
  UrgenciaNecessidade,
} from "@/types";
import { diasAteVencer } from "@/lib/estoque";

/** Resultado de uma baixa FEFO no estoque (HU-08). */
export interface ResultadoBaixa
{
  /** Estoque já com as unidades descontadas — lotes zerados são removidos. */
  lotes: LoteHemocomponente[];
  consumos: ConsumoDeLote[];
  unidadesBaixadas: number;
  unidadesFaltantes: number;
}

/** Lotes compatíveis com o pedido, do que vence primeiro ao que vence por último. */
export function lotesCompativeis(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
)
{
  return lotes
    .filter(
      (lote) =>
        lote.componente === componente &&
        lote.tipoSanguineo === tipoSanguineo &&
        lote.unidades > 0 &&
        diasAteVencer(lote) >= 0,
    )
    .sort((a, b) => diasAteVencer(a) - diasAteVencer(b));
}

/** Unidades disponíveis para o par componente + tipo sanguíneo. */
export function unidadesDisponiveis(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
)
{
  return lotesCompativeis(lotes, componente, tipoSanguineo).reduce(
    (total, lote) => total + lote.unidades,
    0,
  );
}

/**
 * Reserva `unidades` seguindo FEFO (*first expired, first out*): consome os
 * lotes mais próximos do vencimento primeiro, para reduzir descarte por
 * validade. Quando o estoque não cobre o pedido, baixa o que há e devolve o
 * saldo em `unidadesFaltantes` — quem chama decide o que fazer com a diferença.
 */
export function reservarPorFefo(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
  unidades: number,
): ResultadoBaixa
{
  const fila = lotesCompativeis(lotes, componente, tipoSanguineo);
  const consumos: ConsumoDeLote[] = [];
  const restantes = new Map<string, number>();
  let pendente = Math.max(0, unidades);

  for (const lote of fila)
  {
    if (pendente === 0) break;
    const retirada = Math.min(lote.unidades, pendente);
    consumos.push({ codigo: lote.codigo, unidades: retirada });
    restantes.set(lote.id, lote.unidades - retirada);
    pendente -= retirada;
  }

  const atualizados = lotes
    .map((lote) => (restantes.has(lote.id) ? { ...lote, unidades: restantes.get(lote.id)! } : lote))
    .filter((lote) => lote.unidades > 0);

  return {
    lotes: atualizados,
    consumos,
    unidadesBaixadas: Math.max(0, unidades) - pendente,
    unidadesFaltantes: pendente,
  };
}

const PESO_STATUS: Record<UrgenciaNecessidade, number> = { critico: 0, atencao: 1, estavel: 2 };

/** Ordena a fila clínica: crítico antes de atenção, atenção antes de estável. */
export function ordenarPorUrgencia(pacientes: PessoaNecessitada[])
{
  return [...pacientes].sort((a, b) => PESO_STATUS[a.status] - PESO_STATUS[b.status]);
}

/** Quantos pacientes há em cada nível de urgência. */
export function contarPorUrgencia(pacientes: PessoaNecessitada[]): Record<UrgenciaNecessidade, number>
{
  return pacientes.reduce(
    (total, paciente) => ({ ...total, [paciente.status]: total[paciente.status] + 1 }),
    { critico: 0, atencao: 0, estavel: 0 },
  );
}
