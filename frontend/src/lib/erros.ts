import { ErroHttp } from "@/services/clienteHttp";

/**
 * Mensagem para mostrar na tela: o detail que a API devolveu (RFC 7807) ou, quando a requisição nem
 * chegou ao backend, o texto padrão informado.
 */
export function mensagemDoErro(falha: unknown, semConexao: string): string
{
  if (falha instanceof ErroHttp)
  {
    return falha.detalhe || falha.message;
  }
  return semConexao;
}
