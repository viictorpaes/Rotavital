import { useEffect, useState } from "react";
import type { PontoRedeDTO } from "@/types/dtos";
import { listarPontosRede } from "@/services/api";

/** Hospitais da rede (GET /pontos, tipo HOSPITAL), para os formulários de paciente e de campanha. */
export function useHospitais()
{
  const [hospitais, setHospitais] = useState<PontoRedeDTO[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() =>
  {
    let ativo = true;
    listarPontosRede()
      .then((pontos) =>
      {
        if (ativo) setHospitais(pontos.filter((ponto) => ponto.tipo === "HOSPITAL"));
      })
      .catch(() =>
      {
        if (ativo) setErro("Não foi possível carregar a lista de hospitais.");
      });
    return () =>
    {
      ativo = false;
    };
  }, []);

  return { hospitais, erro };
}
