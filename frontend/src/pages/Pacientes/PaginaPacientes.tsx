import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import type { TipoSanguineo } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { FiltroTipoSanguineo } from "@/components/donations/FiltroTipoSanguineo";
import { PontoStatus } from "@/components/ui/PontoStatus";

export default function PaginaPacientes()
{
  const { pacientes: pessoasNecessitadas } = useDados();
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");

  const pacientesFiltrados = useMemo(() =>
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-400">
            Pacientes · Necessidades ativas
          </p>
          <h1 className="mt-1 text-3xl font-extrabold text-gray-900">Pacientes</h1>
        </div>
        <span className="rounded-full border border-rota-border bg-white px-3 py-1.5 text-xs font-medium text-gray-600">
          {pacientesFiltrados.length} pacientes encontrados
        </span>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por paciente, causa ou componente..."
            className="w-full rounded-lg border border-rota-border bg-white py-2.5 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:outline-none"
          />
        </div>
        <FiltroTipoSanguineo value={filtroTipo} onChange={setFiltroTipo} />
      </div>

      {pacientesFiltrados.length === 0 ? (
        <p className="rounded-xl border border-rota-border bg-white p-6 text-center text-sm text-gray-500">
          Nenhum paciente encontrado.
        </p>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-rota-border bg-white shadow-card">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead>
              <tr className="border-b border-rota-border text-xs font-semibold uppercase tracking-wide text-gray-400">
                <th className="px-5 py-3">Paciente</th>
                <th className="px-5 py-3">Tipo</th>
                <th className="px-5 py-3">Componente</th>
                <th className="px-5 py-3">Distância</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3">Causa</th>
              </tr>
            </thead>
            <tbody>
              {pacientesFiltrados.map((paciente) => (
                <tr key={paciente.id} className="border-b border-rota-border last:border-0">
                  <td className="px-5 py-4">
                    <p className="font-semibold text-gray-900">{paciente.nome}</p>
                    <p className="text-xs text-gray-500">
                      {paciente.sexo} · {paciente.idade} anos
                    </p>
                  </td>
                  <td className="px-5 py-4 font-bold text-rota-red">{paciente.tipoSanguineo}</td>
                  <td className="px-5 py-4 text-gray-600">{paciente.componente}</td>
                  <td className="px-5 py-4 text-gray-600">
                    {paciente.distanciaKm.toLocaleString("pt-BR", { minimumFractionDigits: 1 })} km
                  </td>
                  <td className="px-5 py-4">
                    <PontoStatus status={paciente.status} />
                  </td>
                  <td className="px-5 py-4 text-gray-600">{paciente.causa}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
