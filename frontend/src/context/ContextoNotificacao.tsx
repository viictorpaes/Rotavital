import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { AlertCircle, AlertTriangle, CheckCircle2, Info, X } from "lucide-react";
import { registrarNotificadorHttp } from "@/services/clienteHttp";
import { cn } from "@/lib/utilitarios";

export type TipoNotificacao = "erro" | "aviso" | "sucesso" | "info";

export interface ItemNotificacao
{
  id: string;
  tipo: TipoNotificacao;
  titulo: string;
  mensagem: string;
  duracaoMs: number;
}

interface ValorContextoNotificacao
{
  notificar: (tipo: TipoNotificacao, titulo: string, mensagem: string, duracaoMs?: number) => void;
  notificarErro: (titulo: string, mensagem: string, duracaoMs?: number) => void;
  notificarSucesso: (titulo: string, mensagem: string, duracaoMs?: number) => void;
  notificarAviso: (titulo: string, mensagem: string, duracaoMs?: number) => void;
  notificarInfo: (titulo: string, mensagem: string, duracaoMs?: number) => void;
  removerNotificacao: (id: string) => void;
}

const ContextoNotificacao = createContext<ValorContextoNotificacao | undefined>(undefined);

export function ProvedorNotificacao({ children }: { children: ReactNode })
{
  const [notificacoes, setNotificacoes] = useState<ItemNotificacao[]>([]);

  const removerNotificacao = useCallback((id: string) =>
  {
    setNotificacoes((atuais) => atuais.filter((n) => n.id !== id));
  }, []);

  const notificar = useCallback(
    (tipo: TipoNotificacao, titulo: string, mensagem: string, duracaoMs: number = 6000) =>
    {
      const id = `${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
      const novoItem: ItemNotificacao = { id, tipo, titulo, mensagem, duracaoMs };

      setNotificacoes((atuais) => [...atuais, novoItem]);

      if (duracaoMs > 0)
      {
        setTimeout(() =>
        {
          removerNotificacao(id);
        }, duracaoMs);
      }
    },
    [removerNotificacao],
  );

  const notificarErro = useCallback(
    (titulo: string, mensagem: string, duracaoMs?: number) => notificar("erro", titulo, mensagem, duracaoMs),
    [notificar],
  );

  const notificarSucesso = useCallback(
    (titulo: string, mensagem: string, duracaoMs?: number) => notificar("sucesso", titulo, mensagem, duracaoMs),
    [notificar],
  );

  const notificarAviso = useCallback(
    (titulo: string, mensagem: string, duracaoMs?: number) => notificar("aviso", titulo, mensagem, duracaoMs),
    [notificar],
  );

  const notificarInfo = useCallback(
    (titulo: string, mensagem: string, duracaoMs?: number) => notificar("info", titulo, mensagem, duracaoMs),
    [notificar],
  );

  // Conecta o Cliente HTTP global com o sistema de Toasts do React
  useEffect(() =>
  {
    registrarNotificadorHttp((tipo, titulo, mensagem) =>
    {
      notificar(tipo, titulo, mensagem);
    });
  }, [notificar]);

  return (
    <ContextoNotificacao.Provider
      value={{
        notificar,
        notificarErro,
        notificarSucesso,
        notificarAviso,
        notificarInfo,
        removerNotificacao,
      }}
    >
      {children}
      <ContainerToasts notificacoes={notificacoes} onRemover={removerNotificacao} />
    </ContextoNotificacao.Provider>
  );
}

export function useNotificacao()
{
  const ctx = useContext(ContextoNotificacao);
  if (!ctx)
  {
    throw new Error("useNotificacao deve ser usado dentro de ProvedorNotificacao");
  }
  return ctx;
}

function ContainerToasts({
  notificacoes,
  onRemover,
}: {
  notificacoes: ItemNotificacao[];
  onRemover: (id: string) => void;
})
{
  if (notificacoes.length === 0) return null;

  return (
    <div
      aria-live="polite"
      className="pointer-events-none fixed bottom-5 right-5 z-50 flex max-w-md w-full flex-col gap-2.5 px-4 sm:px-0"
    >
      {notificacoes.map((item) => (
        <CardToast key={item.id} item={item} onRemover={() => onRemover(item.id)} />
      ))}
    </div>
  );
}

function CardToast({
  item,
  onRemover,
}: {
  item: ItemNotificacao;
  onRemover: () => void;
})
{
  const icones = {
    erro: <AlertCircle className="h-5 w-5 text-rose-600 shrink-0" />,
    aviso: <AlertTriangle className="h-5 w-5 text-amber-600 shrink-0" />,
    sucesso: <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />,
    info: <Info className="h-5 w-5 text-blue-600 shrink-0" />,
  };

  const estilosBorda = {
    erro: "border-rose-200 bg-white border-l-4 border-l-rose-600",
    aviso: "border-amber-200 bg-white border-l-4 border-l-amber-500",
    sucesso: "border-emerald-200 bg-white border-l-4 border-l-emerald-600",
    info: "border-blue-200 bg-white border-l-4 border-l-blue-600",
  };

  return (
    <div
      className={cn(
        "pointer-events-auto flex items-start gap-3 rounded-xl border p-4 shadow-lg transition-all animate-in slide-in-from-right-4 duration-200",
        estilosBorda[item.tipo],
      )}
      role="alert"
    >
      {icones[item.tipo]}

      <div className="flex-1 min-w-0 pr-1">
        <p className="text-sm font-bold text-gray-900 leading-tight">{item.titulo}</p>
        <p className="mt-1 text-xs text-gray-600 leading-relaxed break-words">{item.mensagem}</p>
      </div>

      <button
        type="button"
        onClick={onRemover}
        className="shrink-0 rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition"
        title="Fechar notificação"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}
