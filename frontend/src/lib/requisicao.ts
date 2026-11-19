import type {
  AlaHospitalarBackend,
  AlaMedica,
  LoteHemocomponente,
  NivelUrgenciaBackend,
  RequisicaoRecebida,
  TipoComponente,
  TipoSanguineo,
  UrgenciaNecessidade,
} from "@/types";
import { FAIXAS_IDEAIS } from "@/data/estoqueMock";
import { diasAteVencer } from "@/lib/estoque";

export const ALAS_MEDICAS: AlaMedica[] =
[
  "UTI Adulto",
  "UTI Neonatal",
  "Centro Cirúrgico",
  "Emergência",
  "Oncologia",
  "Hemodiálise",
  "Maternidade",
];

export const COMPONENTES: TipoComponente[] =
[
  "Concentrado de Hemácias",
  "Plasma Fresco Congelado",
  "Concentrado de Plaquetas",
  "Crioprecipitado",
];

/** Validade de um lote recém-recebido, em dias, por hemocomponente (RDC 34/2014). */
const VALIDADE_DIAS: Record<TipoComponente, number> =
{
  "Concentrado de Hemácias": 35,
  "Plasma Fresco Congelado": 365,
  "Concentrado de Plaquetas": 5,
  "Crioprecipitado": 365,
};

/** Volume nominal da bolsa, em mL, por hemocomponente. */
const VOLUME_ML: Record<TipoComponente, number> =
{
  "Concentrado de Hemácias": 450,
  "Plasma Fresco Congelado": 300,
  "Concentrado de Plaquetas": 60,
  "Crioprecipitado": 25,
};

const PREFIXO_CODIGO: Record<TipoComponente, string> =
{
  "Concentrado de Hemácias": "CH",
  "Plasma Fresco Congelado": "PL",
  "Concentrado de Plaquetas": "PQ",
  "Crioprecipitado": "CR",
};

export const ALA_PARA_BACKEND: Record<AlaMedica, AlaHospitalarBackend> =
{
  "UTI Adulto": "UTI_ADULTO",
  "UTI Neonatal": "UTI_NEONATAL",
  "Centro Cirúrgico": "CENTRO_CIRURGICO",
  "Emergência": "EMERGENCIA",
  "Oncologia": "ONCOLOGIA",
  "Hemodiálise": "HEMODIALISE",
  "Maternidade": "MATERNIDADE",
};

export const URGENCIA_PARA_BACKEND: Record<UrgenciaNecessidade, NivelUrgenciaBackend> =
{
  critico: "ALTA",
  atencao: "MEDIA",
  estavel: "BAIXA",
};

/** Mesmo separador que o backend usa entre hospital, ala e leito (AlaHospitalar.SEPARADOR). */
const SEPARADOR_LOCALIZACAO = " · ";
const PREFIXO_TRANSPORTE = "Em transporte refrigerado → ";

/** Destino mostrado antes de enviar, no mesmo formato que o backend grava na bolsa. */
export function descreverDestino(hospital: string, ala: AlaMedica, leito: string)
{
  const partes = [hospital, ala];
  if (leito.trim()) partes.push(`Leito ${leito.trim()}`);
  return partes.join(SEPARADOR_LOCALIZACAO);
}

const NOMES_COMPARTIMENTO: Record<string, string> =
{
  R: "Refrigerador",
  F: "Freezer",
  C: "Câmara",
  G: "Gaveta",
  P: "Prateleira",
  N: "Nível",
};

/**
 * Explica o código de posição física da bolsa no hemocentro: "R1 · P3 · N2" vira
 * ["Refrigerador 1", "Prateleira 3", "Nível 2"]. Trecho que não for código fica como veio
 * ("Câmara 01", "Gaveta B").
 */
export function descreverLocalizacao(localizacao: string | null | undefined): string[]
{
  if (!localizacao?.trim()) return [];

  return localizacao
    .split(/\s*[·|,/]\s*/)
    .map((trecho) => trecho.trim())
    .filter(Boolean)
    .map((trecho) =>
    {
      const codigo = /^([A-Za-z])\s*-?\s*([0-9]+|[A-Za-z])$/.exec(trecho);
      const nome = codigo && NOMES_COMPARTIMENTO[codigo[1].toUpperCase()];
      return nome ? `${nome} ${codigo[2].toUpperCase()}` : trecho;
    });
}

