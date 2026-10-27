import { useState } from "react";
import { CheckCircle2, Clock, MapPin } from "lucide-react";
import type { PessoaNecessitada } from "@/types";
import type { AgendamentoDTO } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import {
  HORARIOS_ATENDIMENTO,
  amanha,
  ehDomingo,
  formatarHorario,
  horarioDoBackend,
  horarioParaBackend,
  mensagemDeErro,
} from "@/lib/doacoes";
import { agendarDoacao, remarcarAgendamento } from "@/services/api";
import { CLASSES_CONTROLE, CampoFormulario } from "@/components/requisicao/CampoFormulario";
import { Modal } from "@/components/ui/Modal";

interface Props
{
  /** Paciente da campanha em que o doador vai agendar. */
  pessoa?: PessoaNecessitada;
  /** Agendamento a remarcar. Com ele, o modal muda só o horário. */
  agendamento?: AgendamentoDTO;
  onClose: () => void;
  /** Chamado com o agendamento gravado. */
  onConcluido: (agendamento: AgendamentoDTO) => void;
}

/** HU-10 · PI3-192 — O doador escolhe dia e horário da doação no hemocentro, ou remarca a que já tem. */
export function ModalAgendarDoacao({ pessoa, agendamento, onClose, onConcluido }: Readonly<Props>)
{
  const { recarregarCampanhas } = useDados();
  const inicial = agendamento ? horarioDoBackend(agendamento.agendadoPara) : { data: amanha(), hora: "09:00" };
  const [data, setData] = useState(inicial.data);
  const [hora, setHora] = useState(inicial.hora);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [gravado, setGravado] = useState<AgendamentoDTO | null>(null);

  const domingo = data !== "" && ehDomingo(data);
  const nomePaciente = pessoa?.nome ?? agendamento?.pacienteNome;

  async function confirmar(evento: React.FormEvent)
  {
    evento.preventDefault();
    const horario = horarioParaBackend(data, hora);

    setEnviando(true);
    setErro(null);
    try
    {
      const salvo = agendamento
        ? await remarcarAgendamento(agendamento.id, horario)
        : await agendarDoacao(pessoa!.campanhaId, horario);
      await recarregarCampanhas();
      setGravado(salvo);
      onConcluido(salvo);
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
    finally
    {
      setEnviando(false);
    }
  }

  return (
    <Modal titulo={agendamento ? "Remarcar doação" : "Agende sua doação"} onClose={onClose}>
      {gravado ? (
        <div className="space-y-4">
          <p className="flex items-center gap-2 text-lg font-bold text-emerald-700">
            <CheckCircle2 className="h-5 w-5" />
            {agendamento ? "Doação remarcada" : "Doação agendada"}
          </p>
          <p className="text-sm text-gray-700">
            Esperamos você em <span className="font-semibold">{formatarHorario(gravado.agendadoPara)}</span> no{" "}
            {gravado.bancoNome}. Obrigado por ajudar {nomePaciente ?? "quem precisa"}!
          </p>
          <button
            type="button"
            onClick={onClose}
            className="w-full rounded-lg bg-gray-900 px-4 py-2.5 text-sm font-semibold text-white hover:bg-gray-700"
          >
            Fechar
          </button>
        </div>
      ) : (
        <form onSubmit={confirmar} className="space-y-4">
          {pessoa && (
            <p className="text-sm text-gray-700">
              Você vai doar para <span className="font-bold text-gray-900">{pessoa.nome}</span> — tipo{" "}
              <span className="font-bold text-rota-red">{pessoa.tipoSanguineo}</span> · {pessoa.componente}.
            </p>
          )}
          {agendamento && (
            <p className="text-sm text-gray-700">
              Horário atual: <span className="font-semibold">{formatarHorario(agendamento.agendadoPara)}</span>
              {nomePaciente ? ` · doação para ${nomePaciente}` : ""}.
            </p>
          )}

          <EsbocoMapaRota />

          <div className="flex items-start gap-2 text-sm text-gray-700">
            <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
            <div>
              <p className="font-semibold text-gray-900">Rota Vital — Hemocentro Central</p>
              <p className="text-gray-500">R. Arnóbio Marques, 310 — Santo Amaro, Recife — PE</p>
            </div>
          </div>

          <div className="flex items-start gap-2 text-sm text-gray-700">
            <Clock className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
            <p className="font-semibold text-gray-900">Seg a Sáb · 07h às 19h</p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <CampoFormulario rotulo="Dia" htmlFor="agendar-dia">
              <input
                id="agendar-dia"
                type="date"
                required
                min={amanha()}
                value={data}
                onChange={(e) => setData(e.target.value)}
                className={CLASSES_CONTROLE}
              />
            </CampoFormulario>
            <CampoFormulario rotulo="Horário" htmlFor="agendar-hora">
              <select
                id="agendar-hora"
                value={hora}
                onChange={(e) => setHora(e.target.value)}
                className={CLASSES_CONTROLE}
              >
                {HORARIOS_ATENDIMENTO.map((item) => (
                  <option key={item}>{item}</option>
                ))}
              </select>
            </CampoFormulario>
          </div>

          {domingo && (
            <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
              O hemocentro não abre aos domingos. Escolha outro dia.
            </p>
          )}
          {erro && (
            <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {erro}
            </p>
          )}

          <button
            type="submit"
            disabled={enviando || domingo}
            className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Gravando..." : agendamento ? "Confirmar novo horário" : "Confirmar agendamento"}
          </button>
        </form>
      )}
    </Modal>
  );
}

function EsbocoMapaRota()
{
  return (
    <div className="relative h-28 overflow-hidden rounded-lg border border-rota-border bg-rota-surface2">
      <span className="absolute right-2 top-1.5 font-mono text-[10px] uppercase tracking-widest text-gray-400">
        Recife
      </span>
      <svg viewBox="0 0 300 100" className="h-full w-full" aria-hidden="true">
        <path
          d="M 30 75 Q 100 20 150 50 T 260 25"
          fill="none"
          stroke="#c1272d"
          strokeWidth="2"
          strokeDasharray="6 5"
        />
        <circle cx="30" cy="75" r="5" fill="#c1272d" />
        <rect x="252" y="18" width="14" height="14" rx="2" fill="#c1272d" />
      </svg>
    </div>
  );
}
