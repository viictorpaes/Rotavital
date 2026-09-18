import type { Conexao, PontoDeRede } from "@/types";

/**
 * Rede de distribuição do hemocentro em Recife — PE (HU-07).
 * Dados mockados nesta primeira versão — substituídos por GET /rede.
 */
export const hemocentro: PontoDeRede =
{
  id: "cesar-life",
  nome: "Cesar Life — Hemocentro",
  endereco: "R. Joaquim Nabuco, 171 — Graças, Recife",
  latitude: -8.0530,
  longitude: -34.8952,
  origem: true,
};

export const hospitais: PontoDeRede[] =
[
  { id: "hc-pe", nome: "Hospital das Clínicas de PE", endereco: "Av. Prof. Moraes Rego, 1235 — Cidade Universitária", latitude: -8.0512, longitude: -34.9478 },
  { id: "real-portugues", nome: "Real Hospital Português", endereco: "Av. Gov. Agamenon Magalhães, 4760 — Paissandu", latitude: -8.0479, longitude: -34.8993 },
  { id: "barao-lucena", nome: "Hospital Barão de Lucena", endereco: "Av. Caxangá, 3860 — Iputinga", latitude: -8.0432, longitude: -34.9331 },
  { id: "getulio-vargas", nome: "Hospital Getúlio Vargas", endereco: "R. Cons. Portela, 1034 — Afogados", latitude: -8.0812, longitude: -34.9127 },
  { id: "upa-norte", nome: "UPA Norte — Macaxeira", endereco: "Av. Norte Miguel Arraes, 7200 — Macaxeira", latitude: -8.0098, longitude: -34.9296 },
];

export const conexoes: Conexao[] =
[
  {
    hospitalId: "hc-pe",
    distanciaKm: 7.8,
    tempoMin: 18,
    status: "estavel",
    trajeto: [
      { latitude: -8.0498, longitude: -34.9080 },
      { latitude: -8.0455, longitude: -34.9290 },
      { latitude: -8.0489, longitude: -34.9420 },
    ],
  },
  {
    hospitalId: "real-portugues",
    distanciaKm: 2.1,
    tempoMin: 7,
    status: "atencao",
    trajeto: [{ latitude: -8.0501, longitude: -34.8961 }],
  },
  {
    hospitalId: "barao-lucena",
    distanciaKm: 1.9,
    tempoMin: 6,
    status: "estavel",
    trajeto: [
      { latitude: -8.0489, longitude: -34.9075 },
      { latitude: -8.0447, longitude: -34.9240 },
    ],
  },
  {
    hospitalId: "getulio-vargas",
    distanciaKm: 7.1,
    tempoMin: 16,
    status: "critico",
    trajeto: [
      { latitude: -8.0625, longitude: -34.8975 },
      { latitude: -8.0742, longitude: -34.9038 },
      { latitude: -8.0795, longitude: -34.9090 },
    ],
  },
  {
    hospitalId: "upa-norte",
    distanciaKm: 8.4,
    tempoMin: 20,
    status: "atencao",
    trajeto: [
      { latitude: -8.0380, longitude: -34.9018 },
      { latitude: -8.0216, longitude: -34.9180 },
    ],
  },
];
