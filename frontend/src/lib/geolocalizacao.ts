/**
 * PI3-261 — Posição atual do doador pela Geolocation API do navegador.
 *
 * A posição só é pedida quando o doador clica para usar a localização (consentimento explícito)
 * e não é gravada: vale só para calcular a rota até o hospital da campanha naquele momento.
 */

/** Onde o doador está, em graus decimais (WGS 84, o mesmo do GPS e do Google Maps). */
export interface PosicaoDoador
{
  latitude: number;
  longitude: number;
  /** Raio de incerteza que o navegador informa, em metros (GPS: ~5–50 m; Wi-Fi/IP: centenas). */
  precisaoMetros: number;
}

/**
 * aguardando: ainda não pediu · localizando: o navegador está perguntando ou lendo o GPS ·
 * localizado: tem posição · negado: o doador recusou · indisponivel: o aparelho não conseguiu.
 */
export type SituacaoGeolocalizacao = "aguardando" | "localizando" | "localizado" | "negado" | "indisponivel";

/**
 * GPS quando houver (enableHighAccuracy), até 15 s para responder e aceita uma posição lida
 * há até 1 min (evita esperar o GPS de novo ao reabrir a rota).
 */
export const OPCOES_GEOLOCALIZACAO: PositionOptions =
{
  enableHighAccuracy: true,
  timeout: 15_000,
  maximumAge: 60_000,
};

// Códigos do GeolocationPositionError (W3C). Ficam aqui porque nem todo navegador expõe as constantes globais.
const PERMISSAO_NEGADA = 1;
const POSICAO_INDISPONIVEL = 2;
const TEMPO_ESGOTADO = 3;

/**
 * Por que o navegador não vai informar a posição, ou null se ele pode. A Geolocation API só
 * funciona em contexto seguro: HTTPS ou localhost. Pelo IP da rede (http://192.168…) o celular recusa.
 */
export function motivoSemGeolocalizacao(): string | null
{
  if (typeof navigator === "undefined" || !("geolocation" in navigator))
  {
    return "Este navegador não informa a localização. Use o botão do Google Maps: o app usa o GPS do aparelho.";
  }
  if (typeof window !== "undefined" && !window.isSecureContext)
  {
    return "O navegador só libera a localização em páginas seguras (https://). Use o botão do Google Maps: o app usa o GPS do aparelho.";
  }
  return null;
}

/** Mensagem para o doador a partir do erro do getCurrentPosition. */
export function mensagemDoErroDeGeolocalizacao(erro: Pick<GeolocationPositionError, "code">): string
{
  switch (erro.code)
  {
    case PERMISSAO_NEGADA:
      return "Você não permitiu o acesso à localização. Para calcular a distância, libere a localização deste site nas configurações do navegador e tente de novo.";
    case POSICAO_INDISPONIVEL:
      return "Não foi possível descobrir onde você está. Confira se o GPS (ou a localização do aparelho) está ligado.";
    case TEMPO_ESGOTADO:
      return "A localização demorou demais para responder. Tente de novo em um lugar com sinal melhor.";
    default:
      return "Não foi possível ler a sua localização.";
  }
}

/** Recusa do doador (diferente de falha do aparelho): a tela não insiste e explica como liberar. */
export function foiNegada(erro: Pick<GeolocationPositionError, "code">): boolean
{
  return erro.code === PERMISSAO_NEGADA;
}

/** Converte a posição do navegador, descartando o que a rota não usa (altitude, velocidade…). */
export function posicaoDoNavegador(posicao: Pick<GeolocationPosition, "coords">): PosicaoDoador
{
  return {
    latitude: posicao.coords.latitude,
    longitude: posicao.coords.longitude,
    precisaoMetros: Math.round(posicao.coords.accuracy),
  };
}

/** "±25 m" ou "±1,2 km", para o doador saber se a posição veio do GPS ou de uma estimativa por rede. */
export function formatarPrecisao(metros: number): string
{
  if (metros < 1000)
  {
    return `±${metros} m`;
  }
  return `±${(metros / 1000).toLocaleString("pt-BR", { maximumFractionDigits: 1 })} km`;
}
