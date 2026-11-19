import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, Clock, MapPin } from "lucide-react";
import type { PessoaNecessitada } from "@/types";
import type { AgendamentoDTO, HorarioDTO } from "@/types/dtos";
import { useDados } from "@/context/ContextoDados";
import { useHemocentros } from "@/hooks/useHemocentros";
import {
  enderecoDoHemocentro,
  formatarHorario,
  hojeEmRecife,
  horarioDoBackend,
  mensagemDeErro,
} from "@/lib/doacoes";
import { cn } from "@/lib/utilitarios";
import { agendarDoacao, listarHorarios, remarcarAgendamento } from "@/services/api";
import { CLASSES_CONTROLE, CampoFormulario } from "@/components/requisicao/CampoFormulario";
import { MapaHemocentro } from "@/components/donations/MapaHemocentro";
import { Modal } from "@/components/ui/Modal";

interface Props
{
  /** Paciente da campanha em que o doador vai agendar. */
  pessoa?: PessoaNecessitada;
  /** Agendamento a remarcar. Com ele, o modal muda só o horário (o hemocentro fica o mesmo). */
  agendamento?: AgendamentoDTO;
  onClose: () => void;
  /** Chamado com o agendamento gravado. */
  onConcluido: (agendamento: AgendamentoDTO) => void;
}

/**
 * HU-10 · PI3-204 — O doador escolhe o hemocentro, o dia e um horário com vaga, ou remarca a doação
 * que já tem. Hemocentro, endereço, expediente e horários vêm do backend (PI3-205).
 */
