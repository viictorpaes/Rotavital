import { useState } from "react";
import type { FormEvent } from "react";
import { TriangleAlert } from "lucide-react";
import type { LoteHemocomponente } from "@/types";
import type { BolsaHemocomponenteDTO } from "@/types/dtos";
import { formatarData } from "@/lib/estoque";
import { mensagemDoErro } from "@/lib/erros";
import { atualizarHemocomponente } from "@/services/api";
import { AlertaErroFormulario } from "./AlertaErroFormulario";
import { BotoesFormulario } from "./BotoesFormulario";
import { JanelaModal } from "./JanelaModal";

interface Props
{
  lote: LoteHemocomponente;
  onClose: () => void;
  /** Chamado com a bolsa já DESCARTADA, como a API devolveu. */
  onDescartada: (bolsa: BolsaHemocomponenteDTO) => void;
}

/**
 * PI3-190 — Confirmação do descarte: PATCH /api/v1/hemocomponentes/{id} com status DESCARTADA. O
 * descarte não se desfaz (a API não aceita mais alterações na bolsa), por isso pede confirmação.
 */
export function ModalDescartarBolsa({ lote, onClose, onDescartada }: Readonly<Props>)
{
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function aoConfirmar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);

    try
    {
      onDescartada(await atualizarHemocomponente(lote.id, { status: "DESCARTADA" }));
    }
    catch (falha: unknown)
    {
      setErro(mensagemDoErro(falha, "Não foi possível conectar com o back-end. A bolsa não foi descartada."));
      setEnviando(false);
    }
  }

  return (
    <JanelaModal
      idTitulo="titulo-descartar-bolsa"
      titulo={`Descartar bolsa ${lote.id}`}
      larguraMaxima="max-w-md"
      bloqueada={enviando}
      onClose={onClose}
    >
      <form onSubmit={aoConfirmar} className="space-y-4">
        <p className="text-sm text-gray-700">
          <span className="font-bold text-gray-900">{lote.componente}</span> ·{" "}
          <span className="font-bold text-rota-red">{lote.tipoSanguineo}</span> · lote {lote.codigo} · validade{" "}
          {formatarData(lote.dataValidade)}
        </p>

        <p className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          <TriangleAlert className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
          A bolsa sai do estoque e não pode voltar: depois do descarte, ela não aceita mais edição nem reserva.
        </p>

        {erro && <AlertaErroFormulario mensagem={erro} />}

        <BotoesFormulario
          enviando={enviando}
          rotulo="Descartar bolsa"
          rotuloEnviando="Descartando..."
          variante="perigo"
          onCancelar={onClose}
        />
      </form>
    </JanelaModal>
  );
}