export interface DestinoDaBolsa
{
  hospital: string;
  ala: AlaMedica;
  leito?: string;
  /** Ainda no caminho entre o hemocentro e o hospital. */
  emTransito: boolean;
}

/**
 * Lê a localização gravada por uma requisição (HU-04): "Hospital X · UTI Adulto · Leito 04",
 * com o prefixo de transporte enquanto a bolsa viaja. Localização de geladeira devolve null.
 */
export function destinoDaBolsa(localizacao: string | null | undefined): DestinoDaBolsa | null
{
  if (!localizacao) return null;

  const emTransito = localizacao.startsWith(PREFIXO_TRANSPORTE);
  const texto = emTransito ? localizacao.slice(PREFIXO_TRANSPORTE.length) : localizacao;
  const partes = texto.split(SEPARADOR_LOCALIZACAO);
  const indiceAla = partes.findIndex((parte, indice) => indice > 0 && ALAS_MEDICAS.includes(parte as AlaMedica));

  if (indiceAla < 1) return null;

  const leito = partes[indiceAla + 1]?.replace(/^Leito\s+/i, "");
  return {
    hospital: partes.slice(0, indiceAla).join(SEPARADOR_LOCALIZACAO),
    ala: partes[indiceAla] as AlaMedica,
    leito: leito || undefined,
    emTransito,
  };
}

/**
 * Contingência do FEFO quando o backend não responde: lotes do mesmo tipo e componente, do que
 * vence primeiro ao último, até cobrir as unidades pedidas.
 */
export function sugerirLotesFefo(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
  unidades: number,
): { lotes: LoteHemocomponente[]; disponiveis: number }
{
  const candidatos = lotes
    .filter(
      (lote) =>
        lote.componente === componente &&
        lote.tipoSanguineo === tipoSanguineo &&
        lote.unidades > 0 &&
        (lote.statusBolsa ?? "DISPONIVEL") === "DISPONIVEL" &&
        diasAteVencer(lote) >= 0,
    )
    .sort((a, b) => diasAteVencer(a) - diasAteVencer(b));

  const escolhidos: LoteHemocomponente[] = [];
  let acumulado = 0;
  for (const lote of candidatos)
  {
    if (acumulado >= unidades) break;
    escolhidos.push(lote);
    acumulado += lote.unidades;
  }

  return { lotes: escolhidos, disponiveis: Math.min(acumulado, unidades) };
}

/** Data ISO (AAAA-MM-DD) daqui a `dias`. */
function emDias(dias: number)
{
  const data = new Date();
  data.setHours(0, 0, 0, 0);
  data.setDate(data.getDate() + dias);
  return data.toISOString().slice(0, 10);
}

/**
 * Converte uma remessa conferida em lote de estoque (HU-05) — validade, volume
 * e temperatura assumem os valores nominais do hemocomponente.
 */
export function loteDaRequisicaoRecebida(requisicao: RequisicaoRecebida): LoteHemocomponente
{
  const faixa = FAIXAS_IDEAIS[requisicao.componente];
  const codigo = `${PREFIXO_CODIGO[requisicao.componente]}-${requisicao.id.replace(/\D/g, "")}`;

  return {
    id: `${codigo}-${requisicao.id}`,
    codigo,
    componente: requisicao.componente,
    tipoSanguineo: requisicao.tipoSanguineo,
    unidades: requisicao.unidades,
    volumeMl: VOLUME_ML[requisicao.componente],
    dataValidade: emDias(VALIDADE_DIAS[requisicao.componente]),
    temperaturaAtual: (faixa.minima + faixa.maxima) / 2,
    temperaturaIdeal: faixa,
    localizacao: "Recebimento · triagem",
  };
}

/** Protocolo curto exibido na confirmação (mock — o backend é quem numera). */
export function gerarProtocolo(prefixo: string)
{
  return `${prefixo}-${Math.floor(1000 + Math.random() * 9000)}`;
}
