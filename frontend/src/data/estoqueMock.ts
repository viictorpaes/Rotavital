import type { FaixaTemperatura, LoteHemocomponente, TipoComponente } from "@/types";

// Faixas ideais de conservação por hemocomponente (RDC 34/2014).
export const FAIXAS_IDEAIS: Record<TipoComponente, FaixaTemperatura> =
{
  "Concentrado de Hemácias": { minima: 2, maxima: 6 },
  "Plasma Fresco Congelado": { minima: -30, maxima: -18 },
  "Concentrado de Plaquetas": { minima: 20, maxima: 24 },
};

/** Data ISO (AAAA-MM-DD) daqui a `dias` — mantém o mock sempre coerente com "hoje". */
function emDias(dias: number)
{
  const data = new Date();
  data.setHours(0, 0, 0, 0);
  data.setDate(data.getDate() + dias);
  return data.toISOString().slice(0, 10);
}

interface EntradaMock
{
  codigo: string;
  componente: TipoComponente;
  tipoSanguineo: LoteHemocomponente["tipoSanguineo"];
  unidades: number;
  volumeMl: number;
  diasParaVencer: number;
  temperaturaAtual: number;
  localizacao: string;
}

const ENTRADAS: EntradaMock[] =
[
  { codigo: "CH-1060", componente: "Concentrado de Hemácias", tipoSanguineo: "A+", unidades: 22, volumeMl: 450, diasParaVencer: 27, temperaturaAtual: 4.0, localizacao: "R2 · P1 · N1" },
  { codigo: "PL-2018", componente: "Plasma Fresco Congelado", tipoSanguineo: "A+", unidades: 14, volumeMl: 300, diasParaVencer: 150, temperaturaAtual: -22.1, localizacao: "F1 · P2 · N1" },
  { codigo: "PQ-3014", componente: "Concentrado de Plaquetas", tipoSanguineo: "A+", unidades: 1, volumeMl: 60, diasParaVencer: 1, temperaturaAtual: 25.6, localizacao: "A1 · P2 · N2" },
  { codigo: "CH-1061", componente: "Concentrado de Hemácias", tipoSanguineo: "A-", unidades: 5, volumeMl: 450, diasParaVencer: 9, temperaturaAtual: 5.9, localizacao: "R2 · P1 · N4" },
  { codigo: "CH-1062", componente: "Concentrado de Hemácias", tipoSanguineo: "B+", unidades: 9, volumeMl: 450, diasParaVencer: 34, temperaturaAtual: 3.4, localizacao: "R3 · P1 · N2" },
  { codigo: "PQ-3015", componente: "Concentrado de Plaquetas", tipoSanguineo: "B+", unidades: 3, volumeMl: 60, diasParaVencer: 4, temperaturaAtual: 22.0, localizacao: "A1 · P1 · N3" },
  { codigo: "CH-1063", componente: "Concentrado de Hemácias", tipoSanguineo: "B-", unidades: 4, volumeMl: 450, diasParaVencer: 12, temperaturaAtual: 4.6, localizacao: "R3 · P2 · N1" },
  { codigo: "PL-2019", componente: "Plasma Fresco Congelado", tipoSanguineo: "AB+", unidades: 11, volumeMl: 300, diasParaVencer: 210, temperaturaAtual: -24.8, localizacao: "F1 · P1 · N3" },
  { codigo: "CH-1064", componente: "Concentrado de Hemácias", tipoSanguineo: "AB-", unidades: 2, volumeMl: 450, diasParaVencer: 2, temperaturaAtual: 6.8, localizacao: "R1 · P3 · N2" },
  { codigo: "CH-1065", componente: "Concentrado de Hemácias", tipoSanguineo: "O+", unidades: 31, volumeMl: 450, diasParaVencer: 19, temperaturaAtual: 4.2, localizacao: "R1 · P1 · N1" },
  { codigo: "PQ-3016", componente: "Concentrado de Plaquetas", tipoSanguineo: "O+", unidades: 6, volumeMl: 60, diasParaVencer: 3, temperaturaAtual: 21.4, localizacao: "A2 · P1 · N1" },
  { codigo: "PL-2020", componente: "Plasma Fresco Congelado", tipoSanguineo: "O+", unidades: 18, volumeMl: 300, diasParaVencer: 120, temperaturaAtual: -19.5, localizacao: "F2 · P1 · N2" },
  { codigo: "CH-1066", componente: "Concentrado de Hemácias", tipoSanguineo: "O-", unidades: 7, volumeMl: 450, diasParaVencer: 6, temperaturaAtual: 2.9, localizacao: "R1 · P2 · N4" },
  { codigo: "PQ-3017", componente: "Concentrado de Plaquetas", tipoSanguineo: "O-", unidades: 2, volumeMl: 60, diasParaVencer: 5, temperaturaAtual: 19.2, localizacao: "A2 · P2 · N3" },
];

// Dados mockados desta primeira versão — substituídos por GET /hemocomponentes.
export const lotesEstoque: LoteHemocomponente[] = ENTRADAS.map((entrada) => (
{
  id: entrada.codigo,
  codigo: entrada.codigo,
  componente: entrada.componente,
  tipoSanguineo: entrada.tipoSanguineo,
  unidades: entrada.unidades,
  volumeMl: entrada.volumeMl,
  dataValidade: emDias(entrada.diasParaVencer),
  temperaturaAtual: entrada.temperaturaAtual,
  temperaturaIdeal: FAIXAS_IDEAIS[entrada.componente],
  localizacao: entrada.localizacao,
}));
