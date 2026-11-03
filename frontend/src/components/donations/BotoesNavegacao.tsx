import { ExternalLink, Navigation } from "lucide-react";
import { linkGoogleMaps, linkWaze, type PontoGeografico } from "@/lib/navegacao";
import { cn } from "@/lib/utilitarios";

interface Props
{
  /** Coordenadas cadastradas do hospital do paciente. */
  destino: PontoGeografico;
  /** Nome do hospital, para o leitor de tela dizer para onde o link leva. */
  nomeDestino: string;
  /** Posição que o doador liberou; sem ela, o Google Maps sai da localização do aparelho. */
  origem?: PontoGeografico | null;
  className?: string;
}

/**
 * PI3-263 — Botões que abrem a navegação GPS até o hospital no Google Maps ou no Waze.
 * No celular com o app instalado, o sistema abre o app; sem ele, e no computador, abre o site.
 */
export function BotoesNavegacao({ destino, nomeDestino, origem, className }: Readonly<Props>)
{
  return (
    <div className={cn("space-y-2", className)}>
      <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
        <a
          href={linkGoogleMaps(destino, origem)}
          target="_blank"
          rel="noopener noreferrer"
          aria-label={`Abrir a rota até ${nomeDestino} no Google Maps`}
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
        >
          <Navigation className="h-4 w-4" fill="currentColor" />
          Abrir no Google Maps
        </a>
        <a
          href={linkWaze(destino)}
          target="_blank"
          rel="noopener noreferrer"
          aria-label={`Abrir a rota até ${nomeDestino} no Waze`}
          className="inline-flex items-center justify-center gap-2 rounded-lg border border-sky-300 bg-sky-50 px-4 py-2.5 text-sm font-semibold text-sky-800 transition-colors hover:bg-sky-100"
        >
          <ExternalLink className="h-4 w-4" />
          Abrir no Waze
        </a>
      </div>
      <p className="text-center text-xs text-gray-400">
        No celular, abre o app instalado; no computador, abre o site.
      </p>
    </div>
  );
}
