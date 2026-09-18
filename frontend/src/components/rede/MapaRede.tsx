import { useMemo } from "react";
import type { Coordenada, PontoDeRede } from "@/types";
import { conexoes, hemocentro, hospitais } from "@/data/redeMock";
import { conexaoDe, tracadoDaRota } from "@/lib/rede";

const LARGURA = 1000;
const ALTURA = 600;
const MARGEM = 0.16;
/** Comprimento de 1° de latitude, usado na barra de escala. */
const KM_POR_GRAU = 111.32;

/** Capibaribe — única referência geográfica do desenho. */
const RIO: Coordenada[] = [
  { latitude: -8.0425, longitude: -34.9600 },
  { latitude: -8.0470, longitude: -34.9300 },
  { latitude: -8.0512, longitude: -34.9080 },
  { latitude: -8.0545, longitude: -34.8900 },
  { latitude: -8.0600, longitude: -34.8700 },
];

interface Projecao
{
  projetar: (ponto: Coordenada) => [number, number];
  /** Pixels equivalentes a 1 km — usado na barra de escala. */
  pixelsPorKm: number;
}

/** Projeção equirretangular: suficiente na escala de uma cidade, sem tiles nem chave de API. */
function criarProjecao(pontos: Coordenada[]): Projecao
{
  const latitudes = pontos.map((p) => p.latitude);
  const longitudes = pontos.map((p) => p.longitude);
  const latMedia = (Math.min(...latitudes) + Math.max(...latitudes)) / 2;
  const escalaLongitude = Math.cos((latMedia * Math.PI) / 180);

  let minX = Math.min(...longitudes) * escalaLongitude;
  let maxX = Math.max(...longitudes) * escalaLongitude;
  let minY = -Math.max(...latitudes);
  let maxY = -Math.min(...latitudes);

  const folgaX = (maxX - minX) * MARGEM;
  const folgaY = (maxY - minY) * MARGEM;
  minX -= folgaX; maxX += folgaX;
  minY -= folgaY; maxY += folgaY;

  // Iguala o formato da área geográfica ao do quadro, para preencher sem distorcer.
  const proporcao = LARGURA / ALTURA;
  if ((maxX - minX) / (maxY - minY) < proporcao)
  {
    const sobra = ((maxY - minY) * proporcao - (maxX - minX)) / 2;
    minX -= sobra; maxX += sobra;
  }
  else
  {
    const sobra = ((maxX - minX) / proporcao - (maxY - minY)) / 2;
    minY -= sobra; maxY += sobra;
  }

  return {
    projetar: ({ latitude, longitude }) =>
    [
      ((longitude * escalaLongitude - minX) / (maxX - minX)) * LARGURA,
      ((-latitude - minY) / (maxY - minY)) * ALTURA,
    ],
    pixelsPorKm: ALTURA / (maxY - minY) / KM_POR_GRAU,
  };
}

interface Props
{
  selecionado: PontoDeRede;
  onSelecionar: (hospital: PontoDeRede) => void;
}

/**
 * Mapa da rede de distribuição (HU-07): pontos posicionados por latitude e
 * longitude reais, todas as conexões visíveis e a rota até o hospital
 * selecionado em destaque. Clicar em um ponto seleciona o hospital.
 */
export function MapaRede({ selecionado, onSelecionar }: Readonly<Props>)
{
  const { projetar, pixelsPorKm } = useMemo(
    () => criarProjecao([hemocentro, ...hospitais, ...RIO]),
    [],
  );

  const emPontos = (pontos: Coordenada[]) =>
    pontos.map((ponto) => projetar(ponto).map(Math.round).join(",")).join(" ");

  const conexaoAtiva = conexaoDe(selecionado.id);
  const [origemX, origemY] = projetar(hemocentro);
  const escalaKm = 2;

  return (
    <svg
      viewBox={`0 0 ${LARGURA} ${ALTURA}`}
      role="img"
      aria-label={`Mapa da rede: rota do hemocentro até ${selecionado.nome}`}
      className="block h-[340px] w-full bg-rota-surface2 md:h-[420px]"
    >
      <polyline
        points={emPontos(RIO)}
        fill="none"
        stroke="#dfe4e9"
        strokeWidth={18}
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      {/* Todas as conexões da rede, em segundo plano. */}
      {conexoes.map((conexao) =>
      {
        const hospital = hospitais.find((h) => h.id === conexao.hospitalId);
        if (!hospital || conexao.hospitalId === selecionado.id) return null;
        return (
          <polyline
            key={conexao.hospitalId}
            points={emPontos(tracadoDaRota(conexao, hospital))}
            fill="none"
            stroke="#c9c6bd"
            strokeWidth={2.5}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        );
      })}

      {conexaoAtiva && (
        <polyline
          points={emPontos(tracadoDaRota(conexaoAtiva, selecionado))}
          fill="none"
          stroke="#c1272d"
          strokeWidth={4}
          strokeDasharray="12 9"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      )}

      {hospitais.map((hospital) =>
      {
        const [x, y] = projetar(hospital);
        const ativo = hospital.id === selecionado.id;
        const aEsquerda = x > LARGURA / 2;
        return (
          <g
            key={hospital.id}
            role="button"
            tabIndex={0}
            aria-label={hospital.nome}
            aria-pressed={ativo}
            className="cursor-pointer outline-none"
            onClick={() => onSelecionar(hospital)}
            onKeyDown={(evento) =>
            {
              if (evento.key === "Enter" || evento.key === " ")
              {
                evento.preventDefault();
                onSelecionar(hospital);
              }
            }}
          >
            {/* Alvo de clique generoso, invisível. */}
            <circle cx={x} cy={y} r={22} fill="transparent" />
            <circle
              cx={x}
              cy={y}
              r={ativo ? 9 : 6}
              fill={ativo ? "#111827" : "#9ca3af"}
              stroke="#ffffff"
              strokeWidth={ativo ? 3 : 2}
            />
            <text
              x={aEsquerda ? x - 16 : x + 16}
              y={y + 5}
              textAnchor={aEsquerda ? "end" : "start"}
              fontSize={17}
              fontWeight={ativo ? 700 : 500}
              fill={ativo ? "#111827" : "#6b7280"}
            >
              {hospital.nome}
            </text>
          </g>
        );
      })}

      <g>
        <rect
          x={origemX - 9}
          y={origemY - 9}
          width={18}
          height={18}
          fill="#c1272d"
          stroke="#ffffff"
          strokeWidth={3}
        />
        <text x={origemX} y={origemY - 18} textAnchor="middle" fontSize={17} fontWeight={700} fill="#c1272d">
          Cesar Life
        </text>
      </g>

      {/* Barra de escala. */}
      <g transform={`translate(28 ${ALTURA - 26})`}>
        <line x1={0} y1={0} x2={escalaKm * pixelsPorKm} y2={0} stroke="#9ca3af" strokeWidth={3} />
        <line x1={0} y1={-5} x2={0} y2={5} stroke="#9ca3af" strokeWidth={3} />
        <line
          x1={escalaKm * pixelsPorKm}
          y1={-5}
          x2={escalaKm * pixelsPorKm}
          y2={5}
          stroke="#9ca3af"
          strokeWidth={3}
        />
        <text x={escalaKm * pixelsPorKm + 10} y={5} fontSize={16} fill="#9ca3af">{escalaKm} km</text>
      </g>
    </svg>
  );
}
