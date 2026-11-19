import type { AvisoPainel, LoteHemocomponente, PessoaNecessitada, UrgenciaNecessidade } from "@/types";
import { TIPOS_SANGUINEOS, diasAteVencer, statusLote, temperaturaForaDaFaixa } from "@/lib/estoque";

/** Abaixo deste total de unidades o tipo sanguíneo entra em desabastecimento (HU-02). */
export const NIVEL_SEGURANCA = 10;

/** Janela de vencimento que exige priorização de liberação. */
export const JANELA_VENCIMENTO_DIAS = 7;

export interface IndicadorPainel
{
  id: string;
  rotulo: string;
  valor: number;
  descricao: string;
  status: UrgenciaNecessidade;
  destino: string;
}

function unidadesPorTipo(lotes: LoteHemocomponente[])
{
  return TIPOS_SANGUINEOS.map((tipo) => ({
    tipo,
    unidades: lotes
      .filter((lote) => lote.tipoSanguineo === tipo)
      .reduce((total, lote) => total + lote.unidades, 0),
  }));
}

function inicioDeHoje()
{
  const data = new Date();
  data.setHours(0, 0, 0, 0);
  return data;
}

/** Rotina automática de conferência de estoque — usa o último ciclo executado (6h de hoje ou 18h de ontem). */
function rotinaDeHoje()
{
  const agora = new Date();
  const hojeAsSeis = inicioDeHoje();
  hojeAsSeis.setHours(6, 0, 0, 0);

  if (agora >= hojeAsSeis)
  {
    return hojeAsSeis;
  }

  // Se agora for antes das 6h (madrugada), a última rotina de conferência ocorreu às 18h de ontem
  const ontemAsDezoito = new Date(hojeAsSeis);
  ontemAsDezoito.setDate(ontemAsDezoito.getDate() - 1);
  ontemAsDezoito.setHours(18, 0, 0, 0);
  return ontemAsDezoito;
}

function avisoDePaciente(pessoa: PessoaNecessitada): AvisoPainel
{
  return {
    id: `paciente-${pessoa.id}`,
    origem: "paciente",
    severidade: pessoa.status,
    titulo: `Paciente ${pessoa.nome} precisa de sangue ${pessoa.tipoSanguineo}`,
    detalhe: `${pessoa.causa} · ${pessoa.componente} · ${pessoa.hospital}`,
    registradoEm: new Date(pessoa.publicadaEm),
    destino: "/pacientes",
  };
}

function avisoDeDesabastecimento(tipo: string, unidades: number): AvisoPainel
{
  return {
    id: `estoque-${tipo}`,
    origem: "estoque",
    severidade: unidades < NIVEL_SEGURANCA / 2 ? "critico" : "atencao",
    titulo: `Estoque de sangue tipo ${tipo} próximo do desabastecimento`,
    detalhe: `Apenas ${unidades} ${unidades === 1 ? "unidade" : "unidades"} em estoque · nível de segurança: ${NIVEL_SEGURANCA}`,
    registradoEm: rotinaDeHoje(),
    destino: "/estoque",
  };
}

function avisoDeLote(lote: LoteHemocomponente): AvisoPainel
{
  const dias = diasAteVencer(lote);
  const foraDaFaixa = temperaturaForaDaFaixa(lote);

  let titulo: string;
  let detalheValidade: string;

  if (dias < 0)
  {
    const vencidoHa = Math.abs(dias);
    titulo = `Hemocomponente ${lote.codigo} (${lote.componente}) vencido`;
    detalheValidade = `vencido há ${vencidoHa} ${vencidoHa === 1 ? "dia" : "dias"}`;
  }
  else if (dias === 0)
  {
    titulo = `Hemocomponente ${lote.codigo} (${lote.componente}) vence hoje`;
    detalheValidade = "vence hoje";
  }
  else if (foraDaFaixa)
  {
    titulo = `Hemocomponente ${lote.codigo} (${lote.componente}) fora da faixa de temperatura`;
    detalheValidade = `vence em ${dias} ${dias === 1 ? "dia" : "dias"}`;
  }
  else
  {
    titulo = `Hemocomponente ${lote.codigo} (${lote.componente}) próximo do vencimento`;
    detalheValidade = `vence em ${dias} ${dias === 1 ? "dia" : "dias"}`;
  }

  return {
    id: `validade-${lote.id}`,
    origem: "validade",
    severidade: statusLote(lote),
    titulo,
    detalhe: `${lote.tipoSanguineo} · ${detalheValidade} · local ${lote.localizacao}`,
    registradoEm: rotinaDeHoje(),
    destino: "/estoque",
  };
}

