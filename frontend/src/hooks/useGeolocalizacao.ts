import { useCallback, useEffect, useRef, useState } from "react";
import {
  OPCOES_GEOLOCALIZACAO,
  foiNegada,
  mensagemDoErroDeGeolocalizacao,
  motivoSemGeolocalizacao,
  posicaoDoNavegador,
  type PosicaoDoador,
  type SituacaoGeolocalizacao,
} from "@/lib/geolocalizacao";

interface Opcoes
{
  /**
   * Se o doador já liberou a localização deste site antes, lê a posição sem esperar o clique:
   * o navegador não pergunta de novo, então não há um segundo consentimento a pedir.
   */
  pedirSeJaPermitida?: boolean;
}

/**
 * PI3-261 — Pede e lê a posição atual do doador (Geolocation API). Nada acontece até chamar
 * solicitar(), que é o que dispara a pergunta "Permitir localização?" do navegador.
 */
export function useGeolocalizacao({ pedirSeJaPermitida = false }: Opcoes = {})
{
  const [situacao, setSituacao] = useState<SituacaoGeolocalizacao>("aguardando");
  const [posicao, setPosicao] = useState<PosicaoDoador | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  // A resposta do GPS pode chegar depois que o modal fechou: aí ela é ignorada.
  const montado = useRef(true);

  useEffect(() =>
  {
    montado.current = true;
    return () =>
    {
      montado.current = false;
    };
  }, []);

  const solicitar = useCallback(() =>
  {
    const motivo = motivoSemGeolocalizacao();
    if (motivo)
    {
      setErro(motivo);
      setSituacao("indisponivel");
      return;
    }

    setErro(null);
    setSituacao("localizando");
    navigator.geolocation.getCurrentPosition(
      (lida) =>
      {
        if (!montado.current) return;
        setPosicao(posicaoDoNavegador(lida));
        setSituacao("localizado");
      },
      (falha) =>
      {
        if (!montado.current) return;
        setErro(mensagemDoErroDeGeolocalizacao(falha));
        setSituacao(foiNegada(falha) ? "negado" : "indisponivel");
      },
      OPCOES_GEOLOCALIZACAO,
    );
  }, []);

  // Permissions API: "granted" quer dizer que o doador já consentiu antes. Navegador sem ela segue no clique.
  useEffect(() =>
  {
    if (!pedirSeJaPermitida || motivoSemGeolocalizacao() || !navigator.permissions?.query) return;
    let ativo = true;
    navigator.permissions
      .query({ name: "geolocation" })
      .then((estado) =>
      {
        if (ativo && estado.state === "granted") solicitar();
      })
      .catch(() =>
      {
        // Sem a consulta, o doador clica no botão normalmente.
      });
    return () =>
    {
      ativo = false;
    };
  }, [pedirSeJaPermitida, solicitar]);

  return { situacao, posicao, erro, solicitar };
}
