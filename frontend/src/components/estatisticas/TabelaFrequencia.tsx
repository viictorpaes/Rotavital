import type { DistribuicaoDTO } from "@/types/dtos";
import { formatarPercentual } from "@/lib/estatisticas";

/** Tabela de distribuição de frequências: fi, fr e, para classes ordenadas, a Fr acumulada. */
export function TabelaFrequencia({ distribuicao, rotular, cabecalho, comAcumulada = false }: Readonly<{
  distribuicao: DistribuicaoDTO;
  rotular: (categoria: string) => string;
  cabecalho: string;
  comAcumulada?: boolean;
}>)
{
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[320px] text-sm">
        <thead>
          <tr className="border-b border-rota-border text-left font-mono text-[11px] uppercase tracking-widest text-gray-500">
            <th className="py-2 pr-3 font-semibold">{cabecalho}</th>
            <th className="py-2 pr-3 text-right font-semibold normal-case" title="Frequência absoluta">fi</th>
            <th className="py-2 pr-3 text-right font-semibold normal-case" title="Frequência relativa">fr</th>
            {comAcumulada && (
              <th className="py-2 text-right font-semibold normal-case" title="Frequência relativa acumulada">Fr</th>
            )}
          </tr>
        </thead>
        <tbody className="tabular-nums">
          {distribuicao.linhas.map((linha) => (
            <tr key={linha.categoria} className="border-b border-rota-border/60">
              <td className="py-1.5 pr-3 text-gray-800">{rotular(linha.categoria)}</td>
              <td className="py-1.5 pr-3 text-right text-gray-800">{linha.frequenciaAbsoluta}</td>
              <td className="py-1.5 pr-3 text-right text-gray-600">{formatarPercentual(linha.frequenciaRelativa)}</td>
              {comAcumulada && (
                <td className="py-1.5 text-right text-gray-600">{formatarPercentual(linha.frequenciaRelativaAcumulada)}</td>
              )}
            </tr>
          ))}
        </tbody>
        <tfoot className="tabular-nums">
          <tr className="font-semibold text-gray-900">
            <td className="py-2 pr-3">Total</td>
            <td className="py-2 pr-3 text-right">{distribuicao.total}</td>
            <td className="py-2 pr-3 text-right">{distribuicao.total === 0 ? "—" : "100,0%"}</td>
            {comAcumulada && <td />}
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