export function ModalAgendarDoacao({ pessoa, agendamento, onClose, onConcluido }: Readonly<Props>)
{
  const { recarregarCampanhas } = useDados();
  const { hemocentros, situacao: situacaoHemocentros, erro: erroHemocentros } = useHemocentros();
  const [hemocentroId, setHemocentroId] = useState<string | null>(agendamento?.bancoId ?? null);
  const [data, setData] = useState(agendamento ? horarioDoBackend(agendamento.agendadoPara).data : hojeEmRecife());
  // null enquanto carrega os horários do dia escolhido.
  const [horarios, setHorarios] = useState<HorarioDTO[] | null>(null);
  const [erroHorarios, setErroHorarios] = useState<string | null>(null);
  const [escolhido, setEscolhido] = useState<HorarioDTO | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [gravado, setGravado] = useState<AgendamentoDTO | null>(null);

  const hemocentro = hemocentros.find((item) => item.id === hemocentroId) ?? (agendamento ? undefined : hemocentros[0]);
  const nomePaciente = pessoa?.nome ?? agendamento?.pacienteNome;
  const horarioAtual = agendamento ? horarioDoBackend(agendamento.agendadoPara) : null;

  const carregarHorarios = useCallback(async (id: string, dia: string, ativo: () => boolean) =>
  {
    setHorarios(null);
    setErroHorarios(null);
    setEscolhido(null);
    try
    {
      const lista = await listarHorarios(id, dia);
      if (ativo()) setHorarios(lista);
    }
    catch (e)
    {
      if (ativo())
      {
        setErroHorarios(mensagemDeErro(e));
        setHorarios([]);
      }
    }
  }, []);

  useEffect(() =>
  {
    if (!hemocentro || data === "") return;
    let ativo = true;
    carregarHorarios(hemocentro.id, data, () => ativo);
    return () =>
    {
      ativo = false;
    };
  }, [hemocentro, data, carregarHorarios]);

  async function confirmar(evento: React.FormEvent)
  {
    evento.preventDefault();
    if (!escolhido || !hemocentro) return;

    setEnviando(true);
    setErro(null);
    try
    {
      const salvo = agendamento
        ? await remarcarAgendamento(agendamento.id, escolhido.inicio)
        : await agendarDoacao(pessoa!.campanhaId, escolhido.inicio, hemocentro.id);
      await recarregarCampanhas();
      setGravado(salvo);
      onConcluido(salvo);
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
      // Outro doador pode ter pego a última vaga enquanto o modal estava aberto.
      carregarHorarios(hemocentro.id, data, () => true);
    }
    finally
    {
      setEnviando(false);
    }
  }

  if (gravado)
  {
    return (
      <Modal titulo={agendamento ? "Remarcar doação" : "Agende sua doação"} onClose={onClose}>
        <div className="space-y-4">
          <p className="flex items-center gap-2 text-lg font-bold text-emerald-700">
            <CheckCircle2 className="h-5 w-5" />
            {agendamento ? "Doação remarcada" : "Doação agendada"}
          </p>
          <p className="text-sm text-gray-700">
            Esperamos você em <span className="font-semibold">{formatarHorario(gravado.agendadoPara)}</span> no{" "}
            {gravado.bancoNome}
            {hemocentro ? ` (${enderecoDoHemocentro(hemocentro)})` : ""}. Obrigado por ajudar{" "}
            {nomePaciente ?? "quem precisa"}!
          </p>
          <button
            type="button"
            onClick={onClose}
            className="w-full rounded-lg bg-gray-900 px-4 py-2.5 text-sm font-semibold text-white hover:bg-gray-700"
          >
            Fechar
          </button>
        </div>
      </Modal>
    );
  }

  return (
    <Modal titulo={agendamento ? "Remarcar doação" : "Agende sua doação"} onClose={onClose} className="max-w-lg">
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

        {situacaoHemocentros === "carregando" && <p className="text-sm text-gray-500">Carregando hemocentros...</p>}
        {situacaoHemocentros === "erro" && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            Não foi possível carregar os hemocentros: {erroHemocentros}
          </p>
        )}
        {situacaoHemocentros === "pronto" && !hemocentro && (
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
            Nenhum hemocentro cadastrado recebe doações agora.
          </p>
        )}

        {hemocentro && (
          <>
            {hemocentros.length > 1 && !agendamento && (
              <CampoFormulario rotulo="Hemocentro" htmlFor="agendar-hemocentro">
                <select
                  id="agendar-hemocentro"
                  value={hemocentro.id}
                  onChange={(e) => setHemocentroId(e.target.value)}
                  className={CLASSES_CONTROLE}
                >
                  {hemocentros.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.nome}
                    </option>
                  ))}
                </select>
              </CampoFormulario>
            )}

            <MapaHemocentro hemocentro={hemocentro} />

            <div className="flex items-start gap-2 text-sm text-gray-700">
              <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
              <div>
                <p className="font-semibold text-gray-900">{hemocentro.nome}</p>
                <p className="text-gray-500">{enderecoDoHemocentro(hemocentro)}</p>
              </div>
            </div>

            <div className="flex items-start gap-2 text-sm text-gray-700">
              <Clock className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
              <p>
                <span className="font-semibold text-gray-900">{hemocentro.atendimento}</span>
                <span className="text-gray-500"> · até {hemocentro.vagasPorHorario} doadores por horário</span>
              </p>
            </div>

            <CampoFormulario rotulo="Dia" htmlFor="agendar-dia">
              <input
                id="agendar-dia"
                type="date"
                required
                min={hojeEmRecife()}
                value={data}
                onChange={(e) => setData(e.target.value)}
                className={CLASSES_CONTROLE}
              />
            </CampoFormulario>

            <fieldset>
              <legend className="mb-1.5 block font-mono text-[11px] font-semibold uppercase tracking-widest text-gray-400">
                Horário
              </legend>
              {horarios === null ? (
                <p className="text-sm text-gray-500">Carregando horários...</p>
              ) : erroHorarios ? (
                <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                  Não foi possível carregar os horários: {erroHorarios}
                </p>
              ) : horarios.length === 0 ? (
                <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  O {hemocentro.nome} não abre neste dia. Escolha outro dia.
                </p>
              ) : horarios.every((horario) => !horario.disponivel) ? (
                <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  Não há mais horários livres neste dia. Escolha outro dia.
                </p>
              ) : (
                <div className="grid grid-cols-4 gap-2 sm:grid-cols-5">
                  {horarios.map((horario) =>
                  {
                    const atual = horarioAtual?.data === data && horarioAtual.hora === horario.hora;
                    const selecionado = escolhido?.inicio === horario.inicio;
                    return (
                      <button
                        key={horario.inicio}
                        type="button"
                        disabled={!horario.disponivel}
                        onClick={() => setEscolhido(horario)}
                        aria-pressed={selecionado}
                        title={
                          horario.disponivel
                            ? `${horario.vagasRestantes} vaga(s)`
                            : horario.vagasRestantes === 0
                              ? "Lotado"
                              : "Já passou"
                        }
                        className={cn(
                          "rounded-lg border px-2 py-1.5 text-center text-sm transition-colors",
                          selecionado
                            ? "border-rota-red bg-rota-red font-semibold text-white"
                            : "border-rota-border bg-white text-gray-800 hover:border-rota-red",
                          "disabled:cursor-not-allowed disabled:border-rota-border disabled:bg-rota-surface2 disabled:text-gray-300",
                        )}
                      >
                        <span className="block font-mono">{horario.hora}</span>
                        <span
                          className={cn(
                            "block text-[10px]",
                            selecionado ? "text-white/80" : horario.disponivel ? "text-gray-400" : "text-gray-300",
                          )}
                        >
                          {atual
                            ? "atual"
                            : horario.disponivel
                              ? `${horario.vagasRestantes} vaga${horario.vagasRestantes === 1 ? "" : "s"}`
                              : horario.vagasRestantes === 0
                                ? "lotado"
                                : "passou"}
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </fieldset>
          </>
        )}

        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
          </p>
        )}

        <button
          type="submit"
          disabled={enviando || !escolhido}
          className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-60"
        >
          {enviando
            ? "Gravando..."
            : !escolhido
              ? "Escolha um horário"
              : agendamento
                ? `Confirmar novo horário (${escolhido.hora})`
                : `Confirmar agendamento (${escolhido.hora})`}
        </button>
      </form>
    </Modal>
  );
}
