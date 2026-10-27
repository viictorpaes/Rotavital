/**
 * Tipagens TypeScript para DTOs e Respostas da API Spring Boot.
 *
 * Mapeamento 1-para-1 com os Records, Enums e DTOs do backend:
 * - com.rotavital.dominio.*
 * - com.rotavital.api.dto.*
 */

// ======================================================================
// ENUMS DO BACKEND
// ======================================================================

export type TipoSanguineoBackend =
  | "A_POSITIVO"
  | "A_NEGATIVO"
  | "B_POSITIVO"
  | "B_NEGATIVO"
  | "AB_POSITIVO"
  | "AB_NEGATIVO"
  | "O_POSITIVO"
  | "O_NEGATIVO";

export type TipoComponenteBackend =
  | "HEMACIAS"
  | "PLASMA"
  | "PLAQUETAS"
  | "CRIOPRECIPITADO";

export type StatusBolsaBackend =
  | "DISPONIVEL"
  | "RESERVADA"
  | "EM_TRANSITO"
  | "ENTREGUE"
  | "DESCARTADA"
  | "UTILIZADA";

export type StatusRequisicaoBackend =
  | "PENDENTE"
  | "ALOCADA"
  | "EM_TRANSITO"
  | "ENTREGUE"
  | "CANCELADA";

export type NivelUrgenciaBackend = "BAIXA" | "MEDIA" | "ALTA";

export type TipoAcessoBackend = "MEDICO" | "DOADOR" | "ADMIN";

export type TipoPontoRedeBackend = "BANCO_DE_SANGUE" | "HOSPITAL";

// ======================================================================
// DTOs - ESTOQUE
// ======================================================================

export interface BolsaHemocomponenteDTO
{
  id: string;
  tipoComponente: TipoComponenteBackend | string;
  tipoSanguineo: TipoSanguineoBackend | string;
  dataValidade: string;
  volumeMl: number;
  status: StatusBolsaBackend | string;
  dataColeta?: string;
  loteSintetico?: string;
  temperaturaCelsius?: number;
  temperaturaAtual?: number;
  temperaturaIdealMinima?: number;
  temperaturaIdealMaxima?: number;
  foraDaFaixa?: boolean;
  foraDaFaixaIdeal?: boolean;
  localizacao?: string;
  localizacaoFisica?: string;
  bancoOrigemId?: string;
}

/** Filtros opcionais e combináveis de GET /hemocomponentes; page começa em 0 e size vai de 1 a 100. */
export interface FiltrosHemocomponentesDTO
{
  bancoId?: string;
  tipoComponente?: TipoComponenteBackend;
  tipoSanguineo?: TipoSanguineoBackend;
  status?: StatusBolsaBackend;
  page?: number;
  size?: number;
}

/**
 * Corpo de POST /hemocomponentes (PI3-179). Os campos que o formulário pode deixar em branco aceitam
 * null: quem valida é a API, que devolve todos os problemas de uma vez (400 ou 422).
 */
export interface NovaBolsaHemocomponenteDTO
{
  tipoComponente: TipoComponenteBackend;
  tipoSanguineo: TipoSanguineoBackend;
  dataColeta: string | null;
  dataValidade: string | null;
  loteSintetico: string;
  volumeMl: number | null;
  temperaturaCelsius: number | null;
  /** Opcional: refrigerador/prateleira/nível. */
  localizacao?: string;
  bancoOrigemId: string;
}

export interface EstoqueDTO
{
  bancoDeSangueId: string;
  totalBolsas: number;
  bolsas: BolsaHemocomponenteDTO[];
}

// ======================================================================
// DTOs - REQUISIÇÕES & ALOCAÇÃO
// ======================================================================

export interface RequisicaoHospitalarDTO
{
  id: string;
  hospitalId: string;
  tipoComponente: TipoComponenteBackend;
  tipoSanguineo: TipoSanguineoBackend;
  quantidade: number;
  urgencia: NivelUrgenciaBackend;
  dataSolicitacao: string;
  status: StatusRequisicaoBackend;
}

export interface CriarRequisicaoPayloadDTO
{
  hospitalId: string;
  tipoComponente: TipoComponenteBackend;
  tipoSanguineo: TipoSanguineoBackend;
  quantidade: number;
  urgencia: NivelUrgenciaBackend;
}

export interface AlocacaoDTO
{
  requisicaoId: string;
  bolsaAlocadaId: string;
  dataAlocacao: string;
  status: StatusRequisicaoBackend;
}

// ======================================================================
// DTOs - ROTAS & REDE HOSPITALAR
// ======================================================================

export interface PontoRedeDTO
{
  id: string;
  nome: string;
  tipo: TipoPontoRedeBackend;
  latitude: number;
  longitude: number;
}

export interface ConexaoDTO
{
  origemId: string;
  destinoId: string;
  distanciaKm: number;
  tempoEstimadoMin: number;
}

