import { useCallback, useState } from "react";
import type { Coordenada } from "@/types";
import type { PontoRedeDTO } from "@/types/dtos";
import type { DestaqueDoPonto } from "@/lib/marcadoresDaRede";
import { MapaGoogle } from "@/components/rede/MapaGoogle";
import { MapaLeaflet } from "@/components/rede/MapaLeaflet";

export interface PropsDoMapa
{
  /** Hemocentros e hospitais da rede (GET /pontos). */
  pontos: PontoRedeDTO[];
  /** Papel de cada ponto na rota; quem não está aqui é um ponto comum. */
  destaques: Record<string, DestaqueDoPonto>;
  /** Linha da rota a desenhar; com menos de 2 pontos, nada é desenhado. */
  tracado: Coordenada[];
  onSelecionar: (ponto: PontoRedeDTO) => void;
}

const CHAVE_GOOGLE_MAPS = import.meta.env.VITE_GOOGLE_MAPS_KEY ?? "";

/**
 * Mapa da rede (PI3-258): Google Maps quando há chave para o navegador; sem ela, ou se o Google
 * recusar a chave, OpenStreetMap, para a tela de Rede nunca ficar sem mapa.
 */
export function MapaRede({ altura = "420px", ...mapa }: Readonly<PropsDoMapa & { altura?: string }>)
{
  const [googleFalhou, setGoogleFalhou] = useState(false);
  const usarOpenStreetMap = useCallback(() => setGoogleFalhou(true), []);

  return (
    <div style={{ height: altura }}>
      {CHAVE_GOOGLE_MAPS && !googleFalhou
        ? <MapaGoogle chave={CHAVE_GOOGLE_MAPS} onFalha={usarOpenStreetMap} {...mapa} />
        : <MapaLeaflet {...mapa} />}
    </div>
  );
}
