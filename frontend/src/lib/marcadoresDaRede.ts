import type { TipoPontoRedeBackend } from "@/types/dtos";

/** Papel do ponto na rota mostrada: muda a cor e o tamanho do marcador. */
export type DestaqueDoPonto = "origem" | "destino" | "parada" | "normal";

const GOTA = '<path d="M12 2.5s-6.5 7.4-6.5 11.5a6.5 6.5 0 0 0 13 0C18.5 9.9 12 2.5 12 2.5z" fill="#fff"/>';
const CRUZ = '<path d="M10 4.5h4V10h5.5v4H14v5.5h-4V14H4.5v-4H10z" fill="#fff"/>';

export const COR_DO_DESTAQUE: Record<DestaqueDoPonto, string> =
{
  origem: "#c1272d",
  destino: "#111827",
  parada: "#d97706",
  normal: "#6b7280",
};

/**
 * Marcador da rede (PI3-258) em HTML, para sair igual no Google Maps e no OpenStreetMap:
 * hemocentro é um quadrado vermelho com uma gota; hospital, um círculo com uma cruz, na cor do
 * papel dele na rota (PI3-259).
 */
export function marcadorDoPonto(tipo: TipoPontoRedeBackend, destaque: DestaqueDoPonto)
{
  const hemocentro = tipo === "BANCO_DE_SANGUE";
  const tamanho = hemocentro ? 30 : destaque === "normal" ? 22 : 28;
  const fundo = hemocentro ? COR_DO_DESTAQUE.origem : COR_DO_DESTAQUE[destaque];
  const icone = Math.round(tamanho * 0.62);

  return {
    tamanho,
    html:
      `<div style="width:${tamanho}px;height:${tamanho}px;background:${fundo};border:2px solid #fff;` +
      `border-radius:${hemocentro ? "6px" : "50%"};box-shadow:0 1px 6px rgba(0,0,0,.35);cursor:pointer;` +
      `display:flex;align-items:center;justify-content:center">` +
      `<svg viewBox="0 0 24 24" width="${icone}" height="${icone}" aria-hidden="true">${hemocentro ? GOTA : CRUZ}</svg>` +
      `</div>`,
  };
}
