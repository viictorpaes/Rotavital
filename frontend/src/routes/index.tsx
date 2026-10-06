import { Navigate, Route, Routes } from "react-router-dom";
import { LayoutPrincipal } from "@/components/layout/LayoutPrincipal";
import PaginaLogin from "@/pages/Login/PaginaLogin";
import PaginaPainel from "@/pages/PainelOperacional/PaginaPainel";
import PaginaEstoque from "@/pages/Estoque/PaginaEstoque";
import PaginaRequisicao from "@/pages/Requisicao/PaginaRequisicao";
import PaginaRede from "@/pages/RedeHospitalar/PaginaRede";
import PaginaPacientes from "@/pages/Pacientes/PaginaPacientes";
import PaginaDoacoes from "@/pages/Doacoes/PaginaDoacoes";
import PaginaPortalDoador from "@/pages/PortalDoador/PaginaPortalDoador";
import PaginaAdmin from "@/pages/Admin/PaginaAdmin";

import { useAutenticacao } from "@/context/ContextoAutenticacao";

function RotaAdmin({ children }: { children: React.ReactNode })
{
  const { usuario } = useAutenticacao();
  if (!usuario)
  {
    return <Navigate to="/login" replace />;
  }
  if (usuario.papel !== "admin")
  {
    return <Navigate to="/painel" replace />;
  }
  return <>{children}</>;
}

export function AppRoutes()
{
  const { usuario } = useAutenticacao();

  return (
    <Routes>
      <Route
        path="/"
        element={
          usuario ? (
            <Navigate
              to={
                usuario.papel === "admin"
                  ? "/admin"
                  : usuario.papel === "doador"
                  ? "/portal-doador"
                  : "/painel"
              }
              replace
            />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route path="/login" element={<PaginaLogin />} />

      <Route path="/portal-doador" element={<PaginaPortalDoador />} />

      <Route element={<LayoutPrincipal />}>
        <Route path="/painel" element={<PaginaPainel />} />
        <Route path="/estoque" element={<PaginaEstoque />} />
        <Route path="/requisicao" element={<PaginaRequisicao />} />
        <Route path="/rede" element={<PaginaRede />} />
        <Route path="/pacientes" element={<PaginaPacientes />} />
        <Route path="/doacoes" element={<PaginaDoacoes />} />
        <Route
          path="/admin"
          element={
            <RotaAdmin>
              <PaginaAdmin />
            </RotaAdmin>
          }
        />
      </Route>

      <Route path="*" element={<Navigate to={usuario ? "/painel" : "/login"} replace />} />
    </Routes>
  );
}

