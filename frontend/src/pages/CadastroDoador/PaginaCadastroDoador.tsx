import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { AlertCircle, CheckCircle2, Droplet, Eye, EyeOff, Loader2 } from "lucide-react";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { RASCUNHO_DOADOR_VAZIO, mensagemDeErro, rascunhoDoadorParaBackend, type RascunhoDoador } from "@/lib/doacoes";
import { cn } from "@/lib/utilitarios";
import { cadastrarDoador } from "@/services/api";
import { CLASSES_CONTROLE, CampoFormulario } from "@/components/requisicao/CampoFormulario";
import { CamposDoador } from "@/components/donations/CamposDoador";

/**
 * PI3-203 — Cadastro do doador. A conta de login é criada no Supabase Auth e o perfil na tabela
 * usuario; com a sessão na resposta, o doador já entra no portal. Médico e admin não se cadastram
 * aqui: as contas deles são criadas pela equipe.
 */
export default function PaginaCadastroDoador()
{
  const { usuario, login } = useAutenticacao();
  const navigate = useNavigate();
  const [dados, setDados] = useState<RascunhoDoador>(RASCUNHO_DOADOR_VAZIO);
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [senhaVisivel, setSenhaVisivel] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [aguardandoConfirmacao, setAguardandoConfirmacao] = useState<string | null>(null);

  if (usuario && !enviando)
  {
    return <Navigate to={usuario.telaInicial} replace />;
  }

  async function cadastrar(evento: FormEvent<HTMLFormElement>)
  {
    evento.preventDefault();
    if (enviando) return;

    setEnviando(true);
    setErro(null);
    try
    {
      const resposta = await cadastrarDoador({ ...rascunhoDoadorParaBackend(dados), email: email.trim(), senha });
      if (resposta.acesso)
      {
        const logado = login(resposta.acesso);
        navigate(logado.telaInicial, { replace: true });
        return;
      }
      setAguardandoConfirmacao(resposta.doador.email ?? email.trim());
      setEnviando(false);
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-rota-bg px-4 py-10">
      <div className="w-full max-w-lg">
        <header className="mb-8 flex flex-col items-center text-center">
          <div className="mb-3 flex h-16 w-16 items-center justify-center rounded-md bg-rota-red shadow-sm">
            <Droplet className="h-8 w-8 text-white" fill="currentColor" />
          </div>
          <h1 className="text-3xl font-extrabold text-gray-900">Seja um doador</h1>
          <p className="mt-1 text-sm text-gray-500">
            Crie sua conta para ver quem precisa de sangue e agendar sua doação.
          </p>
        </header>

        {aguardandoConfirmacao ? (
          <div className="space-y-4 rounded-2xl border border-rota-border bg-white p-6 shadow-card">
            <p className="flex items-center gap-2 text-lg font-bold text-emerald-700">
              <CheckCircle2 className="h-5 w-5" />
              Conta criada
            </p>
            <p className="text-sm text-gray-700">
              Enviamos um link de confirmação para <span className="font-semibold">{aguardandoConfirmacao}</span>.
              Confirme o e-mail e depois entre com a sua senha.
            </p>
            <Link
              to="/login"
              className="block w-full rounded-lg bg-gray-900 px-4 py-2.5 text-center text-sm font-semibold text-white hover:bg-gray-700"
            >
              Ir para o login
            </Link>
          </div>
        ) : (
          <form onSubmit={cadastrar} className="space-y-4 rounded-2xl border border-rota-border bg-white p-6 shadow-card">
            <CamposDoador valor={dados} onChange={setDados} prefixoId="cadastro" />

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <CampoFormulario rotulo="E-mail" htmlFor="cadastro-email">
                <input
                  id="cadastro-email"
                  type="email"
                  required
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={CLASSES_CONTROLE}
                />
              </CampoFormulario>

              <CampoFormulario rotulo="Senha" htmlFor="cadastro-senha">
                <div className="relative">
                  <input
                    id="cadastro-senha"
                    type={senhaVisivel ? "text" : "password"}
                    required
                    minLength={6}
                    autoComplete="new-password"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    className={cn(CLASSES_CONTROLE, "pr-11")}
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
              </CampoFormulario>
            </div>
            <p className="text-xs text-gray-500">A senha precisa ter pelo menos 6 caracteres.</p>

            {erro && (
              <div
                role="alert"
                className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-sm text-red-800"
              >
                <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-rota-red" />
                <span>{erro}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={enviando}
              className={cn(
                "flex w-full items-center justify-center gap-2 rounded-lg px-4 py-3 text-sm font-bold text-white shadow-sm transition-colors",
                enviando ? "cursor-wait bg-rota-red/70" : "bg-rota-red hover:bg-rota-redDark",
              )}
            >
              {enviando && <Loader2 className="h-4 w-4 animate-spin" />}
              {enviando ? "Criando conta…" : "Criar conta"}
            </button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-gray-500">
          Já tem conta?{" "}
          <Link to="/login" className="font-semibold text-rota-red hover:underline">
            Entrar
          </Link>
        </p>
      </div>
    </div>
  );
}
