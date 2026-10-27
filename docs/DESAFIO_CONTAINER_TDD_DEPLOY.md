<h1 align="center">
  Rota Vital — Documentação Técnica <br> Desafio do PI — Container, TDD e Deploy <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸🐳
</h1>

<p align="center">
<a href="../README.md#container-tdd-deploy">⬅️ Voltar ao desafio no README</a> ·
<a href="DOCUMENTACAO.md">📚 Índice da documentação</a> ·
<a href="../pdf/Desafio%20do%20PI%20-%20Container%20TDD%20e%20Deploy%20automatizados%20-%202026.2.pdf">📄 Enunciado (PDF)</a>
</p>

> Material de apoio do desafio: as saídas de terminal que comprovam o E1 e o passo a passo para ligar o deploy
> (E3). As seções que o enunciado pede no README (como rodar, testes, pipeline e deploy, decisões e uso de IA)
> continuam lá.

<h2 align="left" id="e1-evidencias">📸 1. Evidências do E1 — Container</h2>

Saídas reais do terminal (macOS + Docker Desktop 29.7):

```text
# 18/10/2026, volume novo, com a main mesclada: o 1º estágio do Dockerfile roda os testes
#24 15.05 [INFO] Tests run: 208, Failures: 0, Errors: 0, Skipped: 0
#24 15.40 [INFO] BUILD SUCCESS

# 14/10/2026
$ docker compose ps
SERVICE    IMAGE                      STATUS                    PORTS
backend    rotavital-backend:local    Up 27 seconds (healthy)   0.0.0.0:8080->8080/tcp
db         rotavital-db:local         Up 32 seconds (healthy)   5432/tcp
frontend   rotavital-frontend:local   Up 16 seconds (healthy)   0.0.0.0:80->8080/tcp

$ curl localhost:8080/actuator/health
{"status":"UP"}
$ curl "localhost/api/v1/bancos/BS-01/sugestao-lote?tipoComponente=HEMACIAS&tipoSanguineo=A_NEGATIVO&quantidade=2"
{"bancoDeSangueId":"BS-01",...,"quantidadeSugerida":2,"faltam":0,"lotes":[{"id":"CH-1061",...},{"id":"CH-1042",...}]}

# os dados sobrevivem ao down (volume nomeado)
$ curl -X PATCH localhost/api/v1/hemocomponentes/CH-1080 -d {"status":"RESERVADA"}
$ docker compose down && docker compose up -d
$ curl localhost/api/v1/hemocomponentes/CH-1080
status: RESERVADA
```

Imagens finais, ambas sem root: `rotavital-backend` com 378 MB (usuário `rotavital`) e `rotavital-frontend` com
91 MB (usuário `101`).

<h3 align="left" id="supabase-evidencias">🟢 Docker + Supabase, sem banco local (23/10/2026)</h3>

`docker compose -f compose.yaml -f compose.supabase.yaml up -d --build`, com as chamadas passando pelo Nginx do
frontend (`localhost/api/v1`) e cada resultado conferido com uma consulta direta no Supabase:

```text
$ docker compose -f compose.yaml -f compose.supabase.yaml ps
SERVICE    IMAGE                      STATUS                    PORTS
backend    rotavital-backend:local    Up 39 seconds (healthy)   0.0.0.0:8080->8080/tcp
frontend   rotavital-frontend:local   Up 28 seconds (healthy)   0.0.0.0:80->8080/tcp
# backend: "✅ CONECTADO AO BANCO DE DADOS COM SUCESSO!" · jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres
```

| # | Teste | API local | Supabase | Resultado |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Saúde | `/actuator/health` → `UP` (inclui o banco) | — | ✅ |
| 2 | Estoque do `BS-01` | 20 bolsas, todas `DISPONIVEL` | 20 linhas `DISPONIVEL` | ✅ |
| 3 | Rede | 7 pontos | os mesmos 7 `id` em `ponto_rede` | ✅ |
| 4 | Sugestão de lote (A−, 2 hemácias) | `CH-1092`, cadastrada em 20/10 (não existe no seed) | `CH-1092` presente | ✅ |
| 5 | Login com senha errada / e-mail sem cadastro | `401` "E-mail ou senha incorretos." | Supabase Auth respondeu (sem a chave seria `503`) | ✅ |
| 6 | Login com campos vazios | `400` com os dois campos obrigatórios | — | ✅ |
| 7 | `PATCH CH-1092` → `RESERVADA` | `200` | `status = RESERVADA` | ✅ |
| 8 | `PATCH CH-1092` → `ENTREGUE` (proibido) | `422` "está RESERVADA e não pode passar para ENTREGUE" | continuou `RESERVADA` | ✅ |
| 9 | `PATCH CH-1092` → `DISPONIVEL` (desfaz o teste 7) | `200` | `status = DISPONIVEL`; o `BS-01` voltou a 20 `DISPONIVEL` | ✅ |
| 10 | Login com a senha certa: médico, doador e admin | `200` com `telaInicial` `/painel`, `/portal-doador` e `/admin` | os três usuários de `auth.users`, ligados a `usuario` | ✅ |

