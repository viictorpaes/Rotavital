import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { PapelUsuario, Usuario } from "@/types";

const CHAVE_ARMAZENAMENTO = "rotavital.usuario";

interface ValorContextoAutenticacao
{
  usuario: Usuario | null;
  login: (nome: string, papel: PapelUsuario) => void;
  logout: () => void;
}

const ContextoAutenticacao = createContext<ValorContextoAutenticacao | undefined>(undefined);

export function ProvedorAutenticacao({ children }: { children: ReactNode })
{
  const [usuario, setUsuario] = useState<Usuario | null>(() =>
  {
    try
    {
      const bruto = sessionStorage.getItem(CHAVE_ARMAZENAMENTO);
      return bruto ? (JSON.parse(bruto) as Usuario) : null;
    }
    catch
    {
      return null;
    }
  });

  useEffect(() =>
  {
    if (usuario)
    {
      sessionStorage.setItem(CHAVE_ARMAZENAMENTO, JSON.stringify(usuario));
    }
    else
    {
      sessionStorage.removeItem(CHAVE_ARMAZENAMENTO);
    }
  }, [usuario]);

  function login(nome: string, papel: PapelUsuario)
  {
    setUsuario({ nome: nome.trim(), papel });
  }

  function logout()
  {
    setUsuario(null);
  }

  return (
    <ContextoAutenticacao.Provider value={{ usuario, login, logout }}>
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
