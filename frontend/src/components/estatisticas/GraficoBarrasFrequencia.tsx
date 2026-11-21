import type { LinhaFrequenciaDTO } from "@/types/dtos";
import { formatarPercentual } from "@/lib/estatisticas";

/**
 * Gráfico de barras horizontais de uma variável qualitativa: o comprimento é a frequência
 * relativa (fr) e o rótulo traz fi e fr. A moda aparece em vermelho.
 */
export function GraficoBarrasFrequencia({ linhas, rotular, moda = [] }: Readonly<{
  linhas: LinhaFrequenciaDTO[];
  rotular: (categoria: string) => string;
  moda?: string[];
}>)
{
  const maior = Math.max(...linhas.map((linha) => linha.frequenciaRelativa), 0);

  return (
    <ol className="space-y-2.5">
      {linhas.map((linha) =>
      {
        const largura = maior === 0 ? 0 : (linha.frequenciaRelativa / maior) * 100;
        const ehModa = moda.includes(linha.categoria);
        return (
          <li key={linha.categoria} className="grid grid-cols-[6.5rem_1fr_6.5rem] items-center gap-3 text-sm">
            <span className={ehModa ? "font-bold text-rota-red" : "font-medium text-gray-700"}>
              {rotular(linha.categoria)}
            </span>
            <div
              className="h-3 overflow-hidden rounded-full bg-gray-100"
              role="img"
              aria-label={`${rotular(linha.categoria)}: ${linha.frequenciaAbsoluta} bolsas, ${formatarPercentual(linha.frequenciaRelativa)}`}
            >
              <div
                className={ehModa ? "h-full rounded-full bg-rota-red" : "h-full rounded-full bg-gray-400"}
                style={{ width: `${largura}%` }}
              />
            </div>
            <span className="text-right font-mono text-xs tabular-nums text-gray-600">
              {linha.frequenciaAbsoluta} · {formatarPercentual(linha.frequenciaRelativa)}
            </span>
          </li>
        );
      })}
    </ol>
  );
}
