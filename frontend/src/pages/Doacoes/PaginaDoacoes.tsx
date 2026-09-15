import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import type { PessoaNecessitada, TipoSanguineo } from "@/types";
import { pessoasNecessitadas } from "@/data/pessoasMock";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { CartaoPessoa } from "@/components/donations/CartaoPessoa";
import { ModalAgendarDoacao } from "@/components/donations/ModalAgendarDoacao";

export default function PaginaDoacoes()
{
  const { usuario } = useAutenticacao();
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [selecionada, setSelecionada] = useState<PessoaNecessitada | null>(null);

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
  }, [busca, filtroTipo]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Doações · Pessoas que precisam
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Olá, {usuario?.nome}</h1>
        </div>
        <span className="rounded-full border border-rota-border bg-white px-3 py-1.5 text-xs font-medium text-gray-600">
          {pessoasFiltradas.length} pessoas encontradas
        </span>
      </div>

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

      {pessoasFiltradas.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Nenhuma pessoa encontrada.
        </p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {pessoasFiltradas.map((pessoa) => (
            <CartaoPessoa key={pessoa.id} pessoa={pessoa} onDoar={setSelecionada} />
          ))}
        </div>
      )}

      {selecionada && (
        <ModalAgendarDoacao pessoa={selecionada} onClose={() => setSelecionada(null)} />
      )}
    </div>
  );
}