export interface RotaCalculadaDTO
{
  origemId: string;
  origemNome: string;
  destinoId: string;
  destinoNome: string;
  distanciaKmTotal: number;
  duracaoMinutosTotal: number;
  partidaEstimada: string;
  chegadaEstimada: string;
  atendeJanelaTempo: boolean;
  caminho: PontoRedeDTO[];
}

// ======================================================================
// DTOs - TELEMETRIA & MONITORAMENTO
// ======================================================================

export interface LeituraTelemetriaDTO
{
  entregaId: string;
  timestamp: string;
  latitude: number;
  longitude: number;
  temperaturaCelsius: number;
}

export interface MonitoramentoEntregaDTO
{
  entregaId: string;
  rotaId?: string;
  status: string;
  ultimaLeitura?: LeituraTelemetriaDTO;
  historicoLeituras?: LeituraTelemetriaDTO[];
}

// ======================================================================
// DTOs - AUTENTICAÇÃO & ACESSO
// ======================================================================

export interface RespostaAcesso
{
  id: string;
  nome: string;
  tipoAcesso: TipoAcessoBackend;
  /** Rota do frontend: /painel, /portal-doador ou /admin. */
  telaInicial: string;
  menu: string[];
  /** Token do Supabase Auth; a API ainda não o exige nos outros endpoints. */
  accessToken: string;
  expiraEmSegundos: number;
}

export interface LoginPayloadDTO
{
  email: string;
  senha: string;
}

// ======================================================================
// DTOs - CAMPANHAS & AGENDAMENTOS DE DOAÇÃO
// ======================================================================

export type SexoBackend = "FEMININO" | "MASCULINO";

export type GravidadeBackend = "CRITICO" | "ATENCAO" | "ESTAVEL";

export type StatusPacienteBackend = "AGUARDANDO" | "ATENDIDO" | "CANCELADO";

export type StatusCampanhaBackend = "ATIVA" | "ENCERRADA";

export type StatusAgendamentoBackend = "AGENDADO" | "REALIZADO" | "CANCELADO" | "FALTOU";

export interface PacienteDTO
{
  id: string;
  nome: string;
  sexo: SexoBackend;
  /** AAAA-MM-DD */
  dataNascimento: string;
  idade: number;
  tipoSanguineo: TipoSanguineoBackend;
  tipoComponente: TipoComponenteBackend;
  unidadesNecessarias: number;
  causa: string;
  gravidade: GravidadeBackend;
  status: StatusPacienteBackend;
  hospitalId: string;
  hospitalNome: string;
}

export interface CampanhaDTO
{
  id: string;
  status: StatusCampanhaBackend;
  publicadaEm: string;
  encerradaEm: string | null;
  publicadaPorNome: string | null;
  agendamentosEmAberto: number;
  paciente: PacienteDTO;
}

/** Corpo do POST e do PUT de /campanhas. */
export interface CampanhaPayloadDTO
{
  hospitalId: string;
  nome: string;
  sexo: SexoBackend;
  dataNascimento: string;
  tipoSanguineo: TipoSanguineoBackend;
  tipoComponente: TipoComponenteBackend;
  unidadesNecessarias: number;
  causa: string;
  gravidade: GravidadeBackend;
}

export interface AgendamentoDTO
{
  id: string;
  campanhaId: string | null;
  pacienteNome: string | null;
  doadorId: string;
  doadorNome: string;
  doadorTipoSanguineo: TipoSanguineoBackend | null;
  bancoId: string;
  bancoNome: string;
  /** Data e hora com fuso (ISO 8601). */
  agendadoPara: string;
  status: StatusAgendamentoBackend;
  criadoEm: string;
}

// ======================================================================
// DTOs - DIAGNÓSTICO & AUDITORIA HTTP
// ======================================================================

export interface LogHttpItemDTO
{
  id: string;
  horario: string;
  metodo: string;
  rota: string;
  status: number;
  duracaoMs: number;
  statusTag: string;
}

export interface BackendInfo
{
  status: string;
  uptimeSegundos: number;
  versaoJava: string;
  versaoSpringBoot: string;
  dataHora: string;
}

export interface BancoInfo
{
  status: string;
  latenciaMs: number;
  produto?: string;
  catalogo?: string;
  schema?: string;
  urlMascarada?: string;
  totalPontosRede: number;
  totalBolsasEstoque: number;
  totalConexoes: number;
  estadoTabelas: string;
  mensagemErro?: string;
}

export interface DiagnosticoDados
{
  backend: BackendInfo;
  bancoDeDados: BancoInfo;
  logsRecentes: LogHttpItemDTO[];
}

// ======================================================================
// DTOs - ERROS PADRONIZADOS (RFC 7807 ProblemDetails)
// ======================================================================

export interface ErroDTO
{
  type?: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
}
