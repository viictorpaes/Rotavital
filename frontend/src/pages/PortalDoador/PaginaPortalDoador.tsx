import { useCallback, useEffect, useMemo, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Droplet, Search } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { useDados } from "@/context/ContextoDados";
import type { PessoaNecessitada, TipoSanguineo } from "@/types";
import type { AgendamentoDTO } from "@/types/dtos";
import { agendamentoEmAberto } from "@/lib/doacoes";
import { listarMeusAgendamentos } from "@/services/api";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { CartaoPessoa } from "@/components/donations/CartaoPessoa";
import { ModalAgendarDoacao } from "@/components/donations/ModalAgendarDoacao";
import { MinhasDoacoes } from "@/components/donations/MinhasDoacoes";

/**
 * HU-10 · PI3-192 — Portal do doador: pacientes das campanhas ativas, agendamento da
 * doação e acompanhamento (remarcar ou cancelar) dos próprios agendamentos.
 */
export default function PaginaPortalDoador()
{
  const { usuario, logout } = useAutenticacao();
  const navigate = useNavigate();
  const { pacientes: pessoasNecessitadas, situacaoCampanhas, erroCampanhas } = useDados();
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [selecionada, setSelecionada] = useState<PessoaNecessitada | null>(null);
  const [remarcando, setRemarcando] = useState<AgendamentoDTO | null>(null);
  const [meusAgendamentos, setMeusAgendamentos] = useState<AgendamentoDTO[]>([]);

  const ehDoador = usuario?.papel === "doador";

  const carregarMeusAgendamentos = useCallback(async () =>
  {
    try
    {
      setMeusAgendamentos(await listarMeusAgendamentos());
    }
    catch
    {
      // Sem a lista, o portal continua mostrando as campanhas; o backend recusa um segundo agendamento.
    }
  }, []);

  useEffect(() =>
  {
    if (ehDoador) carregarMeusAgendamentos();
  }, [ehDoador, carregarMeusAgendamentos]);

  const emAberto = agendamentoEmAberto(meusAgendamentos);

  const pessoasFiltradas = useMemo(() =>
  {
    const termo = busca.trim().toLowerCase();
    return pessoasNecessitadas.filter((p) =>
    {
      const combinaTipo = filtroTipo === "todos" || p.tipoSanguineo === filtroTipo;
      const combinaBusca =
        termo.length === 0 ||
        p.nome.toLowerCase().includes(termo) ||
        p.causa.toLowerCase().includes(termo) ||
        p.componente.toLowerCase().includes(termo);
      return combinaTipo && combinaBusca;
    });
  }, [pessoasNecessitadas, busca, filtroTipo]);

  if (!usuario)
  {
    return <Navigate to="/login" replace />;
  }

  // Médico e admin logados que abrem o portal voltam para a sua tela inicial (PI3-184).
  if (usuario.papel !== "doador")
  {
    return <Navigate to={usuario.telaInicial} replace />;
  }

  function handleSair()
  {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="min-h-screen bg-rota-bg">
      <header className="flex items-center justify-between border-b border-rota-border bg-white px-6 py-4">
        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-md bg-rota-red">
            <Droplet className="h-4 w-4 text-white" fill="currentColor" />
          </div>
          <p className="text-sm font-bold text-gray-900">
            Rota Vital <span className="font-normal text-gray-400">· Portal do Doador</span>
          </p>
        </div>
        <button
          type="button"
          onClick={handleSair}
          className="rounded-lg border border-rota-border px-4 py-1.5 text-sm font-medium text-gray-700 hover:border-gray-400"
        >
          Sair
        </button>
      </header>

      <main className="mx-auto max-w-5xl space-y-6 px-6 py-8">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
              Doações · Pessoas que precisam
            </p>
            <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Olá, {usuario.nome}</h1>
          </div>
          <span className="rounded-full border border-rota-border bg-white px-3 py-1.5 text-xs font-medium text-gray-600">
            {pessoasFiltradas.length} pessoas encontradas
          </span>
        </div>

        <MinhasDoacoes
          agendamentos={meusAgendamentos}
          onRemarcar={setRemarcando}
          onAlterado={carregarMeusAgendamentos}
        />

        <div className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              placeholder="Buscar por pessoa, causa ou necessidade..."
              className="w-full rounded-lg border border-rota-border bg-white py-2.5 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:outline-none"
            />
          </div>
          <FiltroTipoSanguineo value={filtroTipo} onChange={setFiltroTipo} />
        </div>

        {emAberto && (
          <p className="rounded-lg border border-sky-200 bg-sky-50 px-4 py-2.5 text-sm text-sky-800">
            Você já tem uma doação agendada. Para doar para outra pessoa, remarque ou cancele a atual.
          </p>
        )}

        {situacaoCampanhas === "carregando" ? (
          <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
            Carregando campanhas...
          </p>
        ) : situacaoCampanhas === "erro" ? (
          <p className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-sm text-red-700">
            Não foi possível carregar as campanhas: {erroCampanhas}
          </p>
        ) : pessoasFiltradas.length === 0 ? (
          <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
            Nenhuma pessoa encontrada.
          </p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {pessoasFiltradas.map((pessoa) => (
              <CartaoPessoa
                key={pessoa.id}
                pessoa={pessoa}
                onDoar={setSelecionada}
                desabilitado={emAberto !== undefined}
              />
            ))}
          </div>
        )}
      </main>

      {selecionada && (
        <ModalAgendarDoacao
          pessoa={selecionada}
          onClose={() => setSelecionada(null)}
          onConcluido={carregarMeusAgendamentos}
        />
      )}

      {remarcando && (
        <ModalAgendarDoacao
          agendamento={remarcando}
          onClose={() => setRemarcando(null)}
          onConcluido={carregarMeusAgendamentos}
        />
      )}
    </div>
  );
}
