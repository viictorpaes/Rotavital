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
proxy reverso: qualquer chamada para `/api/` é encaminhada, com o caminho intacto, para `http://backend:8080` (o backend expõe tudo em `/api/v1`).

**Quantos serviços de backend existem? Monolito ou vários?**
Um único serviço: monolito Spring Boot em [`backend/`](../backend), porta `8080`, com cinco controllers REST
sob `/api/v1` hoje — [`EstoqueController`](../backend/src/main/java/com/rotavital/api/EstoqueController.java),
[`AcessoController`](../backend/src/main/java/com/rotavital/api/AcessoController.java),
[`RotaController`](../backend/src/main/java/com/rotavital/api/RotaController.java) (grafo de distribuição +
menor caminho via Dijkstra, sobre uma rede montada a cada chamada com os dados do banco) e
[`BenchmarkController`](../backend/src/main/java/com/rotavital/benchmark/controller/BenchmarkController.java)
(auditoria de telemetria sequencial × paralela) e
[`DiagnosticoController`](../backend/src/main/java/com/rotavital/diagnostico/DiagnosticoController.java)
(saúde do backend, latência do banco e últimas requisições HTTP, usado pela tela de Administração).

**Quais bancos de dados?**
Um só: **PostgreSQL gerenciado (Supabase)**, configurado em
[`application.properties`](../backend/src/main/resources/application.properties) (`spring.datasource.*`),
com a senha injetada via `SUPABASE_DB_PASSWORD` (`.env`, fora do Git). O `EstoqueController` e o
`RotaController` leem dele via JPA (Spring Data, pacotes `repositorio` e `servico`): pontos da rede,
conexões e bolsas vêm das tabelas `ponto_rede`, `conexao` e `bolsa_hemocomponente`. O schema vem só das
migrations em [`supabase/migrations/`](../supabase/migrations/) — o Hibernate apenas confere se as classes
batem com as tabelas (`ddl-auto=validate`). Até a PI3-122, estoque e grafo ficavam em memória, em classes
que foram removidas (PI3-151 e PI3-152). Não há cache, banco de busca nem armazenamento de arquivos (sem
Redis, Elasticsearch, S3 etc.).

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
Não há ferramenta externa de observabilidade. O próprio backend registra cada requisição HTTP no
[`HttpLoggingFilter`](../backend/src/main/java/com/rotavital/config/HttpLoggingFilter.java), guarda as últimas
60 em memória (`LogHttpService`) e as expõe, junto com uptime e latência do banco, em
`GET /api/v1/diagnostico`, exibido na tela de Administração. Fora isso, só logs no `stdout` (log padrão do
Spring Boot/Hibernate, `spring.jpa.show-sql=true`). Sem Prometheus, Grafana, ELK, Sentry ou qualquer alerta.
Como isso roda dentro do backend, não é um componente separado no diagrama.

<h2 align="left" id="2-componentes">🧩 2. Lista de componentes</h2>