As senhas dos usuários de teste não estão no repositório: ficam com o grupo.

<h2 align="left" id="cdt-configuracao">🛠️ 2. Configuração única do deploy (quem tem acesso às contas)</h2>

Passo a passo para ligar o deploy e colher as evidências do E3. As regras de proteção da `main` e a tabela de
evidências estão em [Pipeline e deploy](../README.md#cdt-pipeline), no README; os segredos, [abaixo](#cdt-segredos).

> [!WARNING]
> Mescle o PR desta entrega com **"Create a merge commit"**: o *squash* junta tudo num commit só e apaga o
> red → green.

1. **Branch vermelha de demonstração:** `git push origin 67d4c55:refs/heads/demo/esteira-vermelha` e abra um PR dela para
   a `main`. O `67d4c55` é o teste de ABO/Rh antes do código.
2. Proteja a `main` ([regras no README](../README.md#cdt-pipeline)). No PR de demonstração, o **Backend · testes** fica
   vermelho e o merge é bloqueado → 📸 **print 🔴**. Feche o PR sem mesclar e apague a branch.
3. ✅ *Feito em 20/10:* o PR #31 (`feature/container-tdd-deploy`) passou na esteira e foi mesclado com merge commit. A
   [execução na `main`](https://github.com/viictorpaes/Rotavital/actions/runs/39512621182) passou nos testes e nas
   imagens e falhou no deploy, como esperado sem os segredos.
4. **GHCR:** em *Packages*, deixe o `rotavital-backend` como **Public**.
5. **Render:** *New → Blueprint →* este repositório. O `render.yaml` cria o `rotavital-api` e pede a `SUPABASE_DB_PASSWORD`
   e a `SUPABASE_KEY`; se o serviço já existia, cadastre a chave em *Environment*. Copie o *Deploy Hook* e apague o
   `rotavital-web` antigo, se ele existir.
6. **Vercel:** dentro de `frontend/`, rode `npx vercel@64.0.2 login` e `npx vercel@64.0.2 link` (projeto novo
   `rotavital-web`, diretório `./`). O `orgId` e o `projectId` ficam em `.vercel/project.json`. Crie também um token.
7. Cadastre os segredos e as variáveis da [tabela abaixo](#cdt-segredos) no environment `producao`. Se a
   API ganhar outro domínio, ajuste o rewrite em [`frontend/vercel.json`](../frontend/vercel.json).
8. Em *Actions*, abra a [execução do passo 3](https://github.com/viictorpaes/Rotavital/actions/runs/39512621182) e clique
   em *Re-run failed jobs* → 📸 **print 🟢** + **URL pública**.
9. Depois do próximo merge, rode *Run workflow* com o sha anterior → 📸 **print ↩️**. Salve os prints em
   `docs/evidencias/container-tdd-deploy/` e preencha a [tabela de evidências](../README.md#cdt-evidencias-e3) do README.

<h3 align="left" id="cdt-segredos">🔐 Segredos e variáveis</h3>

| Segredo / configuração | Onde fica |
| :--- | :--- |
| `DB_PASSWORD` (Postgres local); `SUPABASE_KEY` e `SUPABASE_DB_PASSWORD`, opcionais, para o login no Compose | `.env` de cada um, no `.gitignore` (modelo em [`.env.example`](../.env.example)) |
| `SUPABASE_DB_PASSWORD`, `SUPABASE_KEY` | Variáveis do `rotavital-api` no **Render** ([`render.yaml`](../render.yaml)); sem a chave, o login responde `503` |
| `RENDER_DEPLOY_HOOK_API`, `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID` | GitHub → Environments → `producao` → Secrets |
| `API_URL`, `WEB_URL` (não são segredo) | GitHub → Environments → `producao` → Variables |
| Login no GHCR | `GITHUB_TOKEN` automático do Actions |
