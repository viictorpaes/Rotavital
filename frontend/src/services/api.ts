// API Client centralizado para comunicação do Frontend com o Backend Spring Boot
import { clienteHttp, ErroHttp } from "./clienteHttp";
export { clienteHttp, ErroHttp };

export * from "@/types/dtos";
import type {
  LoginPayloadDTO,
  RespostaAcesso,
  EstoqueDTO,
  PontoRedeDTO,
  RotaCalculadaDTO,
  DiagnosticoDados,
} from "@/types/dtos";

// ----------------------------------------------------------------------
// Funções de Chamada à API
// ----------------------------------------------------------------------

/**
 * Login com e-mail e senha (Supabase Auth, conferido no backend). Devolve o perfil e a tela inicial.
 * Silencioso: a tela de login mostra o aviso no próprio formulário, sem o toast global.
 */
export async function autenticarUsuario(email: string, senha: string): Promise<RespostaAcesso>
{
  const payload: LoginPayloadDTO = { email, senha };
  return clienteHttp.post<RespostaAcesso>("/acessos", payload, { silencioso: true });
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

