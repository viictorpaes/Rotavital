import type { PessoaNecessitada, Sexo, TipoComponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import type {
  AgendamentoDTO,
  CampanhaDTO,
  GravidadeBackend,
  SexoBackend,
  StatusAgendamentoBackend,
  TipoComponenteBackend,
  TipoSanguineoBackend,
} from "@/types/dtos";
import { converterTipoComponente, converterTipoSanguineo } from "@/lib/estoque";
import { ErroHttp } from "@/services/clienteHttp";

/** Conversões entre os valores do backend (campanhas e agendamentos) e os da tela. */

const TIPO_SANGUINEO_BACKEND: Record<TipoSanguineo, TipoSanguineoBackend> =
{
  "A+": "A_POSITIVO",
  "A-": "A_NEGATIVO",
  "B+": "B_POSITIVO",
  "B-": "B_NEGATIVO",
  "AB+": "AB_POSITIVO",
  "AB-": "AB_NEGATIVO",
  "O+": "O_POSITIVO",
  "O-": "O_NEGATIVO",
};

const COMPONENTE_BACKEND: Record<TipoComponente, TipoComponenteBackend> =
{
  "Concentrado de Hemácias": "HEMACIAS",
  "Plasma Fresco Congelado": "PLASMA",
  "Concentrado de Plaquetas": "PLAQUETAS",
  "Crioprecipitado": "CRIOPRECIPITADO",
};

const GRAVIDADE: Record<GravidadeBackend, UrgenciaNecessidade> =
{
  CRITICO: "critico",
  ATENCAO: "atencao",
  ESTAVEL: "estavel",
};

const SEXO: Record<SexoBackend, Sexo> = { FEMININO: "Feminino", MASCULINO: "Masculino" };

export const ROTULO_STATUS_AGENDAMENTO: Record<StatusAgendamentoBackend, string> =
{
  AGENDADO: "Agendado",
  REALIZADO: "Doação feita",
  CANCELADO: "Cancelado",
  FALTOU: "Faltou",
};

export function tipoSanguineoParaBackend(tipo: TipoSanguineo): TipoSanguineoBackend
{
  return TIPO_SANGUINEO_BACKEND[tipo];
}

export function componenteParaBackend(componente: TipoComponente): TipoComponenteBackend
{
  return COMPONENTE_BACKEND[componente];
}

export function gravidadeDoBackend(gravidade: GravidadeBackend): UrgenciaNecessidade
{
  return GRAVIDADE[gravidade];
}

export function gravidadeParaBackend(gravidade: UrgenciaNecessidade): GravidadeBackend
{
  return gravidade.toUpperCase() as GravidadeBackend;
}

export function sexoDoBackend(sexo: SexoBackend): Sexo
{
  return SEXO[sexo];
}

export function sexoParaBackend(sexo: Sexo): SexoBackend
{
  return sexo === "Feminino" ? "FEMININO" : "MASCULINO";
}

/** Paciente divulgado pela campanha, no formato dos cartões de Doações, Pacientes e do portal. */
export function pessoaDaCampanha(campanha: CampanhaDTO): PessoaNecessitada
{
  const { paciente } = campanha;
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
    campanhaId: campanha.id,
    publicadaEm: campanha.publicadaEm,
  };
}

/** "qua., 04/11 às 09:30", no fuso do hemocentro. */
export function formatarHorario(iso: string)
{
  const data = new Date(iso);
  const dia = data.toLocaleDateString("pt-BR", {
    weekday: "short",
    day: "2-digit",
    month: "2-digit",
    timeZone: "America/Recife",
  });
  const hora = data.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit", timeZone: "America/Recife" });
  return `${dia} às ${hora}`;
}

/** Data (AAAA-MM-DD) e hora (HH:MM) de Recife para o formato que o backend aceita. */
export function horarioParaBackend(data: string, hora: string)
{
  return `${data}T${hora}:00-03:00`;
}

/** Separa um horário do backend em data e hora de Recife, para preencher o formulário de remarcação. */
export function horarioDoBackend(iso: string): { data: string; hora: string }
{
  const partes = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Recife",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  }).formatToParts(new Date(iso));
  const valor = (tipo: string) => partes.find((parte) => parte.type === tipo)?.value ?? "";
  return { data: `${valor("year")}-${valor("month")}-${valor("day")}`, hora: `${valor("hour")}:${valor("minute")}` };
}

/** Horários do hemocentro (segunda a sábado, 07h às 19h), de meia em meia hora. */
export const HORARIOS_ATENDIMENTO: string[] = Array.from({ length: 25 }, (_, i) =>
{
  const minutos = 7 * 60 + i * 30;
  return `${String(Math.floor(minutos / 60)).padStart(2, "0")}:${String(minutos % 60).padStart(2, "0")}`;
});

/** Amanhã, no formato do input de data — o primeiro dia que o formulário oferece. */
export function amanha()
{
  const data = new Date();
  data.setDate(data.getDate() + 1);
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}-${String(data.getDate()).padStart(2, "0")}`;
}

export function ehDomingo(data: string)
{
  return new Date(`${data}T12:00:00`).getDay() === 0;
}

/** O agendamento em aberto do doador, se houver (o backend aceita só um por vez). */
export function agendamentoEmAberto(agendamentos: AgendamentoDTO[])
{
  return agendamentos.find((agendamento) => agendamento.status === "AGENDADO");
}

/** Mensagem do backend (campo detail) para mostrar perto da ação que falhou. */
export function mensagemDeErro(erro: unknown)
{
  if (erro instanceof ErroHttp)
  {
    return erro.detalhe || erro.titulo;
  }
  return "Não foi possível falar com o servidor. Tente de novo em instantes.";
}
