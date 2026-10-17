import { cn } from "@/lib/utilitarios";

interface Opcao<T extends string>
{
  valor: T;
  rotulo: string;
}

interface Props<T extends string>
{
  id: string;
  rotulo: string;
  valor: T | "todos";
  opcoes: Array<Opcao<T>>;
  onChange: (valor: T | "todos") => void;
  className?: string;
}

/** Lista suspensa de um filtro da tela de Estoque (componente, status), com "Todos" no topo. */
export function SeletorFiltro<T extends string>({ id, rotulo, valor, opcoes, onChange, className }: Readonly<Props<T>>)
{
  return (
    <div className={cn("flex flex-col gap-1", className)}>
      <label htmlFor={id} className="font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">
        {rotulo}
      </label>
      <select
        id={id}
        value={valor}
        onChange={(evento) => onChange(evento.target.value as T | "todos")}
        className="rounded-md border border-rota-border bg-white px-3 py-1.5 text-sm font-semibold text-gray-700 hover:border-gray-400 focus:border-gray-900 focus:outline-none"
      >
        <option value="todos">Todos</option>
        {opcoes.map((opcao) => (
          <option key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
    </div>
  );
}
