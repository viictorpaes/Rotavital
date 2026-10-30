import { useState } from "react";
import { TriangleAlert } from "lucide-react";
import type { PacienteNaFila } from "@/types";
import { Modal } from "@/components/ui/Modal";
import { mensagemDeErro } from "@/lib/doacoes";
import { removerPaciente } from "@/services/api";

interface Props
{
  paciente: PacienteNaFila;
  onClose: () => void;
  onRemovido: (paciente: PacienteNaFila) => void;
}

/**
 * PI3-195 — Confirma a remoção (DELETE /pacientes/{id}). A remoção é para cadastro feito por engano:
 * paciente com campanha, procedimento ou requisição fica, e a mensagem da API (422) aparece aqui.
 */
export function ModalRemoverPaciente({ paciente, onClose, onRemovido }: Readonly<Props>)
{
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function confirmar()
  {
    setEnviando(true);
    setErro(null);

    try
    {
      await removerPaciente(paciente.id);
      onRemovido(paciente);
    }
    catch (falha)
    {
      setErro(mensagemDeErro(falha));
      setEnviando(false);
    }
  }

  function fechar()
  {
    if (!enviando) onClose();
  }

  return (
    <Modal titulo={`Remover paciente · ${paciente.nome}`} onClose={fechar}>
      <div className="space-y-4">
        <p className="text-sm text-gray-700">
          {paciente.componente} · <span className="font-bold text-rota-red">{paciente.tipoSanguineo}</span> ·{" "}
          {paciente.hospital}
        </p>

        <p className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          <TriangleAlert className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
          O cadastro é apagado de vez. Use só para paciente lançado por engano: quem tem campanha de doação,
          procedimento ou requisição não pode ser removido.
        </p>

        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
          </p>
        )}

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={fechar}
            disabled={enviando}
            className="rounded-lg border border-rota-border bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:border-gray-400 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={confirmar}
            disabled={enviando}
            className="rounded-lg bg-rose-700 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rose-800 disabled:opacity-60"
          >
            {enviando ? "Removendo..." : "Remover paciente"}
          </button>
        </div>
      </div>
    </Modal>
  );
}
