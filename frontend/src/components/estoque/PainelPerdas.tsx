import type { ResumoPerdasDTO, TotalPerdaDTO } from "@/types/dtos";
import { cn } from "@/lib/utilitarios";
import { converterTipoComponente, converterTipoSanguineo, formatarData, rotuloCategoriaDescarte } from "@/lib/estoque";

/** Períodos do painel: últimos N dias (contando hoje) ou todo o histórico (null). */
const PERIODOS_PERDAS: Array<{ dias: number | null; rotulo: string }> =
[
  { dias: 7, rotulo: "7 dias" },
  { dias: 30, rotulo: "30 dias" },
  { dias: 90, rotulo: "90 dias" },
  { dias: null, rotulo: "Tudo" },
];

function formatarVolume(volumeMl: number)
{
  if (volumeMl >= 1000)
  {
    return `${(volumeMl / 1000).toLocaleString("pt-BR", { maximumFractionDigits: 2 })} L`;
  }
  return `${volumeMl.toLocaleString("pt-BR", { maximumFractionDigits: 0 })} mL`;
}

function formatarPercentual(valor: number)
{
  return `${valor.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}%`;
}

function plural(quantidade: number, singular: string, pluralizado: string)
{
  return `${quantidade.toLocaleString("pt-BR")} ${quantidade === 1 ? singular : pluralizado}`;
}

function CartaoResumo({ rotulo, valor, detalhe, destaque }: Readonly<{
  rotulo: string;
  valor: string;
  detalhe: string;
  destaque?: boolean;
}>)
{
  return (
    <article className={cn("rounded-xl border p-5", destaque ? "border-red-200 bg-red-50" : "border-rota-border bg-white")}>
      <p className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-500">{rotulo}</p>
      <p className="mt-2 text-3xl font-extrabold tabular-nums text-red-700">{valor}</p>
      <p className="mt-1 text-xs text-gray-500">{detalhe}</p>
    </article>
  );
}

