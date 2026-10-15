<h1 align="center">Evidências · PI3-184 e PI3-196 · Login por perfil e avisos <br>
<img src="https://img.shields.io/badge/-React-111827?style=flat-square&logo=react&logoColor=61DAFB"/>
<img src="https://img.shields.io/badge/-Spring_Boot-111827?style=flat-square&logo=springboot&logoColor=6DB33F"/>
<img src="https://img.shields.io/badge/-Supabase_Auth-111827?style=flat-square&logo=supabase&logoColor=3ECF8E"/></h1>

> Sistema rodando localmente em **14/10/2026** (frontend Vite na porta `5173`, backend Spring Boot na `8080`
> ligado ao Supabase), sobre a PI3-183 (`POST /api/v1/acessos` com e-mail e senha). Regras de referência:
> [`PERFIS_DE_ACESSO.md`](../PERFIS_DE_ACESSO.md), seções 3 e 4.

## O que mudou na tela

- A tela de login pede **e-mail e senha**. Saíram os cards "Médico"/"Doador" e o atalho de digitar `admin`:
  o perfil vem da API, não de uma escolha na tela.
- Depois do login, cada perfil vai para a `telaInicial` que a API devolve (`/painel`, `/portal-doador` ou
  `/admin`). Essa rota fica na sessão e é usada em todos os redirecionamentos.
- Quem abre uma tela que não é do seu perfil volta para a **sua** tela inicial. Antes, médico e admin que
  abriam `/portal-doador` eram mandados para `/login` (nota da seção 4 da PI3-182).
- Os avisos aparecem no próprio formulário, sem o toast global do `clienteHttp`.

## Avisos do login (PI3-196)

| Situação | Resposta da API | Aviso na tela |
| :--- | :--- | :--- |
| E-mail ou senha em branco | *(não chama a API)* | Preencha o e-mail e a senha para entrar. |
| Senha errada | `401` | E-mail ou senha incorretos. |
| E-mail sem cadastro | `401` (a mesma resposta) | E-mail ou senha incorretos. |
| Supabase Auth fora do ar | `503` | Não foi possível conferir o login agora. Tente novamente em instantes. |
| Backend fora do ar ou sem rede | *(sem resposta)* | Não foi possível falar com o servidor agora. Confira sua conexão e tente novamente. |

Senha errada e e-mail sem cadastro dão o **mesmo aviso**, para a tela não revelar quais e-mails existem
(recomendação da PI3-182). Depois de uma recusa a senha é apagada e o foco volta para ela.

<img src="PI3-184/01_login_senha_errada.png" width="320"/>

## Cenários conferidos

| # | Perfil | Cenário | Esperado | Obtido | Como |
| :---: | :--- | :--- | :--- | :--- | :--- |
| 1 | — | Entrar com os campos vazios | aviso de campos vazios | ✅ | navegador |
| 2 | — | `medico@rotavital.test` com senha errada | `401` + "E-mail ou senha incorretos." | ✅ | navegador, API real |
| 3 | — | `ninguem@rotavital.test` | o mesmo aviso do cenário 2 | ✅ | navegador, API real |
| 4 | — | Backend sem resposta | aviso de conexão, sem toast | ✅ | navegador, falha simulada |
| 5 | Médico | Login com a senha certa | `200` · `/painel` | ✅ | `Invoke-RestMethod`, API real |
| 6 | Doador | Login com a senha certa | `200` · `/portal-doador` | ✅ | `Invoke-RestMethod`, API real |
| 7 | Admin | Login com a senha certa | `200` · `/admin` | ✅ | `Invoke-RestMethod`, API real |
| 8 | Médico | Entra e cai em | `/painel`, menu sem "Diagnóstico & Logs" | ✅ | navegador ¹ |
| 9 | Médico | Abre `/admin`, `/portal-doador`, `/login` e uma rota inexistente | volta para `/painel` | ✅ | navegador ¹ |
| 10 | Doador | Entra e cai em | `/portal-doador` ("Olá, Carlos Souza") | ✅ | navegador ¹ |
| 11 | Doador | Abre `/estoque`, `/admin` e `/` | volta para `/portal-doador` | ✅ | navegador ¹ |
| 12 | Admin | Entra e cai em | `/admin`, menu com "Diagnóstico & Logs" | ✅ | navegador ¹ |
| 13 | Admin | Abre `/estoque` | permitido | ✅ | navegador ¹ |
| 14 | Admin | Abre `/portal-doador` | volta para `/admin` | ✅ | navegador ¹ |
| 15 | Sem login | Abre `/painel` e `/portal-doador` | `/login` | ✅ | navegador |
| 16 | — | Sessão salva no formato antigo (só nome e papel) | descartada, vai para `/login` | ✅ | navegador |

¹ No navegador, a resposta `200` do `POST /acessos` foi simulada com os mesmos campos que a API real devolve
(cenários 5 a 7), para não digitar as senhas reais dos usuários de teste numa ferramenta automatizada. O resto
da navegação usa o backend real.

A tela de login também foi conferida na largura de celular (375 px).

## Como reproduzir à mão

```bash
# terminal 1 — backend (backend/.env com SUPABASE_DB_PASSWORD e SUPABASE_KEY)
cd backend && mvn spring-boot:run

# terminal 2 — frontend
cd frontend && npm run dev
```

Abra http://localhost:5173 e entre com `medico@`, `doador@` ou `admin@rotavital.test` (senhas combinadas com
o time, fora do Git). Os usuários são ligados à tabela `usuario` por
[`supabase/usuarios_de_teste.sql`](../../supabase/usuarios_de_teste.sql).
