import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2, Heart } from "lucide-react";
import type { Sexo, TipoComponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import { useDados } from "@/context/ContextoDados";
import { TIPOS_SANGUINEOS } from "@/lib/estoque";
import { COMPONENTES } from "@/lib/requisicao";
import { cn } from "@/lib/utilitarios";
import { CLASSES_CONTROLE, CampoFormulario } from "./CampoFormulario";
import { SeletorUrgencia } from "./SeletorUrgencia";

const SEXOS: Sexo[] = ["Feminino", "Masculino"];

/**
 * HU-06 — Campanha de doação: publica o paciente nas telas de Doações e de
 * Pacientes para que doadores voluntários o encontrem.
 */
export function FormularioCampanha()
{
  const { publicarCampanha } = useDados();
  const navigate = useNavigate();

  const [nome, setNome] = useState("");
  const [idade, setIdade] = useState("");
  const [sexo, setSexo] = useState<Sexo>("Feminino");
  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>("O-");
  const [componente, setComponente] = useState<TipoComponente>(COMPONENTES[0]);
  const [causa, setCausa] = useState("");
  const [gravidade, setGravidade] = useState<UrgenciaNecessidade>("atencao");
  const [protocolo, setProtocolo] = useState<string | null>(null);

  function handleSubmit(evento: React.FormEvent)
  {
    evento.preventDefault();
    setProtocolo(
      publicarCampanha({
        nome,
        sexo,
        idade: Number(idade),
        tipoSanguineo,
        componente,
        causa,
        gravidade,
      }),
    );
  }

  function novaCampanha()
  {
    setProtocolo(null);
    setNome("");
    setIdade("");
    setCausa("");
  }

  if (protocolo)
  {
    return (
      <div className="max-w-3xl space-y-4 rounded-xl border border-emerald-200 bg-emerald-50 p-6">
        <p className="flex items-center gap-2 text-lg font-bold text-emerald-700">
          <CheckCircle2 className="h-5 w-5" />
          Campanha publicada
        </p>
        <p className="text-sm text-emerald-800">
          Protocolo <span className="font-mono font-semibold">{protocolo}</span>. O paciente já aparece
          nas telas de Doações e de Pacientes.
        </p>
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={() => navigate("/doacoes")}
            className="rounded-lg bg-rota-red px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
          >
            Ver em Doações
          </button>
          <button
            type="button"
            onClick={() => navigate("/pacientes")}
            className="rounded-lg bg-gray-900 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-gray-700"
          >
            Ver em Pacientes
          </button>
          <button
            type="button"
            onClick={novaCampanha}
            className="rounded-lg border border-rota-border bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:border-gray-400"
          >
            Nova campanha
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl space-y-4">
      <p className="border-l-2 border-rota-red pl-4 text-sm text-gray-500">
        Uma campanha publica o paciente nas telas de <strong>Doações</strong> e{" "}
        <strong>Pacientes</strong>, permitindo que doadores voluntários se cadastrem para ajudar
        diretamente.
      </p>

      <form
        onSubmit={handleSubmit}
        className="space-y-5 rounded-xl border border-rota-border bg-white p-6 shadow-card"
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <CampoFormulario rotulo="Nome do paciente" htmlFor="cam-nome">
            <input
              id="cam-nome"
              type="text"
              required
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Ex.: Ana Paula Ramos"
              className={CLASSES_CONTROLE}
            />
          </CampoFormulario>

          <CampoFormulario rotulo="Idade" htmlFor="cam-idade">
            <input
              id="cam-idade"
              type="number"
              min={1}
              max={120}
              required
              value={idade}
              onChange={(e) => setIdade(e.target.value)}
              placeholder="Ex.: 42"
              className={CLASSES_CONTROLE}
            />
          </CampoFormulario>
        </div>

        <CampoFormulario rotulo="Sexo">
          <div role="group" aria-label="Sexo" className="flex gap-2">
            {SEXOS.map((opcao) => (
              <button
                key={opcao}
                type="button"
                aria-pressed={sexo === opcao}
                onClick={() => setSexo(opcao)}
                className={cn(
                  "flex-1 rounded-lg border px-3 py-2.5 font-mono text-sm font-semibold transition-colors",
                  sexo === opcao
                    ? "border-gray-900 bg-gray-900 text-white"
                    : "border-rota-border bg-white text-gray-600 hover:border-gray-400",
                )}
              >
                {opcao}
              </button>
            ))}
          </div>
        </CampoFormulario>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <CampoFormulario rotulo="Tipo sanguíneo" htmlFor="cam-tipo">
            <select
              id="cam-tipo"
              value={tipoSanguineo}
              onChange={(e) => setTipoSanguineo(e.target.value as TipoSanguineo)}
              className={CLASSES_CONTROLE}
            >
              {TIPOS_SANGUINEOS.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </CampoFormulario>

          <CampoFormulario rotulo="Hemocomponente necessário" htmlFor="cam-componente">
            <select
              id="cam-componente"
              value={componente}
              onChange={(e) => setComponente(e.target.value as TipoComponente)}
              className={CLASSES_CONTROLE}
            >
              {COMPONENTES.map((item) => (
                <option key={item}>{item}</option>
              ))}
            </select>
          </CampoFormulario>
        </div>

        <CampoFormulario rotulo="Causa / diagnóstico" htmlFor="cam-causa">
          <textarea
            id="cam-causa"
            required
            rows={3}
            value={causa}
            onChange={(e) => setCausa(e.target.value)}
            placeholder="Ex.: Leucemia aguda — necessita de transfusão semanal"
            className={cn(CLASSES_CONTROLE, "resize-none")}
          />
        </CampoFormulario>

        <CampoFormulario rotulo="Nível de gravidade">
          <SeletorUrgencia rotuloGrupo="Nível de gravidade" value={gravidade} onChange={setGravidade} />
        </CampoFormulario>

        <button
          type="submit"
          className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark"
        >
          <Heart className="h-4 w-4" />
          Publicar campanha de doação
        </button>
      </form>
    </div>
  );
}
