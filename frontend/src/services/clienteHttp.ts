/**
 * Cliente HTTP Base Global para a aplicação Rota Vital.
 *
 * Implementa comunicação HTTP tipada baseada na Fetch API moderna, com suporte a:
 * - baseURL centralizada (configurável via Vite env ou padrão '/api/v1')
 * - Timeouts automáticos com AbortController
 * - Interceptors para injeção de autenticação/headers em requisições
 * - Interceptors para tratamento global de respostas e erros HTTP (401, 403, 500, RFC 7807)
 */

export type NotificadorGlobal = (tipo: "erro" | "aviso" | "sucesso" | "info", titulo: string, mensagem: string) => void;

let notificadorGlobal: NotificadorGlobal | null = null;

export function registrarNotificadorHttp(notificador: NotificadorGlobal): void
{
  notificadorGlobal = notificador;
}

export interface ConfiguracaoRequisicao extends RequestInit
{
  timeoutMs?: number;
  params?: Record<string, string | number | boolean | undefined | null>;
  /** Se verdadeiro, não dispara notificação de erro visual automaticamente */
  silencioso?: boolean;
}

export interface ErroApi
{
  status: number;
  titulo: string;
  detalhe: string;
  rota?: string;
  raw?: any;
}

export class ErroHttp extends Error
{
  public readonly status: number;
  public readonly titulo: string;
  public readonly detalhe: string;
  public readonly rota?: string;
  public readonly dados?: any;

  constructor(erro: ErroApi)
  {
    super(erro.detalhe || erro.titulo || `Erro HTTP ${erro.status}`);
    this.name = "ErroHttp";
    this.status = erro.status;
    this.titulo = erro.titulo;
    this.detalhe = erro.detalhe;
    this.rota = erro.rota;
    this.dados = erro.raw;
  }
}

type InterceptorRequisicao = (config: RequestInit & { url: string }) => Promise<RequestInit & { url: string }> | (RequestInit & { url: string });
type InterceptorResposta = (resposta: Response) => Promise<Response> | Response;
type InterceptorErro = (erro: any) => Promise<any> | any;

class ClienteHttp
{
  private readonly baseUrl: string;
  private readonly timeoutPadraoMs: number;
  private readonly interceptorsRequisicao: InterceptorRequisicao[] = [];
  private readonly interceptorsResposta: InterceptorResposta[] = [];
  private readonly interceptorsErro: InterceptorErro[] = [];

  constructor(baseUrl: string = "/api/v1", timeoutPadraoMs: number = 10000)
  {
    this.baseUrl = baseUrl.replace(/\/+$/, "");
    this.timeoutPadraoMs = timeoutPadraoMs;

    this.configurarInterceptorsPadrao();
  }

  /**
   * Configura os interceptors essenciais de autenticação e tratamento de respostas.
   */
  private configurarInterceptorsPadrao(): void
  {
    // 1. Interceptor de Requisição: Injeta headers padrão e dados de sessão
    this.interceptorsRequisicao.push((config) =>
    {
      const headers = new Headers(config.headers || {});

      if (!headers.has("Content-Type") && !(config.body instanceof FormData))
      {
        headers.set("Content-Type", "application/json");
      }
      if (!headers.has("Accept"))
      {
        headers.set("Accept", "application/json, application/problem+json");
      }

      // Injeta credenciais da sessão armazenada no navegador (se disponível)
      try
      {
        const brutoUsuario = sessionStorage.getItem("rotavital.usuario");
        if (brutoUsuario)
        {
          const usuario = JSON.parse(brutoUsuario);
          if (usuario?.nome)
          {
            headers.set("X-Usuario-Nome", encodeURIComponent(usuario.nome));
          }
          if (usuario?.papel)
          {
            headers.set("X-Usuario-Papel", usuario.papel);
          }
        }
      }
      catch
      {
        // Ignora falhas de leitura do sessionStorage
      }

      return {
        ...config,
        headers,
      };
    });

    // 2. Interceptor de Resposta: Tratamento de expiração de sessão e status HTTP
    this.interceptorsResposta.push((resposta) =>
    {
      if (resposta.status === 401 || resposta.status === 403)
      {
        console.warn(`[ClienteHttp] Acesso não autorizado (${resposta.status}) na rota: ${resposta.url}`);
        // Se a sessão expirou no backend, podemos redirecionar ou emitir evento
        if (typeof window !== "undefined" && window.location.pathname !== "/login")
        {
          // Não redireciona imediatamente se já estiver na página de login
        }
      }
      return resposta;
    });

    // 3. Interceptor de Erro: formata timeout e erros de conexão
    this.interceptorsErro.push((erro) =>
    {
      if (erro.name === "AbortError")
      {
        const erroTimeout = new ErroHttp({
          status: 408,
          titulo: "Tempo Limite Excedido (408)",
          detalhe: `A requisição demorou mais de ${this.timeoutPadraoMs / 1000}s para responder. Verifique sua conexão com o servidor.`,
        });

        if (notificadorGlobal)
        {
          notificadorGlobal("erro", erroTimeout.titulo, erroTimeout.detalhe);
        }

        throw erroTimeout;
      }
      throw erro;
    });
  }

  /**
   * Registra um interceptor customizado de requisição.
   */
  public adicionarInterceptorRequisicao(interceptor: InterceptorRequisicao): void
  {
    this.interceptorsRequisicao.push(interceptor);
  }

