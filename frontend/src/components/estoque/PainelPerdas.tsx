import type { ResumoPerdasDTO } from "@/types/dtos";
import { converterTipoComponente, converterTipoSanguineo } from "@/lib/estoque";

function formatarVolume(volumeMl: number)
{
  if (volumeMl >= 1000)
  {
    return `${(volumeMl / 1000).toLocaleString("pt-BR", { maximumFractionDigits: 2 })} L`;
  }
  return `${volumeMl.toLocaleString("pt-BR", { maximumFractionDigits: 0 })} mL`;
}

function CartaoResumo({ rotulo, valor, detalhe }: Readonly<{
  rotulo: string;
  valor: string;
  detalhe: string;
}>)
{
  return (
    <article className="rounded-xl border border-rota-border bg-white p-5">
      <p className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-500">{rotulo}</p>
      <p className="mt-2 text-3xl font-extrabold text-red-700">{valor}</p>
      <p className="mt-1 text-xs text-gray-500">{detalhe}</p>
    </article>
  );
}

function GraficoPerdas({ titulo, dados, rotular }: Readonly<{
  titulo: string;
  dados: ResumoPerdasDTO["porComponente"];
  rotular: (categoria: string) => string;
}>)
{
  const maiorVolume = Math.max(...dados.map((item) => item.volumeMl), 0);

  return (
    <section className="rounded-xl border border-rota-border bg-white p-5">
      <h3 className="font-semibold text-gray-900">{titulo}</h3>
      {dados.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500">Nenhuma perda registrada.</p>
      ) : (
        <ol className="mt-4 space-y-4">
          {dados.map((item, indice) =>
          {
            const largura = maiorVolume === 0 ? 0 : (item.volumeMl / maiorVolume) * 100;
            return (
              <li key={item.categoria}>
                <div className="flex items-center justify-between gap-3 text-sm">
                  <span className="font-medium text-gray-800">
                    <span className="mr-2 font-mono text-xs text-gray-400">#{indice + 1}</span>
                    {rotular(item.categoria)}
                  </span>
                  <span className="shrink-0 text-right text-gray-600">
                    {formatarVolume(item.volumeMl)} · {item.bolsas} {item.bolsas === 1 ? "bolsa" : "bolsas"}
                  </span>
                </div>
                <div
                  className="mt-2 h-2.5 overflow-hidden rounded-full bg-red-50"
                  role="img"
                  aria-label={`${rotular(item.categoria)}: ${formatarVolume(item.volumeMl)} descartados`}
                >
                  <div className="h-full rounded-full bg-red-600 transition-all" style={{ width: `${largura}%` }} />
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </section>
  );
}

export function PainelPerdas({ resumo, carregando, erro }: Readonly<{
  resumo: ResumoPerdasDTO | null;
  carregando: boolean;
  erro: string | null;
}>)
{
  return (
    <section aria-labelledby="titulo-painel-perdas" className="space-y-4">
      <div>
        <p className="font-mono text-xs font-semibold uppercase tracking-widest text-red-700">Indicadores de descarte</p>
        <h2 id="titulo-painel-perdas" className="mt-1 text-2xl font-extrabold text-gray-900">
          Perdas por tipo sanguíneo e componente
        </h2>
        <p className="mt-1 text-sm text-gray-500">Volume calculado pela soma do volume unitário das bolsas descartadas.</p>
      </div>

      {erro ? (
        <p role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          Não foi possível carregar os indicadores de perdas: {erro}.
        </p>
      ) : carregando || !resumo ? (
        <p role="status" className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-500">
          Carregando indicadores de perdas...
        </p>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <CartaoResumo
              rotulo="Bolsas descartadas"
              valor={resumo.totalBolsas.toLocaleString("pt-BR")}
              detalhe={`${resumo.totalBolsas} ${resumo.totalBolsas === 1 ? "bolsa" : "bolsas"} no total`}
            />
            <CartaoResumo
              rotulo="Volume descartado"
              valor={formatarVolume(resumo.totalVolumeMl)}
              detalhe={`${resumo.totalVolumeMl.toLocaleString("pt-BR")} mL no total`}
            />
          </div>

          {resumo.totalBolsas === 0 ? (
            <p className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-600">
              Ainda não há bolsas descartadas para compor os gráficos.
            </p>
          ) : (
            <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
              <GraficoPerdas
                titulo="Ranking por tipo sanguíneo"
                dados={resumo.porTipoSanguineo}
                rotular={converterTipoSanguineo}
              />
              <GraficoPerdas
                titulo="Ranking por componente"
                dados={resumo.porComponente}
                rotular={converterTipoComponente}
              />
            </div>
          )}
        </>
      )}
    </section>
  );
}
