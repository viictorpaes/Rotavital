/// <reference types="vite/client"/>

interface ImportMetaEnv
{
  /** Chave do Google Maps para o navegador. Vazia: a tela de Rede usa o OpenStreetMap. */
  readonly VITE_GOOGLE_MAPS_KEY?: string;
}
