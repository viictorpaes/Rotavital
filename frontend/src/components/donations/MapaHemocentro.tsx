import { MapContainer, Marker, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { HemocentroDTO } from "@/types/dtos";

// Mesmo marcador do hemocentro no mapa da rede (MapaRede): quadrado vermelho desenhado em HTML.
const ICONE_HEMOCENTRO = L.divIcon({
  html: '<div style="width:16px;height:16px;background:#c1272d;border:2px solid #fff;border-radius:2px;box-shadow:0 1px 6px rgba(0,0,0,.35)"></div>',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
  className: "",
});

/**
 * Onde fica o hemocentro (PI3-205), pelas coordenadas que o backend devolve, sobre os tiles
 * abertos do OpenStreetMap. Substitui o esboço de mapa que era desenhado à mão no modal.
 */
export function MapaHemocentro({ hemocentro }: Readonly<{ hemocentro: HemocentroDTO }>)
{
  const posicao: [number, number] = [hemocentro.latitude, hemocentro.longitude];
  const linkMapa =
    `https://www.openstreetmap.org/?mlat=${hemocentro.latitude}&mlon=${hemocentro.longitude}` +
    `#map=17/${hemocentro.latitude}/${hemocentro.longitude}`;

  return (
    <div className="mapa-rede relative h-36 overflow-hidden rounded-lg border border-rota-border">
      {/* key: o MapContainer não muda de centro depois de montado; outro hemocentro monta outro mapa. */}
      <MapContainer
        key={hemocentro.id}
        center={posicao}
        zoom={15}
        zoomControl={false}
        scrollWheelZoom={false}
        attributionControl={false}
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" maxZoom={19} />
        <Marker position={posicao} icon={ICONE_HEMOCENTRO} title={hemocentro.nome} />
      </MapContainer>

      <a
        href={linkMapa}
        target="_blank"
        rel="noreferrer"
        className="absolute bottom-2 left-2 z-[998] rounded bg-white/90 px-2 py-0.5 text-xs font-semibold text-rota-red hover:underline"
      >
        Abrir no mapa
      </a>
      <p className="absolute bottom-2 right-2 z-[998] bg-white/90 px-1.5 py-0.5 font-mono text-[9px] text-gray-500">
        © OpenStreetMap
      </p>
    </div>
  );
}
