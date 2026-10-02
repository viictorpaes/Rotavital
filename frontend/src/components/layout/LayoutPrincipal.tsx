import { Outlet } from "react-router-dom";
import { BarraLateral } from "./BarraLateral";

export function LayoutPrincipal()
{
  return (
    <div className="flex min-h-screen bg-rota-bg">
      <BarraLateral />
      <main className="min-w-0 flex-1 overflow-y-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
