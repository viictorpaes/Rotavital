import { NavLink, useNavigate } from "react-router-dom";
import {
  Droplet,
  HeartHandshake,
  LayoutDashboard,
  Network,
  Package,
  ClipboardList,
  Stethoscope,
} from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { cn } from "@/lib/utilitarios";

const ITENS_NAVEGACAO =
[
  { to: "/painel", label: "Início", icon: LayoutDashboard },
  { to: "/estoque", label: "Estoque", icon: Package },
  { to: "/requisicao", label: "Requisição", icon: ClipboardList },
  { to: "/rede", label: "Rede", icon: Network },
  { to: "/pacientes", label: "Pacientes", icon: Stethoscope },
  { to: "/doacoes", label: "Doações", icon: HeartHandshake },
];

export function BarraLateral()
{
  const { usuario, logout } = useAutenticacao();
  const navigate = useNavigate();

  function handleSair()
  {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <aside className="hidden w-60 shrink-0 flex-col border-r border-rota-border bg-white md:flex">
      <div className="flex items-center gap-2 border-b border-rota-border px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-md bg-rota-red">
          <Droplet className="h-4 w-4 text-white" fill="currentColor" />
        </div>
        <div>
          <p className="text-sm font-extrabold leading-tight text-gray-900">Rota Vital</p>
          <p className="font-mono text-[10px] font-semibold uppercase tracking-widest text-gray-400">
            Estoque Inteligente
          </p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {ITENS_NAVEGACAO.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                isActive ? "bg-rota-red text-white" : "text-gray-700 hover:bg-rota-surface2",
              )
            }
          >
            <Icon className="h-5 w-5" />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-rota-border px-5 py-4">
        <p className="text-sm font-bold text-gray-900">{usuario?.nome}</p>
        <p className="font-mono text-[11px] text-gray-400">HEMOPE · Recife — PE</p>
        <button
          type="button"
          onClick={handleSair}
          className="mt-2 font-mono text-[11px] font-semibold text-rota-red hover:underline"
        >
          Sair →
        </button>
      </div>
    </aside>
  );
}
