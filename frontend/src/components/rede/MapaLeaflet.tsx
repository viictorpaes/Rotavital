import { useEffect } from "react";
import { MapContainer, Marker, Polyline, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { Coordenada } from "@/types";
import { marcadorDoPonto, type DestaqueDoPonto } from "@/lib/marcadoresDaRede";
import type { PontoRedeDTO, TipoPontoRedeBackend } from "@/types/dtos";
import type { PropsDoMapa } from "@/components/rede/MapaRede";

type Posicao = [number, number];

const posicao = ({ latitude, longitude }: Coordenada): Posicao => [latitude, longitude];

function iconeDoPonto(tipo: TipoPontoRedeBackend, destaque: DestaqueDoPonto)
{
  const { html, tamanho } = marcadorDoPonto(tipo, destaque);
  return L.divIcon({ html, iconSize: [tamanho, tamanho], iconAnchor: [tamanho / 2, tamanho / 2], className: "" });
}

/** Reenquadra o mapa para caber a rota inteira sempre que ela muda. */
function AjustarEnquadramento({ pontos }: Readonly<{ pontos: Posicao[] }>)
{
  const mapa = useMap();
  const chave = pontos.length > 1 ? `${pontos.length}:${pontos[0]};${pontos[pontos.length - 1]}` : "";

  useEffect(() =>
  {
    if (pontos.length >= 2)
    {
      mapa.fitBounds(L.latLngBounds(pontos), { padding: [44, 44] });
    }
    // Reenquadra apenas quando a rota muda de origem/destino.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chave]);

  return null;
}

function centroDe(pontos: PontoRedeDTO[]): Posicao
{
  const soma = pontos.reduce(([lat, lon], ponto) => [lat + ponto.latitude, lon + ponto.longitude], [0, 0]);
  return [soma[0] / pontos.length, soma[1] / pontos.length];
}

/**
 * Plano B do mapa da rede: tiles abertos do OpenStreetMap, sem chave, para a tela funcionar
 * quando o Google Maps não está configurado ou recusa a chave (ver `MapaRede`).
 */
export function MapaLeaflet({ pontos, destaques, tracado, onSelecionar }: Readonly<PropsDoMapa>)
{
  const linha = tracado.map(posicao);

  return (
    <div className="mapa-rede relative h-full">
      <MapContainer
        center={centroDe(pontos)}
        zoom={12}
        zoomControl={false}
        scrollWheelZoom={false}
        attributionControl={false}
        style={{ height: "100%", width: "100%" }}
      >
        {/* O tom neutro do basemap vem do filtro `.mapa-rede` em `index.css`. */}
        <TileLayer
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="© OpenStreetMap"
          maxZoom={19}
        />

        {linha.length >= 2 && (
          <>
            <Polyline positions={linha} pathOptions={{ color: "#c1272d", weight: 4, opacity: 0.9 }} />
            <AjustarEnquadramento pontos={linha} />
          </>
        )}

        {pontos.map((ponto) => (
          <Marker
            key={ponto.id}
            position={posicao(ponto)}
            icon={iconeDoPonto(ponto.tipo, destaques[ponto.id] ?? "normal")}
            title={ponto.nome}
            eventHandlers={{ click: () => onSelecionar(ponto) }}
          />
        ))}
      </MapContainer>

      <p className="absolute bottom-2 right-2 z-[998] bg-white/90 px-1.5 py-0.5 font-mono text-[9px] text-gray-500">
        © OpenStreetMap
      </p>
    </div>
  );
}
