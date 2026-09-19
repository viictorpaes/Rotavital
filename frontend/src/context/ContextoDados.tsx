import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import type { LoteHemocomponente, PessoaNecessitada, RequisicaoRecebida } from "@/types";
import { lotesEstoque } from "@/data/estoqueMock";
import { pessoasNecessitadas } from "@/data/pessoasMock";
import { requisicoesRecebidas } from "@/data/requisicoesMock";
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

    return {
      lotes,
      pacientes,
      recebimentosPendentes,
      recebimentosConfirmados,
      confirmarRecebimento,
      publicarCampanha,
    };
  }, [lotes, pacientes, recebimentosPendentes, recebimentosConfirmados]);

  return <ContextoDados.Provider value={valor}>{children}</ContextoDados.Provider>;
}

export function useDados()
{
  const ctx = useContext(ContextoDados);
  if (!ctx) throw new Error("useDados deve ser usado dentro de ProvedorDados");
  return ctx;
}
