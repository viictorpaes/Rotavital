import { Ban, X } from "lucide-react";
import type { BolsaHemocomponenteDTO } from "@/types/dtos";
import { cn } from "@/lib/utilitarios";
import {
  converterTipoComponente,
  converterTipoSanguineo,
  formatarData,
  formatarDataHora,
  formatarTemperatura,
  obterFaixaTemperatura,
  rotuloCategoriaDescarte,
  rotuloStatusBolsa,
} from "@/lib/estoque";

function Campo({ rotulo, children, destaque }: Readonly<{ rotulo: string; children: React.ReactNode; destaque?: boolean }>)
{
  return (
    <div className="bg-white p-3">
      <dt className="font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">{rotulo}</dt>
      <dd className={cn("mt-1 font-mono text-sm", destaque ? "font-semibold text-red-700" : "text-gray-900")}>{children}</dd>
    </div>
  );
}

/**
 * Detalhes de uma bolsa em modo consulta (PI3-243). Bolsa descartada ganha o selo
 * "INUTILIZÁVEL / DESCARTADA" e o motivo, a observação e a data do descarte. Não há ações
 * aqui: nada de reservar, usar, transferir ou mover; a bolsa descartada não volta ao uso.
 */
export function ModalDetalhesBolsa({ bolsa, onClose }: Readonly<{
  bolsa: BolsaHemocomponenteDTO;
  onClose: () => void;
}>)
{
  const descartada = bolsa.status === "DESCARTADA";
  const faixa = obterFaixaTemperatura(bolsa.tipoComponente);
  const temperatura = bolsa.temperaturaCelsius ?? bolsa.temperaturaAtual;
  const foraDaFaixa = bolsa.foraDaFaixa ?? false;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-950/50 p-4">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby={`titulo-detalhes-${bolsa.id}`}
        className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-xl bg-white p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <p className="font-mono text-xs uppercase tracking-widest text-gray-400">Bolsa {bolsa.id}</p>
            <h2 id={`titulo-detalhes-${bolsa.id}`} className="mt-1 text-xl font-bold text-gray-900">
              {converterTipoComponente(bolsa.tipoComponente)}{" "}
              <span className="text-rota-red">{converterTipoSanguineo(bolsa.tipoSanguineo)}</span>
            </h2>
          </div>
          <button
            type="button"
            aria-label="Fechar"
            onClick={onClose}
            className="rounded-md p-1 text-gray-500 hover:bg-gray-100"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {descartada && (
          <div className="mt-4 flex items-start gap-3 rounded-lg border border-red-300 bg-red-50 p-4">
            <Ban className="mt-0.5 h-5 w-5 shrink-0 text-red-700" />
            <div className="text-sm text-red-900">
              <p className="font-mono text-xs font-bold uppercase tracking-widest text-red-700">Inutilizável / Descartada</p>
              <p className="mt-1">
                Esta bolsa não pode ser reservada, usada em procedimento, transferida nem sugerida para pacientes.
              </p>
            </div>
          </div>
        )}

        <dl className="mt-4 grid grid-cols-1 gap-px overflow-hidden rounded-lg border border-rota-border bg-rota-border sm:grid-cols-2">
          <Campo rotulo="Código">{bolsa.id}</Campo>
          <Campo rotulo="Lote">{bolsa.loteSintetico ?? "—"}</Campo>
          <Campo rotulo="Tipo ABO/Rh">{converterTipoSanguineo(bolsa.tipoSanguineo)}</Campo>
          <Campo rotulo="Hemocomponente">{converterTipoComponente(bolsa.tipoComponente)}</Campo>
          <Campo rotulo="Volume">{bolsa.volumeMl} mL</Campo>
          <Campo rotulo="Status">{rotuloStatusBolsa(bolsa.status)}</Campo>
          <Campo rotulo="Coleta">{bolsa.dataColeta ? formatarData(bolsa.dataColeta) : "—"}</Campo>
          <Campo rotulo="Validade">{formatarData(bolsa.dataValidade)}</Campo>
          <Campo rotulo="Temperatura registrada" destaque={foraDaFaixa}>
            {temperatura === undefined ? "—" : formatarTemperatura(temperatura)}
            <span className="ml-1 text-xs font-normal text-gray-400">
              ideal {formatarTemperatura(faixa.minima)}…{formatarTemperatura(faixa.maxima)}
            </span>
          </Campo>
          <Campo rotulo="Localização">{bolsa.localizacao || bolsa.localizacaoFisica || "—"}</Campo>
          <Campo rotulo="Banco de origem">{bolsa.bancoOrigemId ?? "—"}</Campo>
          <Campo rotulo="Hospital atual">{bolsa.hospitalAtualId ?? "—"}</Campo>
        </dl>

        {descartada && (
          <dl className="mt-4 grid grid-cols-1 gap-px overflow-hidden rounded-lg border border-red-200 bg-red-200 sm:grid-cols-2">
            <Campo rotulo="Motivo do descarte" destaque>
              {bolsa.categoriaDescarte ? rotuloCategoriaDescarte(bolsa.categoriaDescarte) : "Não informado"}
            </Campo>
            <Campo rotulo="Data do descarte">
              {bolsa.dataDescarte ? formatarDataHora(bolsa.dataDescarte) : "Não registrada"}
            </Campo>
            <div className="bg-white p-3 sm:col-span-2">
              <dt className="font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">Observação</dt>
              <dd className="mt-1 text-sm text-gray-900">{bolsa.motivoDescarte || "Sem observação."}</dd>
            </div>
          </dl>
        )}

        <div className="mt-5 flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
          >
            Fechar
          </button>
        </div>
      </section>
    </div>
  );
}
