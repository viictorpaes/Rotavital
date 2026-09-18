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
  | "Concentrado de Plaquetas";

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
