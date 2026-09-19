import type { RequisicaoRecebida } from "@/types";

/**
 * Remessas chegando de outras instituições, ainda sem conferência física (HU-05).
 * Mock desta primeira versão — substituído por GET /requisicoes?status=em-transito.
 */
export const requisicoesRecebidas: RequisicaoRecebida[] =
[
  {
    id: "INC-001",
    componente: "Concentrado de Hemácias",
    tipoSanguineo: "O-",
    unidades: 8,
    origem: "HEMORIO — Intercâmbio Nordeste",
    chegadaEm: "Hoje · 08:30",
  },
  {
    id: "INC-002",
    componente: "Plasma Fresco Congelado",
    tipoSanguineo: "A+",
    unidades: 12,
    origem: "Hemocentro de Caruaru",
    chegadaEm: "Hoje · 10:15",
  },
  {
    id: "INC-003",
    componente: "Concentrado de Plaquetas",
    tipoSanguineo: "AB+",
    unidades: 4,
    origem: "Doação coletiva — Nordeste Energia S.A.",
    chegadaEm: "Hoje · 11:00",
  },
  {
    id: "INC-004",
    componente: "Crioprecipitado",
    tipoSanguineo: "O-",
    unidades: 2,
    origem: "HEMOAL — Intercâmbio PE/AL",
    chegadaEm: "Ontem · 16:45",
  },
];
