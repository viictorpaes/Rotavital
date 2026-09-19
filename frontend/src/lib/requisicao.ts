import type {
  AlaMedica,
  LoteHemocomponente,
  PessoaNecessitada,
  RequisicaoRecebida,
  TipoComponente,
  TipoSanguineo,
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

/**
 * FEFO (*first expired, first out*): entre os lotes compatíveis com o pedido,
 * sugere o que vence primeiro, para reduzir descarte por validade (HU-04).
 */
export function sugerirLoteFefo(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
): LoteHemocomponente | undefined
{
  return lotes
    .filter(
      (lote) =>
        lote.componente === componente &&
        lote.tipoSanguineo === tipoSanguineo &&
        lote.unidades > 0 &&
        diasAteVencer(lote) >= 0,
    )
    .sort((a, b) => diasAteVencer(a) - diasAteVencer(b))[0];
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

export interface DadosCampanha
{
  nome: string;
  sexo: PessoaNecessitada["sexo"];
  idade: number;
  tipoSanguineo: TipoSanguineo;
  componente: TipoComponente;
  causa: string;
  gravidade: PessoaNecessitada["status"];
}

/** Paciente publicado por uma campanha de doação (HU-06). */
export function pacienteDaCampanha(dados: DadosCampanha, id: string): PessoaNecessitada
{
  return {
    id,
    nome: dados.nome.trim(),
    sexo: dados.sexo,
    idade: dados.idade,
    tipoSanguineo: dados.tipoSanguineo,
    componente: dados.componente,
    distanciaKm: 0,
    status: dados.gravidade,
    causa: dados.causa.trim(),
  };
}

/** Protocolo curto exibido na confirmação (mock — o backend é quem numera). */
export function gerarProtocolo(prefixo: string)
{
  return `${prefixo}-${Math.floor(1000 + Math.random() * 9000)}`;
}
