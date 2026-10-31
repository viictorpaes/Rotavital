import type { Dispatch, SetStateAction } from "react";
import type { TipoSanguineo } from "@/types";
import type { RascunhoDoador } from "@/lib/doacoes";
import { TIPOS_SANGUINEOS } from "@/lib/estoque";
import { CLASSES_CONTROLE, CampoFormulario } from "@/components/requisicao/CampoFormulario";

interface Props
{
  valor: RascunhoDoador;
  onChange: Dispatch<SetStateAction<RascunhoDoador>>;
  /** Prefixo dos ids dos campos, para o cadastro e a edição não repetirem id. */
  prefixoId: string;
}

/** Dados pessoais do doador, iguais no cadastro e na edição do perfil (PI3-203). */
export function CamposDoador({ valor, onChange, prefixoId }: Readonly<Props>)
{
  function mudar<C extends keyof RascunhoDoador>(campo: C, novo: RascunhoDoador[C])
  {
    onChange((atual) => ({ ...atual, [campo]: novo }));
  }

  const id = (campo: string) => `${prefixoId}-${campo}`;

  return (
    <>
      <CampoFormulario rotulo="Nome completo" htmlFor={id("nome")}>
        <input
          id={id("nome")}
          type="text"
          required
          autoComplete="name"
          maxLength={120}
          value={valor.nome}
          onChange={(e) => mudar("nome", e.target.value)}
          className={CLASSES_CONTROLE}
        />
      </CampoFormulario>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <CampoFormulario rotulo="Data de nascimento" htmlFor={id("nascimento")}>
          <input
            id={id("nascimento")}
            type="date"
            required
            autoComplete="bday"
            value={valor.dataNascimento}
            onChange={(e) => mudar("dataNascimento", e.target.value)}
            className={CLASSES_CONTROLE}
          />
        </CampoFormulario>

        <CampoFormulario rotulo="Tipo sanguíneo" htmlFor={id("tipo")}>
          <select
            id={id("tipo")}
            value={valor.tipoSanguineo}
            onChange={(e) => mudar("tipoSanguineo", e.target.value as TipoSanguineo | "")}
            className={CLASSES_CONTROLE}
          >
            <option value="">Ainda não sei</option>
            {TIPOS_SANGUINEOS.map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo}
              </option>
            ))}
          </select>
        </CampoFormulario>
      </div>

      <CampoFormulario rotulo="Telefone (opcional)" htmlFor={id("telefone")}>
        <input
          id={id("telefone")}
          type="tel"
          autoComplete="tel-national"
          placeholder="(81) 99876-5432"
          value={valor.telefone}
          onChange={(e) => mudar("telefone", e.target.value)}
          className={CLASSES_CONTROLE}
        />
      </CampoFormulario>

      <p className="text-xs text-gray-500">Para doar é preciso ter entre 16 e 69 anos.</p>
    </>
  );
}