function RankingPerdas({ titulo, dados, rotular }: Readonly<{
  titulo: string;
  dados: TotalPerdaDTO[];
  rotular: (categoria: string) => string;
}>)
{
  const maior = Math.max(...dados.map((item) => item.bolsas), 0);

  return (
    <section className="rounded-xl border border-rota-border bg-white p-5">
      <h3 className="font-semibold text-gray-900">{titulo}</h3>
      {dados.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500">Nenhuma perda no período.</p>
      ) : (
        <ol className="mt-4 space-y-3.5">
          {dados.map((item, indice) =>
          {
            const largura = maior === 0 ? 0 : (item.bolsas / maior) * 100;
            return (
              <li key={item.categoria}>
                <div className="flex items-center justify-between gap-3 text-sm">
                  <span className="font-medium text-gray-800">
                    <span className="mr-2 font-mono text-xs text-gray-400">#{indice + 1}</span>
                    {rotular(item.categoria)}
                  </span>
                  <span className="shrink-0 text-right tabular-nums text-gray-600">
                    {plural(item.bolsas, "bolsa", "bolsas")} · {formatarVolume(item.volumeMl)}
                  </span>
                </div>
                <div
                  className="mt-1.5 h-2.5 overflow-hidden rounded-full bg-red-50"
                  role="img"
                  aria-label={`${rotular(item.categoria)}: ${plural(item.bolsas, "bolsa descartada", "bolsas descartadas")}`}
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

/**
 * Painel de perdas da tela de Estoque (PI3-243), lido de GET /bancos/{id}/estoque/perdas?dias=N:
 * total de descartes no período, taxa de perda, tipo sanguíneo com mais descartes e os rankings
 * por tipo, componente e motivo, do maior para o menor número de bolsas.
 */
export function PainelPerdas({ resumo, carregando, erro, periodoDias, onMudarPeriodo }: Readonly<{
  resumo: ResumoPerdasDTO | null;
  carregando: boolean;
  erro: string | null;
  periodoDias: number | null;
  onMudarPeriodo: (dias: number | null) => void;
}>)
{
  const periodo = PERIODOS_PERDAS.find((opcao) => opcao.dias === periodoDias) ?? PERIODOS_PERDAS[1];
  const descricaoPeriodo = resumo?.desde ? `desde ${formatarData(resumo.desde)}` : "em todo o histórico";

  return (
    <section aria-labelledby="titulo-painel-perdas" className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-red-700">Indicadores de descarte</p>
          <h2 id="titulo-painel-perdas" className="mt-1 text-2xl font-extrabold text-gray-900">
            Perdas por tipo sanguíneo e componente
          </h2>
          <p className="mt-1 text-sm text-gray-500">
            Bolsas descartadas {resumo ? descricaoPeriodo : `nos últimos ${periodo.rotulo}`}, pela data do descarte.
          </p>
        </div>
        <div className="flex gap-1 rounded-lg border border-rota-border bg-white p-1" role="group" aria-label="Período das perdas">
          {PERIODOS_PERDAS.map((opcao) => (
            <button
              key={opcao.rotulo}
              type="button"
              onClick={() => onMudarPeriodo(opcao.dias)}
              aria-pressed={opcao.dias === periodoDias}
              className={cn(
                "rounded-md px-3 py-1 text-xs font-semibold transition-colors",
                opcao.dias === periodoDias ? "bg-red-700 text-white" : "text-gray-600 hover:bg-gray-100",
              )}
            >
              {opcao.rotulo}
            </button>
          ))}
        </div>
      </div>

      {erro ? (
        <p role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          Não foi possível carregar os indicadores de perdas: {erro}.
        </p>
      ) : !resumo ? (
        <p role="status" className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-500">
          Carregando indicadores de perdas...
        </p>
      ) : (
        <div className={cn("space-y-4 transition-opacity", carregando && "opacity-60")}>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <CartaoResumo
              rotulo="Descartes no período"
              valor={resumo.totalBolsas.toLocaleString("pt-BR")}
              detalhe={`${formatarVolume(resumo.totalVolumeMl)} descartados ${descricaoPeriodo}`}
            />
            <CartaoResumo
              rotulo="Taxa de perda"
              valor={formatarPercentual(resumo.taxaDePerda)}
              detalhe={`${plural(resumo.descartadasDasColetadas, "descartada", "descartadas")} de ${plural(resumo.coletadasNoPeriodo, "bolsa coletada", "bolsas coletadas")} no período`}
            />
            <CartaoResumo
              rotulo="Tipo com mais descartes"
              valor={resumo.tiposComMaisDescartes.length === 0
                ? "—"
                : resumo.tiposComMaisDescartes.map(converterTipoSanguineo).join(" · ")}
              detalhe={resumo.tiposComMaisDescartes.length === 0
                ? "Nenhum descarte no período"
                : `${plural(resumo.porTipoSanguineo[0].bolsas, "bolsa descartada", "bolsas descartadas")}${resumo.tiposComMaisDescartes.length > 1 ? " cada (empate)" : ""}`}
              destaque={resumo.tiposComMaisDescartes.length > 0}
            />
          </div>

          {resumo.totalBolsas === 0 ? (
            <p className="rounded-xl border border-rota-border bg-white p-5 text-sm text-gray-600">
              Nenhuma bolsa foi descartada {descricaoPeriodo}.
            </p>
          ) : (
            <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
              <RankingPerdas titulo="Por tipo sanguíneo" dados={resumo.porTipoSanguineo} rotular={converterTipoSanguineo} />
              <RankingPerdas titulo="Por componente" dados={resumo.porComponente} rotular={converterTipoComponente} />
              <RankingPerdas titulo="Por motivo" dados={resumo.porMotivo} rotular={rotuloCategoriaDescarte} />
            </div>
          )}
        </div>
      )}
    </section>
  );
}
