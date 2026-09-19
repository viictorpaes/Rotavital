export type TipoSanguineo = "A+" | "A-" | "B+" | "B-" | "AB+" | "AB-" | "O+" | "O-";

export type PapelUsuario = "medico" | "doador";

export interface Usuario
{
  nome: string;
  papel: PapelUsuario;
}

export type Sexo = "Feminino" | "Masculino";

export type UrgenciaNecessidade = "critico" | "atencao" | "estavel";

export type TipoComponente =
  | "Concentrado de Hemácias"
  | "Plasma Fresco Congelado"
  | "Concentrado de Plaquetas"
  | "Crioprecipitado";

export interface PessoaNecessitada
{
  id: string;
  nome: string;
  sexo: Sexo;
  idade: number;
  tipoSanguineo: TipoSanguineo;
  componente: TipoComponente;
  /** Unidades previstas para o procedimento — base da baixa FEFO (HU-08). */
  unidadesNecessarias: number;
  distanciaKm: number;
  status: UrgenciaNecessidade;
  causa: string;
}

/** Como o hemocomponente do procedimento foi obtido (HU-08). */
export type OrigemAtendimento = "doacao-externa" | "estoque-interno";

/** Unidades retiradas de um lote específico durante a baixa FEFO. */
export interface ConsumoDeLote
{
  codigo: string;
  unidades: number;
}

/** Registro de um procedimento concluído (HU-08). */
export interface ProcedimentoConcluido
{
  protocolo: string;
  pacienteId: string;
  paciente: string;
  componente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  origem: OrigemAtendimento;
  /** Unidades efetivamente baixadas do estoque — sempre 0 na doação externa. */
  unidadesBaixadas: number;
  /** Lotes tocados pela baixa FEFO — vazio na doação externa. */
  consumos: ConsumoDeLote[];
  /** Unidades que o estoque não cobriu, quando houver. */
  unidadesFaltantes: number;
}

export interface FaixaTemperatura
{
  minima: number;
  maxima: number;
}

export interface LoteHemocomponente
{
  id: string;
  codigo: string;
  componente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  unidades: number;
  volumeMl: number;
  /** Data de validade no formato ISO (AAAA-MM-DD). */
  dataValidade: string;
  temperaturaAtual: number;
  temperaturaIdeal: FaixaTemperatura;
  /** Localização física: refrigerador/prateleira/nível. */
  localizacao: string;
}

/** Origem de um aviso do painel operacional (HU-02). */
export type OrigemAviso = "paciente" | "estoque" | "validade";

export interface AvisoPainel
{
  id: string;
  origem: OrigemAviso;
  severidade: UrgenciaNecessidade;
  titulo: string;
  detalhe: string;
  /** Momento em que o aviso foi registrado — usado na ordenação por tempo. */
  registradoEm: Date;
  /** Rota para onde o atalho do aviso leva. */
  destino: string;
}

/** Ponto da rede de distribuição (HU-07) — hemocentro ou hospital conectado. */
export interface PontoDeRede
{
  id: string;
  nome: string;
  endereco: string;
  latitude: number;
  longitude: number;
  /** `true` para o hemocentro de origem (Cesar Life). */
  origem?: boolean;
}

/** Coordenada geográfica de um vértice do trajeto. */
export interface Coordenada
{
  latitude: number;
  longitude: number;
}

export interface Conexao
{
  hospitalId: string;
  distanciaKm: number;
  tempoMin: number;
  status: UrgenciaNecessidade;
}

/** Ala médica de destino de uma requisição (HU-04). */
export type AlaMedica =
  | "UTI Adulto"
  | "UTI Neonatal"
  | "Centro Cirúrgico"
  | "Emergência"
  | "Oncologia"
  | "Hemodiálise"
  | "Maternidade";

/** Requisição preenchida pelo médico (HU-04). */
export interface RequisicaoHemocomponente
{
  protocolo: string;
  componente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  ala: AlaMedica;
  unidades: number;
  paciente: string;
  urgencia: UrgenciaNecessidade;
  /** Código do lote sugerido pela regra FEFO — ausente quando não há estoque. */
  loteSugerido?: string;
}

/** Remessa vinda de outra instituição, aguardando conferência física (HU-05). */
export interface RequisicaoRecebida
{
  id: string;
  componente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  unidades: number;
  origem: string;
  /** Momento da chegada, já formatado para exibição (mock). */
  chegadaEm: string;
}
