import type { MedidasComponenteDTO } from "@/types/dtos";
import {
  formatarNumero,
  formatarPercentual,
  resumoDa,
  rotuloComponente,
  type VariavelQuantitativa,
} from "@/lib/estatisticas";
import { BoxPlot } from "./BoxPlot";

const COLUNAS: Array<{ chave: string; rotulo: string; titulo: string }> =
[
  { chave: "n", rotulo: "n", titulo: "Tamanho da amostra" },
  { chave: "media", rotulo: "x̄", titulo: "Média" },
  { chave: "mediana", rotulo: "Md", titulo: "Mediana" },
  { chave: "desvioPadrao", rotulo: "s", titulo: "Desvio padrão amostral" },
  { chave: "coeficienteDeVariacao", rotulo: "CV", titulo: "Coeficiente de variação" },
  { chave: "minimo", rotulo: "mín", titulo: "Mínimo" },
  { chave: "primeiroQuartil", rotulo: "Q1", titulo: "Primeiro quartil" },
  { chave: "terceiroQuartil", rotulo: "Q3", titulo: "Terceiro quartil" },
  { chave: "maximo", rotulo: "máx", titulo: "Máximo" },
];

/** Medidas de posição e dispersão de uma variável, uma linha por componente, com o boxplot ao lado. */
export function QuadroMedidas({ componentes, variavel, casas, unidade }: Readonly<{
  componentes: MedidasComponenteDTO[];
  variavel: VariavelQuantitativa;
  casas: number;
  unidade: string;
}>)
{
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[820px] text-sm">
        <thead>
          <tr className="border-b border-rota-border text-left font-mono text-[11px] uppercase tracking-widest text-gray-500">
            <th className="py-2 pr-3 font-semibold">Componente</th>
            {COLUNAS.map((coluna) => (
              <th key={coluna.chave} className="py-2 pr-3 text-right font-semibold normal-case" title={coluna.titulo}>
                {coluna.rotulo}
              </th>
            ))}
            <th className="py-2 pl-3 font-semibold">Boxplot ({unidade})</th>
          </tr>
        </thead>
        <tbody className="tabular-nums">
          {componentes.map((medidas) =>
          {
            const resumo = resumoDa(medidas, variavel);
            return (
              <tr key={medidas.componente} className="border-b border-rota-border/60 align-middle">
                <td className="py-2.5 pr-3 font-medium text-gray-800">{rotuloComponente(medidas.componente)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-800">{resumo.n}</td>
                <td className="py-2.5 pr-3 text-right font-semibold text-gray-900">{formatarNumero(resumo.media, casas)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-700">{formatarNumero(resumo.mediana, casas)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-700">{formatarNumero(resumo.desvioPadrao, casas + 1)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-700">{formatarPercentual(resumo.coeficienteDeVariacao)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-500">{formatarNumero(resumo.minimo, casas)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-500">{formatarNumero(resumo.primeiroQuartil, casas)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-500">{formatarNumero(resumo.terceiroQuartil, casas)}</td>
                <td className="py-2.5 pr-3 text-right text-gray-500">{formatarNumero(resumo.maximo, casas)}</td>
                <td className="py-2.5 pl-3">
                  <BoxPlot resumo={resumo} casas={casas} unidade={unidade} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
