import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import type {
  LoteHemocomponente,
  OrigemAtendimento,
  PessoaNecessitada,
  ProcedimentoConcluido,
  RequisicaoRecebida,
} from "@/types";
import { lotesEstoque } from "@/data/estoqueMock";
import { pessoasNecessitadas } from "@/data/pessoasMock";
import { requisicoesRecebidas } from "@/data/requisicoesMock";
import { reservarPorFefo } from "@/lib/pacientes";
import {
  gerarProtocolo,
  loteDaRequisicaoRecebida,
  pacienteDaCampanha,
  type DadosCampanha,
} from "@/lib/requisicao";

interface ValorContextoDados
{
  lotes: LoteHemocomponente[];
  pacientes: PessoaNecessitada[];
  /** Remessas ainda sem conferência física (HU-05). */
  recebimentosPendentes: RequisicaoRecebida[];
  /** Remessas conferidas nesta sessão, da mais recente para a mais antiga. */
  recebimentosConfirmados: RequisicaoRecebida[];
  /** Confere a remessa e soma as unidades ao estoque (HU-05). */
  confirmarRecebimento: (id: string) => void;
  /** Publica o paciente nas telas de Doações e Pacientes (HU-06). */
  publicarCampanha: (dados: DadosCampanha) => string;
  /** Procedimentos encerrados nesta sessão, do mais recente para o mais antigo. */
  procedimentosConcluidos: ProcedimentoConcluido[];
  /** Encerra o atendimento do paciente e dá a baixa correspondente (HU-08). */
  concluirProcedimento: (
    pacienteId: string,
    origem: OrigemAtendimento,
  ) => ProcedimentoConcluido | undefined;
}

const ContextoDados = createContext<ValorContextoDados | undefined>(undefined);

/**
 * Estado compartilhado das telas operacionais — ainda em memória, alimentado
 * pelos mocks. Existe para que o recebimento de remessas e a publicação de
 * campanhas reflitam de imediato em Estoque, Pacientes e Doações.
 */
export function ProvedorDados({ children }: { children: ReactNode })
{
  const [lotes, setLotes] = useState<LoteHemocomponente[]>(lotesEstoque);
  const [pacientes, setPacientes] = useState<PessoaNecessitada[]>(pessoasNecessitadas);
  const [recebimentosPendentes, setPendentes] = useState<RequisicaoRecebida[]>(requisicoesRecebidas);
  const [recebimentosConfirmados, setConfirmados] = useState<RequisicaoRecebida[]>([]);
  const [procedimentosConcluidos, setProcedimentos] = useState<ProcedimentoConcluido[]>([]);

  const valor = useMemo<ValorContextoDados>(() =>
  {
    function confirmarRecebimento(id: string)
    {
      const requisicao = recebimentosPendentes.find((item) => item.id === id);
      if (!requisicao) return;

      setLotes((atuais) => [...atuais, loteDaRequisicaoRecebida(requisicao)]);
      setPendentes((atuais) => atuais.filter((item) => item.id !== id));
      setConfirmados((atuais) => [requisicao, ...atuais]);
    }

    function publicarCampanha(dados: DadosCampanha)
    {
      const protocolo = gerarProtocolo("CAM");
      setPacientes((atuais) => [pacienteDaCampanha(dados, protocolo.toLowerCase()), ...atuais]);
      return protocolo;
    }

    /**
     * HU-08 — As duas origens são mutuamente exclusivas: a doação externa é
     * atendida por um doador que comparece ao hemocentro e não toca no estoque;
     * o atendimento pelo estoque interno baixa as unidades por FEFO. Em ambos
     * os casos o paciente sai da fila de necessidades ativas.
     */
    function concluirProcedimento(pacienteId: string, origem: OrigemAtendimento)
    {
      const paciente = pacientes.find((item) => item.id === pacienteId);
      if (!paciente) return undefined;

      const baixa =
        origem === "estoque-interno"
          ? reservarPorFefo(
              lotes,
              paciente.componente,
              paciente.tipoSanguineo,
              paciente.unidadesNecessarias,
            )
          : undefined;

      if (baixa) setLotes(baixa.lotes);

      const registro: ProcedimentoConcluido = {
        protocolo: gerarProtocolo("PRC"),
        pacienteId: paciente.id,
        paciente: paciente.nome,
        componente: paciente.componente,
        tipoSanguineo: paciente.tipoSanguineo,
        origem,
        unidadesBaixadas: baixa?.unidadesBaixadas ?? 0,
        consumos: baixa?.consumos ?? [],
        unidadesFaltantes: baixa?.unidadesFaltantes ?? 0,
      };

      setPacientes((atuais) => atuais.filter((item) => item.id !== pacienteId));
      setProcedimentos((atuais) => [registro, ...atuais]);
      return registro;
    }

    return {
      lotes,
      pacientes,
      recebimentosPendentes,
      recebimentosConfirmados,
      confirmarRecebimento,
      publicarCampanha,
      procedimentosConcluidos,
      concluirProcedimento,
    };
  }, [lotes, pacientes, recebimentosPendentes, recebimentosConfirmados, procedimentosConcluidos]);

  return <ContextoDados.Provider value={valor}>{children}</ContextoDados.Provider>;
}

export function useDados()
{
  const ctx = useContext(ContextoDados);
  if (!ctx) throw new Error("useDados deve ser usado dentro de ProvedorDados");
  return ctx;
}