/**
 * Avisos do painel derivados dos mesmos mocks das telas de Estoque e Pacientes,
 * para que os números do painel e das telas de detalhe nunca divirjam.
 */
export function montarAvisos(
  lotesEstoque: LoteHemocomponente[],
  pessoasNecessitadas: PessoaNecessitada[],
): AvisoPainel[]
{
  const pacientes = pessoasNecessitadas
    .filter((pessoa) => pessoa.status !== "estavel")
    .map(avisoDePaciente);

  const desabastecidos = unidadesPorTipo(lotesEstoque)
    .filter((grupo) => grupo.unidades > 0 && grupo.unidades < NIVEL_SEGURANCA)
    .map((grupo) => avisoDeDesabastecimento(grupo.tipo, grupo.unidades));

  const lotes = lotesEstoque
    .filter((lote) => diasAteVencer(lote) <= JANELA_VENCIMENTO_DIAS || temperaturaForaDaFaixa(lote))
    .map(avisoDeLote);

  return [...pacientes, ...desabastecidos, ...lotes];
}

export function montarIndicadores(
  lotesEstoque: LoteHemocomponente[],
  avisos: AvisoPainel[],
): IndicadorPainel[]
{
  const unidades = lotesEstoque.reduce((total, lote) => total + lote.unidades, 0);
  const componentes = new Set(lotesEstoque.map((lote) => lote.componente)).size;
  const tiposEmRisco = unidadesPorTipo(lotesEstoque).filter(
    (grupo) => grupo.unidades < NIVEL_SEGURANCA,
  ).length;
  const vencendo = lotesEstoque.filter((lote) => {
    const d = diasAteVencer(lote);
    return d >= 0 && d <= JANELA_VENCIMENTO_DIAS;
  }).length;
  const criticos = avisos.filter((aviso) => aviso.severidade === "critico").length;

  return [
    {
      id: "unidades",
      rotulo: "Unidades em estoque",
      valor: unidades,
      descricao: `${componentes} tipos de hemocomponente`,
      status: "estavel",
      destino: "/estoque",
    },
    {
      id: "risco",
      rotulo: "Tipos em risco",
      valor: tiposEmRisco,
      descricao: "abaixo do nível de segurança",
      status: tiposEmRisco === 0 ? "estavel" : "critico",
      destino: "/estoque",
    },
    {
      id: "vencimento",
      rotulo: `Vencendo em ${JANELA_VENCIMENTO_DIAS} dias`,
      valor: vencendo,
      descricao: "priorizar liberação",
      status: vencendo === 0 ? "estavel" : "atencao",
      destino: "/estoque",
    },
    {
      id: "criticos",
      rotulo: "Alertas críticos",
      valor: criticos,
      descricao: "requerem ação imediata",
      status: criticos === 0 ? "estavel" : "critico",
      destino: "/pacientes",
    },
  ];
}

const PESO_SEVERIDADE: Record<UrgenciaNecessidade, number> = { critico: 2, atencao: 1, estavel: 0 };

export type OrdenacaoAvisos = "severidade" | "tempo";

/** Ordena a caixa de avisos; em empate de severidade o mais recente vem antes. */
export function ordenarAvisos(avisos: AvisoPainel[], ordenacao: OrdenacaoAvisos)
{
  return [...avisos].sort((a, b) =>
  {
    if (ordenacao === "severidade")
    {
      const diferenca = PESO_SEVERIDADE[b.severidade] - PESO_SEVERIDADE[a.severidade];
      if (diferenca !== 0) return diferenca;
    }
    return b.registradoEm.getTime() - a.registradoEm.getTime();
  });
}

/** "agora mesmo", "há 4 min", "há 3 h", "ontem", ou "há 2 dias". */
export function formatarTempoRelativo(data: Date)
{
  const agora = Date.now();
  const diferencaMs = agora - data.getTime();
  if (diferencaMs < 0) return "agora mesmo";

  const minutos = Math.floor(diferencaMs / 60_000);
  if (minutos < 1) return "agora mesmo";
  if (minutos < 60) return `há ${minutos} min`;

  const horas = Math.floor(minutos / 60);
  if (horas < 24 && data >= inicioDeHoje())
  {
    return `há ${horas} ${horas === 1 ? "h" : "h"}`;
  }

  // Se o registro ocorreu ontem
  const ontem = inicioDeHoje();
  ontem.setDate(ontem.getDate() - 1);
  if (data >= ontem && data < inicioDeHoje())
  {
    return "ontem";
  }

  const dias = Math.floor(minutos / 1_440);
  const diasCorrigidos = Math.max(1, dias);
  return `há ${diasCorrigidos} ${diasCorrigidos === 1 ? "dia" : "dias"}`;
}
