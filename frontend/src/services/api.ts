// API Client centralizado para comunicação do Frontend com o Backend Spring Boot

const API_BASE_URL = "/api/v1";

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

  const resposta = await fetch(`${API_BASE_URL}/acessos`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      nome,
      tipoAcesso: tipoAcessoBackend,
    }),
  });

  if (!resposta.ok)
  {
    const erro = await resposta.json().catch(() => ({}));
    throw new Error(erro.detail || erro.title || "Erro ao autenticar usuário.");
  }

  return resposta.json();
}

/**
 * Consulta as bolsas de hemocomponentes em estoque de um banco de sangue no Supabase.
 */
export async function buscarEstoque(bancoId: string = "BS-01", tipoSanguineo?: string): Promise<EstoqueDTO>
{
  const params = new URLSearchParams();
  if (tipoSanguineo)
  {
    params.set("tipoSanguineo", tipoSanguineo);
  }

  const query = params.toString() ? `?${params.toString()}` : "";
  const resposta = await fetch(`${API_BASE_URL}/bancos/${bancoId}/estoque${query}`);

  if (!resposta.ok)
  {
    const erro = await resposta.json().catch(() => ({}));
    throw new Error(erro.detail || erro.title || `Erro ao buscar estoque do banco ${bancoId}`);
  }

  return resposta.json();
}

/**
 * Lista todos os pontos cadastrados na rede hospitalar (bancos de sangue e hospitais).
 */
export async function listarPontosRede(): Promise<PontoRedeDTO[]>
{
  const resposta = await fetch(`${API_BASE_URL}/pontos`);

  if (!resposta.ok)
  {
    throw new Error("Erro ao carregar os pontos da rede de distribuição.");
  }

  return resposta.json();
}

/**
 * Calcula a rota ótima entre dois pontos da rede com previsão de chegada.
 */
export async function calcularRota(origemId: string, destinoId: string, partida?: string): Promise<RotaCalculadaDTO>
{
  const params = new URLSearchParams({ origemId, destinoId });
  if (partida)
  {
    params.set("partida", partida);
  }

  const resposta = await fetch(`${API_BASE_URL}/rotas?${params.toString()}`);

  if (!resposta.ok)
  {
    const erro = await resposta.json().catch(() => ({}));
    throw new Error(erro.detail || erro.title || "Erro ao calcular rota entre os pontos informados.");
  }

  return resposta.json();
}

/**
 * Obtém diagnóstico em tempo real do backend, banco Supabase e logs HTTP.
 */
export async function obterDiagnostico(): Promise<DiagnosticoDados>
{
  const resposta = await fetch(`${API_BASE_URL}/diagnostico`);

  if (!resposta.ok)
  {
    throw new Error(`Falha ao obter diagnóstico do backend (HTTP ${resposta.status}).`);
  }

  return resposta.json();
}

