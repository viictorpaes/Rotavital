import type { Dispatch, SetStateAction } from "react";
import type { Sexo, TipoComponente, TipoSanguineo } from "@/types";
import type { PontoRedeDTO } from "@/types/dtos";
import type { RascunhoPaciente } from "@/lib/pacientes";
import { TIPOS_SANGUINEOS } from "@/lib/estoque";
import { COMPONENTES } from "@/lib/requisicao";
import { cn } from "@/lib/utilitarios";
import { CLASSES_CONTROLE, CampoFormulario } from "@/components/requisicao/CampoFormulario";
import { SeletorUrgencia } from "@/components/requisicao/SeletorUrgencia";

const SEXOS: Sexo[] = ["Feminino", "Masculino"];

interface Props
{
  valor: RascunhoPaciente;
  /** O setState do rascunho: cada campo muda com atualização funcional, sem perder mudanças seguidas. */
  onChange: Dispatch<SetStateAction<RascunhoPaciente>>;
  hospitais: PontoRedeDTO[];
  /** Prefixo dos ids dos campos, para dois formulários na mesma tela não repetirem id. */
  prefixoId: string;
}

/**
 * Campos clínicos do paciente, iguais no cadastro de pacientes (PI3-195) e na campanha de
 * doação (HU-06): nome, nascimento, sexo, hospital, tipo, componente, unidades, causa e gravidade.
 */
export function CamposPaciente({ valor, onChange, hospitais, prefixoId }: Readonly<Props>)
{
  function mudar<C extends keyof RascunhoPaciente>(campo: C, novo: RascunhoPaciente[C])
  {
    onChange((atual) => ({ ...atual, [campo]: novo }));
  }

  const id = (campo: string) => `${prefixoId}-${campo}`;

  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <CampoFormulario rotulo="Nome do paciente" htmlFor={id("nome")}>
          <input
            id={id("nome")}
            type="text"
            required
            value={valor.nome}
            onChange={(e) => mudar("nome", e.target.value)}
            placeholder="Ex.: Ana Paula Ramos"
            className={CLASSES_CONTROLE}
          />
        </CampoFormulario>

        <CampoFormulario rotulo="Data de nascimento" htmlFor={id("nascimento")}>
          <input
            id={id("nascimento")}
            type="date"
            required
            min="1900-01-02"
            max={new Date().toISOString().slice(0, 10)}
            value={valor.dataNascimento}
            onChange={(e) => mudar("dataNascimento", e.target.value)}
            className={CLASSES_CONTROLE}
          />
        </CampoFormulario>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <CampoFormulario rotulo="Sexo">
          <div role="group" aria-label="Sexo" className="flex gap-2">
            {SEXOS.map((opcao) => (
              <button
                key={opcao}
                type="button"
                aria-pressed={valor.sexo === opcao}
                onClick={() => mudar("sexo", opcao)}
                className={cn(
                  "flex-1 rounded-lg border px-3 py-2.5 font-mono text-sm font-semibold transition-colors",
                  valor.sexo === opcao
                    ? "border-gray-900 bg-gray-900 text-white"
                    : "border-rota-border bg-white text-gray-600 hover:border-gray-400",
                )}
              >
                {opcao}
              </button>
            ))}
          </div>
        </CampoFormulario>

        <CampoFormulario rotulo="Hospital" htmlFor={id("hospital")}>
          <select
            id={id("hospital")}
            required
            value={valor.hospitalId}
            onChange={(e) => mudar("hospitalId", e.target.value)}
            className={CLASSES_CONTROLE}
          >
            {hospitais.length === 0 && <option value="">Carregando hospitais...</option>}
            {hospitais.map((hospital) => (
              <option key={hospital.id} value={hospital.id}>
                {hospital.nome}
              </option>
            ))}
          </select>
        </CampoFormulario>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <CampoFormulario rotulo="Tipo sanguíneo" htmlFor={id("tipo")}>
          <select
            id={id("tipo")}
            value={valor.tipoSanguineo}
            onChange={(e) => mudar("tipoSanguineo", e.target.value as TipoSanguineo)}
            className={CLASSES_CONTROLE}
          >
            {TIPOS_SANGUINEOS.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </CampoFormulario>

        <CampoFormulario rotulo="Hemocomponente necessário" htmlFor={id("componente")}>
          <select
            id={id("componente")}
            value={valor.componente}
            onChange={(e) => mudar("componente", e.target.value as TipoComponente)}
            className={CLASSES_CONTROLE}
          >
            {COMPONENTES.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </CampoFormulario>

        <CampoFormulario rotulo="Unidades necessárias" htmlFor={id("unidades")}>
          <input
            id={id("unidades")}
            type="number"
            min={1}
            max={50}
            required
            value={valor.unidades}
            onChange={(e) => mudar("unidades", e.target.value)}
            className={CLASSES_CONTROLE}
          />
        </CampoFormulario>
      </div>

      <CampoFormulario rotulo="Causa / diagnóstico" htmlFor={id("causa")}>
        <textarea
          id={id("causa")}
          required
          rows={3}
          value={valor.causa}
          onChange={(e) => mudar("causa", e.target.value)}
          placeholder="Ex.: Leucemia aguda — necessita de transfusão semanal"
          className={cn(CLASSES_CONTROLE, "resize-none")}
        />
      </CampoFormulario>

      <CampoFormulario rotulo="Nível de gravidade">
        <SeletorUrgencia rotuloGrupo="Nível de gravidade" value={valor.gravidade} onChange={(g) => mudar("gravidade", g)} />
      </CampoFormulario>
    </>
  );
}
