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

export function AppRoutes()
{
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<PaginaLogin />} />

      <Route path="/portal-doador" element={<PaginaPortalDoador />} />

      <Route element={<LayoutPrincipal />}>
        <Route path="/painel" element={<PaginaPainel />} />
        <Route path="/estoque" element={<PaginaEstoque />} />
        <Route path="/requisicao" element={<PaginaRequisicao />} />
        <Route path="/rede" element={<PaginaRede />} />
        <Route path="/pacientes" element={<PaginaPacientes />} />
        <Route path="/doacoes" element={<PaginaDoacoes />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
