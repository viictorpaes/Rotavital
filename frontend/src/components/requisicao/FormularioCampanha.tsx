import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2, Heart } from "lucide-react";
import type { CampanhaDTO } from "@/types/dtos";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { useDados } from "@/context/ContextoDados";
import { useHospitais } from "@/hooks/useHospitais";
import { mensagemDeErro } from "@/lib/doacoes";
import { payloadDoRascunho, rascunhoDoPaciente, rascunhoVazio } from "@/lib/pacientes";
import type { RascunhoPaciente } from "@/lib/pacientes";
import { editarCampanha, publicarCampanha } from "@/services/api";
import { cn } from "@/lib/utilitarios";
import { CamposPaciente } from "@/components/pacientes/CamposPaciente";

interface Props
{
  /** Campanha a editar. Sem ela, o formulário publica uma campanha nova. */
  campanha?: CampanhaDTO;
  /** Chamado depois que a edição é gravada. */
  onSalvo?: (campanha: CampanhaDTO) => void;
  onCancelar?: () => void;
}

/**
 * HU-06 — Campanha de doação: publica o paciente nas telas de Doações e de
 * Pacientes para que doadores voluntários o encontrem. Grava no backend
 * (POST /campanhas); com `campanha`, edita os dados divulgados (PUT /campanhas/{id}).
 */
export function FormularioCampanha({ campanha, onSalvo, onCancelar }: Readonly<Props>)
{
  const { usuario } = useAutenticacao();
  const { recarregarCampanhas } = useDados();
  const navigate = useNavigate();
  const paciente = campanha?.paciente;
  const editando = campanha !== undefined;

  const { hospitais, erro: erroHospitais } = useHospitais();
  const [rascunho, setRascunho] = useState<RascunhoPaciente>(() => (paciente ? rascunhoDoPaciente(paciente) : rascunhoVazio()));
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [publicada, setPublicada] = useState<CampanhaDTO | null>(null);

  // Só médico publica e edita campanha (decisão 2 de docs/PERFIS_DE_ACESSO.md); o backend também confere.
  const podeGravar = usuario?.papel === "medico";

  // Campanha nova: o primeiro hospital da lista já vem escolhido.
  useEffect(() =>
  {
    setRascunho((atual) => (atual.hospitalId || hospitais.length === 0 ? atual : { ...atual, hospitalId: hospitais[0].id }));
  }, [hospitais]);

  async function handleSubmit(evento: React.FormEvent)
  {
    evento.preventDefault();
    const dados = payloadDoRascunho(rascunho);

    setEnviando(true);
    setErro(null);
    try
    {
      const salva = campanha ? await editarCampanha(campanha.id, dados) : await publicarCampanha(dados);
      await recarregarCampanhas();
      if (editando) onSalvo?.(salva);
      else setPublicada(salva);
    }
    catch (e)
    {
      setErro(mensagemDeErro(e));
    }
    finally
    {
      setEnviando(false);
    }
  }

  function novaCampanha()
  {
    setPublicada(null);
    // Limpa só o que é de cada paciente; hospital, sexo, tipo, componente e gravidade ficam como estavam.
    setRascunho((atual) => ({ ...atual, nome: "", dataNascimento: "", unidades: "1", causa: "" }));
  }

  if (publicada)
  {
    return (
      <div className="max-w-3xl space-y-4 rounded-xl border border-emerald-200 bg-emerald-50 p-6">
        <p className="flex items-center gap-2 text-lg font-bold text-emerald-700">
          <CheckCircle2 className="h-5 w-5" />
          Campanha publicada
        </p>
        <p className="text-sm text-emerald-800">
          A campanha de <span className="font-semibold">{publicada.paciente.nome}</span> foi gravada e o
          paciente já aparece nas telas de Doações e de Pacientes e no Portal do Doador.
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
      {!editando && (
        <p className="border-l-2 border-rota-red pl-4 text-sm text-gray-500">
          Uma campanha publica o paciente nas telas de <strong>Doações</strong> e{" "}
          <strong>Pacientes</strong> e no Portal do Doador, para que doadores voluntários agendem uma
          doação.
        </p>
      )}

      <form
        onSubmit={handleSubmit}
        className={cn("space-y-5", !editando && "rounded-xl border border-rota-border bg-white p-6 shadow-card")}
      >
        <CamposPaciente valor={rascunho} onChange={setRascunho} hospitais={hospitais} prefixoId="cam" />

        {!podeGravar && (
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
            Só médico pode {editando ? "editar" : "publicar"} campanha de doação.
          </p>
        )}
        {(erro ?? erroHospitais) && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro ?? erroHospitais}
          </p>
        )}

        <div className="flex gap-3">
          {onCancelar && (
            <button
              type="button"
              onClick={onCancelar}
              className="rounded-lg border border-rota-border bg-white px-4 py-3 text-sm font-medium text-gray-700 hover:border-gray-400"
            >
              Cancelar
            </button>
          )}
          <button
            type="submit"
            disabled={!podeGravar || enviando}
            className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-not-allowed disabled:opacity-60"
          >
            <Heart className="h-4 w-4" />
            {enviando ? "Gravando..." : editando ? "Salvar alterações" : "Publicar campanha de doação"}
          </button>
        </div>
      </form>
    </div>
  );
}
