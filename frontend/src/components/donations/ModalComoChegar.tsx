import { Clock, Hospital, Loader2, LocateFixed, MapPin, Route } from "lucide-react";
import { useGeolocalizacao } from "@/hooks/useGeolocalizacao";
import { useRotaAteHospital } from "@/hooks/useRotaAteHospital";
import { formatarPrecisao } from "@/lib/geolocalizacao";
import {
  DESCRICAO_FONTE,
  enderecoDoDestino,
  formatarDistancia,
  formatarDuracao,
  horarioDeChegada,
} from "@/lib/rotaAteHospital";
import { cn } from "@/lib/utilitarios";
import type { TrajetoDTO } from "@/types/dtos";
import { BotoesNavegacao } from "@/components/donations/BotoesNavegacao";
import { Modal } from "@/components/ui/Modal";

interface Props
{
  campanhaId: string;
  nomePaciente: string;
  /** Mostrado enquanto o endereço completo do hospital chega do backend. */
  nomeHospital: string;
  onClose: () => void;
}

/**
 * HU-10 · PI3-237 — "Como chegar": rota do doador até o hospital onde está o paciente da campanha.
 *
 * 1. O destino (coordenadas cadastradas do hospital) vem do backend assim que o modal abre.
 * 2. Com o consentimento do doador, o navegador informa onde ele está (PI3-261).
 * 3. O backend calcula distância e tempo pelo Google, com o trânsito de agora (PI3-262).
 * 4. Os botões abrem a navegação no Google Maps ou no Waze (PI3-263) — mesmo sem a localização.
 */
export function ModalComoChegar({ campanhaId, nomePaciente, nomeHospital, onClose }: Readonly<Props>)
{
  const geolocalizacao = useGeolocalizacao({ pedirSeJaPermitida: true });
  const { posicao } = geolocalizacao;
  const { rota, situacao, erro } = useRotaAteHospital(campanhaId, posicao);

  const destino = rota?.destino;
  const trajeto = posicao && situacao === "pronto" ? rota?.trajeto : null;

  return (
    <Modal titulo="Como chegar" onClose={onClose} className="max-w-lg">
      <div className="space-y-4">
        <div className="flex items-start gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-rota-surface2">
            <Hospital className="h-5 w-5 text-rota-red" />
          </div>
          <div className="min-w-0">
            <p className="text-xs text-gray-500">Doação para {nomePaciente}</p>
            <p className="font-bold text-gray-900">{destino?.nome ?? nomeHospital}</p>
            {destino ? (
              <>
                <p className="text-sm text-gray-600">{enderecoDoDestino(destino)}</p>
                <p className="mt-0.5 font-mono text-[11px] text-gray-400" title="Coordenadas cadastradas do hospital">
                  {destino.latitude}, {destino.longitude}
                </p>
              </>
            ) : situacao === "carregando" ? (
              <p className="text-sm text-gray-400">Carregando o endereço…</p>
            ) : null}
          </div>
        </div>

        <PainelTrajeto
          geolocalizacao={geolocalizacao}
          calculando={posicao !== null && situacao === "carregando"}
          trajeto={trajeto ?? null}
        />

        {erro && (
          <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            Não foi possível calcular a rota: {erro}
          </p>
        )}

        {destino && (
          <BotoesNavegacao
            destino={destino}
            nomeDestino={destino.nome}
            origem={trajeto ? posicao : null}
          />
        )}
      </div>
    </Modal>
  );
}

interface PropsPainel
{
  geolocalizacao: ReturnType<typeof useGeolocalizacao>;
  calculando: boolean;
  trajeto: TrajetoDTO | null;
}

/** Distância e tempo, ou o passo que falta para tê-los (consentimento, GPS, cálculo). */
function PainelTrajeto({ geolocalizacao, calculando, trajeto }: Readonly<PropsPainel>)
{
  const { situacao, posicao, erro, solicitar } = geolocalizacao;

  if (trajeto && posicao)
  {
    return (
      <div className="rounded-xl border border-rota-border bg-rota-bg p-4">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <p className="flex items-center gap-1.5 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              <Route className="h-3.5 w-3.5" /> Distância
            </p>
            <p className="text-2xl font-extrabold text-gray-900">{formatarDistancia(trajeto.distanciaKm)}</p>
          </div>
          <div>
            <p className="flex items-center gap-1.5 font-mono text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              <Clock className="h-3.5 w-3.5" /> Tempo de carro
            </p>
            <p className="text-2xl font-extrabold text-gray-900">{formatarDuracao(trajeto.duracaoMinutos)}</p>
          </div>
        </div>
        <p className="mt-2 text-sm text-gray-600">
          Saindo agora, chega por volta das <strong>{horarioDeChegada(trajeto.duracaoMinutos)}</strong>.
        </p>
        <p
          className={cn(
            "mt-2 text-xs",
            trajeto.fonte === "GOOGLE_ROUTES" ? "text-emerald-700" : "text-amber-700",
          )}
        >
          {DESCRICAO_FONTE[trajeto.fonte]} · sua posição {formatarPrecisao(posicao.precisaoMetros)}
        </p>
      </div>
    );
  }

  if (situacao === "localizando" || calculando)
  {
    return (
      <p className="flex items-center gap-2 rounded-xl border border-rota-border bg-rota-bg p-4 text-sm text-gray-600">
        <Loader2 className="h-4 w-4 animate-spin" />
        {situacao === "localizando" ? "Procurando a sua localização…" : "Calculando a rota pelo Google Maps…"}
      </p>
    );
  }

  if (situacao === "negado" || situacao === "indisponivel")
  {
    return (
      <div className="space-y-2 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
        <p>{erro}</p>
        <p>Sem a localização, os botões abaixo abrem a rota a partir de onde o seu celular estiver.</p>
        <button
          type="button"
          onClick={solicitar}
          className="font-semibold underline underline-offset-2 hover:text-amber-900"
        >
          Tentar de novo
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-3 rounded-xl border border-rota-border bg-rota-bg p-4">
      <p className="flex items-start gap-2 text-sm text-gray-600">
        <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-gray-400" />
        Para mostrar a distância e o tempo até o hospital, o Rota Vital usa a sua localização só nesta consulta.
        Ela não fica gravada.
      </p>
      <button
        type="button"
        onClick={solicitar}
        className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-gray-900 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-gray-700"
      >
        <LocateFixed className="h-4 w-4" />
        Usar minha localização
      </button>
    </div>
  );
}
