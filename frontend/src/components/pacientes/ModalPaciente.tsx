import { useEffect, useState } from "react";
import type { PacienteDTO } from "@/types/dtos";
import { Modal } from "@/components/ui/Modal";
import { useHospitais } from "@/hooks/useHospitais";
import { mensagemDeErro } from "@/lib/doacoes";
import { payloadDoRascunho, rascunhoDoPaciente, rascunhoVazio } from "@/lib/pacientes";
import type { RascunhoPaciente } from "@/lib/pacientes";
import { cadastrarPaciente, editarPaciente } from "@/services/api";
import { CamposPaciente } from "./CamposPaciente";

interface Props
{
  /** Paciente a editar. Sem ele, o formulário cadastra um paciente novo. */
  paciente?: PacienteDTO;
  onClose: () => void;
  /** Chamado com o paciente como a API devolveu, depois de gravado. */
  onSalvo: (paciente: PacienteDTO, novo: boolean) => void;
}

/**
 * PI3-195 — Cadastro (POST /pacientes) e edição (PUT /pacientes/{id}) de paciente. Os campos são os
 * mesmos da campanha de doação; o erro da API (400, 403, 422) aparece dentro do formulário.
 */
export function ModalPaciente({ paciente, onClose, onSalvo }: Readonly<Props>)
{
  const editando = paciente !== undefined;
  const { hospitais, erro: erroHospitais } = useHospitais();
  const [rascunho, setRascunho] = useState<RascunhoPaciente>(() => (paciente ? rascunhoDoPaciente(paciente) : rascunhoVazio()));
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  // Cadastro novo: o primeiro hospital da lista já vem escolhido.
  useEffect(() =>
  {
    setRascunho((atual) => (atual.hospitalId || hospitais.length === 0 ? atual : { ...atual, hospitalId: hospitais[0].id }));
  }, [hospitais]);

  async function aoEnviar(evento: React.FormEvent)
  {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);

    try
    {
      const dados = payloadDoRascunho(rascunho);
      const salvo = paciente ? await editarPaciente(paciente.id, dados) : await cadastrarPaciente(dados);
      onSalvo(salvo, !editando);
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
    <Modal titulo={editando ? `Editar paciente · ${paciente.nome}` : "Novo paciente"} onClose={fechar} className="max-w-2xl">
      <form onSubmit={aoEnviar} className="space-y-5">
        <CamposPaciente valor={rascunho} onChange={setRascunho} hospitais={hospitais} prefixoId="pac" />

        {(erro ?? erroHospitais) && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro ?? erroHospitais}
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
            type="submit"
            disabled={enviando}
            className="rounded-lg bg-rota-red px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Gravando..." : editando ? "Salvar alterações" : "Cadastrar paciente"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
