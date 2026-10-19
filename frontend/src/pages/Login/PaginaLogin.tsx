import { useEffect, useRef, useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { AlertCircle, Droplet, Eye, EyeOff, Loader2 } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { autenticarUsuario, ErroHttp } from "@/services/api";
import { cn } from "@/lib/utilitarios";

const AVISO_CAMPOS_VAZIOS = "Preencha o e-mail e a senha para entrar.";
const AVISO_SEM_CONEXAO = "Não foi possível falar com o servidor agora. Confira sua conexão e tente novamente.";

/**
 * Aviso mostrado no formulário quando o login falha (PI3-196). Senha errada e e-mail sem cadastro
 * chegam como o mesmo 401, então a tela não diz qual dos dois está errado.
 */
function avisoDoErro(erro: unknown): string
{
  if (erro instanceof ErroHttp && [400, 401, 503].includes(erro.status) && erro.detalhe)
  {
    return erro.detalhe;
  }
  return AVISO_SEM_CONEXAO;
}

export default function PaginaLogin()
{
  const { usuario, login } = useAutenticacao();
  const navigate = useNavigate();
  const campoSenha = useRef<HTMLInputElement>(null);

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [senhaVisivel, setSenhaVisivel] = useState(false);
  const [entrando, setEntrando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);

  // Depois de uma recusa do servidor a senha é apagada; o foco volta para ela, já reabilitada.
  useEffect(() =>
  {
    if (aviso && aviso !== AVISO_CAMPOS_VAZIOS)
    {
      campoSenha.current?.focus();
    }
  }, [aviso]);

  // Quem já está logado e abre /login vai direto para a sua tela inicial.
  if (usuario && !entrando)
  {
    return <Navigate to={usuario.telaInicial} replace />;
  }

  async function handleEntrar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    if (entrando) return;

    if (!email.trim() || !senha)
    {
      setAviso(AVISO_CAMPOS_VAZIOS);
      return;
    }

    setEntrando(true);
    setAviso(null);

    try
    {
      const acesso = await autenticarUsuario(email.trim(), senha);
      const logado = login(acesso);
      navigate(logado.telaInicial, { replace: true });
    }
    catch (erro)
    {
      setAviso(avisoDoErro(erro));
      setSenha("");
      setEntrando(false);
    }
  }

  function limparAviso()
  {
    if (aviso) setAviso(null);
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

        <form
          noValidate
          onSubmit={handleEntrar}
          className="space-y-5 rounded-2xl border border-rota-border bg-white p-6 shadow-card"
        >
          <div>
            <h2 className="text-lg font-bold text-gray-900">Entrar</h2>
            <p className="mt-0.5 text-sm text-gray-500">
              Use o e-mail e a senha cadastrados. Cada perfil abre a sua tela inicial.
            </p>
          </div>

          <div>
            <label
              htmlFor="email"
              className="mb-1.5 block font-mono text-xs font-semibold uppercase tracking-widest text-gray-500"
            >
              E-mail
            </label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              autoFocus
              value={email}
              onChange={(e) =>
              {
                setEmail(e.target.value);
                limparAviso();
              }}
              placeholder="ana.lima@hospital.com.br"
              disabled={entrando}
              aria-invalid={aviso !== null}
              aria-describedby={aviso ? "aviso-login" : undefined}
              className="w-full rounded-lg border border-rota-border bg-rota-surface2 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-rota-red focus:bg-white focus:outline-none disabled:opacity-60"
            />
          </div>

          <div>
            <label
              htmlFor="senha"
              className="mb-1.5 block font-mono text-xs font-semibold uppercase tracking-widest text-gray-500"
            >
              Senha
            </label>
            <div className="relative">
              <input
                id="senha"
                ref={campoSenha}
                type={senhaVisivel ? "text" : "password"}
                autoComplete="current-password"
                value={senha}
                onChange={(e) =>
                {
                  setSenha(e.target.value);
                  limparAviso();
                }}
                disabled={entrando}
                aria-invalid={aviso !== null}
                aria-describedby={aviso ? "aviso-login" : undefined}
                className="w-full rounded-lg border border-rota-border bg-rota-surface2 py-2.5 pl-3 pr-11 text-sm text-gray-900 focus:border-rota-red focus:bg-white focus:outline-none disabled:opacity-60"
              />
              <button
                type="button"
                onClick={() => setSenhaVisivel((v) => !v)}
                aria-label={senhaVisivel ? "Esconder senha" : "Mostrar senha"}
                className="absolute inset-y-0 right-0 flex w-10 items-center justify-center text-gray-400 hover:text-gray-700"
              >
                {senhaVisivel ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
          </div>

          {aviso && (
            <div
              id="aviso-login"
              role="alert"
              className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm text-red-800"
            >
              <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
              <span>{aviso}</span>
            </div>
          )}

          <button
            type="submit"
            disabled={entrando}
            className={cn(
              "flex w-full items-center justify-center gap-2 rounded-lg px-4 py-3 text-sm font-bold text-white shadow-sm transition-colors",
              entrando ? "cursor-wait bg-rota-red/70" : "bg-rota-red hover:bg-rota-redDark",
            )}
          >
            {entrando && <Loader2 className="h-4 w-4 animate-spin" />}
            {entrando ? "Entrando…" : "Entrar"}
          </button>
        </form>

        <p className="mt-6 text-center font-mono text-xs text-gray-400">
          HEMOPE · Recife — PE · Atualizado em tempo real
        </p>
      </div>
    </div>
  );
}
