import { useState } from "react";
import type { DoadorDTO } from "@/types/dtos";
import { mensagemDeErro, rascunhoDoadorParaBackend, rascunhoDoPerfil } from "@/lib/doacoes";
import { atualizarMeuPerfil } from "@/services/api";
import { CamposDoador } from "@/components/donations/CamposDoador";
import { Modal } from "@/components/ui/Modal";

interface Props
{
  perfil: DoadorDTO;
  onClose: () => void;
  onSalvo: (perfil: DoadorDTO) => void;
}

/** PI3-203 — O doador corrige os próprios dados. O e-mail não muda: é o login. */
export function ModalEditarPerfil({ perfil, onClose, onSalvo }: Readonly<Props>)
{
  const [rascunho, setRascunho] = useState(() => rascunhoDoPerfil(perfil));
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function salvar(evento: React.FormEvent)
  {
    evento.preventDefault();
    setSalvando(true);
    setErro(null);
    try
    {
      onSalvo(await atualizarMeuPerfil(rascunhoDoadorParaBackend(rascunho)));
      onClose();
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
    finally
    {
      setSalvando(false);
    }
  }

  return (
    <Modal titulo="Meus dados" onClose={onClose}>
      <form onSubmit={salvar} className="space-y-4">
        <p className="text-sm text-gray-600">
          E-mail de acesso: <span className="font-semibold text-gray-900">{perfil.email ?? "—"}</span>
        </p>

        <CamposDoador valor={rascunho} onChange={setRascunho} prefixoId="perfil" />

        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
          </p>
        )}

        <button
          type="submit"
          disabled={salvando}
          className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-60"
        >
          {salvando ? "Salvando..." : "Salvar"}
        </button>
      </form>
    </Modal>
  );
}
