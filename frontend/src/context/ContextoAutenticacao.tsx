import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { PapelUsuario, Usuario } from "@/types";
import type { RespostaAcesso, TipoAcessoBackend } from "@/types/dtos";

const CHAVE_ARMAZENAMENTO = "rotavital.usuario";

const PAPEL_DO_BACKEND: Record<TipoAcessoBackend, PapelUsuario> =
{
  MEDICO: "medico",
  DOADOR: "doador",
  ADMIN: "admin",
};

interface ValorContextoAutenticacao
{
  usuario: Usuario | null;
  /** Guarda a sessão devolvida pelo POST /acessos. */
  login: (acesso: RespostaAcesso) => Usuario;
  logout: () => void;
  /** Troca o nome da sessão depois que o doador corrige o perfil. */
  atualizarNome: (nome: string) => void;
}

const ContextoAutenticacao = createContext<ValorContextoAutenticacao | undefined>(undefined);

// Sessão salva por uma versão anterior (só nome e papel, sem login de verdade) não vale mais:
// sem id e tela inicial, a pessoa entra de novo.
function lerSessaoSalva(): Usuario | null
{
  try
  {
    const bruto = sessionStorage.getItem(CHAVE_ARMAZENAMENTO);
    const salvo = bruto ? (JSON.parse(bruto) as Partial<Usuario>) : null;
    if (salvo?.id && salvo.nome && salvo.papel && salvo.telaInicial)
    {
      return salvo as Usuario;
    }
    return null;
  }
  catch
  {
    return null;
  }
}

export function ProvedorAutenticacao({ children }: { children: ReactNode })
{
  const [usuario, setUsuario] = useState<Usuario | null>(lerSessaoSalva);

  useEffect(() =>
  {
    try
    {
      if (usuario)
      {
        sessionStorage.setItem(CHAVE_ARMAZENAMENTO, JSON.stringify(usuario));
      }
      else
      {
        sessionStorage.removeItem(CHAVE_ARMAZENAMENTO);
      }
    }
    catch
    {
      // Sem sessionStorage (ex.: navegação privada bloqueada) a sessão só dura enquanto a aba estiver aberta.
    }
  }, [usuario]);

  function login(acesso: RespostaAcesso): Usuario
  {
    const novo: Usuario =
    {
      id: acesso.id,
      nome: acesso.nome,
      papel: PAPEL_DO_BACKEND[acesso.tipoAcesso],
      telaInicial: acesso.telaInicial,
      accessToken: acesso.accessToken,
    };
    setUsuario(novo);
    return novo;
  }

  function logout()
  {
    setUsuario(null);
  }

  function atualizarNome(nome: string)
  {
    setUsuario((atual) => (atual ? { ...atual, nome } : atual));
  }

  return (
    <ContextoAutenticacao.Provider value={{ usuario, login, logout, atualizarNome }}>
      {children}
    </ContextoAutenticacao.Provider>
  );
}

export function useAutenticacao()
{
  const ctx = useContext(ContextoAutenticacao);
  if (!ctx) throw new Error("useAutenticacao deve ser usado dentro de ProvedorAutenticacao");
  return ctx;
}
