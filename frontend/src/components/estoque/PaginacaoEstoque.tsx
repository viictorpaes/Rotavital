import { ChevronLeft, ChevronRight } from "lucide-react";

interface Props
{
  /** Página atual, começando em 0 (como o parâmetro page da API). */
  pagina: number;
  temProxima: boolean;
  carregando: boolean;
  onMudar: (pagina: number) => void;
}

const CLASSES_BOTAO =
  "flex items-center gap-1 rounded-md border border-rota-border bg-white px-3 py-1.5 text-sm font-semibold " +
  "text-gray-700 hover:border-gray-400 disabled:cursor-not-allowed disabled:opacity-40";

/**
 * Anterior/Próxima da listagem de bolsas. A API não devolve o total de registros, então há
 * próxima página sempre que a página atual veio cheia.
 */
export function PaginacaoEstoque({ pagina, temProxima, carregando, onMudar }: Readonly<Props>)
{
  return (
    <nav aria-label="Paginação do estoque" className="flex items-center justify-center gap-3 border-t border-rota-border pt-6">
      <button
        type="button"
        className={CLASSES_BOTAO}
        disabled={pagina === 0 || carregando}
        onClick={() => onMudar(pagina - 1)}
      >
        <ChevronLeft className="h-4 w-4" />
        Anterior
      </button>
      <span className="font-mono text-sm text-gray-500">Página {pagina + 1}</span>
      <button
        type="button"
        className={CLASSES_BOTAO}
        disabled={!temProxima || carregando}
        onClick={() => onMudar(pagina + 1)}
      >
        Próxima
        <ChevronRight className="h-4 w-4" />
      </button>
    </nav>
  );
}
