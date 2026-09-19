import { useEffect } from "react";
import { MapContainer, Marker, Polyline, TileLayer, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { Coordenada, PontoDeRede } from "@/types";
import { hemocentro, hospitais } from "@/data/redeMock";
import type { RotaCalculada } from "@/lib/roteirizacao";

type Posicao = [number, number];

/** Marcador desenhado em HTML — evita depender das imagens padrão do Leaflet. */
function iconePonto(cor: string, tamanho: number, formato: "circulo" | "quadrado" = "circulo")
{
  return L.divIcon({
    html: `<div style="width:${tamanho}px;height:${tamanho}px;background:${cor};border:2px solid #fff;border-radius:${
      formato === "quadrado" ? "2px" : "50%"
    };box-shadow:0 1px 6px rgba(0,0,0,.35)"></div>`,
    iconSize: [tamanho, tamanho],
    iconAnchor: [tamanho / 2, tamanho / 2],
    className: "",
  });
}

const ICONE_ORIGEM = iconePonto("#c1272d", 14, "quadrado");
const ICONE_ATIVO = iconePonto("#111827", 12);
const ICONE_INATIVO = iconePonto("#9ca3af", 9);

const posicao = ({ latitude, longitude }: Coordenada): Posicao => [latitude, longitude];

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

interface Props
{
  selecionado: PontoDeRede;
  rota: RotaCalculada;
  calculando: boolean;
  onSelecionar: (hospital: PontoDeRede) => void;
  altura?: string;
}

/**
 * Mapa da rede (HU-07) sobre tiles do OpenStreetMap — basemap aberto, sem chave
 * de API. Clicar em um marcador seleciona o hospital, e o traçado exibido é o da
 * rota já calculada para ele.
 */
export function MapaRede({ selecionado, rota, calculando, onSelecionar, altura = "420px" }: Readonly<Props>)
{
  const tracado = rota.pontos.map(posicao);

  const centro: Posicao = [
    (hemocentro.latitude + selecionado.latitude) / 2,
    (hemocentro.longitude + selecionado.longitude) / 2,
  ];

  return (
    <div className="mapa-rede relative" style={{ height: altura }}>
      {calculando && !rota.doRoteador && (
        <div className="pointer-events-none absolute inset-0 z-[999] flex items-center justify-center bg-white/70">
          <span className="font-mono text-[11px] uppercase tracking-widest text-gray-500">
            calculando rota…
          </span>
        </div>
      )}

      <MapContainer
        center={centro}
        zoom={13}
        zoomControl={false}
        scrollWheelZoom={false}
        attributionControl={false}
        style={{ height: "100%", width: "100%" }}
      >
        {/* Tiles do OpenStreetMap — abertos, sem chave de API. O tom neutro do
            basemap vem do filtro `.mapa-rede` em `index.css`. */}
        <TileLayer
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="© OpenStreetMap"
          maxZoom={19}
        />

        {tracado.length >= 2 && (
          <>
            <Polyline
              positions={tracado}
              pathOptions={{ color: "#c1272d", weight: 3.5, dashArray: "10 6", opacity: 0.9 }}
            />
            <AjustarEnquadramento pontos={tracado} />
          </>
        )}

        <Marker position={posicao(hemocentro)} icon={ICONE_ORIGEM} title={hemocentro.nome} />

        {hospitais.map((hospital) => (
          <Marker
            key={hospital.id}
            position={posicao(hospital)}
            icon={hospital.id === selecionado.id ? ICONE_ATIVO : ICONE_INATIVO}
            title={hospital.nome}
            eventHandlers={{ click: () => onSelecionar(hospital) }}
          />
        ))}
      </MapContainer>

      <p className="absolute bottom-2 right-2 z-[998] bg-white/90 px-1.5 py-0.5 font-mono text-[9px] text-gray-500">
        © OpenStreetMap
      </p>
    </div>
  );
}
