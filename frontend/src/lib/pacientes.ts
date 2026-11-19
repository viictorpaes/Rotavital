import type {
  ConsumoDeLote,
  LoteHemocomponente,
  PacienteNaFila,
  Sexo,
  TipoComponente,
  TipoSanguineo,
  UrgenciaNecessidade,
} from "@/types";
import type { PacienteDTO, PacientePayloadDTO } from "@/types/dtos";
import { converterTipoComponente, converterTipoSanguineo, diasAteVencer } from "@/lib/estoque";
import {
  componenteParaBackend,
  gravidadeDoBackend,
  gravidadeParaBackend,
  sexoDoBackend,
  sexoParaBackend,
  tipoSanguineoParaBackend,
} from "@/lib/doacoes";

/** Resultado de uma baixa FEFO no estoque (HU-08). */
export interface ResultadoBaixa
{
  /** Estoque já com as unidades descontadas — lotes zerados são removidos. */
  lotes: LoteHemocomponente[];
  consumos: ConsumoDeLote[];
  unidadesBaixadas: number;
  unidadesFaltantes: number;
}

/** Lotes compatíveis com o pedido, do que vence primeiro ao que vence por último. */
export function lotesCompativeis(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
)

{
  return lotes
    .filter(
      (lote) =>
        lote.componente === componente &&
        lote.tipoSanguineo === tipoSanguineo &&
        lote.unidades > 0 &&
        diasAteVencer(lote) >= 0,
    )
    .sort((a, b) => diasAteVencer(a) - diasAteVencer(b));
}

/** Unidades disponíveis para o par componente + tipo sanguíneo. */
export function unidadesDisponiveis
(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
)

{
  return lotesCompativeis(lotes, componente, tipoSanguineo).reduce(
    (total, lote) => total + lote.unidades,
    0,
  );
}

/**
 * Reserva `unidades` seguindo FEFO (*first expired, first out*): consome os
 * lotes mais próximos do vencimento primeiro, para reduzir descarte por
 * validade. Quando o estoque não cobre o pedido, baixa o que há e devolve o
 * saldo em `unidadesFaltantes` — quem chama decide o que fazer com a diferença.
 */
export function reservarPorFefo
(
  lotes: LoteHemocomponente[],
  componente: TipoComponente,
  tipoSanguineo: TipoSanguineo,
  unidades: number,
): ResultadoBaixa

{
  const fila = lotesCompativeis(lotes, componente, tipoSanguineo);
  const consumos: ConsumoDeLote[] = [];
  const restantes = new Map<string, number>();
  let pendente = Math.max(0, unidades);

  for (const lote of fila)
  {
    if (pendente === 0) 
    {
      break;
    }

    const retirada = Math.min(lote.unidades, pendente);
    consumos.push({ codigo: lote.codigo, unidades: retirada });
    restantes.set(lote.id, lote.unidades - retirada);
    pendente -= retirada;
  }

  const atualizados = lotes
    .map((lote) => (restantes.has(lote.id) ? { ...lote, unidades: restantes.get(lote.id)! } : lote))
    .filter((lote) => lote.unidades > 0);

  return {
    lotes: atualizados,
    consumos,
    unidadesBaixadas: Math.max(0, unidades) - pendente,
    unidadesFaltantes: pendente,
  };
}

const PESO_STATUS: Record<UrgenciaNecessidade, number> = { critico: 0, atencao: 1, estavel: 2 };

/** Ordena a fila clínica: crítico antes de atenção, atenção antes de estável. */
export function ordenarPorUrgencia(pacientes: PacienteNaFila[])
{
  return [...pacientes].sort((a, b) => PESO_STATUS[a.status] - PESO_STATUS[b.status]);
}

/** Quantos pacientes há em cada nível de urgência. */
export function contarPorUrgencia(pacientes: PacienteNaFila[]): Record<UrgenciaNecessidade, number>
{
  return pacientes.reduce(
    (total, paciente) => ({ ...total, [paciente.status]: total[paciente.status] + 1 }),
    { critico: 0, atencao: 0, estavel: 0 },
  );
}
// ----------------------------------------------------------------------
// Cadastro de pacientes (PI3-195): o rascunho do formulário e as conversões com a API.
// ----------------------------------------------------------------------

/** O que o formulário de paciente edita, no formato da tela. Unidades fica em texto, como no input. */
export interface RascunhoPaciente
{
  hospitalId: string;
  nome: string;
  /** AAAA-MM-DD */
  dataNascimento: string;
  sexo: Sexo;
  tipoSanguineo: TipoSanguineo;
  componente: TipoComponente;
  unidades: string;
  causa: string;
  gravidade: UrgenciaNecessidade;
}

/** Formulário em branco (cadastro novo). O hospital é escolhido quando a lista de hospitais chega. */
export function rascunhoVazio(): RascunhoPaciente
{
  return {
    hospitalId: "",
    nome: "",
    dataNascimento: "",
    sexo: "Feminino",
    tipoSanguineo: "O-",
    componente: "Concentrado de Hemácias",
    unidades: "1",
    causa: "",
    gravidade: "atencao",
  };
}

/** Formulário preenchido com o paciente que a API devolveu (edição). */
export function rascunhoDoPaciente(paciente: PacienteDTO): RascunhoPaciente
{
  return {
    hospitalId: paciente.hospitalId,
    nome: paciente.nome,
    dataNascimento: paciente.dataNascimento,
    sexo: sexoDoBackend(paciente.sexo),
    tipoSanguineo: converterTipoSanguineo(paciente.tipoSanguineo),
    componente: converterTipoComponente(paciente.tipoComponente),
    unidades: String(paciente.unidadesNecessarias),
    causa: paciente.causa,
    gravidade: gravidadeDoBackend(paciente.gravidade),
  };
}

/** Corpo do POST/PUT de /pacientes e de /campanhas. A validação fica na API. */
export function payloadDoRascunho(rascunho: RascunhoPaciente): PacientePayloadDTO
{
  return {
    hospitalId: rascunho.hospitalId,
    nome: rascunho.nome,
    sexo: sexoParaBackend(rascunho.sexo),
    dataNascimento: rascunho.dataNascimento,
    tipoSanguineo: tipoSanguineoParaBackend(rascunho.tipoSanguineo),
    tipoComponente: componenteParaBackend(rascunho.componente),
    unidadesNecessarias: Number(rascunho.unidades),
    causa: rascunho.causa,
    gravidade: gravidadeParaBackend(rascunho.gravidade),
  };
}

/** Paciente da API no formato dos cartões da tela de Pacientes. */
export function pessoaDoPaciente(paciente: PacienteDTO): PacienteNaFila
{
  return {
    id: paciente.id,
    nome: paciente.nome,
    sexo: sexoDoBackend(paciente.sexo),
    idade: paciente.idade,
    tipoSanguineo: converterTipoSanguineo(paciente.tipoSanguineo),
    componente: converterTipoComponente(paciente.tipoComponente),
    unidadesNecessarias: paciente.unidadesNecessarias,
    hospital: paciente.hospitalNome,
    status: gravidadeDoBackend(paciente.gravidade),
    causa: paciente.causa,
  };
}
