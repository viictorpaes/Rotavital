<h1 align="center">
  Rota Vital — Documentação Técnica <br> Inventário de Componentes (Etapa 1) <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸🗺️
</h1>

<p align="center">
    <img src="https://img.shields.io/badge/-Markdown-111827?style=for-the-badge&logo=markdown&logoColor=white" height="28"/>
    <img src="https://img.shields.io/badge/Pré%E2%80%93draw.io-Etapa%201-6f42c1?style=for-the-badge" alt="Etapa 1"/>
    <img src="https://img.shields.io/badge/Componentes%20reais-6-brightgreen?style=for-the-badge" alt="Componentes reais"/>
</p>

> Levantamento de tudo que **executa código** ou **guarda dado** no Rota Vital, feito lendo o repositório
> (não a intenção do projeto). É o insumo da Etapa 1 do enunciado de arquitetura — **antes** de abrir o
> draw.io. Só entra aqui o que existe hoje no código, no `docker-compose.yml` e nos `Dockerfile`s; nada é
> incluído "porque parece profissional".

<h2 align="left">🧭 Sumário: </h2>

1. [Perguntas-guia respondidas](#1-perguntas)
2. [Lista de componentes](#2-componentes)
3. [Decisão registrada — planejado × existente](#3-decisao)
4. [Regra de escopo para o draw.io](#4-regra)
5. [Próximo passo](#5-proximo)

<h2 align="left" id="1-perguntas">📖 1. Perguntas-guia respondidas</h2>

**Quem inicia as requisições?**
Só o **navegador** (usuário humano usando a SPA React). Não há app mobile, nem outro sistema externo
chamando o Rota Vital, nem job agendado — não existe `@Scheduled`/`@EnableScheduling` em nenhum lugar do
backend, e não há workflow de CI que rode ou faça deploy da aplicação (o único conteúdo em
[`.github/`](../.github) é uma ferramenta de modernização de código, não um pipeline de execução).

**Existe frontend separado do backend? Ele é servido de onde?**
Sim. [`frontend/`](../frontend) é uma SPA React 18 + Vite + TypeScript, com build estático servido por um
container **Nginx** próprio ([`frontend/Dockerfile`](../frontend/Dockerfile),
[`frontend/nginx.conf`](../frontend/nginx.conf)) — não é servido pelo Spring Boot. Esse Nginx também faz
proxy reverso: qualquer chamada para `/api/` é encaminhada para `http://backend:8080/`.

**Quantos serviços de backend existem? Monolito ou vários?**
Um único serviço: monolito Spring Boot em [`backend/`](../backend), porta `8080`, com três controllers REST
hoje — [`EstoqueController`](../backend/src/main/java/com/rotavital/api/EstoqueController.java),
[`AcessoController`](../backend/src/main/java/com/rotavital/api/AcessoController.java) e
[`RotaController`](../backend/src/main/java/com/rotavital/api/RotaController.java) (grafo de distribuição +
menor caminho via Dijkstra, sobre uma rede populada em memória por `RedeDistribuicaoEmMemoria`).

**Quais bancos de dados?**
Dois candidatos, em estados diferentes — ver a [decisão registrada](#3-decisao) para o corte exato:
- **Memória do processo Java**: `HashMap` em
  [`BancosEmMemoria`](../backend/src/main/java/com/rotavital/api/BancosEmMemoria.java) (estoque, lido pelo
  `EstoqueController`) e listas em
  [`RedeDistribuicaoEmMemoria`](../backend/src/main/java/com/rotavital/api/RedeDistribuicaoEmMemoria.java)
  (grafo de pontos/conexões, lido pelo `RotaController`). Ambos somem a cada restart do backend.
- **PostgreSQL gerenciado (Supabase)**: configurado em
  [`application.properties`](../backend/src/main/resources/application.properties)
  (`spring.datasource.*`) e testado na subida da aplicação
  ([`RotaVitalApplication.testarConexaoBanco`](../backend/src/main/java/com/rotavital/RotaVitalApplication.java)),
  com a senha injetada via `SUPABASE_DB_PASSWORD` (`.env`, fora do Git). Não há cache, banco de busca nem
  armazenamento de arquivos (sem Redis, Elasticsearch, S3 etc.).

**Existe fila ou mensageria?**
Não. Nenhuma dependência de RabbitMQ, Kafka, SQS ou similar no `pom.xml` ou no código.

**Existe integração com terceiros?**
Duas, ambas chamadas **diretamente do navegador**, sem passar pelo backend:
- **OSRM** (`router.project-osrm.org`) — roteirização real (distância/tempo por ruas) em
  [`frontend/src/lib/roteirizacao.ts`](../frontend/src/lib/roteirizacao.ts), com timeout de 8s e
  fallback para uma rota de referência mockada se falhar.
- **OpenStreetMap** (`tile.openstreetmap.org`) — tiles do mapa em
  [`MapaRede.tsx`](../frontend/src/components/rede/MapaRede.tsx).

Não há integração com e-mail, gateway de pagamento ou API de CEP.

**Existe infraestrutura no caminho?**
Só o container **Nginx** do frontend, que acumula o papel de servidor de estático *e* proxy reverso para
`/api/`. Não existe load balancer, API gateway ou CDN — o
[`docker-compose.yml`](../docker-compose.yml) sobe só os dois containers (`backend`, `frontend`) lado a
lado, sem camada extra.

**Como o sistema é observado?**
Não há observabilidade formal. Só logs no `stdout`: `System.out.println` de diagnóstico em
`RotaVitalApplication` (sucesso/falha da conexão com o Supabase) e o log padrão do Spring Boot/Hibernate
(`spring.jpa.show-sql=true`). Sem Prometheus, Grafana, ELK, Sentry ou qualquer alerta.

<h2 align="left" id="2-componentes">🧩 2. Lista de componentes</h2>

| # | Componente | Tipo | Função no sistema | Onde vive |
| :-: | :--- | :--- | :--- | :--- |
| 1 | **Navegador (usuário)** | Cliente | Único ponto de entrada; roda a SPA e chama OSRM/OSM direto | — |
| 2 | **SPA React** (`rotavital-frontend`) | Frontend | Telas do painel operacional e do portal do doador; hoje consome **dados mockados locais** (`frontend/src/data/*Mock.ts`), não o backend | [`frontend/src`](../frontend/src) |
| 3 | **Nginx (container frontend)** | Infra / servidor web + proxy | Serve o build estático da SPA e faz proxy reverso de `/api/*` para o backend | [`frontend/nginx.conf`](../frontend/nginx.conf) |
| 4 | **Backend Spring Boot** (`rotavital-backend`) | Backend (monolito) | Expõe `GET /estoque/{bancoId}`, `POST /acesso` e `GET/GET/POST /rotas/*` (grafo + Dijkstra); hoje não é chamado pela SPA | [`backend/src/main/java/com/rotavital`](../backend/src/main/java/com/rotavital) |
| 5 | **`BancosEmMemoria` / `RedeDistribuicaoEmMemoria`** | Armazenamento (em memória) | Estoque e grafo de distribuição hardcoded em `HashMap`/listas — o que `EstoqueController` e `RotaController` de fato leem | [`BancosEmMemoria.java`](../backend/src/main/java/com/rotavital/api/BancosEmMemoria.java) · [`RedeDistribuicaoEmMemoria.java`](../backend/src/main/java/com/rotavital/api/RedeDistribuicaoEmMemoria.java) |
| 6 | **PostgreSQL / Supabase** | Banco de dados (planejado) | Configurado e testado na subida; nenhuma entidade/repositório JPA ainda o usa | [`application.properties`](../backend/src/main/resources/application.properties) |
| 7 | **OSRM** (`router.project-osrm.org`) | Serviço de terceiro | Calcula rota real (distância/tempo) entre hemocentro e hospital | [`roteirizacao.ts`](../frontend/src/lib/roteirizacao.ts) |
| 8 | **OpenStreetMap tiles** (`tile.openstreetmap.org`) | Serviço de terceiro | Fornece os tiles do mapa exibido em Rede/Rotas | [`MapaRede.tsx`](../frontend/src/components/rede/MapaRede.tsx) |

Fora da tabela, por não executarem código nem guardarem dado fora do processo: os módulos
`frontend/src/data/*Mock.ts` (fixtures estáticas embutidas no bundle JS) e o `docker-compose.yml`
(orquestra os containers 2‑4, mas não é um componente em si).

<h2 align="left" id="3-decisao">✅ 3. Decisão registrada — planejado × existente</h2>

O enunciado pede só o que é **real**. Aplicando esse corte:

- **`BancosEmMemoria` e `RedeDistribuicaoEmMemoria` entram no diagrama como o banco de dados atual.** São o
  que os endpoints de leitura (`GET /estoque/{bancoId}`, `GET/GET/POST /rotas/*`) de fato usam — confirma o
  que a PI3-122 registrou ("sem banco de dados, dados em memória").
- **Supabase/PostgreSQL entra, mas marcado como *em provisionamento*, não como banco em uso.** A conexão
  existe, é testada no boot (`testarConexaoBanco`) e a dependência (`spring-boot-starter-data-jpa`,
  `postgresql`) já está no `pom.xml` — não é especulação, é código real no branch. Mas nenhum `@Entity` ou
  `@Repository` existe ainda, então nenhum dado passa por ele hoje. Sugestão para o draw.io: desenhar com
  estilo tracejado/rótulo "planejado" para não confundir com o que já está em produção.
- **SPA ↔ Backend: sem chamada real ainda.** O Nginx já está configurado para rotear `/api/`, mas a SPA
  consome mocks locais (`frontend/src/data`), não `EstoqueController`/`AcessoController`/`RotaController`.
  Isso deve aparecer no diagrama como uma conexão **planejada** (linha tracejada), não como fluxo ativo — é
  exatamente o que a PI3-104 descreve ("frontend em andamento").
- **OSRM e OpenStreetMap entram como reais e ativos.** Não é mock: o código chama a API pública de verdade,
  com timeout e fallback tratado.
- **Nada de Kafka, filas, gateway, CDN, cache ou observabilidade** — ausentes do código, portanto ausentes
  do diagrama, mesmo sendo comuns em arquiteturas "de livro".

<h2 align="left" id="4-regra">⚠️ 4. Regra de escopo para o draw.io</h2>

Componente fora da tabela da seção 2 **não pode aparecer** no diagrama. Componente da tabela **tem que
aparecer** — inclusive os dois marcados como planejados (Supabase e a conexão SPA→Backend), desde que
visualmente diferenciados (ex.: linha tracejada + rótulo "planejado") dos componentes já ativos.

<h2 align="left" id="5-proximo">➡️ 5. Próximo passo</h2>

✅ **Etapa 2 concluída:** [`diagrama-arquitetura.drawio`](diagrama-arquitetura.drawio) — diagrama de
contêineres com exatamente os 8 componentes desta lista, respeitando a distinção ativo × planejado da
seção 3 (linha sólida = real hoje, linha tracejada = planejado). Abra no
[app.diagrams.net](https://app.diagrams.net) ou na extensão draw.io do VS Code.
