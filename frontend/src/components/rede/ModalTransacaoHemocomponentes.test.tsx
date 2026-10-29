import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import { ModalTransacaoHemocomponentes } from "./ModalTransacaoHemocomponentes";
import { hospitais } from "@/data/redeMock";

const rotaMock = {
  pontos: [
    { latitude: -8.053, longitude: -34.8952 },
    { latitude: -8.0479, longitude: -34.8993 },
  ],
  distanciaKm: 2.1,
  tempoMin: 7,
  doRoteador: false,
};

describe("ModalTransacaoHemocomponentes", () => {
  it("exibe a melhor rota e o painel logístico do hospital selecionado", () => {
    render(
      <ModalTransacaoHemocomponentes
        hospital={hospitais[1]}
        rota={rotaMock}
        onClose={() => undefined}
      />,
    );

    expect(screen.getByText(/melhor rota/i)).toBeInTheDocument();
    expect(screen.getByText(/Cesar Life/i)).toBeInTheDocument();
    expect(screen.getByText(/2.1 km · 7 min/i)).toBeInTheDocument();
    expect(screen.getByText(/Temperatura ideal/i)).toBeInTheDocument();
    expect(screen.getByText(/Cadeia fria/i)).toBeInTheDocument();
    expect(screen.getByText(/Veículo refrigerado monitorado/i)).toBeInTheDocument();
  });

  it("altera o componente selecionado e mostra a recomendação de transporte correspondente", async () => {
    const user = userEvent.setup();

    render(
      <ModalTransacaoHemocomponentes
        hospital={hospitais[1]}
        rota={rotaMock}
        onClose={() => undefined}
      />,
    );

    await user.selectOptions(screen.getByLabelText(/hemocomponente/i), "Plasma Fresco Congelado");

    expect(screen.getByText(/caixa térmica com gelo seco/i)).toBeInTheDocument();
    expect(screen.getAllByText(/-30 a -18\s*°C/i).length).toBeGreaterThan(0);
    expect(screen.queryByText(/2 a 6\s*°C/i)).not.toBeInTheDocument();
  });
});
