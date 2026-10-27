import { useState } from "react";
import type { CampanhaDTO } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import { mensagemDeErro } from "@/lib/doacoes";
import { encerrarCampanha } from "@/services/api";
import { Modal } from "@/components/ui/Modal";

interface Props
{
  campanha: CampanhaDTO;
  onClose: () => void;
}

/** Confirma o encerramento: a campanha sai do portal e os agendamentos em aberto são cancelados. */
export function ModalEncerrarCampanha({ campanha, onClose }: Readonly<Props>)
{
  const { recarregarCampanhas } = useDados();
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function confirmar()
  {
    setEnviando(true);
    setErro(null);
    try
    {
      await encerrarCampanha(campanha.id);
      await recarregarCampanhas();
      onClose();
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
      setEnviando(false);
    }
  }

  const emAberto = campanha.agendamentosEmAberto;

  return (
    <Modal titulo="Encerrar campanha" onClose={onClose}>
      <div className="space-y-4">
        <p className="text-sm text-gray-700">
          A campanha de <span className="font-bold text-gray-900">{campanha.paciente.nome}</span> deixa de
          aparecer para os doadores e não poderá mais ser editada.
        </p>
        {emAberto > 0 && (
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
            {emAberto === 1
              ? "1 agendamento em aberto será cancelado."
              : `${emAberto} agendamentos em aberto serão cancelados.`}
          </p>
        )}
        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
          </p>
        )}
        <div className="flex gap-3">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 rounded-lg border border-rota-border px-4 py-2.5 text-sm font-medium text-gray-700 hover:border-gray-400"
          >
            Voltar
          </button>
          <button
            type="button"
            onClick={confirmar}
            disabled={enviando}
            className="flex-1 rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white hover:bg-rota-redDark disabled:opacity-60"
          >
            {enviando ? "Encerrando..." : "Encerrar campanha"}
          </button>
        </div>
      </div>
    </Modal>
  );
}
