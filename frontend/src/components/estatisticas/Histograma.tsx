import type { LinhaFrequenciaDTO } from "@/types/dtos";
import { formatarPercentual } from "@/lib/estatisticas";

/**
 * Histograma de classes ordenadas: colunas encostadas umas nas outras (a variável é contínua),
 * com a altura proporcional à frequência absoluta da classe.
 */
export function Histograma({ linhas }: Readonly<{ linhas: LinhaFrequenciaDTO[] }>)
{
  const maior = Math.max(...linhas.map((linha) => linha.frequenciaAbsoluta), 0);

  return (
    <div>
      <div className="flex h-48 items-end border-b border-l border-gray-300">
        {linhas.map((linha) =>
        {
          const altura = maior === 0 ? 0 : (linha.frequenciaAbsoluta / maior) * 100;
          return (
            <div key={linha.categoria} className="flex h-full flex-1 flex-col justify-end">
              <span className="pb-1 text-center font-mono text-xs tabular-nums text-gray-600">
                {linha.frequenciaAbsoluta}
              </span>
              <div
                className="border-r border-white bg-rota-red/80"
                style={{ height: `${altura}%` }}
                role="img"
                aria-label={`${linha.categoria}: ${linha.frequenciaAbsoluta} bolsas, ${formatarPercentual(linha.frequenciaRelativa)}`}
              />
            </div>
          );
        })}
      </div>
      <div className="flex pt-2">
        {linhas.map((linha) => (
          <span key={linha.categoria} className="flex-1 px-1 text-center text-[11px] leading-tight text-gray-500">
            {linha.categoria}
          </span>
        ))}
      </div>
    </div>
  );
}
