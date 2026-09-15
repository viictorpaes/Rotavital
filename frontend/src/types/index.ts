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
