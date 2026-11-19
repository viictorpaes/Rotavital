import type { DestinoHospitalDTO, FonteDoTrajetoBackend } from "@/types/dtos";

/** PI3-262 — Como a tela mostra a rota até o hospital: distância, tempo, horário de chegada e a fonte. */

/** "850 m" até 1 km; depois "9,4 km". */
export function formatarDistancia(km: number): string
{
  if (km < 1)
  {
    return `${Math.round(km * 1000)} m`;
  }
  return `${km.toLocaleString("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 })} km`;
}

/** "menos de 1 min", "19 min" ou "1 h 05 min". */
export function formatarDuracao(minutos: number): string
{
  if (minutos < 1)
  {
    return "menos de 1 min";
  }
  if (minutos < 60)
  {
    return `${minutos} min`;
  }
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto === 0 ? `${horas} h` : `${horas} h ${String(resto).padStart(2, "0")} min`;
}

/** Hora de chegada saindo agora, no fuso de Recife: "14:32". */
export function horarioDeChegada(minutos: number, agora: Date = new Date()): string
{
  const chegada = new Date(agora.getTime() + minutos * 60_000);
  return chegada.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit", timeZone: "America/Recife" });
}

/** "Av. Prof. Moraes Rego, 1235 — Recife/PE": cidade e UF são opcionais no cadastro do hospital. */
export function enderecoDoDestino(destino: DestinoHospitalDTO): string
{
  const local = [destino.cidade, destino.uf].filter(Boolean).join("/");
  return local ? `${destino.logradouro} — ${local}` : destino.logradouro;
}

/** De onde vieram os números, para o doador saber o quanto confiar no tempo mostrado. */
export const DESCRICAO_FONTE: Record<FonteDoTrajetoBackend, string> =
{
  GOOGLE_ROUTES: "Google Maps · pelas ruas, com o trânsito de agora",
  ESTIMATIVA: "Estimativa pela distância em linha reta · o Google Maps não respondeu",
  CONEXOES_DA_REDE: "Conexões cadastradas na rede · sem o trânsito do Google Maps",
};
