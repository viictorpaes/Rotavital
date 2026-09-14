import { Construction } from "lucide-react";

export function EmBreve({
  titulo,
  descricao,
}: Readonly<{ titulo: string; descricao: string }>)
{
  return (
    <div className="mx-auto flex max-w-2xl flex-col items-center gap-3 rounded-xl border border-dashed border-rota-border bg-white p-10 text-center">
      <Construction className="h-8 w-8 text-gray-400" />
      <h1 className="text-lg font-bold text-gray-900">{titulo}</h1>
      <p className="text-sm text-gray-500">{descricao}</p>
    </div>
  );
}
