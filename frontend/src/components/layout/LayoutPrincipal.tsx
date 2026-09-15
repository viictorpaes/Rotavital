import { Navigate, Outlet } from "react-router-dom";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { BarraLateral } from "./BarraLateral";

export function LayoutPrincipal()
{
  const { usuario } = useAutenticacao();

  if (usuario?.papel !== "medico")
  {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="flex min-h-screen bg-rota-bg">
      <BarraLateral />
      <main className="min-w-0 flex-1 overflow-y-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