| # | Componente | Tipo | Função no sistema | Onde vive |
| :-: | :--- | :--- | :--- | :--- |
| 1 | **Navegador (usuário)** | Cliente | Único ponto de entrada; roda a SPA e chama OSRM/OSM direto | — |
| 2 | **SPA React** (`rotavital-frontend`) | Frontend | Telas do painel operacional e do portal do doador. As telas de **Estoque** e de **Administração** já chamam a API por [`services/api.ts`](../frontend/src/services/api.ts); rede hospitalar, requisições, pacientes e doações ainda usam dados mockados (`frontend/src/data/*Mock.ts`) | [`frontend/src`](../frontend/src) |
| 3 | **Nginx (container frontend)** | Infra / servidor web + proxy | Serve o build estático da SPA e faz proxy reverso de `/api/*` para o backend | [`frontend/nginx.conf`](../frontend/nginx.conf) |
| 4 | **Backend Spring Boot** (`rotavital-backend`) | Backend (monolito) | Expõe, sob `/api/v1`, `POST /acessos`, `GET /bancos/{bancoId}/estoque`, `GET /pontos`, `GET /conexoes`, `GET /rotas` (grafo + Dijkstra), `GET /diagnostico` e `/benchmarks/auditoria-telemetria`; chamado pela SPA via Nginx (`/api/*`) | [`backend/src/main/java/com/rotavital`](../backend/src/main/java/com/rotavital) |
| 5 | **PostgreSQL / Supabase** | Banco de dados | Guarda pontos da rede, conexões e bolsas; lido via JPA pelo `EstoqueController` e pelo `RotaController` | [`application.properties`](../backend/src/main/resources/application.properties) · [`supabase/migrations`](../supabase/migrations) |
| 6 | **OSRM** (`router.project-osrm.org`) | Serviço de terceiro | Calcula rota real (distância/tempo) entre hemocentro e hospital | [`roteirizacao.ts`](../frontend/src/lib/roteirizacao.ts) |
| 7 | **OpenStreetMap tiles** (`tile.openstreetmap.org`) | Serviço de terceiro | Fornece os tiles do mapa exibido em Rede/Rotas | [`MapaRede.tsx`](../frontend/src/components/rede/MapaRede.tsx) |

Fora da tabela, por não executarem código nem guardarem dado fora do processo: os módulos
`frontend/src/data/*Mock.ts` (fixtures estáticas embutidas no bundle JS) e o `docker-compose.yml`
(orquestra os containers 2‑4, mas não é um componente em si).

<h2 align="left" id="3-decisao">✅ 3. Decisão registrada — planejado × existente</h2>

O enunciado pede só o que é **real**. Aplicando esse corte:

- **Supabase/PostgreSQL entra como o banco de dados em uso.** Desde a PI3-122, os endpoints de leitura
  (`GET /api/v1/bancos/{bancoId}/estoque`, `GET /api/v1/pontos`, `/conexoes`, `/rotas`) leem do banco via
  JPA (`@Entity` + repositórios Spring Data). Antes disso, estoque e grafo ficavam em memória e o Supabase
  aparecia no diagrama como planejado; o diagrama foi atualizado na PI3-153.
- **SPA ↔ Backend: ligação ativa.** A SPA chama a API pelo Nginx (`/api/*` → `backend:8080`): a tela de
  Estoque lê `GET /bancos/{bancoId}/estoque` e a de Administração lê `/diagnostico`, `/bancos/{bancoId}/estoque`
  e `/pontos`. Por isso a seta Nginx → Backend aparece no diagrama como fluxo ativo (linha contínua). As demais
  telas ainda usam mocks (`frontend/src/data`), e as operações de escrita existem só no contrato OpenAPI —
  elas aparecem em laranja, como planejadas, dentro da caixa do Backend.
- **OSRM e OpenStreetMap entram como reais e ativos.** Não é mock: o código chama a API pública de verdade,
  com timeout e fallback tratado.
- **Nada de Kafka, filas, gateway, CDN, cache ou observabilidade** — ausentes do código, portanto ausentes
  do diagrama, mesmo sendo comuns em arquiteturas "de livro".

<h2 align="left" id="4-regra">⚠️ 4. Regra de escopo para o draw.io</h2>

Componente fora da tabela da seção 2 **não pode aparecer** no diagrama. Componente da tabela **tem que
aparecer**. O que existe só no contrato OpenAPI (endpoints de escrita) entra apenas como texto, diferenciado
em laranja, sem seta própria — não há tráfego real para desenhar.

<h2 align="left" id="5-proximo">➡️ 5. Próximo passo</h2>

✅ **Etapa 2 concluída:** [`Arquitetura-RotaVital-Victor-Paes.drawio`](Arquitetura-RotaVital-Victor-Paes.drawio) — diagrama de
arquitetura com os componentes desta lista (Navegador e SPA React num único ícone, já que a SPA roda no
navegador), distribuídos na VPC e nas sub-redes pública, de aplicação e de dados. Abra no
[app.diagrams.net](https://app.diagrams.net) ou na extensão draw.io do VS Code.
