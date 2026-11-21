import type { ResumoNumericoDTO } from "@/types/dtos";
import { formatarNumero } from "@/lib/estatisticas";

/**
 * Diagrama de caixa (boxplot) de uma amostra: bigodes do mínimo ao máximo, caixa do Q1 ao Q3
 * e a mediana como traço. A escala vai do mínimo ao máximo da própria amostra.
 */
export function BoxPlot({ resumo, casas, unidade }: Readonly<{
  resumo: ResumoNumericoDTO;
  casas: number;
  unidade: string;
}>)
{
  const { minimo, primeiroQuartil, mediana, terceiroQuartil, maximo } = resumo;
  if (resumo.n === 0 || minimo === null || maximo === null || primeiroQuartil === null
    || terceiroQuartil === null || mediana === null)
  {
    return <span className="text-xs text-gray-400">Sem bolsas</span>;
  }

  const amplitude = maximo - minimo;
  const posicao = (valor: number) => (amplitude === 0 ? 50 : ((valor - minimo) / amplitude) * 100);

  return (
    <div className="min-w-[160px]">
      <div
        className="relative h-6"
        role="img"
        aria-label={`Mínimo ${formatarNumero(minimo, casas)}, Q1 ${formatarNumero(primeiroQuartil, casas)}, mediana ${formatarNumero(mediana, casas)}, Q3 ${formatarNumero(terceiroQuartil, casas)}, máximo ${formatarNumero(maximo, casas)} ${unidade}`}
      >
        <div className="absolute top-1/2 h-px w-full -translate-y-1/2 bg-gray-400" />
        <div className="absolute top-1 bottom-1 w-px bg-gray-500" style={{ left: "0%" }} />
        <div className="absolute top-1 bottom-1 w-px bg-gray-500" style={{ left: "calc(100% - 1px)" }} />
        <div
          className="absolute top-0.5 bottom-0.5 rounded-sm border border-rota-red bg-red-50"
          style={{
            left: `${posicao(primeiroQuartil)}%`,
            width: `max(2px, ${posicao(terceiroQuartil) - posicao(primeiroQuartil)}%)`,
          }}
        />
        <div className="absolute top-0 bottom-0 w-0.5 bg-rota-red" style={{ left: `${posicao(mediana)}%` }} />
      </div>
      <div className="flex justify-between font-mono text-[10px] tabular-nums text-gray-400">
        <span>{formatarNumero(minimo, casas)}</span>
        <span>{formatarNumero(maximo, casas)}</span>
      </div>
    </div>
  );
}
