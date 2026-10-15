import { useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { ClipboardList, Droplet, Heart, ShieldAlert } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import type { PapelUsuario } from "@/types";
import { cn } from "@/lib/utilitarios";

export default function PaginaLogin()
{
  const [papel, setPapel] = useState<PapelUsuario | null>(null);
  const [nome, setNome] = useState("");
  const { login } = useAutenticacao();
  const navigate = useNavigate();

  const isAdmin = nome.trim().toLowerCase() === "admin" || papel === "admin";
  // O botão fica ATIVO se for admin OU se tiver papel selecionado e nome preenchido
  const podeEntrar = isAdmin || (papel !== null && nome.trim().length > 0);

  function selecionarPapel(novoPapel: PapelUsuario)
  {
    setPapel(novoPapel);
    if (novoPapel === "admin")
    {
      setNome("admin");
    }
  }

  function handleEntrar()
  {
    if (!podeEntrar) return;

    if (isAdmin)
    {
      login("Administrador", "admin");
      navigate("/admin", { replace: true });
      return;
    }

    if (!papel) return;
    login(nome, papel);
    navigate(papel === "medico" ? "/painel" : "/portal-doador", { replace: true });
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-rota-bg px-4 py-10">
      <div className="w-full max-w-md">
        <header className="mb-8 flex flex-col items-center text-center">
          <div className="mb-3 flex h-16 w-16 items-center justify-center rounded-md bg-rota-red shadow-sm">
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
                selected={papel === "medico" && !isAdmin}
                onClick={() => selecionarPapel("medico")}
              />
              <CartaoPapel
                icon={<Heart className="h-5 w-5" />}
                title="Doador"
                description="Portal de doações"
                selected={papel === "doador" && !isAdmin}
                onClick={() => selecionarPapel("doador")}
              />
            </div>
          </div>

          <div className="space-y-5 p-6">
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label
                  htmlFor="nome"
                  className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-500"
                >
                  Nome
                </label>
                {isAdmin && (
                  <span className="inline-flex items-center gap-1 rounded bg-purple-100 px-2 py-0.5 text-[10px] font-bold text-purple-700">
                    Admin Detectado
                  </span>
                )}
              </div>
              <input
                id="nome"
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && podeEntrar) {
                    handleEntrar();
                  }
                }}
                placeholder="Dr. Ana Lima ou 'admin'"
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-sm transition-all focus:outline-none",
                  isAdmin
                    ? "border-purple-500 bg-purple-50/40 text-purple-900 focus:border-purple-600 focus:bg-white"
                    : "border-rota-border bg-rota-surface2 text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:bg-white"
                )}
              />
            </div>

            {isAdmin && (
              <div className="rounded-lg border border-purple-200 bg-purple-50 p-3 text-xs text-purple-800">
                <div className="font-bold flex items-center gap-1.5 mb-1">
                  <ShieldAlert className="h-4 w-4 text-purple-600" /> Modo Administrador
                </div>
                <p className="text-[11px] leading-relaxed text-purple-700">
                  Acesso liberado para validação do <strong>Backend</strong>, <strong>Banco Supabase</strong> e <strong>Logs ao vivo</strong>.
                </p>
              </div>
            )}

            <button
              type="button"
              disabled={!podeEntrar}
              onClick={handleEntrar}
              className={cn(
                "w-full rounded-lg px-4 py-3 text-sm font-bold text-white transition-all shadow-sm",
                podeEntrar
                  ? isAdmin
                    ? "bg-purple-700 hover:bg-purple-800 cursor-pointer"
                    : "bg-rota-red hover:bg-rota-redDark cursor-pointer"
                  : "cursor-not-allowed bg-gray-300 text-gray-500",
              )}
            >
              {isAdmin ? "Entrar como Admin →" : "Entrar"}
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

