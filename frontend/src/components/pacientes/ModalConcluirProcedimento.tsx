import { useMemo, useState } from "react";
import { CheckCircle2, HeartHandshake, Package, TriangleAlert, X } from "lucide-react";
import type {
  LoteHemocomponente,
  OrigemAtendimento,
  PessoaNecessitada,
  ProcedimentoConcluido,
} from "@/types";
import { lotesCompativeis, unidadesDisponiveis } from "@/lib/pacientes";
import { diasAteVencer, formatarData } from "@/lib/estoque";
import { cn } from "@/lib/utilitarios";

interface Props
{
  paciente: PessoaNecessitada;
  lotes: LoteHemocomponente[];
  onConfirmar: (origem: OrigemAtendimento) => ProcedimentoConcluido | undefined;
  onClose: () => void;
}

/**
 * HU-08 — Conclusão do procedimento. As duas origens são mutuamente exclusivas:
 * a doação externa não altera o estoque; o estoque interno baixa as unidades
 * pela regra FEFO, consumindo antes os lotes mais próximos do vencimento.
 */
export function ModalConcluirProcedimento({ paciente, lotes, onConfirmar, onClose }: Readonly<Props>)
{
  const [origem, setOrigem] = useState<OrigemAtendimento | null>(null);
  const [recibo, setRecibo] = useState<ProcedimentoConcluido | null>(null);

  const fila = useMemo(
    () => lotesCompativeis(lotes, paciente.componente, paciente.tipoSanguineo),
    [lotes, paciente],
  );
  const disponiveis = useMemo(
    () => unidadesDisponiveis(lotes, paciente.componente, paciente.tipoSanguineo),
    [lotes, paciente],
  );
  const estoqueInsuficiente = disponiveis < paciente.unidadesNecessarias;

  function handleConfirmar()
  {
    if (!origem) return;
    setRecibo(onConfirmar(origem) ?? null);
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-rota-border bg-white p-6 shadow-card"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="titulo-concluir-procedimento"
      >
        <div className="mb-4 flex items-start justify-between gap-3">
          <div>
            <h2
              id="titulo-concluir-procedimento"
              className="font-mono text-xs font-bold uppercase tracking-widest text-gray-500"
            >
              Concluir procedimento
            </h2>
            <p className="mt-1 text-lg font-bold text-gray-900">{paciente.nome}</p>
            <p className="font-mono text-xs text-gray-500">
              {paciente.componente} ·{" "}
              <span className="font-bold text-rota-red">{paciente.tipoSanguineo}</span> ·{" "}
              {paciente.unidadesNecessarias}{" "}
              {paciente.unidadesNecessarias === 1 ? "unidade" : "unidades"}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-rota-surface2 hover:text-gray-700"
            aria-label="Fechar"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {recibo ? (
          <Recibo recibo={recibo} onClose={onClose} />
        ) : (
          <div className="space-y-4">
            <fieldset className="space-y-3">
              <legend className="sr-only">Origem do hemocomponente</legend>

              <OpcaoOrigem
                valor="doacao-externa"
                selecionada={origem}
                onSelect={setOrigem}
                icone={HeartHandshake}
                titulo="Doação externa"
                descricao="O doador comparece ao hemocentro para este paciente. O estoque não é alterado."
              />

              <OpcaoOrigem
                valor="estoque-interno"
                selecionada={origem}
                onSelect={setOrigem}
                icone={Package}
                titulo="Estoque interno"
                descricao={`Baixa ${paciente.unidadesNecessarias} un. por FEFO — o lote mais próximo do vencimento sai primeiro.`}
                aviso={
                  estoqueInsuficiente
                    ? `Apenas ${disponiveis} un. compatíveis — a diferença fica registrada como pendência.`
                    : undefined
                }
              />
            </fieldset>

            {origem === "estoque-interno" && (
              <PreviaFefo fila={fila} unidades={paciente.unidadesNecessarias} />
            )}

            <button
              type="button"
              onClick={handleConfirmar}
              disabled={!origem}
              className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:bg-gray-300"
            >
              Confirmar conclusão
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

interface PropsOpcao
{
  valor: OrigemAtendimento;
  selecionada: OrigemAtendimento | null;
  onSelect: (valor: OrigemAtendimento) => void;
  icone: typeof Package;
  titulo: string;
  descricao: string;
  aviso?: string;
}

function OpcaoOrigem({
  valor,
  selecionada,
  onSelect,
  icone: Icone,
  titulo,
  descricao,
  aviso,
}: Readonly<PropsOpcao>)
{
  const ativa = selecionada === valor;

  return (
    <label
      className={cn(
        "flex cursor-pointer gap-3 rounded-xl border p-4 transition-colors",
        ativa ? "border-rota-red bg-red-50/60" : "border-rota-border hover:bg-rota-surface2",
      )}
    >
      <input
        type="radio"
        name="origem-procedimento"
        value={valor}
        checked={ativa}
        onChange={() => onSelect(valor)}
        className="mt-1 h-4 w-4 shrink-0 accent-rota-red"
      />
      <div className="min-w-0">
        <p className="flex items-center gap-2 text-sm font-bold text-gray-900">
          <Icone className="h-4 w-4 text-rota-red" />
          {titulo}
        </p>
        <p className="mt-1 text-sm text-gray-600">{descricao}</p>
        {aviso && (
          <p className="mt-1.5 flex items-start gap-1.5 text-xs font-medium text-amber-600">
            <TriangleAlert className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            {aviso}
          </p>
        )}
      </div>
    </label>
  );
}

/** Mostra de quais lotes sairão as unidades antes de o médico confirmar. */
function PreviaFefo({ fila, unidades }: Readonly<{ fila: LoteHemocomponente[]; unidades: number }>)
{
  let pendente = unidades;
  const previa = fila
    .map((lote) =>
    {
      const retirada = Math.min(lote.unidades, pendente);
      pendente -= retirada;
      return { lote, retirada };
    })
    .filter((item) => item.retirada > 0);

  if (previa.length === 0)
  {
    return (
      <p className="rounded-lg border border-rota-border bg-rota-surface2 p-3 text-sm text-gray-500">
        Nenhum lote compatível disponível no estoque.
      </p>
    );
  }

  return (
    <div className="rounded-lg border border-rota-border bg-rota-surface2 p-3">
      <p className="font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-400">
        Baixa prevista · FEFO
      </p>
      <ul className="mt-2 space-y-1.5">
        {previa.map(({ lote, retirada }) => (
          <li key={lote.id} className="flex items-center justify-between gap-3 text-sm">
            <span className="font-mono text-gray-700">{lote.codigo}</span>
            <span className="text-xs text-gray-500">
              vence {formatarData(lote.dataValidade)} · {diasAteVencer(lote)} d
            </span>
            <span className="font-semibold text-gray-900">−{retirada} un.</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

function Recibo({ recibo, onClose }: Readonly<{ recibo: ProcedimentoConcluido; onClose: () => void }>)
{
  return (
    <div className="space-y-4 rounded-xl border border-emerald-200 bg-emerald-50 p-4">
      <p className="flex items-center gap-2 text-base font-bold text-emerald-700">
        <CheckCircle2 className="h-5 w-5" />
        Procedimento concluído · {recibo.protocolo}
      </p>

      <p className="text-sm text-emerald-900">
        {recibo.origem === "doacao-externa"
          ? "Atendido por doação externa — o estoque permanece inalterado."
          : `Baixa de ${recibo.unidadesBaixadas} un. no estoque pela regra FEFO.`}
      </p>

      {recibo.consumos.length > 0 && (
        <ul className="space-y-1 font-mono text-xs text-emerald-900">
          {recibo.consumos.map((consumo) => (
            <li key={consumo.codigo}>
              {consumo.codigo} · −{consumo.unidades} un.
            </li>
          ))}
        </ul>
      )}

      {recibo.unidadesFaltantes > 0 && (
        <p className="flex items-start gap-1.5 text-xs font-medium text-amber-700">
          <TriangleAlert className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          {recibo.unidadesFaltantes} un. ficaram pendentes por falta de estoque compatível.
        </p>
      )}

      <button
        type="button"
        onClick={onClose}
        className="w-full rounded-lg bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-emerald-700"
      >
        Fechar
      </button>
    </div>
  );
}