  /**
   * Registra um interceptor customizado de resposta.
   */
  public adicionarInterceptorResposta(interceptor: InterceptorResposta): void
  {
    this.interceptorsResposta.push(interceptor);
  }

  /**
   * Constrói a URL final considerando a baseURL e parâmetros de busca (query string).
   */
  private montarUrl(caminho: string, params?: Record<string, any>): string
  {
    const caminhoLimpo = caminho.startsWith("/") ? caminho : `/${caminho}`;
    const urlCompleta = caminho.startsWith("http://") || caminho.startsWith("https://")
      ? caminho
      : `${this.baseUrl}${caminhoLimpo}`;

    if (!params)
    {
      return urlCompleta;
    }

    const url = new URL(urlCompleta, typeof window !== "undefined" ? window.location.origin : "http://localhost:5173");
    Object.entries(params).forEach(([chave, valor]) =>
    {
      if (valor !== undefined && valor !== null && valor !== "")
      {
        url.searchParams.set(chave, String(valor));
      }
    });

    return url.toString().replace(url.origin, "");
  }

  /**
   * Executa a requisição HTTP aplicando timeouts, interceptors e parsing tipado.
   */
  public async requisitar<T>(caminho: string, config: ConfiguracaoRequisicao = {}): Promise<T>
  {
    const { timeoutMs = this.timeoutPadraoMs, params, ...opcoesFetch } = config;
    const urlFinal = this.montarUrl(caminho, params);

    // Configura controle de timeout com AbortController
    const controller = new AbortController();
    const timerId = setTimeout(() => controller.abort(), timeoutMs);

    let configProcessada: RequestInit & { url: string } = {
      ...opcoesFetch,
      url: urlFinal,
      signal: controller.signal,
    };

    try
    {
      // Executa a cadeia de interceptors de requisição
      for (const interceptor of this.interceptorsRequisicao)
      {
        configProcessada = await interceptor(configProcessada);
      }

      const { url, ...fetchInit } = configProcessada;
      let resposta = await fetch(url, fetchInit);

      // Executa a cadeia de interceptors de resposta
      for (const interceptor of this.interceptorsResposta)
      {
        resposta = await interceptor(resposta);
      }

      if (!resposta.ok)
      {
        let corpoErro: any = null;
        try
        {
          corpoErro = await resposta.json();
        }
        catch
        {
          corpoErro = await resposta.text().catch(() => null);
        }

        const detalhe = typeof corpoErro === "object" && corpoErro !== null
          ? (corpoErro.detail || corpoErro.mensagem || corpoErro.message || corpoErro.title || "Erro na operação.")
          : (typeof corpoErro === "string" && corpoErro.length > 0 ? corpoErro : `Falha na requisição com código ${resposta.status}`);

        let titulo = typeof corpoErro === "object" && corpoErro !== null
          ? (corpoErro.title || "Erro do Servidor")
          : "Erro na Requisição";

        if (resposta.status === 404) titulo = "Recurso Não Encontrado (404)";
        else if (resposta.status === 400) titulo = "Requisição Inválida (400)";
        else if (resposta.status === 401) titulo = "Sessão Não Autorizada (401)";
        else if (resposta.status === 403) titulo = "Acesso Negado (403)";
        else if (resposta.status >= 500) titulo = "Falha no Servidor (500)";

        const erroHttp = new ErroHttp({
          status: resposta.status,
          titulo,
          detalhe,
          rota: url,
          raw: corpoErro,
        });

        // Dispara feedback visual global (Toast) automaticamente para 4xx e 5xx
        if (!config.silencioso && notificadorGlobal)
        {
          notificadorGlobal("erro", titulo, detalhe);
        }

        throw erroHttp;
      }

      // Se a resposta for 204 No Content
      if (resposta.status === 204)
      {
        return {} as T;
      }

      const dados = await resposta.json();
      return dados as T;
    }
    catch (erro: any)
    {
      // Executa a cadeia de interceptors de erro
      for (const interceptor of this.interceptorsErro)
      {
        await interceptor(erro);
      }
      throw erro;
    }
    finally
    {
      clearTimeout(timerId);
    }
  }

  public get<T>(caminho: string, config?: ConfiguracaoRequisicao): Promise<T>
  {
    return this.requisitar<T>(caminho, { ...config, method: "GET" });
  }

  public post<T>(caminho: string, dados?: any, config?: ConfiguracaoRequisicao): Promise<T>
  {
    return this.requisitar<T>(caminho, {
      ...config,
      method: "POST",
      body: dados !== undefined ? JSON.stringify(dados) : undefined,
    });
  }

  public put<T>(caminho: string, dados?: any, config?: ConfiguracaoRequisicao): Promise<T>
  {
    return this.requisitar<T>(caminho, {
      ...config,
      method: "PUT",
      body: dados !== undefined ? JSON.stringify(dados) : undefined,
    });
  }

  public delete<T>(caminho: string, config?: ConfiguracaoRequisicao): Promise<T>
  {
    return this.requisitar<T>(caminho, { ...config, method: "DELETE" });
  }
}

const URL_BASE_ENV = (typeof import.meta !== "undefined" && import.meta.env?.VITE_API_URL) || "/api/v1";
const TIMEOUT_ENV = (typeof import.meta !== "undefined" && Number(import.meta.env?.VITE_API_TIMEOUT_MS)) || 10000;

// Instância Singleton exportada para uso em toda a aplicação
export const clienteHttp = new ClienteHttp(URL_BASE_ENV, TIMEOUT_ENV);
