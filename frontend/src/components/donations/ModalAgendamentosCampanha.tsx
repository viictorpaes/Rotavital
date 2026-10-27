import { useCallback, useEffect, useState } from "react";
import type { AgendamentoDTO, CampanhaDTO, StatusAgendamentoBackend } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import { converterTipoSanguineo } from "@/lib/estoque";
import { ROTULO_STATUS_AGENDAMENTO, formatarHorario, mensagemDeErro } from "@/lib/doacoes";
import { listarAgendamentosDaCampanha, mudarStatusAgendamento } from "@/services/api";
import { Modal } from "@/components/ui/Modal";
import { cn } from "@/lib/utilitarios";

const CLASSES_STATUS: Record<StatusAgendamentoBackend, string> =
{
  AGENDADO: "bg-sky-50 text-sky-700",
  REALIZADO: "bg-emerald-50 text-emerald-700",
  CANCELADO: "bg-gray-100 text-gray-500",
  FALTOU: "bg-amber-50 text-amber-700",
};

interface Props
{
  campanha: CampanhaDTO;
  /** Médico: registra doação feita, falta ou cancelamento. O admin só acompanha. */
  podeGerenciar: boolean;
  onClose: () => void;
}

/** Agendamentos de doação de uma campanha (PI3-192), do horário mais próximo para o mais distante. */
export function ModalAgendamentosCampanha({ campanha, podeGerenciar, onClose }: Readonly<Props>)
{
  const { recarregarCampanhas } = useDados();
  const [agendamentos, setAgendamentos] = useState<AgendamentoDTO[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [alterando, setAlterando] = useState<string | null>(null);

  const carregar = useCallback(async () =>
  {
    try
    {
      setAgendamentos(await listarAgendamentosDaCampanha(campanha.id));
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
  }, [campanha.id]);

  useEffect(() =>
  {
    carregar();
  }, [carregar]);

  async function mudarStatus(agendamento: AgendamentoDTO, status: Exclude<StatusAgendamentoBackend, "AGENDADO">)
  {
    setAlterando(agendamento.id);
    setErro(null);
    try
    {
      const atualizado = await mudarStatusAgendamento(agendamento.id, status);
      setAgendamentos((atuais) => atuais?.map((item) => (item.id === atualizado.id ? atualizado : item)) ?? null);
      await recarregarCampanhas();
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
    finally
    {
      setAlterando(null);
    }
  }

  return (
    <Modal titulo={`Agendamentos · ${campanha.paciente.nome}`} onClose={onClose} className="max-w-2xl">
      <div className="space-y-3">
        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
          </p>
        )}

        {agendamentos === null && !erro && <p className="text-sm text-gray-500">Carregando agendamentos...</p>}

        {agendamentos?.length === 0 && (
          <p className="rounded-xl border border-rota-border p-6 text-center text-sm text-gray-500">
            Nenhum doador agendou ainda. Os agendamentos são feitos pelos doadores no Portal do Doador.
          </p>
        )}

        {agendamentos?.map((agendamento) => (
          <div
            key={agendamento.id}
            className="flex flex-col gap-3 rounded-xl border border-rota-border p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="min-w-0">
              <p className="font-semibold text-gray-900">
                {agendamento.doadorNome}
                {agendamento.doadorTipoSanguineo && (
                  <span className="ml-2 font-mono text-sm font-bold text-rota-red">
                    {converterTipoSanguineo(agendamento.doadorTipoSanguineo)}
                  </span>
                )}
              </p>
              <p className="text-sm text-gray-500">
                {formatarHorario(agendamento.agendadoPara)} · {agendamento.bancoNome}
              </p>
              <span
                className={cn(
                  "mt-1.5 inline-block rounded-full px-2.5 py-0.5 font-mono text-[11px] font-semibold uppercase tracking-wide",
                  CLASSES_STATUS[agendamento.status],
                )}
              >
                {ROTULO_STATUS_AGENDAMENTO[agendamento.status]}
              </span>
            </div>

            {podeGerenciar && agendamento.status === "AGENDADO" && (
              <div className="flex shrink-0 flex-wrap gap-2">
                <BotaoStatus
                  rotulo="Doação feita"
                  classes="bg-emerald-600 text-white hover:bg-emerald-700"
                  desabilitado={alterando !== null}
                  onClick={() => mudarStatus(agendamento, "REALIZADO")}
                />
                <BotaoStatus
                  rotulo="Faltou"
                  classes="border border-amber-300 text-amber-700 hover:bg-amber-50"
                  desabilitado={alterando !== null}
                  onClick={() => mudarStatus(agendamento, "FALTOU")}
                />
                <BotaoStatus
                  rotulo="Cancelar"
                  classes="border border-rota-border text-gray-600 hover:border-gray-400"
                  desabilitado={alterando !== null}
                  onClick={() => mudarStatus(agendamento, "CANCELADO")}
                />
              </div>
            )}
          </div>
        ))}
      </div>
    </Modal>
  );
}

function BotaoStatus({
  rotulo,
  classes,
  desabilitado,
  onClick,
}: Readonly<{ rotulo: string; classes: string; desabilitado: boolean; onClick: () => void }>)
{
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={desabilitado}
      className={cn("rounded-lg px-3 py-1.5 text-sm font-semibold transition-colors disabled:opacity-60", classes)}
    >
      {rotulo}
    </button>
  );
}
