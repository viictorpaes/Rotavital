import type { LoteHemocomponente, TipoComponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import type {
  BolsaHemocomponenteDTO,
  StatusBolsaBackend,
  TipoComponenteBackend,
  TipoSanguineoBackend,
} from "@/types/dtos";

export const TIPOS_SANGUINEOS: TipoSanguineo[] = 
["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];

/** Dias restantes até a validade (negativo quando o lote já venceu). */
export function diasAteVencer(lote: LoteHemocomponente)
{
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const validade = new Date(`${lote.dataValidade}T00:00:00`);
  return Math.round((validade.getTime() - hoje.getTime()) / 86_400_000);
}

/** Temperatura do lote fora da faixa ideal do hemocomponente (HU-03). */
export function temperaturaForaDaFaixa(lote: LoteHemocomponente)
{
  return(
    lote.temperaturaAtual < lote.temperaturaIdeal.minima ||
    lote.temperaturaAtual > lote.temperaturaIdeal.maxima
  );
}

/** Risco do lote: temperatura fora da faixa ou validade próxima. */
export function statusLote(lote: LoteHemocomponente): UrgenciaNecessidade
{
  const dias = diasAteVencer(lote);
  if (temperaturaForaDaFaixa(lote) || dias <= 3) return "critico";
  
  if (dias <= 15) 
  {
    return "atencao";
  }
  return "estavel";
}

const PESO_STATUS: Record<UrgenciaNecessidade, number> = { critico: 2, atencao: 1, estavel: 0 };

/** Status do tipo sanguíneo = pior status entre seus lotes (HU-03). */
export function statusGrupo(lotes: LoteHemocomponente[]): UrgenciaNecessidade
{
  return lotes.reduce<UrgenciaNecessidade>((pior, lote) =>
  {
    const atual = statusLote(lote);
    return PESO_STATUS[atual] > PESO_STATUS[pior] ? atual : pior;
  }, "estavel");
}

export function formatarData(iso: string)
{
  return new Date(`${iso}T00:00:00`).toLocaleDateString("pt-BR");
}

export function formatarTemperatura(valor: number)
{
  return `${valor.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}°C`;
}

export function converterTipoSanguineo(tipo: string): TipoSanguineo
{
  switch (tipo)
  {
    case "A_POSITIVO":
      return "A+";
    case "A_NEGATIVO":
      return "A-";
    case "B_POSITIVO":
      return "B+";
    case "B_NEGATIVO":
      return "B-";
    case "AB_POSITIVO":
      return "AB+";
    case "AB_NEGATIVO":
      return "AB-";
    case "O_POSITIVO":
      return "O+";
    case "O_NEGATIVO":
      return "O-";
    default:
      return (tipo as TipoSanguineo) || "O+";
  }
}

export function converterTipoComponente(tipo: string): TipoComponente
{
  switch (tipo)
  {
    case "HEMACIAS":
      return "Concentrado de Hemácias";
    case "PLASMA":
      return "Plasma Fresco Congelado";
    case "PLAQUETAS":
      return "Concentrado de Plaquetas";
    case "CRIOPRECIPITADO":
      return "Crioprecipitado";
    default:
      return (tipo as TipoComponente) || "Concentrado de Hemácias";
  }
}

export function obterFaixaTemperatura(tipo: string): { minima: number; maxima: number }
{
  switch (tipo)
  {
    case "HEMACIAS":
    case "Concentrado de Hemácias":
      return { minima: 2.0, maxima: 6.0 };
    case "PLASMA":
    case "Plasma Fresco Congelado":
      return { minima: -30.0, maxima: -18.0 };
    case "PLAQUETAS":
    case "Concentrado de Plaquetas":
      return { minima: 20.0, maxima: 24.0 };
    case "CRIOPRECIPITADO":
    case "Crioprecipitado":
      return { minima: -30.0, maxima: -18.0 };
    default:
      return { minima: 2.0, maxima: 6.0 };
  }
}

export function converterBolsaParaLote(bolsa: BolsaHemocomponenteDTO): LoteHemocomponente
{
  const componente = converterTipoComponente(bolsa.tipoComponente);
  const tipoSanguineo = converterTipoSanguineo(bolsa.tipoSanguineo);

  return {
    id: bolsa.id,
    codigo: bolsa.loteSintetico || bolsa.id,
    componente,
    tipoSanguineo,
    unidades: 1,
    volumeMl: bolsa.volumeMl,
    dataValidade: bolsa.dataValidade,
    temperaturaAtual: bolsa.temperaturaCelsius ?? bolsa.temperaturaAtual ?? 4.0,
    temperaturaIdeal: obterFaixaTemperatura(bolsa.tipoComponente),
    localizacao: bolsa.localizacao || bolsa.localizacaoFisica || "Câmara Fria",
    statusBolsa: bolsa.status,
    dataColeta: bolsa.dataColeta,
  };
}

const TIPO_SANGUINEO_BACKEND: Record<TipoSanguineo, TipoSanguineoBackend> =
{
  "A+": "A_POSITIVO",
  "A-": "A_NEGATIVO",
  "B+": "B_POSITIVO",
  "B-": "B_NEGATIVO",
  "AB+": "AB_POSITIVO",
  "AB-": "AB_NEGATIVO",
  "O+": "O_POSITIVO",
  "O-": "O_NEGATIVO",
};

/** "O-" → "O_NEGATIVO": o caminho inverso de converterTipoSanguineo, para os filtros da API. */
export function paraTipoSanguineoBackend(tipo: TipoSanguineo): TipoSanguineoBackend
{
  return TIPO_SANGUINEO_BACKEND[tipo];
}

/** Componentes que o filtro da tela de Estoque oferece, com o rótulo usado nos cartões. */
export const COMPONENTES_FILTRO: Array<{ valor: TipoComponenteBackend; rotulo: string }> =
(["HEMACIAS", "PLASMA", "PLAQUETAS", "CRIOPRECIPITADO"] as const).map((valor) => (
  { valor, rotulo: converterTipoComponente(valor) }
));

const ROTULOS_STATUS_BOLSA: Record<StatusBolsaBackend, string> =
{
  DISPONIVEL: "Disponível",
  RESERVADA: "Reservada",
  EM_TRANSITO: "Em trânsito",
  ENTREGUE: "Entregue",
  DESCARTADA: "Descartada",
  UTILIZADA: "Utilizada",
};

/** Status que o filtro da tela de Estoque oferece, na ordem do ciclo de vida da bolsa. */
export const STATUS_FILTRO: Array<{ valor: StatusBolsaBackend; rotulo: string }> =
(Object.keys(ROTULOS_STATUS_BOLSA) as StatusBolsaBackend[]).map((valor) => (
  { valor, rotulo: ROTULOS_STATUS_BOLSA[valor] }
));

/** "EM_TRANSITO" → "Em trânsito"; devolve o próprio valor se o backend mandar um status novo. */
export function rotuloStatusBolsa(status: string): string
{
  return ROTULOS_STATUS_BOLSA[status as StatusBolsaBackend] ?? status;
}
/** Validade máxima de cada componente, em dias depois da coleta (TipoComponente no backend). */
export const PRAZO_MAXIMO_DIAS: Record<TipoComponenteBackend, number> =
{
  HEMACIAS: 42,
  PLASMA: 730,
  PLAQUETAS: 7,
  CRIOPRECIPITADO: 365,
};

/** Data de hoje no fuso do navegador, no formato do input date (AAAA-MM-DD). */
export function hojeIso(): string
{
  const hoje = new Date();
  const mes = String(hoje.getMonth() + 1).padStart(2, "0");
  const dia = String(hoje.getDate()).padStart(2, "0");
  return `${hoje.getFullYear()}-${mes}-${dia}`;
}

/** "2026-10-16" + 42 → "2026-11-27". Feito em UTC para o horário de verão não pular um dia. */
export function somarDias(iso: string, dias: number): string
{
  const data = new Date(`${iso}T00:00:00Z`);
  data.setUTCDate(data.getUTCDate() + dias);
  return data.toISOString().slice(0, 10);
}

/** Validade sugerida para uma bolsa nova: o prazo máximo do componente, contado da coleta. */
export function sugerirValidade(componente: TipoComponenteBackend, dataColeta: string): string
{
  return dataColeta ? somarDias(dataColeta, PRAZO_MAXIMO_DIAS[componente]) : "";
}

/** Temperatura inicial do formulário: o meio da faixa ideal do componente (hemácias → 4 °C). */
export function temperaturaSugerida(componente: TipoComponenteBackend): number
{
  const { minima, maxima } = obterFaixaTemperatura(componente);
  return (minima + maxima) / 2;
}

/** Status em que a bolsa ainda está no estoque e aceita edição, reserva e descarte (PI3-190). */
export function bolsaEditavel(lote: LoteHemocomponente): boolean
{
  return lote.statusBolsa === "DISPONIVEL" || lote.statusBolsa === "RESERVADA";
}

/** "Concentrado de Hemácias" → "HEMACIAS": o rótulo da tela de volta para o enum da API. */
export function paraTipoComponenteBackend(componente: TipoComponente): TipoComponenteBackend
{
  return COMPONENTES_FILTRO.find((opcao) => opcao.rotulo === componente)?.valor ?? "HEMACIAS";
}

/** Texto de um input numérico → número; "" vira null e a vírgula decimal é aceita. */
export function lerNumero(valor: string): number | null
{
  const normalizado = valor.trim().replace(",", ".");
  return normalizado === "" ? null : Number(normalizado);
}
