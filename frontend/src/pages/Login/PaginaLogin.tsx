import { useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { ClipboardList, Droplet, Heart } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import type { PapelUsuario } from "@/types";
import { cn } from "@/lib/utilitarios";

export default function PaginaLogin()
{
  const [papel, setPapel] = useState<PapelUsuario | null>(null);
  const [nome, setNome] = useState("");
  const { login } = useAutenticacao();
  const navigate = useNavigate();

  const podeEntrar = papel !== null && nome.trim().length > 0;

  function handleEntrar()
  {
    if (!podeEntrar || !papel) return;
    login(nome, papel);
    navigate(papel === "medico" ? "/painel" : "/portal-doador", { replace: true });
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-rota-bg px-4 py-10">
      <div className="w-full max-w-md">
        <header className="mb-8 flex flex-col items-center text-center">
          <div className="mb-3 flex h-16 w-16 items-center justify-center rounded-md bg-rota-red">
            <Droplet className="h-8 w-8 text-white" fill="currentColor" />
          </div>
          <h1 className="text-3xl font-extrabold text-gray-900">Rota Vital</h1>
          <p className="mt-1 font-mono text-xs font-semibold tracking-widest text-gray-400">
            ESTOQUE INTELIGENTE
          </p>
        </header>

        <div className="divide-y divide-rota-border rounded-2xl border border-rota-border bg-white shadow-card">
          <div className="p-6">
            <p className="mb-3 font-mono text-xs font-semibold uppercase tracking-widest text-gray-500">
              Tipo de acesso
            </p>
            <div className="grid grid-cols-2 gap-3">
              <CartaoPapel
                icon={<ClipboardList className="h-5 w-5" />}
                title="Médico"
                description="Acesso hospitalar completo"
                selected={papel === "medico"}
                onClick={() => setPapel("medico")}
              />
              <CartaoPapel
                icon={<Heart className="h-5 w-5" />}
                title="Doador"
                description="Portal de doações"
                selected={papel === "doador"}
                onClick={() => setPapel("doador")}
              />
            </div>
          </div>

          <div className="space-y-5 p-6">
            <div>
              <label
                htmlFor="nome"
                className="mb-1.5 block font-mono text-xs font-semibold uppercase tracking-widest text-gray-500"
              >
                Nome
              </label>
              <input
                id="nome"
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Dr. Ana Lima"
                className="w-full rounded-lg border border-rota-border bg-rota-surface2 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:bg-white focus:outline-none"
              />
            </div>

            <button
              type="button"
              disabled={!podeEntrar}
              onClick={handleEntrar}
              className={cn(
                "w-full rounded-lg px-4 py-3 text-sm font-bold text-white transition-colors",
                podeEntrar
                  ? "bg-rota-red hover:bg-rota-redDark"
                  : "cursor-not-allowed bg-gray-300 text-gray-500",
              )}
            >
              Entrar
            </button>
          </div>
        </div>

        <p className="mt-6 text-center font-mono text-xs text-gray-400">
          HEMOPE · Recife — PE · Atualizado em tempo real
        </p>
      </div>
    </div>
  );
}

function CartaoPapel({
  icon,
  title,
  description,
  selected,
  onClick,
}: Readonly<{
  icon: ReactNode;
  title: string;
  description: string;
  selected: boolean;
  onClick: () => void;
}>)
{
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "flex flex-col items-start gap-3 rounded-xl border p-4 text-left transition-colors",
        selected ? "border-rota-red bg-rota-red text-white" : "border-rota-border bg-white text-gray-900 hover:border-gray-400",
      )}
    >
      <span>{icon}</span>
      <span>
        <span className="block text-sm font-bold">{title}</span>
        <span className={cn("block text-xs", selected ? "text-white/80" : "text-gray-500")}>
          {description}
        </span>
      </span>
    </button>
  );
}
