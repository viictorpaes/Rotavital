// API Client centralizado para comunicação do Frontend com o Backend Spring Boot
import { clienteHttp, ErroHttp } from "./clienteHttp";
export { clienteHttp, ErroHttp };

export interface RespostaAcesso
{
  nome: string;
  tipoAcesso: "MEDICO" | "DOADOR" | "ADMIN";
  painelPrincipal: string;
  modulosDisponiveis: string[];
}

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

export interface BolsaHemocomponenteDTO
{
  id: string;
  tipoComponente: string;
  tipoSanguineo: string;
  dataValidade: string;
  volumeMl: number;
  status: string;
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

export interface PontoRedeDTO
{
  id: string;
  nome: string;
  tipo: "BANCO_DE_SANGUE" | "HOSPITAL";
  latitude: number;
  longitude: number;
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

// ----------------------------------------------------------------------
// Funções de Chamada à API
// ----------------------------------------------------------------------

/**
 * Autentica o usuário e obtém os módulos permitidos no back-end.
 */
export async function autenticarUsuario(nome: string, papel: "medico" | "doador" | "admin"): Promise<RespostaAcesso>
{
  let tipoAcessoBackend: "MEDICO" | "DOADOR" | "ADMIN" = "MEDICO";
  if (papel === "doador") tipoAcessoBackend = "DOADOR";
  if (papel === "admin") tipoAcessoBackend = "ADMIN";

  return clienteHttp.post<RespostaAcesso>("/acessos", {
    nome,
    tipoAcesso: tipoAcessoBackend,
  });
}

/**
 * Consulta as bolsas de hemocomponentes em estoque de um banco de sangue no Supabase.
 */
export async function buscarEstoque(bancoId: string = "BS-01", tipoSanguineo?: string): Promise<EstoqueDTO>
{
  return clienteHttp.get<EstoqueDTO>(`/bancos/${bancoId}/estoque`, {
    params: { tipoSanguineo },
  });
}

/**
 * Lista todos os pontos cadastrados na rede hospitalar (bancos de sangue e hospitais).
 */
export async function listarPontosRede(): Promise<PontoRedeDTO[]>
{
  return clienteHttp.get<PontoRedeDTO[]>("/pontos");
}

/**
 * Calcula a rota ótima entre dois pontos da rede com previsão de chegada.
 */
export async function calcularRota(origemId: string, destinoId: string, partida?: string): Promise<RotaCalculadaDTO>
{
  return clienteHttp.get<RotaCalculadaDTO>("/rotas", {
    params: { origemId, destinoId, partida },
  });
}

/**
 * Obtém diagnóstico em tempo real do backend, banco Supabase e logs HTTP.
 */
export async function obterDiagnostico(): Promise<DiagnosticoDados>
{
  return clienteHttp.get<DiagnosticoDados>("/diagnostico", { silencioso: true });
}

