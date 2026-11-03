/**
 * PI3-263 — Links que abrem a navegação GPS ponto a ponto até o hospital, no app instalado.
 *
 * São links universais (https), não esquemas próprios como "comgooglemaps://": no celular com o
 * app instalado, o sistema abre o Google Maps ou o Waze; sem o app, e no computador, abre o site.
 * Assim um link só serve para Android, iPhone e desktop, sem detectar o aparelho.
 */

/** Ponto em graus decimais (WGS 84). */
export interface PontoGeografico
{
  latitude: number;
  longitude: number;
}

/**
 * "lat,lng" sem arredondar: o destino precisa ser exatamente a coordenada cadastrada do hospital.
 * String(número) dá a menor representação que volta ao mesmo double, sem casas inventadas.
 */
export function coordenadaNoLink(ponto: PontoGeografico): string
{
  return `${String(ponto.latitude)},${String(ponto.longitude)}`;
}

/**
 * Google Maps URLs (api=1), modo rota de carro. dir_action=navigate já começa a navegação
 * curva a curva no app. Sem origem, o Google usa a localização do aparelho; com origem (a posição
 * que o doador liberou), a rota sai exatamente de onde a distância foi calculada.
 * https://developers.google.com/maps/documentation/urls/get-started#directions-action
 */
export function linkGoogleMaps(destino: PontoGeografico, origem?: PontoGeografico | null): string
{
  const parametros = new URLSearchParams({ api: "1" });
  if (origem)
  {
    parametros.set("origin", coordenadaNoLink(origem));
  }
  parametros.set("destination", coordenadaNoLink(destino));
  parametros.set("travelmode", "driving");
  parametros.set("dir_action", "navigate");
  return `https://www.google.com/maps/dir/?${parametros.toString()}`;
}

/**
 * Deep link do Waze: navega até as coordenadas, saindo sempre da posição atual do aparelho
 * (o Waze não aceita origem). Vírgula sem codificar, como no exemplo da documentação: a
 * coordenada só tem dígitos, sinal e ponto. https://developers.google.com/waze/deeplinks
 */
export function linkWaze(destino: PontoGeografico): string
{
  return `https://waze.com/ul?ll=${coordenadaNoLink(destino)}&navigate=yes`;
}
