// API Client centralizado para comunicação do Frontend com o Backend Spring Boot
import { clienteHttp, ErroHttp } from "./clienteHttp";
export { clienteHttp, ErroHttp };

export * from "@/types/dtos";
import type {
  AgendamentoDTO,
  CampanhaDTO,
  CampanhaPayloadDTO,
  FiltrosPacientesDTO,
  PacienteDTO,
  PacientePayloadDTO,
  StatusAgendamentoBackend,
  StatusCampanhaBackend,
  LoginPayloadDTO,
  RespostaAcesso,
  EstoqueDTO,
  BolsaHemocomponenteDTO,
  FiltrosHemocomponentesDTO,
  NovaBolsaHemocomponenteDTO,
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
 * Lista bolsas com filtros e paginação, da que vence primeiro para a última (FEFO). Filtro ausente
 * não restringe. Silencioso: a tela de Estoque mostra o erro no próprio corpo, sem o toast global.
 */
export async function listarHemocomponentes(filtros: FiltrosHemocomponentesDTO = {}): Promise<BolsaHemocomponenteDTO[]>
{
  return clienteHttp.get<BolsaHemocomponenteDTO[]>("/hemocomponentes", {
    params: { ...filtros },
    silencioso: true,
  });
}

/**
 * Registra uma bolsa nova no estoque (PI3-179) e devolve a bolsa criada, com o id gerado pelo backend.
 * Silencioso: o formulário mostra a mensagem de erro da API dentro dele, sem o toast global.
 */
export async function cadastrarHemocomponente(dados: NovaBolsaHemocomponenteDTO): Promise<BolsaHemocomponenteDTO>
{
  return clienteHttp.post<BolsaHemocomponenteDTO>("/hemocomponentes", dados, { silencioso: true });
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

// ----------------------------------------------------------------------
// Pacientes (PI3-194). Consultar é livre; cadastrar, editar e remover exigem um médico
// (header X-Usuario-Id, mandado pelo clienteHttp). Silenciosas: a tela mostra o erro perto da ação.
// ----------------------------------------------------------------------

/** Pacientes com os filtros informados, na ordem da fila clínica (mais grave e mais antigo primeiro). */
export async function listarPacientes(filtros: FiltrosPacientesDTO = {}): Promise<PacienteDTO[]>
{
  return clienteHttp.get<PacienteDTO[]>("/pacientes", { params: { ...filtros }, silencioso: true });
}

export async function cadastrarPaciente(dados: PacientePayloadDTO): Promise<PacienteDTO>
{
  return clienteHttp.post<PacienteDTO>("/pacientes", dados, { silencioso: true });
}

/** Troca os dados clínicos (só paciente aguardando). */
export async function editarPaciente(id: string, dados: PacientePayloadDTO): Promise<PacienteDTO>
{
  return clienteHttp.put<PacienteDTO>(`/pacientes/${encodeURIComponent(id)}`, dados, { silencioso: true });
}

/** Remove um cadastro feito por engano; paciente com campanha, procedimento ou requisição fica (422). */
export async function removerPaciente(id: string): Promise<void>
{
  return clienteHttp.delete<void>(`/pacientes/${encodeURIComponent(id)}`, { silencioso: true });
}

// ----------------------------------------------------------------------
// Campanhas e agendamentos de doação (PI3-191, PI3-192). As ações que gravam
// exigem o header X-Usuario-Id, que o clienteHttp manda a partir da sessão.
// São silenciosas: cada tela mostra o erro do backend perto de onde a ação aconteceu.
// ----------------------------------------------------------------------

/** Campanhas da mais recente para a mais antiga; sem status, ativas e encerradas. */
export async function listarCampanhas(status?: StatusCampanhaBackend): Promise<CampanhaDTO[]>
{
  return clienteHttp.get<CampanhaDTO[]>("/campanhas", { params: { status }, silencioso: true });
}

/** Publica o paciente para os doadores (só médico). */
export async function publicarCampanha(dados: CampanhaPayloadDTO): Promise<CampanhaDTO>
{
  return clienteHttp.post<CampanhaDTO>("/campanhas", dados, { silencioso: true });
}

/** Troca os dados do paciente divulgado (só médico, só campanha ativa). */
export async function editarCampanha(id: string, dados: CampanhaPayloadDTO): Promise<CampanhaDTO>
{
  return clienteHttp.put<CampanhaDTO>(`/campanhas/${id}`, dados, { silencioso: true });
}

/** Encerra a campanha e cancela os agendamentos em aberto dela (só médico). */
export async function encerrarCampanha(id: string): Promise<CampanhaDTO>
{
  return clienteHttp.patch<CampanhaDTO>(`/campanhas/${id}/encerramento`, undefined, { silencioso: true });
}

/** Agendamentos de uma campanha (médico e admin). */
export async function listarAgendamentosDaCampanha(campanhaId: string): Promise<AgendamentoDTO[]>
{
  return clienteHttp.get<AgendamentoDTO[]>(`/campanhas/${campanhaId}/agendamentos`, { silencioso: true });
}

/** O doador da sessão marca uma doação na campanha. */
export async function agendarDoacao(campanhaId: string, agendadoPara: string, bancoId: string = "BS-01"): Promise<AgendamentoDTO>
{
  return clienteHttp.post<AgendamentoDTO>(
    `/campanhas/${campanhaId}/agendamentos`,
    { bancoId, agendadoPara },
    { silencioso: true },
  );
}

/** Agendamentos do doador da sessão, do mais recente para o mais antigo. */
export async function listarMeusAgendamentos(): Promise<AgendamentoDTO[]>
{
  return clienteHttp.get<AgendamentoDTO[]>("/agendamentos/meus", { silencioso: true });
}

/** O doador muda o horário do próprio agendamento em aberto. */
export async function remarcarAgendamento(id: string, agendadoPara: string): Promise<AgendamentoDTO>
{
  return clienteHttp.patch<AgendamentoDTO>(`/agendamentos/${id}`, { agendadoPara }, { silencioso: true });
}

/** Fecha o agendamento: o doador só cancela; o médico também registra doação feita ou falta. */
export async function mudarStatusAgendamento(
  id: string,
  status: Exclude<StatusAgendamentoBackend, "AGENDADO">,
): Promise<AgendamentoDTO>
{
  return clienteHttp.patch<AgendamentoDTO>(`/agendamentos/${id}/status`, { status }, { silencioso: true });
}

/**
 * Obtém diagnóstico em tempo real do backend, banco Supabase e logs HTTP.
 */
export async function obterDiagnostico(): Promise<DiagnosticoDados>
{
  return clienteHttp.get<DiagnosticoDados>("/diagnostico", { silencioso: true });
}

