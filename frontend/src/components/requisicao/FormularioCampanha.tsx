import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2, Heart } from "lucide-react";
import type { Sexo, TipoComponente, TipoSanguineo, UrgenciaNecessidade } from "@/types";
import type { CampanhaDTO, CampanhaPayloadDTO, PontoRedeDTO } from "@/types/dtos";
import { useAutenticacao } from "@/context/ContextoAutenticacao";
import { useDados } from "@/context/ContextoDados";
import { TIPOS_SANGUINEOS, converterTipoComponente, converterTipoSanguineo } from "@/lib/estoque";
import { COMPONENTES } from "@/lib/requisicao";
import {
  componenteParaBackend,
  gravidadeDoBackend,
  gravidadeParaBackend,
  mensagemDeErro,
  sexoDoBackend,
  sexoParaBackend,
  tipoSanguineoParaBackend,
} from "@/lib/doacoes";
import { editarCampanha, listarPontosRede, publicarCampanha } from "@/services/api";
import { cn } from "@/lib/utilitarios";
import { CLASSES_CONTROLE, CampoFormulario } from "./CampoFormulario";
import { SeletorUrgencia } from "./SeletorUrgencia";

const SEXOS: Sexo[] = ["Feminino", "Masculino"];

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

  const [hospitais, setHospitais] = useState<PontoRedeDTO[]>([]);
  const [hospitalId, setHospitalId] = useState(paciente?.hospitalId ?? "");
  const [nome, setNome] = useState(paciente?.nome ?? "");
  const [dataNascimento, setDataNascimento] = useState(paciente?.dataNascimento ?? "");
  const [sexo, setSexo] = useState<Sexo>(paciente ? sexoDoBackend(paciente.sexo) : "Feminino");
  const [tipoSanguineo, setTipoSanguineo] = useState<TipoSanguineo>(
    paciente ? converterTipoSanguineo(paciente.tipoSanguineo) : "O-",
  );
  const [componente, setComponente] = useState<TipoComponente>(
    paciente ? converterTipoComponente(paciente.tipoComponente) : COMPONENTES[0],
  );
  const [unidades, setUnidades] = useState(String(paciente?.unidadesNecessarias ?? 1));
  const [causa, setCausa] = useState(paciente?.causa ?? "");
  const [gravidade, setGravidade] = useState<UrgenciaNecessidade>(
    paciente ? gravidadeDoBackend(paciente.gravidade) : "atencao",
  );
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const [publicada, setPublicada] = useState<CampanhaDTO | null>(null);

  // Só médico publica e edita campanha (decisão 2 de docs/PERFIS_DE_ACESSO.md); o backend também confere.
  const podeGravar = usuario?.papel === "medico";

  useEffect(() =>
  {
    listarPontosRede()
      .then((pontos) =>
      {
        const lista = pontos.filter((ponto) => ponto.tipo === "HOSPITAL");
        setHospitais(lista);
        setHospitalId((atual) => atual || lista[0]?.id || "");
      })
      .catch(() => setErro("Não foi possível carregar a lista de hospitais."));
  }, []);

  async function handleSubmit(evento: React.FormEvent)
  {
    evento.preventDefault();
    const dados: CampanhaPayloadDTO = {
      hospitalId,
      nome,
      sexo: sexoParaBackend(sexo),
      dataNascimento,
      tipoSanguineo: tipoSanguineoParaBackend(tipoSanguineo),
      tipoComponente: componenteParaBackend(componente),
      unidadesNecessarias: Number(unidades),
      causa,
      gravidade: gravidadeParaBackend(gravidade),
    };

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
    setNome("");
    setDataNascimento("");
    setUnidades("1");
    setCausa("");
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

          <CampoFormulario rotulo="Data de nascimento" htmlFor="cam-nascimento">
            <input
              id="cam-nascimento"
              type="date"
              required
              min="1900-01-02"
              max={new Date().toISOString().slice(0, 10)}
              value={dataNascimento}
              onChange={(e) => setDataNascimento(e.target.value)}
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

          <CampoFormulario rotulo="Hospital" htmlFor="cam-hospital">
            <select
              id="cam-hospital"
              required
              value={hospitalId}
              onChange={(e) => setHospitalId(e.target.value)}
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

          <CampoFormulario rotulo="Unidades necessárias" htmlFor="cam-unidades">
            <input
              id="cam-unidades"
              type="number"
              min={1}
              max={50}
              required
              value={unidades}
              onChange={(e) => setUnidades(e.target.value)}
              className={CLASSES_CONTROLE}
            />
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

        {!podeGravar && (
          <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
            Só médico pode {editando ? "editar" : "publicar"} campanha de doação.
          </p>
        )}
        {erro && (
          <p role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {erro}
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
