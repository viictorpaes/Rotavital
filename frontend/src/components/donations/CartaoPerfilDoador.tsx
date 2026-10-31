import { Pencil, UserRound } from "lucide-react";
import type { DoadorDTO } from "@/types/dtos";
import { converterTipoSanguineo } from "@/lib/estoque";
import { formatarData, formatarHorario, formatarTelefone } from "@/lib/doacoes";

interface Props
{
  perfil: DoadorDTO;
  onEditar: () => void;
}

/** PI3-203 — Os dados do doador e o resumo das doações, como o backend devolve em GET /doadores/eu. */
export function CartaoPerfilDoador({ perfil, onEditar }: Readonly<Props>)
{
  const tipo = perfil.tipoSanguineo ? converterTipoSanguineo(perfil.tipoSanguineo) : null;

  return (
    <section className="space-y-4 rounded-xl border border-rota-border bg-white p-5 shadow-card">
      <div className="flex items-start justify-between gap-3">
        <h2 className="flex items-center gap-2 font-mono text-xs font-bold uppercase tracking-widest text-gray-500">
          <UserRound className="h-4 w-4 text-rota-red" />
          Meu perfil
        </h2>
        <button
          type="button"
          onClick={onEditar}
          className="flex items-center gap-1.5 rounded-lg border border-rota-border px-3 py-1.5 text-sm font-medium text-gray-700 hover:border-gray-400"
        >
          <Pencil className="h-3.5 w-3.5" />
          Editar
        </button>
      </div>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <dl className="grid grid-cols-1 gap-x-8 gap-y-2 text-sm sm:grid-cols-2">
          <Item rotulo="E-mail" valor={perfil.email ?? "—"} />
          <Item rotulo="Telefone" valor={perfil.telefone ? formatarTelefone(perfil.telefone) : "Não informado"} />
          <Item
            rotulo="Nascimento"
            valor={perfil.dataNascimento ? `${formatarData(perfil.dataNascimento)} · ${perfil.idade} anos` : "Não informado"}
          />
          <Item rotulo="Tipo sanguíneo" valor={tipo ?? "Ainda não sei"} />
        </dl>

        <div className="flex gap-3">
          <Numero valor={String(perfil.doacoesRealizadas)} rotulo={perfil.doacoesRealizadas === 1 ? "doação feita" : "doações feitas"} />
          {tipo && <Numero valor={tipo} rotulo="seu tipo" destaque />}
        </div>
      </div>

      {(perfil.ultimaDoacao || perfil.proximaDoacao) && (
        <p className="border-t border-rota-border pt-3 text-sm text-gray-600">
          {perfil.ultimaDoacao && <>Última doação: {formatarHorario(perfil.ultimaDoacao)}. </>}
          {perfil.proximaDoacao && <>Próxima: {formatarHorario(perfil.proximaDoacao)}.</>}
        </p>
      )}
    </section>
  );
}

function Item({ rotulo, valor }: Readonly<{ rotulo: string; valor: string }>)
{
  return (
    <div>
      <dt className="font-mono text-[10px] uppercase tracking-widest text-gray-400">{rotulo}</dt>
      <dd className="font-medium text-gray-900">{valor}</dd>
    </div>
  );
}

function Numero({ valor, rotulo, destaque = false }: Readonly<{ valor: string; rotulo: string; destaque?: boolean }>)
{
  return (
    <div className="min-w-[5.5rem] rounded-lg border border-rota-border bg-rota-surface2 px-3 py-2 text-center">
      <p className={destaque ? "text-2xl font-extrabold text-rota-red" : "text-2xl font-extrabold text-gray-900"}>{valor}</p>
      <p className="font-mono text-[10px] uppercase tracking-widest text-gray-400">{rotulo}</p>
    </div>
  );
}
