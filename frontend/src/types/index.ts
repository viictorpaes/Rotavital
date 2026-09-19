export type TipoSanguineo = "A+" | "A-" | "B+" | "B-" | "AB+" | "AB-" | "O+" | "O-";

export type PapelUsuario = "medico" | "doador";

export interface Usuario
{
  nome: string;
  papel: PapelUsuario;
}

export type Sexo = "Feminino" | "Masculino";

export type UrgenciaNecessidade = "critico" | "atencao" | "estavel";

export interface PessoaNecessitada
{
  id: string;
  nome: string;
  sexo: Sexo;
  idade: number;
  tipoSanguineo: TipoSanguineo;
  componente: string;
  distanciaKm: number;
  status: UrgenciaNecessidade;
  causa: string;
}

export type TipoComponente =
  | "Concentrado de Hemácias"
  | "Plasma Fresco Congelado"
  | "Concentrado de Plaquetas"
  | "Crioprecipitado";

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
