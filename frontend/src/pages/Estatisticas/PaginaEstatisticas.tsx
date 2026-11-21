import { useEffect, useState } from "react";
import { AlertCircle, BarChart3, RefreshCw } from "lucide-react";
import type { EstatisticasDoEstoqueDTO } from "@/types/dtos";
import { buscarEstatisticasDoEstoque } from "@/services/api";
import { ErroHttp } from "@/services/clienteHttp";
import {
  formatarPercentual,
  rotuloComponente,
  rotuloTipoSanguineo,
  VARIAVEIS,
  type VariavelQuantitativa,
} from "@/lib/estatisticas";
import { GraficoBarrasFrequencia } from "@/components/estatisticas/GraficoBarrasFrequencia";
import { TabelaFrequencia } from "@/components/estatisticas/TabelaFrequencia";
import { Histograma } from "@/components/estatisticas/Histograma";
import { QuadroMedidas } from "@/components/estatisticas/QuadroMedidas";

const BANCO_ID = "BS-01";

function formatarData(iso: string): string
{
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

function Indicador({ rotulo, valor, detalhe }: Readonly<{ rotulo: string; valor: string; detalhe: string }>)
{
  return (
    <article className="rounded-xl border border-rota-border bg-white p-5">
      <p className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-500">{rotulo}</p>
      <p className="mt-2 text-3xl font-extrabold tabular-nums text-gray-900">{valor}</p>
      <p className="mt-1 text-xs text-gray-500">{detalhe}</p>
    </article>
  );
}

function Secao({ titulo, descricao, children }: Readonly<{
  titulo: string;
  descricao: string;
  children: React.ReactNode;
}>)
{
  return (
    <section className="space-y-4 border-t border-rota-border pt-6">
      <div>
        <h2 className="text-xl font-extrabold text-gray-900">{titulo}</h2>
        <p className="mt-1 max-w-3xl text-sm text-gray-500">{descricao}</p>
      </div>
      {children}
    </section>
  );
}

function Moda({ moda, rotular }: Readonly<{ moda: string[]; rotular: (categoria: string) => string }>)
{
  return (
    <p className="text-sm text-gray-600">
      <span className="font-semibold text-gray-900">Moda:</span>{" "}
      {moda.length === 0 ? "não há (nenhuma bolsa em estoque)" : moda.map(rotular).join(", ")}
      {moda.length > 1 && " (distribuição multimodal)"}
    </p>
  );
}

/**
 * Estatística descritiva do estoque do banco BS-01 (disciplina de Estatística e Probabilidade):
 * distribuições de frequência das variáveis qualitativas, histograma da validade e medidas de
 * posição e dispersão por componente. Os números vêm de GET /api/v1/bancos/{id}/estoque/estatisticas.
 */
export default function PaginaEstatisticas()
{
  const [dados, setDados] = useState<EstatisticasDoEstoqueDTO | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [recarga, setRecarga] = useState(0);
  const [variavel, setVariavel] = useState<VariavelQuantitativa>("diasAteVencer");

  useEffect(() =>
  {
    let ativa = true;
    setCarregando(true);

    buscarEstatisticasDoEstoque(BANCO_ID)
      .then((resposta) =>
      {
        if (!ativa) return;
        setDados(resposta);
        setErro(null);
      })
      .catch((falha: unknown) =>
      {
        if (!ativa) return;
        const detalhe = falha instanceof ErroHttp ? falha.detalhe : null;
        setErro(detalhe || (falha instanceof Error && falha.message ? falha.message : "Não foi possível conectar com o back-end"));
      })
      .finally(() =>
      {
        if (ativa) setCarregando(false);
      });

    return () =>
    {
      ativa = false;
    };
  }, [recarga]);

  const variavelAtual = VARIAVEIS.find((opcao) => opcao.valor === variavel) ?? VARIAVEIS[0];

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Estatística descritiva · Banco {BANCO_ID}
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Estatísticas do estoque</h1>
          <p className="mt-2 max-w-2xl text-sm text-gray-500">
            Como o estoque se distribui por tipo sanguíneo, componente e validade, e quanto cada componente
            varia em validade, temperatura e volume.
            {dados && <> Dados de {formatarData(dados.dataDeReferencia)}.</>}
          </p>
        </div>
        <button
          type="button"
          onClick={() => setRecarga((valor) => valor + 1)}
          disabled={carregando}
          className="flex items-center gap-2 rounded-lg border border-rota-border bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 shadow-sm hover:bg-gray-50 active:scale-95 disabled:opacity-50"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${carregando ? "animate-spin text-rota-red" : ""}`} />
          {carregando ? "Calculando..." : "Recalcular"}
        </button>
      </div>

      {erro && (
        <div role="alert" className="flex flex-wrap items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
          <AlertCircle className="h-5 w-5 shrink-0 text-rose-600" />
          <span className="flex-1">
            <strong>Não foi possível calcular as estatísticas:</strong> {erro}.
          </span>
          <button
            type="button"
            onClick={() => setRecarga((valor) => valor + 1)}
            className="rounded-lg border border-rose-300 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-100"
          >
            Tentar novamente
          </button>
        </div>
      )}

      {!dados && !erro && (
        <div className="flex flex-col items-center justify-center rounded-xl border border-rota-border bg-white p-12 text-center">
          <BarChart3 className="h-8 w-8 animate-pulse text-rota-red" />
          <p className="mt-3 text-sm font-semibold text-gray-700">Calculando as estatísticas do estoque...</p>
          <p className="text-xs text-gray-400">Endpoint: GET /api/v1/bancos/{BANCO_ID}/estoque/estatisticas</p>
        </div>
      )}

      {dados && (
        <div className={carregando ? "space-y-8 opacity-60 transition-opacity" : "space-y-8 transition-opacity"}>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <Indicador rotulo="Em estoque" valor={String(dados.emEstoque)} detalhe="Bolsas disponíveis e na validade" />
            <Indicador rotulo="Cadastradas" valor={String(dados.cadastradas)} detalhe="Todas as bolsas do banco, em qualquer status" />
            <Indicador
              rotulo="Vencidas sem descarte"
              valor={String(dados.vencidasAindaNoEstoque)}
              detalhe="Ainda marcadas como disponíveis"
            />
            <Indicador
              rotulo="Taxa de perda"
              valor={formatarPercentual(dados.taxaDePerda)}
              detalhe={`${dados.descartadas} descartada(s) de ${dados.cadastradas} cadastrada(s)`}
            />
          </div>

          <Secao
            titulo="Distribuição por tipo sanguíneo"
            descricao="Variável qualitativa nominal. Frequência absoluta (fi) e relativa (fr) das bolsas em estoque; a barra vermelha é a moda."
          >
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <div className="space-y-4 rounded-xl border border-rota-border bg-white p-5">
                <GraficoBarrasFrequencia
                  linhas={dados.porTipoSanguineo.linhas}
                  rotular={rotuloTipoSanguineo}
                  moda={dados.porTipoSanguineo.moda}
                />
                <Moda moda={dados.porTipoSanguineo.moda} rotular={rotuloTipoSanguineo} />
              </div>
              <div className="rounded-xl border border-rota-border bg-white p-5">
                <TabelaFrequencia distribuicao={dados.porTipoSanguineo} rotular={rotuloTipoSanguineo} cabecalho="Tipo" />
              </div>
            </div>
          </Secao>

          <Secao
            titulo="Distribuição por componente"
            descricao="Variável qualitativa nominal: quanto do estoque é de hemácias, plasma, plaquetas e crioprecipitado."
          >
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <div className="space-y-4 rounded-xl border border-rota-border bg-white p-5">
                <GraficoBarrasFrequencia
                  linhas={dados.porComponente.linhas}
                  rotular={rotuloComponente}
                  moda={dados.porComponente.moda}
                />
                <Moda moda={dados.porComponente.moda} rotular={rotuloComponente} />
              </div>
              <div className="rounded-xl border border-rota-border bg-white p-5">
                <TabelaFrequencia distribuicao={dados.porComponente} rotular={rotuloComponente} cabecalho="Componente" />
              </div>
            </div>
          </Secao>

          <Secao
            titulo="Dias até vencer"
            descricao="Variável quantitativa agrupada em classes. O histograma mostra quantas bolsas vencem em cada faixa; a Fr acumulada diz, por exemplo, quanto do estoque vence em até 30 dias."
          >
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
              <div className="rounded-xl border border-rota-border bg-white p-5">
                <Histograma linhas={dados.porDiasAteVencer.linhas} />
              </div>
              <div className="rounded-xl border border-rota-border bg-white p-5">
                <TabelaFrequencia
                  distribuicao={dados.porDiasAteVencer}
                  rotular={(classe) => classe}
                  cabecalho="Classe"
                  comAcumulada
                />
              </div>
            </div>
          </Secao>

          <Secao
            titulo="Medidas de posição e dispersão por componente"
            descricao="Cada componente tem sua própria faixa de validade, temperatura e volume, por isso as medidas são separadas por componente."
          >
            <div className="flex flex-wrap gap-2" role="group" aria-label="Variável analisada">
              {VARIAVEIS.map((opcao) => (
                <button
                  key={opcao.valor}
                  type="button"
                  onClick={() => setVariavel(opcao.valor)}
                  aria-pressed={variavel === opcao.valor}
                  className={variavel === opcao.valor
                    ? "rounded-full bg-rota-red px-3 py-1.5 text-xs font-semibold text-white"
                    : "rounded-full border border-rota-border bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50"}
                >
                  {opcao.rotulo} ({opcao.unidade})
                </button>
              ))}
            </div>
            <div className="rounded-xl border border-rota-border bg-white p-5">
              <QuadroMedidas
                componentes={dados.componentes}
                variavel={variavel}
                casas={variavelAtual.casas}
                unidade={variavelAtual.unidade}
              />
              {variavel === "temperaturaCelsius" && (
                <ul className="mt-4 flex flex-wrap gap-2 text-xs">
                  {dados.componentes.map((medidas) => (
                    <li
                      key={medidas.componente}
                      className={medidas.foraDaFaixa > 0
                        ? "rounded-full bg-rose-50 px-3 py-1 font-medium text-rose-700"
                        : "rounded-full bg-gray-100 px-3 py-1 text-gray-600"}
                    >
                      {rotuloComponente(medidas.componente)}: {medidas.foraDaFaixa} de {medidas.emEstoque} fora da faixa
                      ({formatarPercentual(medidas.percentualForaDaFaixa)})
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </Secao>

          <Secao
            titulo="Perdas por componente"
            descricao="Taxa de perda = bolsas descartadas ÷ bolsas cadastradas do componente."
          >
            <div className="overflow-x-auto rounded-xl border border-rota-border bg-white p-5">
              <table className="w-full min-w-[420px] text-sm">
                <thead>
                  <tr className="border-b border-rota-border text-left font-mono text-[11px] uppercase tracking-widest text-gray-500">
                    <th className="py-2 pr-3 font-semibold">Componente</th>
                    <th className="py-2 pr-3 text-right font-semibold">Cadastradas</th>
                    <th className="py-2 pr-3 text-right font-semibold">Descartadas</th>
                    <th className="py-2 text-right font-semibold">Taxa de perda</th>
                  </tr>
                </thead>
                <tbody className="tabular-nums">
                  {dados.componentes.map((medidas) => (
                    <tr key={medidas.componente} className="border-b border-rota-border/60">
                      <td className="py-2 pr-3 font-medium text-gray-800">{rotuloComponente(medidas.componente)}</td>
                      <td className="py-2 pr-3 text-right text-gray-700">{medidas.cadastradas}</td>
                      <td className="py-2 pr-3 text-right text-gray-700">{medidas.descartadas}</td>
                      <td className="py-2 text-right font-semibold text-gray-900">{formatarPercentual(medidas.taxaDePerda)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Secao>

          <section className="rounded-xl border border-rota-border bg-rota-surface2 p-5 text-xs leading-relaxed text-gray-600">
            <h2 className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-500">Como os números são calculados</h2>
            <ul className="mt-2 list-disc space-y-1 pl-4">
              <li><strong>Em estoque</strong> são as bolsas disponíveis e ainda na validade; é a amostra das distribuições e das medidas.</li>
              <li><strong>Desvio padrão (s)</strong> amostral, dividindo por n − 1. <strong>CV</strong> = s ÷ x̄ × 100.</li>
              <li><strong>Quartis</strong> por interpolação linear, o mesmo método do QUARTIL.INC do Excel, para conferir numa planilha.</li>
              <li><strong>Dias até vencer</strong>: a bolsa que vence hoje conta 0 dias e ainda vale.</li>
            </ul>
          </section>
        </div>
      )}
    </div>
  );
}
