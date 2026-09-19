import { Link } from "react-router-dom";
import { ArrowRight, ClipboardList, HeartHandshake, Network, Package } from "lucide-react";

const ACOES =
[
  { to: "/estoque", titulo: "Ver estoque completo", descricao: "Cartões por tipo sanguíneo", icon: Package },
  { to: "/requisicao", titulo: "Requisitar hemocomponente", descricao: "Ala, urgência e paciente", icon: ClipboardList },
  { to: "/rede", titulo: "Rede hospitalar", descricao: "Rotas e envios entre unidades", icon: Network },
  { to: "/doacoes", titulo: "Centro de doações", descricao: "Quem precisa agora", icon: HeartHandshake },
];

export function AcoesRapidas()
{
  return (
    <section className="space-y-3">
      <h2 className="font-mono text-xs font-semibold uppercase tracking-widest text-gray-500">
        Ações rápidas
      </h2>

      <div className="divide-y divide-rota-border overflow-hidden rounded-xl border border-rota-border bg-white">
        {ACOES.map(({ to, titulo, descricao, icon: Icon }) => (
          <Link
            key={to}
            to={to}
            className="flex items-center gap-3 p-4 transition-colors hover:bg-rota-surface2"
          >
            <Icon className="h-5 w-5 shrink-0 text-rota-red" />
            <div className="min-w-0">
              <p className="text-sm font-semibold text-gray-900">{titulo}</p>
              <p className="text-xs text-gray-500">{descricao}</p>
            </div>
            <ArrowRight className="ml-auto h-4 w-4 shrink-0 text-gray-400" />
          </Link>
        ))}
      </div>
    </section>
  );
}
