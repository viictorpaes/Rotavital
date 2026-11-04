import { useEffect } from "react";
import {
  AdvancedMarker,
  AdvancedMarkerAnchorPoint,
  APIProvider,
  Map as MapaDoGoogle,
  useMap,
} from "@vis.gl/react-google-maps";
import type { Coordenada } from "@/types";
import { marcadorDoPonto } from "@/lib/marcadoresDaRede";
import type { PropsDoMapa } from "@/components/rede/MapaRede";

declare global
{
  interface Window
  {
    gm_authFailure?: () => void;
  }
}

// O marcador personalizado (AdvancedMarker) exige um Map ID; este é o de demonstração do próprio Google.
const ID_DO_MAPA = "DEMO_MAP_ID";

const paraLatLng = ({ latitude, longitude }: Coordenada) => ({ lat: latitude, lng: longitude });

/** Desenha a rota e enquadra o mapa nela: a biblioteca não traz um componente de linha estável. */
function TracadoDaRota({ pontos }: Readonly<{ pontos: Coordenada[] }>)
{
  const mapa = useMap();

  useEffect(() =>
  {
    if (!mapa || pontos.length < 2) return undefined;

    const caminho = pontos.map(paraLatLng);
    const linha = new google.maps.Polyline({
      map: mapa,
      path: caminho,
      strokeColor: "#c1272d",
      strokeOpacity: 0.9,
      strokeWeight: 4,
    });

    const limites = new google.maps.LatLngBounds();
    caminho.forEach((ponto) => limites.extend(ponto));
    mapa.fitBounds(limites, 44);

    return () => linha.setMap(null);
  }, [mapa, pontos]);

  return null;
}

interface Props extends PropsDoMapa
{
  chave: string;
  /** Chave recusada ou script fora do ar: quem chama troca pelo OpenStreetMap. */
  onFalha: () => void;
}

/** Mapa interativo da rede na Maps JavaScript API do Google (PI3-258). */
export function MapaGoogle({ chave, onFalha, pontos, selecionadoId, tracado, onSelecionar }: Readonly<Props>)
{
  useEffect(() =>
  {
    // O Google chama esta função global quando recusa a chave (inválida, API desligada ou domínio não autorizado).
    window.gm_authFailure = onFalha;
    return () =>
    {
      delete window.gm_authFailure;
    };
  }, [onFalha]);

  const centro = paraLatLng({
    latitude: pontos.reduce((soma, ponto) => soma + ponto.latitude, 0) / pontos.length,
    longitude: pontos.reduce((soma, ponto) => soma + ponto.longitude, 0) / pontos.length,
  });

  return (
    <APIProvider apiKey={chave} language="pt-BR" region="BR" onError={onFalha}>
      <MapaDoGoogle
        mapId={ID_DO_MAPA}
        defaultCenter={centro}
        defaultZoom={12}
        gestureHandling="cooperative"
        disableDefaultUI
        zoomControl
        style={{ height: "100%", width: "100%" }}
      >
        <TracadoDaRota pontos={tracado} />

        {pontos.map((ponto) => (
          <AdvancedMarker
            key={ponto.id}
            position={paraLatLng(ponto)}
            title={ponto.nome}
            anchorPoint={AdvancedMarkerAnchorPoint.CENTER}
            onClick={() => onSelecionar(ponto)}
          >
            {/* HTML fixo, montado em `marcadorDoPonto`: nenhum dado vindo da API entra nele. */}
            <div
              dangerouslySetInnerHTML={{
                __html: marcadorDoPonto(ponto.tipo, ponto.id === selecionadoId ? "selecionado" : "normal").html,
              }}
            />
          </AdvancedMarker>
        ))}
      </MapaDoGoogle>
    </APIProvider>
  );
}
