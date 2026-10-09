# Diagrama do RotaVital em draw.io

## Objetivo

Este documento descreve o diagrama de arquitetura do RotaVital entregue na atividade de RSD: redes e
sub-redes, protocolo e porta em todas as setas e legenda obrigatória. O diagrama responde às três perguntas
do enunciado: **quem fala com quem**, **como cada um fala** (protocolo/porta) e **onde cada um mora**
(VPC, sub-redes, público × privado).

## Arquivos

- [`Arquitetura-RotaVital-Victor-Paes.drawio`](./Arquitetura-RotaVital-Victor-Paes.drawio): arquivo-fonte editável,
  aberto no [app.diagrams.net](https://app.diagrams.net) (`File → Open from → Device`) ou na extensão draw.io
  do VS Code.
- [`Arquitetura-RotaVital-Victor-Paes.png`](./Arquitetura-RotaVital-Victor-Paes.png): exportação PNG a 300% (4890 × 3000
  px), usada na seção 3 do PDF.
- [`../pdf/Arquitetura-RotaVital-Victor-Paes.pdf`](../pdf/Arquitetura-RotaVital-Victor-Paes.pdf): documento entregue, com o diagrama e
  as tabelas que ele referencia.

## Camadas do `.drawio`

O arquivo separa o desenho em três camadas (painel **Layers**, `Ctrl+Shift+L`):

| Camada | Conteúdo |
|---|---|
| Redes | Área *Internet* (fora da VPC), VPC `10.0.0.0/16` e as três sub-redes |
| Componentes e ligações | Os 6 componentes e as 5 setas rotuladas |
| Legenda | Caixa com cores, formas e tipos de linha |

## Estrutura do desenho

Fluxo da esquerda (usuário) para a direita (banco):

| Área | CIDR | Tipo | Componentes |
|---|---|---|---|
| Internet (fora da VPC) | — | Externa | Navegador (executa a SPA React), OSRM, OpenStreetMap |
| VPC RotaVital | `10.0.0.0/16` | Rede principal | As três sub-redes abaixo |
| `sub-publica` | `10.0.1.0/24` | Pública | Nginx (container `frontend`) |
| `sub-app` | `10.0.10.0/24` | Privada de aplicação | Backend API (container `backend`) |
| `sub-dados` | `10.0.20.0/24` | Privada de dados, sem internet | PostgreSQL (Supabase) |

Cada caixa de sub-rede traz no cabeçalho o CIDR e as regras de entrada e saída. A caixa do **Backend API**
lista os cinco controllers (`EstoqueController`, `RotaController`, `AcessoController`,
`DiagnosticoController` e `BenchmarkController`) e, em laranja, os recursos que existem só no contrato
OpenAPI (`/hemocomponentes`, `/requisicoes`, `/requisicoes/{id}/alocacoes`, `/entregas/{id}/leituras`).

## Setas

Todas as setas são linhas contínuas (chamadas síncronas) e seguem o rótulo `[nº] PROTOCOLO / porta`. O
número é a linha correspondente da tabela de ligações em [`LIGACOES_E_REDES.md`](./LIGACOES_E_REDES.md).

| Seta | Origem → destino | Rótulo |
|---|---|---|
| [1] | Navegador → Nginx | `HTTPS / 443` (TLS 1.3) |
| [2] | Navegador → OSRM | `HTTPS / 443` (roteirização, timeout 8 s + fallback) |
| [3] | Navegador → OpenStreetMap | `HTTPS / 443` (tiles do mapa) |
| [4] | Nginx → Backend API | `HTTP/1.1 / 8080` (proxy `/api/*`) |
| [5] | Backend API → PostgreSQL | `TCP (PostgreSQL) / 5432` (JDBC com TLS) |

Não há linha tracejada (o projeto não tem fila nem eventos) nem linha pontilhada (não há coletor externo de
métricas ou logs). A legenda explica as duas convenções e registra que elas não são usadas.

## Legenda

| Elemento | Significado |
|---|---|
| Azul | Sub-rede pública (entrada da internet) |
| Verde | Sub-rede privada de aplicação |
| Roxo | Sub-rede privada de dados (sem internet) |
| Cinza | Internet / serviços de terceiros (fora da VPC) |
| Borda grossa escura | Limite da VPC (rede principal) |
| Texto laranja | Endpoint planejado (só contrato OpenAPI, sem tráfego) |
| Boneco | Usuário (navegador) |
| Retângulo arredondado | Container / serviço que executa código |
| Cilindro | Banco de dados |
| Nuvem | Serviço de terceiro na internet |
| Retângulo grande | Rede ou sub-rede; o que está dentro pertence a ela |

## Regras atendidas

- todo componente do inventário está no diagrama, e nenhum foi inventado;
- todas as setas rotuladas com protocolo e porta, com HTTPS na borda da internet;
- banco na sub-rede privada de dados, acessível só pelo Backend na porta 5432;
- componentes da VPC dentro de uma sub-rede; Navegador, OSRM e OpenStreetMap, de propósito, fora da VPC;
- legenda com cores, formas e tipos de linha.

> As redes, as sub-redes e o HTTPS na borda representam o ambiente de produção projetado, como pede a
> atividade. O Docker Compose atual não declara `networks` e publica o Nginx em HTTP/80 e a API em 8080 no
> host; a correspondência está na seção 4 de [`LIGACOES_E_REDES.md`](./LIGACOES_E_REDES.md).

## Como reexportar

Depois de editar o `.drawio` no draw.io:

- `File → Export as → PNG`, zoom `300%`, fundo transparente desmarcado, salvando por cima de
  `Arquitetura-RotaVital-Victor-Paes.png`;
- `File → Save As → Device`, mantendo o nome `Arquitetura-RotaVital-Victor-Paes.drawio`.
