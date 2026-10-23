<h1 align="center">
  Rota Vital — Documentação Técnica <br> Como Executar <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸🚀
</h1>

<p align="center">
<a href="../README.md">⬅️ Voltar ao README principal</a> ·
<a href="DOCUMENTACAO.md">📚 Índice da documentação</a> ·
<a href="../c/README.md">🧬 Como compilar o módulo em C</a>
</p>

> Passo a passo para rodar o backend e o frontend direto na máquina, com Maven e npm. O caminho rápido, tudo
> pelo Docker com um comando, está no [README principal](../README.md#cdt-como-rodar).

<h2 align="left">🧭 Sumário: </h2>

1. [Pré-requisitos](#1-pre-requisitos)
2. [Backend (Spring Boot)](#2-backend)
3. [Frontend (React + Vite)](#3-frontend)
4. [Com Docker](#4-docker)

<h2 align="left" id="1-pre-requisitos">🧰 1. Pré-requisitos</h2>

Java 21+, Maven 3.9+, Node.js 20+ e (opcional) Docker.

```bash
git clone https://github.com/viictorpaes/Rotavital.git
cd Rotavital
cp backend/.env.example backend/.env   # e preencha SUPABASE_DB_PASSWORD
```

<details>
<summary>🪟 <b>Windows (PowerShell)</b></summary>

```powershell
git clone https://github.com/viictorpaes/Rotavital.git
cd Rotavital
Copy-Item backend\.env.example backend\.env   # e preencha SUPABASE_DB_PASSWORD
```
</details>

As variáveis do `backend/.env` (`SUPABASE_DB_PASSWORD`, `SUPABASE_URL`, `SUPABASE_KEY`) estão explicadas em
[`SUPABASE.md`, seção 4](SUPABASE.md#4-variaveis).

<h2 align="left" id="2-backend">☕ 2. Backend (Spring Boot)</h2>

```bash
cd backend
mvn clean package              # compila, roda os testes JUnit e gera target/rotavital-backend-0.1.0-SNAPSHOT.jar
mvn spring-boot:run            # ou: java -jar target/rotavital-backend-0.1.0-SNAPSHOT.jar
```

<details>
<summary>🪟 <b>Windows (PowerShell)</b></summary>

```powershell
cd backend
mvn clean package
mvn spring-boot:run            # ou: java -jar target\rotavital-backend-0.1.0-SNAPSHOT.jar
```
</details>

O `RotaVitalApplication` procura e carrega o `backend/.env` sozinho ao subir (variáveis já exportadas no
ambiente têm prioridade) e imprime "✅ CONECTADO AO BANCO DE DADOS COM SUCESSO!" (com a URL do banco) quando o
banco responde.

A API sobe em `http://localhost:8080/api/v1`:

| Método | Endpoint | Exemplo |
| :--- | :--- | :--- |
| `POST` | `/api/v1/acessos` (HU‑01) | `curl -X POST localhost:8080/api/v1/acessos -H 'Content-Type: application/json' -d '{"email":"medico@exemplo.com","senha":"..."}'` |
| `GET` | `/api/v1/bancos/{bancoId}/estoque?tipoSanguineo=` | `curl localhost:8080/api/v1/bancos/BS-01/estoque` |
| `GET` | `/api/v1/bancos/{bancoId}/sugestao-lote?tipoComponente=&tipoSanguineo=&quantidade=` (HU‑04) | `curl "localhost:8080/api/v1/bancos/BS-01/sugestao-lote?tipoComponente=HEMACIAS&tipoSanguineo=A_NEGATIVO&quantidade=2"` |
| `GET` | `/api/v1/bancos/{bancoId}/alertas` (HU‑02) | `curl localhost:8080/api/v1/bancos/BS-01/alertas` |
| `PATCH` | `/api/v1/hemocomponentes/{id}` (HU‑08) | `curl -X PATCH localhost:8080/api/v1/hemocomponentes/CH-1042 -H 'Content-Type: application/json' -d '{"status":"RESERVADA"}'` |
| `GET` | `/actuator/health` · `/actuator/info` | `curl localhost:8080/actuator/health` |
| `GET` | `/api/v1/pontos` · `/api/v1/conexoes` | `curl localhost:8080/api/v1/pontos` |
| `GET` | `/api/v1/rotas?origemId=&destinoId=` | `curl "localhost:8080/api/v1/rotas?origemId=BS-01&destinoId=HOSP-01"` |
| `GET`/`POST` | `/api/v1/benchmarks/auditoria-telemetria` | `curl "localhost:8080/api/v1/benchmarks/auditoria-telemetria?tamanho=100000&modo=TODOS"` |

> O login confere e-mail e senha no Supabase Auth: sem a `SUPABASE_KEY` no `.env`, `POST /acessos` responde
> `503`. Contrato completo em [`CONTRATOS_DE_API.md`](CONTRATOS_DE_API.md) e [`openapi.yaml`](openapi.yaml).

| Comando (em `backend/`) | Descrição |
| :--- | :--- |
| `mvn compile` | Só compila o código de `src/main/java` |
| `mvn test` | Compila e roda os testes unitários (JUnit 5): domínio e serviços, sem banco de dados (repositórios falsos) |
| `mvn verify` | Unitários + integração e aceitação (`*IT`, Cucumber) contra um Postgres 17 do Testcontainers — precisa de Docker ([detalhes](../README.md#cdt-testes)) |
| `mvn clean package` | Compila, testa e gera o `.jar` executável em `target/` |
| `mvn spring-boot:run` | Sobe a API (precisa do `backend/.env` com `SUPABASE_DB_PASSWORD`) |

**Fluxo de demonstração do domínio** ([`TesteFluxoTest`](../backend/src/test/java/com/rotavital/dominio/TesteFluxoTest.java):
cria banco de sangue, popula estoque, abre requisição e aloca por FEFO/ABO-Rh):

```bash
cd backend
mvn test -Dtest=TesteFluxoTest
```

<h2 align="left" id="3-frontend">🖥️ 3. Frontend (React + Vite)</h2>

Interface em **React + TypeScript + Vite + Tailwind CSS**, com as 10 telas do protótipo (HU‑01 a HU‑10). Login,
Estoque e Admin chamam a API; as demais telas ainda usam dados de exemplo (ver
[`ESTRUTURA_DO_PROJETO.md`](ESTRUTURA_DO_PROJETO.md#notas)).

```bash
cd frontend        # a partir de Rotavital/
npm install
npm run dev
```

O servidor de desenvolvimento sobe em `http://localhost:5173` e repassa `/api` para o backend em
`localhost:8080` (proxy do `vite.config.ts`), então suba o backend antes. As variáveis do Vite estão em
[`frontend/.env.example`](../frontend/.env.example).

| Comando | Descrição |
| :--- | :--- |
| `npm run dev` | Inicia o servidor de desenvolvimento, com hot reload |
| `npm run build` | Roda a checagem de tipos (`tsc -b`) e gera o build de produção em `frontend/dist` |
| `npm run preview` | Serve localmente o build de produção gerado por `npm run build` |
| `npm run lint` | Roda o ESLint sobre o código do frontend |

<h2 align="left" id="4-docker">🐳 4. Com Docker</h2>

O [`compose.yaml`](../compose.yaml) da raiz sobe banco, backend e frontend com um comando. Passo a passo,
serviços e healthchecks estão no [README principal](../README.md#cdt-como-rodar); as saídas de terminal que
comprovam a subida, em [`DESAFIO_CONTAINER_TDD_DEPLOY.md`](DESAFIO_CONTAINER_TDD_DEPLOY.md#e1-evidencias).

```bash
docker compose logs -f backend  # acompanha os logs do Spring Boot
docker compose down             # derruba os containers (os dados ficam no volume)
```

Por padrão o backend usa o Postgres local do Compose, onde o login não funciona: ele confere a senha no Supabase
Auth e busca o perfil na tabela `usuario` do Supabase. Para testar o login, preencha `SUPABASE_KEY` e
`SUPABASE_DB_PASSWORD` no `.env` e suba com o [`compose.supabase.yaml`](../compose.supabase.yaml) por cima. Nesse
modo o container `db` não sobe, e a API lê e grava no banco de produção:

```bash
docker compose down             # se o modo padrão estava no ar: senão o container db continua rodando
docker compose -f compose.yaml -f compose.supabase.yaml up -d --build
docker compose -f compose.yaml -f compose.supabase.yaml ps   # só backend e frontend
```

A chave publicável fica em *Project Settings → API Keys* no painel do Supabase. Os testes feitos nesse modo estão em
[`DESAFIO_CONTAINER_TDD_DEPLOY.md`](DESAFIO_CONTAINER_TDD_DEPLOY.md#supabase-evidencias).

No Docker, o frontend fica em `http://localhost` e o backend em `http://localhost:8080`. O Nginx do frontend
repassa `/api/` para o container do backend (`frontend/nginx/default.conf.template`), então a API também
responde em `http://localhost/api/v1`.
