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
  nome: string;
  tipoAcesso: TipoAcessoBackend;
  painelPrincipal?: string;
  telaInicial?: string;
  modulosDisponiveis?: string[];
  menu?: string[];
}

export interface LoginPayloadDTO
{
  nome: string;
  tipoAcesso: TipoAcessoBackend;
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
