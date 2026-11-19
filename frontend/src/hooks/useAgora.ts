import { useEffect, useState } from "react";

/** Hora atual, em ms, que se atualiza sozinha: para os contadores da tela andarem sem chamar a API. */
export function useAgora(intervaloMs: number)
{
  const [agora, setAgora] = useState(() => Date.now());

  useEffect(() =>
  {
    const relogio = setInterval(() => setAgora(Date.now()), intervaloMs);
    return () => clearInterval(relogio);
  }, [intervaloMs]);

  return agora;
}
