import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import type {
  LoteHemocomponente,
  OrigemAtendimento,
  PacienteNaFila,
  PessoaNecessitada,
  ProcedimentoConcluido,
  RequisicaoRecebida,
} from "@/types";
import type { CampanhaDTO } from "@/types/dtos";
import { lotesEstoque } from "@/data/estoqueMock";
import { requisicoesRecebidas } from "@/data/requisicoesMock";
import { reservarPorFefo } from "@/lib/pacientes";
import { converterBolsaParaLote } from "@/lib/estoque";
import { mensagemDeErro, pessoaDaCampanha } from "@/lib/doacoes";
import { buscarEstoque, listarCampanhas } from "@/services/api";
import { gerarProtocolo, loteDaRequisicaoRecebida } from "@/lib/requisicao";

export type SituacaoCarga = "carregando" | "pronto" | "erro";

interface ValorContextoDados
{
  lotes: LoteHemocomponente[];
  /** Pacientes das campanhas ativas, lidos do backend (HU-06, HU-09). */
  pacientes: PessoaNecessitada[];
  /** Campanhas de doação, ativas e encerradas, da mais recente para a mais antiga (PI3-191). */
  campanhas: CampanhaDTO[];
  situacaoCampanhas: SituacaoCarga;
  erroCampanhas: string | null;
  /** Lê as campanhas de novo — chamado depois de publicar, editar, encerrar ou agendar. */
  recarregarCampanhas: () => Promise<void>;
  /** Remessas ainda sem conferência física (HU-05). */
  recebimentosPendentes: RequisicaoRecebida[];
  /** Remessas conferidas nesta sessão, da mais recente para a mais antiga. */
  recebimentosConfirmados: RequisicaoRecebida[];
  /** Confere a remessa e soma as unidades ao estoque (HU-05). */
  confirmarRecebimento: (id: string) => void;
  /** Procedimentos encerrados nesta sessão, do mais recente para o mais antigo. */
  procedimentosConcluidos: ProcedimentoConcluido[];
  /** Encerra o atendimento do paciente e dá a baixa correspondente (HU-08). */
  concluirProcedimento: (
    paciente: PacienteNaFila,
    origem: OrigemAtendimento,
  ) => ProcedimentoConcluido;
}

const ContextoDados = createContext<ValorContextoDados | undefined>(undefined);

/**
 * Estado compartilhado das telas operacionais. Estoque e campanhas de doação vêm do backend (Supabase).
 */
export function ProvedorDados({ children }: { children: ReactNode })
{
  const [lotes, setLotes] = useState<LoteHemocomponente[]>(lotesEstoque);
  const [campanhas, setCampanhas] = useState<CampanhaDTO[]>([]);
  const [situacaoCampanhas, setSituacaoCampanhas] = useState<SituacaoCarga>("carregando");
  const [erroCampanhas, setErroCampanhas] = useState<string | null>(null);
  // O procedimento (HU-08) ainda não é gravado: o paciente concluído só sai da fila nesta sessão.
  const [pacientesAtendidos, setPacientesAtendidos] = useState<string[]>([]);
  const [recebimentosPendentes, setPendentes] = useState<RequisicaoRecebida[]>(requisicoesRecebidas);
  const [recebimentosConfirmados, setConfirmados] = useState<RequisicaoRecebida[]>([]);
  const [procedimentosConcluidos, setProcedimentos] = useState<ProcedimentoConcluido[]>([]);

  useEffect(() =>
  {
    async function carregarEstoqueReal()
    {
      try
      {
        const res = await buscarEstoque("BS-01");
        if (res && res.bolsas && res.bolsas.length > 0)
        {
          const convertidos = res.bolsas.map(converterBolsaParaLote);
          setLotes(convertidos);
        }
      }
      catch (e)
      {
        console.warn("ContextoDados: usando estoque local de contingência", e);
      }
    }

    carregarEstoqueReal();
  }, []);

  const recarregarCampanhas = useCallback(async () =>
  {
    try
    {
      setCampanhas(await listarCampanhas());
      setErroCampanhas(null);
      setSituacaoCampanhas("pronto");
    }
    catch (e)
    {
      setErroCampanhas(mensagemDeErro(e));
      setSituacaoCampanhas("erro");
    }
  }, []);

  useEffect(() =>
  {
    recarregarCampanhas();
  }, [recarregarCampanhas]);

  const pacientes = useMemo(
    () => campanhas
      .filter((campanha) => campanha.status === "ATIVA")
      .map(pessoaDaCampanha)
      .filter((pessoa) => !pacientesAtendidos.includes(pessoa.id)),
    [campanhas, pacientesAtendidos],
  );

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

    /**
     * HU-08 — As duas origens são mutuamente exclusivas: a doação externa é
     * atendida por um doador que comparece ao hemocentro e não toca no estoque;
     * o atendimento pelo estoque interno baixa as unidades por FEFO. Em ambos
     * os casos o paciente sai da fila de necessidades ativas.
     */
    // Recebe o paciente (não só o id): a tela de Pacientes lê de /pacientes, que inclui quem não tem campanha.
    function concluirProcedimento(paciente: PacienteNaFila, origem: OrigemAtendimento)
    {
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

      setPacientesAtendidos((atuais) => [...atuais, paciente.id]);
      setProcedimentos((atuais) => [registro, ...atuais]);
      return registro;
    }

    return {
      lotes,
      pacientes,
      campanhas,
      situacaoCampanhas,
      erroCampanhas,
      recarregarCampanhas,
      recebimentosPendentes,
      recebimentosConfirmados,
      confirmarRecebimento,
      procedimentosConcluidos,
      concluirProcedimento,
    };
  }, [
    lotes,
    pacientes,
    campanhas,
    situacaoCampanhas,
    erroCampanhas,
    recarregarCampanhas,
    recebimentosPendentes,
    recebimentosConfirmados,
    procedimentosConcluidos,
  ]);

  return <ContextoDados.Provider value={valor}>{children}</ContextoDados.Provider>;
}

export function useDados()
{
  const ctx = useContext(ContextoDados);
  if (!ctx) throw new Error("useDados deve ser usado dentro de ProvedorDados");
  return ctx;
}
