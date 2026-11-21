import { useState } from "react";
import { X } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import type { CategoriaDescarteBackend } from "@/types/dtos";
import { cn } from "@/lib/utilitarios";
import { CATEGORIAS_DESCARTE, diasAteVencer, formatarData, temperaturaForaDaFaixa } from "@/lib/estoque";

const LIMITE_OBSERVACAO = 500;

// Já começa no motivo que o estado da bolsa indica: vencida → validade; fora da faixa → temperatura.
function motivoSugerido(lote: LoteHemocomponente): CategoriaDescarteBackend | null
{
  if (diasAteVencer(lote) < 0) return "VALIDADE_EXPIRADA";
  if (temperaturaForaDaFaixa(lote)) return "TEMPERATURA_FORA_DO_PADRAO";
  return null;
}

/**
 * Confirmação do descarte (PI3-243, só admin): motivo padronizado e observação, obrigatória em
 * "Outro". "Validade expirada" só fica disponível para bolsa vencida, como o backend exige.
 * O erro da API aparece dentro do modal.
 */
export function ModalDescartarBolsa({ lote, onClose, onDescartar }: Readonly<{
  lote: LoteHemocomponente;
  onClose: () => void;
  onDescartar: (categoria: CategoriaDescarteBackend, observacao: string) => Promise<void>;
}>)
{
  const [categoria, setCategoria] = useState<CategoriaDescarteBackend | null>(motivoSugerido(lote));
  const [observacao, setObservacao] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const vencida = diasAteVencer(lote) < 0;
  const faltaObservacao = categoria === "OUTRO" && !observacao.trim();

  async function confirmar()
  {
    if (!categoria)
    {
      setErro("Escolha o motivo do descarte.");
      return;
    }
    if (faltaObservacao)
    {
      setErro("Descreva o motivo do descarte quando ele for \"Outro\".");
      return;
    }

    setEnviando(true);
    setErro(null);
    try
    {
      await onDescartar(categoria, observacao);
    }
    catch (falha)
    {
      setErro(falha instanceof Error && falha.message ? falha.message : "Não foi possível registrar o descarte.");
      setEnviando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-950/50 p-4">
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby={`titulo-descarte-${lote.id}`}
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-xl bg-white p-6 shadow-xl"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id={`titulo-descarte-${lote.id}`} className="text-lg font-bold text-gray-900">
              Descartar bolsa {lote.id}
            </h2>
            <p className="mt-1 text-sm text-gray-600">
              {lote.componente} {lote.tipoSanguineo} · validade {formatarData(lote.dataValidade)}. O descarte é
              definitivo: a bolsa fica registrada, mas não pode mais ser usada.
            </p>
          </div>
          <button
            type="button"
            aria-label="Fechar"
            disabled={enviando}
            onClick={onClose}
            className="rounded-md p-1 text-gray-500 hover:bg-gray-100 disabled:opacity-50"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <fieldset className="mt-5">
          <legend className="text-sm font-medium text-gray-700">Motivo do descarte</legend>
          <div className="mt-2 space-y-2">
            {CATEGORIAS_DESCARTE.map((opcao) =>
            {
              const bloqueada = opcao.valor === "VALIDADE_EXPIRADA" && !vencida;
              const id = `descarte-${lote.id}-${opcao.valor}`;
              return (
                <label
                  key={opcao.valor}
                  htmlFor={id}
                  className={cn(
                    "flex cursor-pointer items-start gap-3 rounded-lg border p-3 text-sm",
                    categoria === opcao.valor ? "border-rose-400 bg-rose-50" : "border-gray-200 hover:bg-gray-50",
                    bloqueada && "cursor-not-allowed opacity-50 hover:bg-white",
                  )}
                >
                  <input
                    id={id}
                    type="radio"
                    name={`categoria-descarte-${lote.id}`}
                    value={opcao.valor}
                    checked={categoria === opcao.valor}
                    disabled={bloqueada || enviando}
                    onChange={() => setCategoria(opcao.valor)}
                    className="mt-0.5 accent-rose-700"
                  />
                  <span>
                    <span className="font-semibold text-gray-900">{opcao.rotulo}</span>
                    <span className="block text-xs text-gray-500">
                      {bloqueada ? "A bolsa ainda está na validade." : opcao.descricao}
                    </span>
                  </span>
                </label>
              );
            })}
          </div>
        </fieldset>

        <label htmlFor={`observacao-descarte-${lote.id}`} className="mt-5 block text-sm font-medium text-gray-700">
          Observação {categoria === "OUTRO" ? "(obrigatória)" : "(opcional)"}
        </label>
        <textarea
          id={`observacao-descarte-${lote.id}`}
          value={observacao}
          onChange={(evento) => setObservacao(evento.target.value)}
          maxLength={LIMITE_OBSERVACAO}
          rows={3}
          disabled={enviando}
          className="mt-2 w-full resize-y rounded-lg border border-gray-300 p-3 text-sm text-gray-900 outline-none focus:border-rota-red focus:ring-2 focus:ring-red-100 disabled:bg-gray-50"
          placeholder="Ex.: geladeira R2 marcou 9 °C durante a madrugada"
        />
        <p className="mt-1 text-right text-xs text-gray-400">{observacao.length}/{LIMITE_OBSERVACAO}</p>

        {erro && (
          <p role="alert" className="mt-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-800">
            {erro}
          </p>
        )}

        <div className="mt-5 flex justify-end gap-3">
          <button
            type="button"
            disabled={enviando}
            onClick={onClose}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            disabled={enviando || !categoria || faltaObservacao}
            onClick={confirmar}
            className="rounded-lg bg-rose-700 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-800 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {enviando ? "Registrando..." : "Confirmar descarte"}
          </button>
        </div>
      </section>
    </div>
  );
}
