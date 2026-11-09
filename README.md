<h1 align="center"> Rotavital 💉🩸🏥🚑<br>
<img src="./img/RotaVital_logo.png" alt="Logo RotaVital" width="620"> <br>
<img src="https://img.shields.io/badge/-Java_21-020617?style=flat-square&logo=openjdk&logoColor=orange" height="19"/>
<img src="https://img.shields.io/badge/-Spring_Boot_3.3-020617?style=flat-square&logo=springboot&logoColor=6DB33F" height="19"/>
<img src="https://img.shields.io/badge/-Maven-020617?style=flat-square&logo=apachemaven&logoColor=C71A36" height="19"/>
<img src="https://img.shields.io/badge/-React_18_·_Vite-020617?style=flat-square&logo=react&logoColor=61DAFB" height="19"/>
<img src="https://img.shields.io/badge/-PostgreSQL_·_Supabase-020617?style=flat-square&logo=supabase&logoColor=3ECF8E" height="19"/>
<img src="https://img.shields.io/badge/-Docker_Compose-020617?style=flat-square&logo=docker&logoColor=2496ED" height="19"/>
<img src="https://img.shields.io/badge/-OpenAPI_3.0.3-020617?style=flat-square&logo=openapiinitiative&logoColor=6BA539" height="19"/>

[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg)](https://github.com/viictorpaes/Rotavital/blob/main/LICENSE)
![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-purple)
![Status](https://img.shields.io/badge/status-em_desenvolvimento-yellow)
</h1>

<p align="center">
Sistema de logística de distribuição de hemocomponentes entre hospitais e bancos de sangue — estoque com
regras de validade, requisições hospitalares com alocação por <b>FEFO + compatibilidade ABO/Rh</b>,
roteirização de entregas, telemetria da cadeia fria e campanhas de doação que ligam doadores a pacientes.
</p>

<h2 align="center" id="visao-geral">🧭 Visão Geral <br>
<img src="https://img.shields.io/badge/Sumário-111827?style=flat&logo=bookstack&logoColor=white" height="22"/>
</h2>

| Item | Resumo |
| :--- | :--- |
| 👥 **Quem usa** | 🩺 **Médico** (hospital ou hemocentro) · ❤️ **Doador** (voluntário) · 🛠️ **Admin** (equipe técnica) |
| 🧩 **Como é feito** | Frontend **React + Vite** (servido pelo Nginx) → API REST **Spring Boot** (`/api/v1`) → **PostgreSQL** (local no Docker ou Supabase na nuvem) |
| 📍 **Situação** | 2026.2 · as 10 histórias do protótipo têm tela; Login, Estoque, Pacientes, Campanhas e Doações já gravam no banco |

**🎯 Objetivos**

1. **Estoque confiável** — cada bolsa com validade e faixa de temperatura, consumida por **FEFO** (vence primeiro, sai primeiro).
2. **Transfusão segura** — sugerir só lotes **ABO/Rh compatíveis** com o paciente.
3. **Entrega eficiente** — menor caminho entre hemocentro e hospitais (grafo + Dijkstra), respeitando a cadeia fria.
4. **Mais doadores** — campanhas por paciente e agendamento de doação pelo Portal do Doador.

**⚡ Rodar em 3 comandos** (só precisa de Git, Docker e Node — passo a passo em [Como Executar](#como-executar)):

```bash
git clone https://github.com/viictorpaes/Rotavital.git && cd Rotavital
cp .env.example .env        # Windows (PowerShell): Copy-Item .env.example .env
npm run docker:local        # banco + API + site → http://localhost:8080
```

**📑 Sumário**

| # | Seção | O que você encontra |
| :---: | :--- | :--- |
| 1 | [👥 Docentes](#docentes) e [Integrantes](#integrantes) | Quem orienta e quem desenvolve |
| 2 | [⛏️ Tecnologias](#tecnologias) | Stack completa |
| 3 | [🚀 Entregas do PI](#entregas) | Entregas 01 a 04: histórias, protótipo, screencasts e issues |
| 4 | [📦 Módulos do Sistema](#modulos) | O que o sistema faz, por módulo: telas, endpoints e status |
| 5 | [🏰 Arquitetura](#arquitetura) | Pastas do projeto e a camada de cada uma (diagramas em `docs/ARQUITETURA.md`) |
| 6 | [🟢 Banco de Dados](#supabase) | Supabase, migrations, tabelas e RLS |
| 7 | [🚀 Como Executar](#como-executar) | Quickstart Docker (local × nuvem), comandos npm e variáveis de ambiente |
| 8 | [🌐 Catálogo da API](#api) | Endpoints REST, Swagger e [usuários de teste](#usuarios-teste) |
| 9 | [🖼️ Figma × Sistema Real](#figma-vs-real) | Protótipo lado a lado com as telas rodando |
| 10 | [📚 Documentação](#documentacao) | Índice dos documentos técnicos em `docs/` |
| 11 | [🐳🧪🚀 Desafio do PI](#container-tdd-deploy) | Container, TDD e deploy automatizados (Infraestrutura de Software) |

<h2 align="center" id="docentes">👥👨🏻‍🏫 Docentes Responsáveis: <br>
<img src="https://img.shields.io/badge/Docentes-111827?style=flat&logo=googleclassroom&logoColor=white" height="22"/>
</h2>
<ul>
  <li><strong>José Maurício da Silva Júnior</strong> — Algoritmos e Estrutura de Dados</li>
  <li><strong>Raoni Monteiro de Oliveira</strong> — Infraestrutura de Software (SO)</li>
  <li><strong>Robson Godoi de Albuquerque Maranhão</strong> — Projeto 3</li>
  <li><strong>Maurício da Motta Braga</strong> — Programação Orientada a Objetos</li>
  <li><strong>Luiz Filipe Guimarães Novaes</strong> — Infraestrutura de Comunicação (RSD)</li>
  <li><strong>André Luiz da Silva Xavier</strong> — Estatística e Probabilidade</li>
</ul>

<h2 align="center" id="integrantes">👤👨‍🎓 Integrantes do Projeto: </h2>
<ul>
<li><b>Victor José Paes e Silva (Líder 👑)</b> <a href="https://www.linkedin.com/in/viictorpaes/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/viictorpaes" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>Eduardo de Souza Cavalcanti Junior <a href="https://www.linkedin.com/in/eduardoscavalcantij/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/eduardo-scavalcanti" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>Felipe Franca Alves de Lima <a href="https://www.linkedin.com/in/felipefrancaal/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/ffrancaal" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>Helamã Leone de Lima Procídio <a href="https://www.linkedin.com/in/helam%C3%A3-procidio-428772367/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/procidiohelama-star" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>João Pedro Arruda Guimarães <a href="https://www.linkedin.com/in/jo%C3%A3o-pedro-arruda-guimar%C3%A3es-157952287/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/Jp230603" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>Júlia Oliveira Veríssimo (<b>Entrou em: 26/09/2026</b>)</li>
<li>Lucas Paguetti Pereira <a href="https://www.linkedin.com/in/lucas-paguetti-pereira" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/wqiluc" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
<li>Tiago Luiz Moreira de Vasconcelos <a href="https://www.linkedin.com/in/tiagoluiz23/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="14"/></a> <a href="https://github.com/tlmv23" target="_blank"><img src="https://img.shields.io/badge/GitHub-000000?style=for-the-badge&logo=github&logoColor=white" height="16"/></a></li>
</ul>

<h2 align="center" id="tecnologias"> ⛏️💻 Tecnologias Utilizadas: <br>
<img src="https://img.shields.io/badge/Stack-111827?style=flat&logo=openjdk&logoColor=orange" height="22"/>
</h2>
<p align="center">
  <img src="https://upload.wikimedia.org/wikipedia/commons/9/9a/Visual_Studio_Code_1.35_icon.svg" width="32" height="32" alt="VS Code"/> <br>
  <img src="https://img.shields.io/badge/-Java_21-111827?style=flat-square&logo=openjdk&logoColor=orange"/>
  <img src="https://img.shields.io/badge/-C99-111827?style=flat-square&logo=c&logoColor=A8B9CC"/>
  <img src="https://img.shields.io/badge/-Spring Boot-111827?style=flat-square&logo=springboot&logoColor=green"/>
  <img src="https://img.shields.io/badge/-Maven-111827?style=flat-square&logo=apachemaven&logoColor=C71A36"/>
  <img src="https://img.shields.io/badge/-JUnit_5-111827?style=flat-square&logo=junit5&logoColor=25A162"/>
  <img src="https://img.shields.io/badge/-OpenAPI_3.0.3-111827?style=flat-square&logo=openapiinitiative&logoColor=6BA539"/>
  <img src="https://img.shields.io/badge/-Supabase-111827?style=flat-square&logo=supabase&logoColor=3ECF8E"/> <br>
  <img src="https://img.shields.io/badge/-React_18-111827?style=flat-square&logo=react&logoColor=61DAFB"/>
  <img src="https://img.shields.io/badge/-TypeScript-111827?style=flat-square&logo=typescript&logoColor=3178C6"/>
  <img src="https://img.shields.io/badge/-Vite-111827?style=flat-square&logo=vite&logoColor=646CFF"/>
  <img src="https://img.shields.io/badge/-Tailwind_CSS-111827?style=flat-square&logo=tailwindcss&logoColor=38BDF8"/>
  <img src="https://img.shields.io/badge/-React_Router-111827?style=flat-square&logo=reactrouter&logoColor=CA4245"/>
  <img src="https://img.shields.io/badge/-ESLint-111827?style=flat-square&logo=eslint&logoColor=4B32C3"/> <br>
  <img src="https://img.shields.io/badge/-Docker-111827?style=flat-square&logo=docker&logoColor=2496ED"/>
  <img src="https://img.shields.io/badge/-Docker%20Compose-111827?style=flat-square&logo=docker&logoColor=2496ED"/>
  <img src="https://img.shields.io/badge/-Testcontainers-111827?style=flat-square&logo=testcontainers&logoColor=9B489A"/>
  <img src="https://img.shields.io/badge/-Cucumber_(Gherkin)-111827?style=flat-square&logo=cucumber&logoColor=23D96C"/>
  <img src="https://img.shields.io/badge/-GitHub_Actions-111827?style=flat-square&logo=githubactions&logoColor=2088FF"/>
  <img src="https://img.shields.io/badge/-Render-111827?style=flat-square&logo=render&logoColor=46E3B7"/>
  <img src="https://img.shields.io/badge/-Vercel-111827?style=flat-square&logo=vercel&logoColor=white"/>
    <img src="https://img.shields.io/badge/-Postman-111827?style=flat-square&logo=postman&logoColor=FF6C37"/>
  <img src="https://img.shields.io/badge/-Insomnia-111827?style=flat-square&logo=insomnia&logoColor=4000BF"/>
  <img src="https://img.shields.io/badge/-Figma-111827?style=flat-square&logo=figma&logoColor=F24E1E"/>
  <img src="https://img.shields.io/badge/Architecture-111827?style=flat-square&logo=instructure&logoColor=white"/> <br>
  <img src="https://img.shields.io/badge/-Git-111827?style=flat-square&logo=git&logoColor=F05032"/>
  <img src="https://img.shields.io/badge/-GitHub-111827?style=flat-square&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/-GitHub%20Desktop-111827?style=flat-square&logo=github&logoColor=6F42C1"/> <br>
</p>

<h2 align="center" id="entregas">🚀 Entregas do Projeto Integrador <br>
<img src="https://img.shields.io/badge/Kickoff_→_Entrega_04-111827?style=flat&logo=todoist&logoColor=E44332" height="22"/>
</h2>

<p align="center">
Cada entrega do Projeto Integrador tem sua própria seção abaixo, com link direto para os artefatos
produzidos (histórias de usuário, protótipo, screencasts). Datas conforme o roadmap do 3º semestre.
</p>

<h3 id="-entrega-01" align="center">📌 Entrega 01 — 31/08 <br>
<a href="https://letter-raven-07178463.figma.site" target="_blank"><img src="https://img.shields.io/badge/-Figma-111827?style=flat-square&logo=figma&logoColor=purple" height="20"/></a>
<a href="https://youtu.be/SRvAFkqi4Nc" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_1-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Status-Concluída-22C55E?style=flat" height="20"/>
</h3>

| Artefato | Descrição | Link |
| :--- | :--- | :--- |
| 🎨 Protótipo low-Fi (Figma Make) | 10 histórias navegadas e confirmadas ponta a ponta (HU‑01 a HU‑10) | [Abrir protótipo](https://letter-raven-07178463.figma.site) |
| 🎬 Screencast (YouTube) | Demonstração do protótipo, explicando cada história implementada | [Assistir vídeo](https://youtu.be/SRvAFkqi4Nc) |

<h4 align="center">📋 Histórias de Usuário — Rota Vital 🩸<br>
Login, Estoque, Requisições, Rede Hospitalar, Pacientes e <b>Doações</b></h4>

> **Formato:** Como `[papel]`, quero `[funcionalidade]`, para que `[benefício]`.

<p align="center">
<img src="https://img.shields.io/badge/HUs%20Implementadas-10-brightgreen?style=flat-square&logo=checkmarx&logoColor=white"/>
<img src="https://img.shields.io/badge/HUs%20Futuras-6-blue?style=flat-square&logo=rocket&logoColor=white"/>
</p>

<h5 align="left">✅ Implementadas</h5>

| # | Tela do Figma | História |
| :---: | :--- | :--- |
| HU‑01 | 🔐 Login / Seleção de acesso | Como **usuário do RotaVital (médico ou doador)**, quero escolher meu tipo de acesso e informar meu nome na tela inicial, para que eu entre direto na experiência certa para o meu papel, sem um cadastro completo. |
| HU‑02 | 📊 Painel Operacional (Início) | Como **médico**, quero ver um painel com números rápidos e uma caixa de avisos com alertas críticos em tempo real, para que eu identifique pacientes em risco e problemas de estoque sem navegar por todas as telas. |
| HU‑03 | 📦 Estoque por tipo sanguíneo | Como **médico**, quero consultar o estoque agrupado por tipo sanguíneo, com o detalhe de cada lote (validade, temperatura e localização física), para que eu saiba exatamente quais lotes estão em risco de vencer ou fora da faixa de temperatura ideal. |
| HU‑04 | 🏥 Requisição — Solicitar | Como **médico**, quero preencher uma requisição informando hemocomponente, tipo sanguíneo, ala de destino, quantidade, paciente e urgência, para que o sistema me sugira automaticamente o lote certo pela regra FEFO. |
| HU‑05 | 🏥 Requisição — Recebidas | Como **médico**, quero registrar o recebimento físico de hemocomponentes vindos de outras instituições, para que essas unidades sejam somadas automaticamente ao estoque, sem lançamento manual. |
| HU‑06 | 🏥 Requisição — Campanha | Como **médico**, quero publicar uma campanha de doação para um paciente específico, para que doadores voluntários o vejam nas telas de Doações e Pacientes e se cadastrem para ajudar diretamente. |
| HU‑07 | 🚚 Rede hospitalar (mapa + transporte) | Como **médico**, quero visualizar o mapa de hospitais conectados, escolher um destino e ver o plano de transporte com a rota calculada e as exigências da cadeia fria, para que a entrega siga pelo caminho mais eficiente, no veículo certo e na temperatura ideal. |
| HU‑08 | 🩺 Pacientes | Como **médico**, quero, ao concluir o atendimento de um paciente, informar se o hemocomponente usado veio do estoque interno (com baixa automática por FEFO) ou de uma doação externa (sem baixa), para que o estoque reflita com exatidão o que foi realmente consumido. |
| HU‑09 | ❤️ Doações (quem precisa + agendar) | Como **médico ou doador**, quero visualizar as pessoas que precisam de doação — filtráveis por tipo sanguíneo — e agendar uma doação para uma delas, para que doadores compatíveis sejam direcionados a quem precisa com mais urgência. |
| HU‑10 | 👤 Portal do Doador | Como **doador**, quero acessar, após o login, uma versão simplificada da tela de Doações, sem o menu operacional do hospital, para que minha experiência seja direta: ver quem precisa e agendar minha doação. |

> Parte da lógica (FEFO, compatibilidade ABO/Rh, grafo de rotas) já existe no backend Java — ver
> [`docs/MODULOS.md`](./docs/MODULOS.md). O que era só protótipo está anotado como "Nível de código" no detalhamento.

<h5 align="left">🚀 Podem Ser Implementadas no Futuro</h5>

> Identificadas navegando o próprio protótipo — coisas que ele hoje **não** cobre e que fariam sentido como
> próximo passo, seja no Figma ou já direto no backend.

| # | História |
| :---: | :--- |
| HU‑F01 | Como **administrador do sistema**, quero que o login exija senha e verificação de identidade (hoje a tela só pede um nome), para que o acesso a dados clínicos e de estoque seja de fato seguro. |
| HU‑F02 | Como **gestor da rede de sangue**, quero um painel que cruze indicadores de vários hemocentros (o painel atual mostra números de um só), para que eu identifique desequilíbrios regionais de estoque e demanda. |
| HU‑F03 | Como **médico**, quero receber uma notificação (push ou e-mail) quando um alerta crítico entrar na Caixa de Avisos, para que eu não dependa de estar com o painel aberto para ser avisado. |
| HU‑F04 | Como **médico**, quero acompanhar a localização em tempo real do veículo refrigerado durante o transporte (o Plano de Transporte hoje só mostra a rota calculada, não o rastreamento ao vivo), para que eu saiba exatamente quando a entrega vai chegar. |
| HU‑F05 | Como **gestor**, quero exportar relatórios de estoque e demanda em CSV/PDF a partir do Painel Operacional, para que eu leve esses números para reuniões e prestação de contas fora do sistema. |
| HU‑F06 | Como **doador**, quero que o sistema verifique minha elegibilidade (intervalo mínimo entre doações, idade, etc.) antes de confirmar o agendamento em "Fazer doação", para que eu não vá até o hemocentro para descobrir lá que não posso doar. |

<h5 align="left">🔍 Detalhamento:</h5>

<p align="center">
Clique em <b>▶️</b> para ver, de cada história, o ator, a tela do Figma, o nível de código e <b>2 cenários BDD</b>
com os dados reais do protótipo.
</p>

<details>
<summary>▶️🔐 <b>HU‑01</b> — Login e seleção de tipo de acesso</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Usuário do sistema (médico ou doador) |
| **Quero** | Escolher meu tipo de acesso ("Médico — acesso hospitalar completo" ou "Doador — portal de doações") e informar meu nome |
| **Para que** | Eu veja a tela adequada ao meu papel sem precisar de um cadastro completo |
| **Tela do Figma** | Tela inicial "Cesar Life · Estoque Inteligente" — cartões `Médico`/`Doador`, campo `Nome`, botão `Entrar` |
| **Nível de código** | Desde a PI3-183/184, a entrada é por **e-mail e senha**: `POST /api/v1/acessos` confere as credenciais no Supabase Auth e devolve o perfil (médico, doador ou admin) e a tela inicial. Os cards do protótipo saíram da tela; ver [`PERFIS_DE_ACESSO.md`](./docs/PERFIS_DE_ACESSO.md) |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Acesso como médico | Estou na tela inicial do Cesar Life | Seleciono o cartão "Médico", informo "Dra. Ana Souza" e clico em "Entrar" | Vejo o Painel Operacional do HEMOPE (Recife — PE), com o menu Início / Estoque / Requisição / Rede / Pacientes / Doações |
| Acesso como doador | Estou na tela inicial | Seleciono o cartão "Doador", informo "João Doador" e clico em "Entrar" | Vejo o cabeçalho "Cesar Life · Portal do Doador" e a lista de pessoas que precisam, sem o menu operacional do hospital |

</details>

<details>
<summary>▶️📊 <b>HU‑02</b> — Painel operacional com alertas em tempo real</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Ver um painel com números rápidos e uma caixa de avisos com alertas críticos em tempo real |
| **Para que** | Eu identifique rapidamente pacientes em risco e problemas de estoque sem navegar por todas as telas |
| **Tela do Figma** | "Início" — Números Rápidos (Unidades em Estoque, Tipos em Risco, Vencendo em 7 dias, Alertas Críticos) + Caixa de Avisos (9 ativos) |
| **Nível de código** | Sem equivalente no contrato/domínio hoje — é um gap real também descrito na visão original do projeto (painel de indicadores) |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Alerta crítico de paciente | A paciente "Mariana Alves" precisa de sangue O- com urgência, em cirurgia cardíaca no Centro Cirúrgico | Entro no Painel Operacional | Vejo o aviso "CRÍTICO · Paciente Mariana Alves precisa de sangue O− urgente" na Caixa de Avisos |
| Alerta de estoque baixo | O tipo AB+ está com apenas 3 unidades de Concentrado de Hemácias | Entro no Painel Operacional | Vejo o aviso "Estoque de sangue tipo AB+ próximo do desabastecimento" e o contador "Tipos em Risco" reflete essa contagem |

</details>

<details>
<summary>▶️📦 <b>HU‑03</b> — Estoque por tipo sanguíneo com detalhe de lote</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Consultar o estoque agrupado por tipo sanguíneo, com detalhe de cada lote (validade, temperatura, localização) |
| **Para que** | Eu saiba exatamente quais lotes estão em risco de vencer ou fora da faixa de temperatura ideal |
| **Tela do Figma** | "Estoque" — filtro por tipo sanguíneo (A+ … O-), cartão de lote com código, unidades, volume, validade e temperatura (com etiqueta "FORA DA FAIXA") |
| **Nível de código** | Parcialmente coberto por `Estoque`/`BolsaHemocomponente` e `GET /api/v1/bancos/{bancoId}/estoque` — mas o contrato atual não tem campos de localização física (rack/posição) nem de temperatura por lote |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Lote fora da faixa de temperatura | O lote "PQ-3014" (Concentrado de Plaquetas, A+) está a 25,6°C, fora da faixa ideal de 20–24°C | Abro a tela de Estoque e filtro por "A+" | Vejo o lote "PQ-3014" marcado com a etiqueta "FORA DA FAIXA" |
| Filtrar por tipo sanguíneo específico | Existem lotes de vários tipos sanguíneos no estoque | Clico no filtro "O-" | Vejo apenas os 3 lotes de O- ("CH-1042", "CH-1043", "CR-4002"), totalizando 13 unidades |

</details>

<details>
<summary>▶️🏥 <b>HU‑04</b> — Solicitar requisição com sugestão de lote por FEFO</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Preencher uma requisição informando hemocomponente, tipo sanguíneo, ala de destino, quantidade, paciente e urgência |
| **Para que** | Eu atenda o paciente rapidamente usando o lote mais próximo do vencimento, sem precisar escolher manualmente |
| **Tela do Figma** | "Requisição" → aba "Solicitar" — formulário completo + painel lateral "Lote Sugerido (FEFO)" |
| **Nível de código** | Corresponde a `POST /api/v1/requisicoes` + à lógica de FEFO já em `Estoque.buscarDisponiveis()`; o contrato atual não modela `alaMedica` nem `nomePaciente` |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Sugestão automática do lote mais próximo do vencimento | Estou solicitando Concentrado de Hemácias tipo O- para a Emergência | Preencho o formulário na aba "Solicitar" | O sistema sugere o lote "CH-1042" com a etiqueta "3D P/ Vencer" e a localização "R1 · P3 · N2" |
| Requisição de emergência | Um paciente precisa de sangue com urgência "Emergência" | Envio a requisição pelo botão "Enviar requisição" | Ela é registrada vinculada ao paciente e à ala médica informados |

</details>

<details>
<summary>▶️🏥 <b>HU‑05</b> — Registrar recebimento de hemocomponentes (aba Recebidas)</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Registrar o recebimento físico de hemocomponentes vindos de outras instituições |
| **Para que** | As unidades recebidas sejam somadas automaticamente ao estoque, sem lançamento manual |
| **Tela do Figma** | "Requisição" → aba "Recebidas" — lista de recebimentos (`INC-001`…`INC-004`) com origem, tipo e quantidade |
| **Nível de código** | Sem endpoint equivalente — é um fluxo de entrada diferente do cadastro manual de `POST /api/v1/hemocomponentes` |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Recebimento de intercâmbio entre hemocentros | Chegaram 8 unidades de Concentrado de Hemácias O- do "HEMORIO — Intercâmbio Nordeste" | Confirmo o recebimento "INC-001" na aba "Recebidas" | As 8 unidades são somadas automaticamente ao estoque de O- |
| Recebimento de doação coletiva corporativa | A empresa "Nordeste Energia S.A." fez uma doação coletiva de 4 unidades de Concentrado de Plaquetas AB+ | Confirmo o recebimento "INC-003" | Essas 4 unidades entram no estoque de AB+ |

</details>

<details>
<summary>▶️🏥 <b>HU‑06</b> — Publicar campanha de doação para um paciente</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Publicar uma campanha de doação informando idade, sexo, tipo sanguíneo, componente necessário, diagnóstico e gravidade do paciente |
| **Para que** | Doadores voluntários vejam esse paciente nas telas de Doações e Pacientes e se cadastrem para ajudar diretamente |
| **Tela do Figma** | "Requisição" → aba "Campanha" — formulário de publicação, com aviso de que ela aparece em Doações e Pacientes |
| **Nível de código** | Sem equivalente no domínio/contrato — não existe conceito de campanha pública nem de doador no backend hoje |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Publicar campanha para paciente crítico | Um paciente com diagnóstico "Insuficiência cardíaca aguda" precisa de sangue O- com urgência | Preencho e publico o formulário na aba "Campanha" | O paciente passa a aparecer nas telas "Doações" e "Pacientes" para possíveis doadores |
| Campanha exige causa/diagnóstico | Estou preenchendo uma campanha de doação | Tento publicar sem informar "Causa/Diagnóstico" | O sistema deveria bloquear a publicação até o campo ser preenchido — regra a confirmar/reforçar na próxima iteração do protótipo |

</details>

<details>
<summary>▶️🚚 <b>HU‑07</b> — Rede hospitalar e plano de transporte na cadeia fria</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Visualizar o mapa de hospitais conectados, escolher um destino e ver o plano de transporte com a rota calculada |
| **Para que** | A entrega siga o caminho mais eficiente, no veículo certo e respeitando a temperatura ideal de transporte |
| **Tela do Figma** | "Rede" — mapa de hospitais conectados (distância/tempo) + painel "Plano de Transporte" (rota, veículo, checklist de hemocomponentes, prioridade de envio por FEFO) |
| **Nível de código** | Corresponde ao módulo de Rotas (`PontoDeRede`, `Conexao`, Dijkstra em `RedeDistribuicao`/`RotaCalculada`) — mas o backend ainda não modela tipo de veículo nem checklist de envio |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Selecionar hospital e calcular rota | O "Hospital das Clínicas de PE" está a 7,8 km / 18 min do hemocentro | Eu o seleciono no mapa da Rede | O Plano de Transporte exibe destino, endereço, melhor rota, "Veículo refrigerado monitorado" e temperatura ideal de 2 a 6°C |
| Priorização de envio por FEFO no transporte | Os lotes "CH-1042" (O-, vence em 3 dias), "CH-1080" (AB+, vence em 6 dias) e "CH-1061" (A-, vence em 9 dias) estão disponíveis para envio | Visualizo a "Prioridade de Envio (FEFO)" do Plano de Transporte | Eles aparecem ranqueados nessa ordem, do mais próximo do vencimento para o mais distante |

</details>

<details>
<summary>▶️🩺 <b>HU‑08</b> — Concluir procedimento com baixa por FEFO ou doação externa</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico |
| **Quero** | Ao concluir o atendimento de um paciente, informar se o hemocomponente veio do estoque interno ou de uma doação externa |
| **Para que** | O estoque reflita com exatidão o que foi realmente consumido, sem duplicar ou perder unidades |
| **Tela do Figma** | "Pacientes" → botão "Concluir procedimento" → modal com as opções "Utilizou hemocomponentes do estoque" (deduz por FEFO) e "Paciente recebeu doação externa" (não deduz) |
| **Nível de código** | A baixa por FEFO já existe conceitualmente em `Estoque.reservar()` + `RequisicaoHospitalar.marcarComoAlocada()`, mas não há classe de domínio para Paciente nem para o registro de conclusão de procedimento |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Concluir procedimento usando estoque interno | A paciente "Mariana Alves" (O-, Concentrado de Hemácias, crítico) está em atendimento | Concluo o procedimento e seleciono "Utilizou hemocomponentes do estoque" | 1 unidade de Concentrado de Hemácias O- é deduzida do estoque seguindo FEFO |
| Concluir procedimento com doação externa | A mesma paciente recebeu sangue por uma doação externa emergencial | Concluo o procedimento e seleciono "Paciente recebeu doação externa" | Nenhuma unidade é deduzida do estoque |

</details>

<details>
<summary>▶️❤️ <b>HU‑09</b> — Ver quem precisa de doação e agendar</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Médico ou doador |
| **Quero** | Visualizar as pessoas que precisam de doação, filtráveis por tipo sanguíneo, e agendar uma doação para uma delas |
| **Para que** | Doadores compatíveis sejam direcionados a quem precisa com mais urgência, com local e horário claros |
| **Tela do Figma** | "Doações" — lista de pessoas com filtro por tipo sanguíneo, botão "Fazer doação" → modal "Agende sua doação" (endereço, horário, "Confirmar e traçar rota no celular") |
| **Nível de código** | Sem equivalente no domínio/contrato — não existe conceito de doador nem de matching paciente↔doador no backend |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Agendar doação para paciente crítico | "Mariana Alves" (O-, Concentrado de Hemácias, crítico, a 1,8 km) aparece na lista de Doações | Clico em "Fazer doação" para ela | Vejo a tela "Agende sua doação" com endereço do hemocentro, horário de funcionamento e a opção "Confirmar e traçar rota no celular" |
| Filtrar pessoas que precisam por tipo sanguíneo | Existem 6 pessoas cadastradas, com tipos sanguíneos diferentes | Filtro por "AB+" | Vejo apenas "Beatriz Carvalho" na lista |

</details>

<details>
<summary>▶️👤 <b>HU‑10</b> — Portal do Doador simplificado</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Doador |
| **Quero** | Acessar, após o login, uma versão simplificada da tela de Doações, sem o menu operacional do hospital |
| **Para que** | Minha experiência como doador seja direta — só ver quem precisa e agendar minha doação |
| **Tela do Figma** | "Portal do Doador" — mesmo header "Cesar Life · Portal do Doador", sem o menu Início/Estoque/Requisição/Rede/Pacientes |
| **Nível de código** | Sem equivalente — perfis de acesso (Médico/Doador) não existem no backend ainda |

| Cenário | Dado | Quando | Então |
| :--- | :--- | :--- | :--- |
| Doador vê apenas o portal de doações | Fiz login como "Doador" com o nome "João Doador" | Acesso o sistema | Vejo o cabeçalho "Cesar Life · Portal do Doador" e a lista de pessoas que precisam, sem os menus Início/Estoque/Requisição/Rede/Pacientes |
| Doador consegue agendar doação do mesmo jeito que o médico | Estou logado como doador | Clico em "Fazer doação" para uma pessoa da lista | Vejo a mesma tela "Agende sua doação", com endereço e horário do hemocentro |

</details>

<h3 id="-entrega-02" align="center">📌 Entrega 02 — 21/09 <br>
<img src="https://img.shields.io/badge/-Spring_Boot-111827?style=flat-square&logo=springboot&logoColor=green" height="20"/>
<img src="https://img.shields.io/badge/-React-111827?style=flat-square&logo=react&logoColor=61DAFB" height="20"/>
<a href="https://youtu.be/3RlgYAGPyaE" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_2-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Status-Concluída-22C55E?style=flat" height="20"/>
</h3>

<p align="center">
Sai do protótipo e liga o domínio a código rodando: back-end em <b>Spring Boot</b> (HU‑01 e HU‑03, do
<a href="https://github.com/viictorpaes/Rotavital/pull/2">PR #2</a>) e front-end em <b>React</b> (HU‑01, HU‑09 e HU‑10)
— 5 histórias com código real, acima do mínimo de 2.
</p>

| Requisito da entrega | Descrição | Status |
| :--- | :--- | :---: |
| ✅ Histórias implementadas | Mínimo de 2 HUs em código real (não só Figma) | <img src="https://img.shields.io/badge/5%2F2-22C55E?style=flat-square" height="18"/> **Feito** |
| 🔁 Versionamento atuante | Commits na `main`, no mínimo semanais | <img src="https://img.shields.io/badge/Commits_semanais_na_main-22C55E?style=flat-square" height="18"/> **Feito** — PRs #2 e #10 mesclados em 14/09, com commits também em 23/08, 09/09 e 12/09 |
| 🐞 Issue/Bug Tracker | GitHub Issues atualizado toda semana + print no README | <img src="https://img.shields.io/badge/5_abertas_%2F_2_fechadas-22C55E?style=flat-square" height="18"/> **Feito** — 7 issues com a label `bug`, lista atual em [Issue/Bug Tracker](#issue-tracker-entrega-04) |

| Artefato | Link |
| :--- | :--- |
| 🎬 Screencast — uso do sistema e explicação do código | [Assistir vídeo](https://youtu.be/3RlgYAGPyaE) |

<h4 align="center">📝 Histórias Implementadas — Entrega 02</h4>

<p align="center">Resumo em <b>post-it</b>, como visto em sala; o detalhamento de cada HU está na <a href="#-entrega-01">Entrega 01</a>.</p>

> 🟨 **HU‑01** — Como **usuário (médico ou doador)**, preciso escolher meu tipo de acesso e informar meu
> nome, para entrar direto na experiência certa para o meu papel, sem um cadastro completo.
>
> ✅ Frontend — [`PaginaLogin.tsx`](./frontend/src/pages/Login/PaginaLogin.tsx)
> ✅ Backend — `POST /api/v1/acessos` via [`AcessoController.java`](./backend/src/main/java/com/rotavital/api/AcessoController.java) (PR #2)

> 🟨 **HU‑03** — Como **médico**, preciso consultar o estoque por tipo sanguíneo, com validade, temperatura
> e localização de cada lote, para saber quais estão em risco de vencer ou fora da faixa ideal.
>
> ✅ Backend — `GET /api/v1/bancos/{bancoId}/estoque?tipoSanguineo=` via [`EstoqueController.java`](./backend/src/main/java/com/rotavital/api/EstoqueController.java) (PR #2)
> ✅ Frontend — [`PaginaEstoque.tsx`](./frontend/src/pages/Estoque/PaginaEstoque.tsx), entregue depois, com filtros, paginação e cadastro de bolsa (PI3-179/180)

> 🟨 **HU‑09** — Como **médico ou doador**, preciso visualizar as pessoas que precisam de doação — filtráveis
> por tipo sanguíneo — e agendar uma doação, para direcionar doadores compatíveis a quem precisa com urgência.
>
> ✅ Frontend — [`PaginaDoacoes.tsx`](./frontend/src/pages/Doacoes/PaginaDoacoes.tsx) + [`ModalAgendarDoacao.tsx`](./frontend/src/components/donations/ModalAgendarDoacao.tsx)

> 🟨 **HU‑10** — Como **doador**, preciso acessar uma versão simplificada da tela de Doações, sem o menu
> operacional do hospital, para uma experiência direta: ver quem precisa e agendar minha doação.
>
> ✅ Frontend — [`PaginaPortalDoador.tsx`](./frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx)

<h3 id="-entrega-03" align="center">📌 Entrega 03 — 19/10 <br>
<img src="https://img.shields.io/badge/-Supabase_Auth-111827?style=flat-square&logo=supabase&logoColor=3ECF8E" height="20"/>
<img src="https://img.shields.io/badge/-CRUD_de_Estoque-111827?style=flat-square&logo=springboot&logoColor=green" height="20"/>
<a href="https://youtu.be/pcgMnh8VSwU" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_3_e_4-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Concluído-green?style=flat" height="20"/>
</h3>

<p align="center">
Duas histórias saem do protótipo e passam a gravar no <b>Supabase</b>: o <b>login com e-mail e senha</b>, que leva
cada perfil (médico, doador, admin) à sua tela, e o <b>estoque de verdade</b>, com cadastro, listagem com filtros e
mensagens de erro claras. No Jira: histórias <b>PI3-171</b> (Login) e <b>PI3-170</b> (Crud de estoque).
</p>

| Requisito da entrega | Descrição | Status |
| :--- | :--- | :---: |
| ✅ Histórias implementadas | Mais 2 HUs em código real, descritas em post-it abaixo | <img src="https://img.shields.io/badge/2%2F2-22C55E?style=flat-square" height="18"/> **Feito** |
| 🔁 Versionamento atuante | Commits de código na `main`, no mínimo semanais | <img src="https://img.shields.io/badge/4_de_4_semanas-22C55E?style=flat-square" height="18"/> **Feito** — commits de código em todas as semanas de 22/09 a 19/10 ([histórico](https://github.com/viictorpaes/Rotavital/commits/main?since=2026-09-22&until=2026-10-19)) |
| 🎬 Screencast — uso do sistema | Vídeo no YouTube, com áudio ou legenda, mostrando as 2 histórias novas | <img src="https://img.shields.io/badge/Publicado-22C55E?style=flat-square" height="18"/> **Feito** (PI3-221) — [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |
| 🎬 Screencast — explicação do código | Vídeo no YouTube, com áudio ou legenda, explicando o código das 2 histórias | <img src="https://img.shields.io/badge/Publicado-22C55E?style=flat-square" height="18"/> **Feito** (PI3-222) — [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |
| 🐞 Issue/Bug Tracker | GitHub Issues atualizado + print no README | <img src="https://img.shields.io/badge/0_abertas_%2F_7_fechadas-22C55E?style=flat-square" height="18"/> **Feito** (PI3-223) — lista atualizada em 06/11, print em [Issue/Bug Tracker](#issue-tracker-entrega-04) |

| Artefato | Link |
| :--- | :--- |
| 🎬 Screencast — uso do sistema e explicação do código (vídeo único das Entregas 03 e 04) | [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |

<h4 align="center">📝 Histórias Implementadas — Entrega 03</h4>

<p align="center">Resumo em <b>post-it</b>, como visto em sala; o detalhamento de cada HU está na <a href="#-entrega-01">Entrega 01</a>.</p>

> 🟨 **HU‑01 · Login (PI3‑171)** — Como **médico, doador ou administrador**, preciso entrar com meu **e-mail e
> senha**, para cair direto na tela do meu perfil e não ver o que não é meu.
>
> ✅ Perfis decididos — quem entra e o que cada um vê: [`PERFIS_DE_ACESSO.md`](./docs/PERFIS_DE_ACESSO.md) (PI3‑182)
> ✅ Backend — `POST /api/v1/acessos` confere a senha no Supabase Auth e devolve o perfil e a tela inicial:
> [`AcessoController.java`](./backend/src/main/java/com/rotavital/api/AcessoController.java),
> [`AutenticacaoService.java`](./backend/src/main/java/com/rotavital/servico/AutenticacaoService.java),
> [`SupabaseAuth.java`](./backend/src/main/java/com/rotavital/supabase/SupabaseAuth.java) (PI3‑183)
> ✅ Frontend — login leva médico ao Painel, doador ao Portal e admin à tela de Admin, com aviso de senha errada
> ou usuário inexistente: [`PaginaLogin.tsx`](./frontend/src/pages/Login/PaginaLogin.tsx),
> [`ContextoAutenticacao.tsx`](./frontend/src/context/ContextoAutenticacao.tsx) (PI3‑184, PI3‑196)
> 📎 Evidências — [`PI3-184_PI3-196_login.md`](./docs/evidencias/PI3-184_PI3-196_login.md)

> 🟨 **HU‑03 · Crud de estoque (PI3‑170)** — Como **médico**, preciso cadastrar as bolsas que chegam e consultar o
> estoque com filtros, para saber o que tenho de cada tipo e componente e o que vence primeiro.
>
> ✅ Backend — `POST /api/v1/hemocomponentes` com as regras de validade, volume e temperatura (PI3‑179);
> `GET /api/v1/hemocomponentes` com filtros de banco, componente, tipo e status, paginação e ordem por validade (PI3‑187);
> respostas 400/404/422 com mensagem clara (PI3‑181); totais por tipo sanguíneo e componente (PI3‑188):
> [`EstoqueController.java`](./backend/src/main/java/com/rotavital/api/EstoqueController.java),
> [`EstoqueService.java`](./backend/src/main/java/com/rotavital/servico/EstoqueService.java)
> ✅ Frontend — tela de Estoque lendo da API, com filtros, paginação e o formulário **Nova bolsa**:
> [`PaginaEstoque.tsx`](./frontend/src/pages/Estoque/PaginaEstoque.tsx),
> [`ModalNovaBolsa.tsx`](./frontend/src/components/estoque/ModalNovaBolsa.tsx) (PI3‑180, PI3‑179)
> 🔄 Edição e descarte de bolsas (PI3‑190) — implementados no [PR #29](https://github.com/viictorpaes/Rotavital/pull/29), aguardando merge

<h3 id="-entrega-04" align="center">📌 Entrega 04 — 09/11 <br>
<img src="https://img.shields.io/badge/-Campanhas_e_Doações-111827?style=flat-square&logo=supabase&logoColor=3ECF8E" height="20"/>
<img src="https://img.shields.io/badge/-Rotas_e_Mapas-111827?style=flat-square&logo=googlemaps&logoColor=4285F4" height="20"/>
<a href="https://youtu.be/pcgMnh8VSwU" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_3_e_4-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Status-Concluído-green?style=flat" height="20"/>
</h3>

<p align="center">
Liga as duas pontas do sistema: o <b>doador</b>, com campanha, agendamento e rota até o hospital, e a <b>logística</b>,
com mapa da rede, menor caminho por Dijkstra e envio de bolsas por FEFO, rastreado até a chegada. No Jira: <b>PI3-174</b>
(Doações), <b>PI3-175</b> (Pacientes), <b>PI3-203 a PI3-205</b> (Portal do Doador), <b>PI3-236</b> (envio de bolsas) e
<b>PI3-237</b> (Como chegar).
</p>

| Requisito da entrega | Descrição | Status |
| :--- | :--- | :---: |
| ✅ Histórias implementadas | Histórias restantes (mínimo de 2), descritas em post-it abaixo | <img src="https://img.shields.io/badge/4%2F2-22C55E?style=flat-square" height="18"/> **Feito** — 4 HUs completas e a HU‑08 em parte |
| 🔁 Versionamento atuante | Commits de código na `main`, no mínimo semanais | <img src="https://img.shields.io/badge/3_de_3_semanas-22C55E?style=flat-square" height="18"/> **Feito** — commits de código em todas as semanas desde 20/10, com os PRs #32, #33, #34, #36 e #37 ([histórico](https://github.com/viictorpaes/Rotavital/commits/main?since=2026-10-20&until=2026-11-09)) |
| 🎬 Screencast — uso do sistema | Vídeo no YouTube, com áudio ou legenda, com ênfase nas histórias novas | <img src="https://img.shields.io/badge/Publicado-22C55E?style=flat-square" height="18"/> **Feito** — [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |
| 🎬 Screencast — explicação do código | Vídeo no YouTube, com áudio ou legenda, explicando o código das histórias | <img src="https://img.shields.io/badge/Publicado-22C55E?style=flat-square" height="18"/> **Feito** — [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |
| 🐞 Issue/Bug Tracker | Uso do GitHub Issues + print no README | <img src="https://img.shields.io/badge/0_abertas_%2F_7_fechadas-22C55E?style=flat-square" height="18"/> **Feito** — print em [Issue/Bug Tracker](#issue-tracker-entrega-04) |

| Artefato | Link |
| :--- | :--- |
| 🎬 Screencast — uso do sistema e explicação do código (vídeo único das Entregas 03 e 04) | [Assistir vídeo](https://youtu.be/pcgMnh8VSwU) |

<h4 align="center">📝 Histórias Implementadas — Entrega 04</h4>

<p align="center">Resumo em <b>post-it</b>, como visto em sala; o detalhamento de cada HU está na <a href="#-entrega-01">Entrega 01</a>.</p>

> 🟨 **HU‑06 · Campanha de doação (PI3‑174)** — Como **médico**, preciso publicar uma campanha para um paciente,
> para que doadores voluntários vejam quem precisa e possam ajudar diretamente.
>
> ✅ Backend — `POST`, `PUT` e `PATCH /api/v1/campanhas`: publicar cria o paciente e a campanha na mesma transação;
> encerrar cancela os agendamentos em aberto (PI3‑191):
> [`CampanhaController.java`](./backend/src/main/java/com/rotavital/api/CampanhaController.java),
> [`CampanhaService.java`](./backend/src/main/java/com/rotavital/servico/CampanhaService.java)
> ✅ Frontend — formulário da aba Campanha e tela de Doações com editar e encerrar (PI3‑193):
> [`FormularioCampanha.tsx`](./frontend/src/components/requisicao/FormularioCampanha.tsx),
> [`PaginaDoacoes.tsx`](./frontend/src/pages/Doacoes/PaginaDoacoes.tsx)
> 📎 Evidências — [`PI3-174_doacoes.md`](./docs/evidencias/PI3-174_doacoes.md)

> 🟨 **HU‑09 · Agendar doação (PI3‑174)** — Como **doador**, preciso escolher dia e horário para doar a quem precisa,
> para ir ao hemocentro com vaga garantida; como **médico**, preciso acompanhar esses agendamentos.
>
> ✅ Backend — doador agenda, remarca e cancela; médico registra doação feita, falta ou cancelamento. Agenda de segunda
> a sábado, das 07h às 19h, com até 3 doadores por horário e um agendamento em aberto por doador (PI3‑192):
> [`AgendamentoController.java`](./backend/src/main/java/com/rotavital/api/AgendamentoController.java),
> [`AgendamentoService.java`](./backend/src/main/java/com/rotavital/servico/AgendamentoService.java),
> [`AgendaDoHemocentro.java`](./backend/src/main/java/com/rotavital/dominio/AgendaDoHemocentro.java)
> ✅ Frontend — [`ModalAgendarDoacao.tsx`](./frontend/src/components/donations/ModalAgendarDoacao.tsx),
> [`MinhasDoacoes.tsx`](./frontend/src/components/donations/MinhasDoacoes.tsx),
> [`ModalAgendamentosCampanha.tsx`](./frontend/src/components/donations/ModalAgendamentosCampanha.tsx) (PI3‑193)

> 🟨 **HU‑10 · Portal do Doador (PI3‑203, PI3‑204, PI3‑205, PI3‑237)** — Como **doador**, preciso criar minha conta,
> manter meu perfil e saber como chegar ao hospital, para doar sem depender do menu operacional do hospital.
>
> ✅ Backend — `POST /api/v1/doadores`, `GET` e `PUT /api/v1/doadores/eu`, hemocentros e horários livres:
> [`DoadorController.java`](./backend/src/main/java/com/rotavital/api/DoadorController.java),
> [`DoadorService.java`](./backend/src/main/java/com/rotavital/servico/DoadorService.java),
> [`HemocentroController.java`](./backend/src/main/java/com/rotavital/api/HemocentroController.java)
> ✅ Como chegar — localização só com o consentimento do doador (PI3‑261), distância e tempo pela Routes API do Google,
> com estimativa própria se ela não responder (PI3‑262), e botões para o Google Maps e o Waze (PI3‑263):
> [`RotaAteHospitalService.java`](./backend/src/main/java/com/rotavital/servico/RotaAteHospitalService.java),
> [`GoogleRoutes.java`](./backend/src/main/java/com/rotavital/google/GoogleRoutes.java),
> [`ModalComoChegar.tsx`](./frontend/src/components/donations/ModalComoChegar.tsx)
> ✅ Frontend — [`PaginaCadastroDoador.tsx`](./frontend/src/pages/CadastroDoador/PaginaCadastroDoador.tsx),
> [`PaginaPortalDoador.tsx`](./frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx),
> [`CartaoPerfilDoador.tsx`](./frontend/src/components/donations/CartaoPerfilDoador.tsx)

> 🟨 **HU‑07 · Rede hospitalar e envio de bolsas (PI3‑236)** — Como **médico**, preciso ver a rede no mapa, calcular o
> melhor caminho e acompanhar a bolsa até a chegada, para que ela chegue a tempo e na temperatura certa.
>
> ✅ Mapa e rota — pontos reais da rede (PI3‑258) e menor caminho por Dijkstra desenhado nas ruas, com origem selecionável (PI3‑259):
> [`RotaNoMapaService.java`](./backend/src/main/java/com/rotavital/servico/RotaNoMapaService.java),
> [`RedeDistribuicao.java`](./backend/src/main/java/com/rotavital/dominio/RedeDistribuicao.java),
> [`MapaRede.tsx`](./frontend/src/components/rede/MapaRede.tsx)
> ✅ Envio — `POST /api/v1/transferencias` despacha por FEFO só bolsa disponível, na validade e dentro da faixa de
> temperatura; `POST /api/v1/transferencias/{id}/recebimento` confirma a chegada:
> [`TransferenciaService.java`](./backend/src/main/java/com/rotavital/servico/TransferenciaService.java),
> [`TransferenciaDeBolsa.java`](./backend/src/main/java/com/rotavital/dominio/TransferenciaDeBolsa.java),
> [`PrioridadeEnvio.tsx`](./frontend/src/components/rede/PrioridadeEnvio.tsx)
> ✅ Acompanhamento — linha do tempo com progresso estimado pelo tempo da rota, atualizada a cada 15 s (PI3‑260):
> [`TransferenciasEmAndamento.tsx`](./frontend/src/components/rede/TransferenciasEmAndamento.tsx),
> [`LinhaDoTempoTransferencia.tsx`](./frontend/src/components/rede/LinhaDoTempoTransferencia.tsx)

> 🟨 **HU‑08 · Fila de pacientes (PI3‑175)** — Como **médico**, preciso cadastrar, editar e remover pacientes e vê-los
> na fila clínica, para atender primeiro quem está em estado mais grave.
>
> ✅ Backend — `GET`, `POST`, `PUT` e `DELETE /api/v1/pacientes`, com filtros e a ordem da fila clínica (PI3‑194):
> [`PacienteController.java`](./backend/src/main/java/com/rotavital/api/PacienteController.java),
> [`PacienteService.java`](./backend/src/main/java/com/rotavital/servico/PacienteService.java)
> ✅ Frontend — Novo paciente, Editar e Remover para o médico; o admin só consulta (PI3‑195):
> [`PaginaPacientes.tsx`](./frontend/src/pages/Pacientes/PaginaPacientes.tsx),
> [`ModalPaciente.tsx`](./frontend/src/components/pacientes/ModalPaciente.tsx)
> 🔄 Concluir procedimento — a tela existe, mas a baixa ainda não é gravada no banco ([pendências](#issue-tracker-entrega-04))

> [!NOTE]
> **Ficaram em aberto:** a HU‑04 (a requisição gera o protocolo só na tela) e a HU‑05 (a aba Recebidas usa remessas de
> exemplo). A API já tem a sugestão de lote em `GET /bancos/{bancoId}/sugestao-lote`, mas a tela ainda não a usa.

<h4 align="center" id="issue-tracker-entrega-04">🐛 Issue/Bug Tracker — Entrega 04 <br>
<img src="https://img.shields.io/badge/0_Issues_Abertas-22C55E?style=flat-square&logo=github&logoColor=white" height="20"/>
<img src="https://img.shields.io/badge/7_Issues_Fechadas-22C55E?style=flat-square&logo=github&logoColor=white" height="20"/>
</h4>

<p align="center">
Lista do <a href="https://github.com/viictorpaes/Rotavital/issues" target="_blank">GitHub Issues</a> em 06/11/2026: as 7
issues estão fechadas, e a tabela detalha as duas desta entrega. O teste de ponta a ponta do mesmo dia encontrou 4
pendências, listadas abaixo, que ainda precisam virar issues.
</p>

<p align="center">
<img src="./img/issues_fechadas_atual.png" width="800"/>
</p>

| Imagem | Issue | Resumo |
| :---: | :--- | :--- |
| <img src="./img/issue-tracker_issue6-painel-operacional.png" width="260"/> | ✅ [#6 — Painel Operacional (HU‑02)](https://github.com/viictorpaes/Rotavital/issues/8) | Fechada em 06/11: números rápidos e caixa de avisos, hoje lendo o estoque e as campanhas do backend. |
| <img src="./img/issue-tracker_issue7-doador-campanha.png" width="260"/> | ✅ [#7 — Modelar Doador e Campanha](https://github.com/viictorpaes/Rotavital/issues/9) | Fechada em 06/11: `CampanhaDoacao`, `AgendamentoDoacao` e o perfil do doador gravam no banco (PRs #32 e #34). |

| Pendência a registrar | Onde | O que acontece |
| :--- | :--- | :--- |
| 🔴 Perfil do doador falha logo após o login | Portal do Doador | As primeiras chamadas a `/doadores/eu` e `/agendamentos/meus` saem antes de a sessão ser gravada e voltam 401; o aviso vermelho some com F5. |
| 🔴 `GET /api/v1/diagnostico` responde 500 | Tela de Admin | `@CrossOrigin(origins = "*")` conflita com `allowCredentials(true)`, e a tela mostra valores de reserva (4 pontos, 6 conexões, 45 ms) no lugar dos reais. |
| 🟠 Requisição e Recebidas não gravam | Requisição (HU‑04, HU‑05) | O protocolo `REQ-…` e a conferência de remessas valem só na sessão do navegador; as remessas são dados de exemplo. |
| 🟠 Concluir procedimento não grava | Pacientes (HU‑08) | A baixa no estoque vale só na sessão: ao recarregar a página, o paciente volta para a fila. |

<h2 align="center" id="modulos">📦 Módulos do Sistema <br>
<img src="https://img.shields.io/badge/Módulos-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="22"/>
</h2>

<p align="center">
Cada módulo junta telas, endpoints (lista completa no <a href="#api">Catálogo da API</a>) e classes de domínio.
Detalhe em <a href="./docs/MODULOS.md"><code>docs/MODULOS.md</code></a>.
</p>

| Módulo | Telas (rota do frontend) | Endpoints | Domínio | Status |
| :--- | :--- | :--- | :--- | :---: |
| 🔑 Acesso e Perfis (HU‑01, HU‑10) | `/login`, `/cadastro-doador` | `POST /acessos`, `POST /doadores`, `GET`·`PUT /doadores/eu` | `Usuario`, `PapelUsuario` + Supabase Auth | ✅ Implementado |
| 📦 Estoque e Hemocomponentes (HU‑02, HU‑03, HU‑08) | `/painel`, `/estoque` | `/bancos/{bancoId}/estoque` · `/resumo` · `/alertas`, `/hemocomponentes` | `Estoque`, `BolsaHemocomponente`, `AlertaDeEstoque` | ✅ Implementado |
| 🏥 Requisições e Alocação (HU‑04, HU‑05) | `/requisicao` (abas Solicitar e Recebidas) | `GET /bancos/{bancoId}/sugestao-lote` | `RequisicaoHospitalar`, `CompatibilidadeSanguinea`, `FilaRequisicoesHospitalares` | ⚠️ Parcial — requisição e recebimento sem controller |
| ❤️ Pacientes e Doações (HU‑06, HU‑08, HU‑09) | `/pacientes`, `/requisicao` (aba Campanha), `/doacoes`, `/portal-doador` | `/pacientes`, `/campanhas`, `/agendamentos`, `/hemocentros` | `Paciente`, `CampanhaDoacao`, `AgendamentoDoacao`, `AgendaDoHemocentro` | ✅ Implementado — conclusão de procedimento ainda não é gravada |
| 🚚 Roteirização e Logística (HU‑07, PI3‑236) | `/rede` | `GET /pontos`, `/rotas`, `/rotas/trajeto`, `/hemocomponentes`, `/transferencias` | `RedeDistribuicao` (grafo + Dijkstra), `Conexao`, `RotaCalculada`, `TransferenciaDeBolsa` | ✅ API · ✅ tela com dados reais |
| 🌡️ Telemetria e Cadeia Fria | — | só contrato (`/entregas/{id}/leituras`) | *(nenhuma)* | ⚠️ Gap de domínio |
| 🛠️ Admin e Infraestrutura | `/admin` | `GET /diagnostico`, `/benchmarks/auditoria-telemetria`, `/actuator/health` | `diagnostico`, `benchmark` (paralelismo) | ✅ Implementado |

<h2 align="center" id="arquitetura">🏰 Arquitetura do Projeto <br>
<img src="https://img.shields.io/badge/Architecture-111827?style=flat-square&logo=instructure&logoColor=white" height="22"/>
</h2>

<p align="center">
Navegador → <b>frontend</b> (React no Nginx) → <b>backend</b> (Spring Boot) → <b>banco</b> (PostgreSQL local ou Supabase).
Diagramas, camadas e fluxos de dados em <a href="./docs/ARQUITETURA.md"><code>docs/ARQUITETURA.md</code></a>.
</p>

<pre>
Rotavital🩸/
├── backend <img src="https://img.shields.io/badge/-Java_21-111827?style=flat&logo=openjdk&logoColor=orange" height="18"/> <img src="https://img.shields.io/badge/-Spring_Boot_3.3-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/-Maven-111827?style=flat&logo=apachemaven&logoColor=C71A36" height="18"/>/
│   ├── Dockerfile <img src="https://img.shields.io/badge/-Multi--stage_·_roda_os_testes_no_1º_estágio-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   └── src
│       ├── main/java/com/rotavital <img src="https://img.shields.io/badge/-Pacote_raiz-111827?style=flat-square&logo=openjdk&logoColor=orange" height="18"/>/
│       │   ├── api <img src="https://img.shields.io/badge/-Controller_·_endpoints_REST_+_DTOs-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │   ├── servico <img src="https://img.shields.io/badge/-Serviço_·_casos_de_uso_e_permissão-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │   ├── dominio <img src="https://img.shields.io/badge/-Model_·_entidades_e_regras_(FEFO,_ABO%2FRh,_Dijkstra)-111827?style=flat&logo=openjdk&logoColor=purple" height="18"/>/
│       │   ├── repositorio <img src="https://img.shields.io/badge/-Persistência_·_Spring_Data_JPA-111827?style=flat&logo=spring&logoColor=6DB33F" height="18"/>/
│       │   ├── supabase <img src="https://img.shields.io/badge/-Login_no_Supabase_Auth-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/>/
│       │   ├── config · diagnostico <img src="https://img.shields.io/badge/-CORS_·_relógio_·_log_HTTP_·_%2Fdiagnostico-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │   └── benchmark <img src="https://img.shields.io/badge/-Paralelismo_(threads)-111827?style=flat&logo=openjdk&logoColor=F46800" height="18"/>/
│       └── test <img src="https://img.shields.io/badge/-JUnit_5-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/> <img src="https://img.shields.io/badge/-Testcontainers-111827?style=flat-square&logo=testcontainers&logoColor=9B489A" height="18"/> <img src="https://img.shields.io/badge/-Cucumber-111827?style=flat-square&logo=cucumber&logoColor=23D96C" height="18"/>/
├── frontend <img src="https://img.shields.io/badge/-React_18-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/> <img src="https://img.shields.io/badge/-TypeScript-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/> <img src="https://img.shields.io/badge/-Vite-111827?style=flat&logo=vite&logoColor=646CFF" height="18"/> <img src="https://img.shields.io/badge/-Tailwind_CSS-111827?style=flat&logo=tailwindcss&logoColor=38BDF8" height="18"/>/
│   ├── Dockerfile · nginx/ <img src="https://img.shields.io/badge/-Build_+_Nginx_·_proxy_%2Fapi_→_backend-111827?style=flat&logo=nginx&logoColor=009639" height="18"/>
│   └── src/
│       ├── pages · components <img src="https://img.shields.io/badge/-View_·_uma_pasta_por_tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       ├── services <img src="https://img.shields.io/badge/-Chamadas_à_API_(api.ts)-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>/
│       ├── context · hooks · lib <img src="https://img.shields.io/badge/-Sessão_·_dados_·_regras_de_tela-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>/
│       └── data <img src="https://img.shields.io/badge/-Dados_de_exemplo_(Rede_e_Recebidas)-111827?style=flat-square&logo=json&logoColor=yellow" height="18"/>/
├── supabase <img src="https://img.shields.io/badge/-Supabase-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/> <img src="https://img.shields.io/badge/-PostgreSQL-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>/
│   ├── migrations <img src="https://img.shields.io/badge/-7_migrations_·_14_tabelas_·_2_views-111827?style=flat-square&logo=postgresql&logoColor=4169E1" height="18"/>/
│   └── seed.sql · usuarios_de_teste.sql <img src="https://img.shields.io/badge/-Dados_e_usuários_de_teste-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
├── db <img src="https://img.shields.io/badge/-Postgres_17_do_Compose-111827?style=flat-square&logo=postgresql&logoColor=4169E1" height="18"/>/
├── c <img src="https://img.shields.io/badge/-C99_·_Estruturas_de_Dados_(AV1)-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>/
├── docs <img src="https://img.shields.io/badge/Docs-111827?style=flat&logo=markdown&logoColor=blue" height="18"/>/ · pdf <img src="https://img.shields.io/badge/-PDF-111827?style=flat-square&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>/ · img <img src="https://img.shields.io/badge/Assets-green?style=flat&logo=image&logoColor=white" height="18"/>/
├── .github/workflows/esteira.yml <img src="https://img.shields.io/badge/-testes_→_imagens_→_deploy-111827?style=flat&logo=githubactions&logoColor=2088FF" height="18"/>
├── compose.yaml · compose.supabase.yaml <img src="https://img.shields.io/badge/-Docker_local_·_Docker_+_Supabase-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
├── package.json <img src="https://img.shields.io/badge/-Atalhos_npm_run_docker-111827?style=flat&logo=npm&logoColor=CB3837" height="18"/>
├── .env.example <img src="https://img.shields.io/badge/-Variáveis_do_Compose-111827?style=flat&logo=dotenv&logoColor=ECD53F" height="18"/>
└── README.md <img src="https://img.shields.io/badge/-Markdown-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
</pre>

<h2 align="center" id="supabase">🟢 Banco de Dados — Supabase <br>
<img src="https://img.shields.io/badge/-Supabase-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="22"/>
<img src="https://img.shields.io/badge/-PostgreSQL-111827?style=flat&logo=postgresql&logoColor=4169E1" height="22"/>
<img src="https://img.shields.io/badge/Tabelas-14_%2B_2_views-3ECF8E?style=flat&logo=supabase&logoColor=white" height="22"/>
<img src="https://img.shields.io/badge/Testes_de_constraints-34%2F34-brightgreen?style=flat" height="22"/>

<br>

<img src="./supabase/img/supabase.png" width="440" alt="init supabase">
</h2>

<p align="center">
A persistência fica no <b>Supabase</b> (PostgreSQL gerenciado). Só o backend acessa o banco; o frontend usa a API
<code>/api/v1</code>. Detalhes em <a href="./docs/SUPABASE.md"><code>docs/SUPABASE.md</code></a> e DER em
<a href="./docs/DER.md"><code>docs/DER.md</code></a>; variáveis em <a href="#variaveis">Como Executar</a>.
</p>

| Item | Valor |
| :--- | :--- |
| 🆔 Project ref | `gilyfswvezmvtmlxgvrd` |
| 🌍 URL do projeto | `https://gilyfswvezmvtmlxgvrd.supabase.co` |
| 📍 Região | AWS `us-east-2` (Ohio) |
| 🐘 Banco | PostgreSQL, database `postgres`, schema `public` |
| 🔌 Pooler | `aws-0-us-east-2.pooler.supabase.com:5432` (Session mode, IPv4) |

<details>
<summary>▶️🔌 <b>Conexão do backend</b> (<code>application.properties</code>)</summary>

```properties
spring.datasource.url=jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.gilyfswvezmvtmlxgvrd
spring.datasource.password=${SUPABASE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
```

- `sslmode=require` 🔒 — o Supabase só aceita conexões criptografadas.
- Session Pooler em vez da conexão direta — a direta é só IPv6; o pooler aceita IPv4 (rede da faculdade e Docker).
- `ddl-auto=validate` ✅ — o Hibernate só confere se as `@Entity` batem com as tabelas e nunca altera o banco: o schema vem só das migrations. Se algo não bater, a aplicação não sobe.
</details>

<details>
<summary>▶️🗂️ <b>Tabelas e migrations</b></summary>

| Tabela | Classe de domínio | PK |
| :--- | :--- | :--- |
| 📍 `ponto_rede` | `Hospital`, `BancoDeSangue` (+ `Endereco`) | `id` (text, ex.: `BS-01`) |
| 🛣️ `conexao` | `Conexao` | `id` (identity) |
| 🩸 `bolsa_hemocomponente` | `BolsaHemocomponente` | `id` (text, ex.: `CH-1042`) |
| 🏥 `requisicao_hospitalar` | `RequisicaoHospitalar` | `id` (uuid) |
| 🔗 `alocacao` | `AlocacaoDTO` | `id` (uuid) |
| 🚑 `entrega` | `MonitoramentoEntregaDTO` | `id` (uuid) |
| 🌡️ `leitura_telemetria` | `LeituraTelemetriaDTO` | `id` (identity) |
| 👤 `usuario` | `Usuario` (HU‑01, HU‑10) | `id` (uuid) |
| 🩺 `paciente` | `Paciente` (HU‑02, HU‑08, HU‑09) | `id` (uuid) |
| 📥 `remessa` | — (HU‑05) | `id` (uuid) |
| 📣 `campanha_doacao` | `CampanhaDoacao` (HU‑06) | `id` (uuid) |
| 📅 `agendamento_doacao` | `AgendamentoDoacao` (HU‑09, HU‑10) | `id` (uuid) |
| ✅ `procedimento` · `procedimento_bolsa` | — (HU‑08) | `id` (uuid) |
| 👁️ `vw_estoque_por_tipo` · `vw_estoque_agrupado` | views do Painel/Estoque (HU‑02, HU‑03) | — |

> Das 7 tabelas da migration `escopo_clinico`, `remessa`, `procedimento` e `procedimento_bolsa` ainda não têm
> classe de domínio nem `@Entity`: as telas delas (Recebidas e Concluir procedimento) seguem sem gravar no banco.
> Detalhe coluna a coluna em [`docs/DER.md`](./docs/DER.md#4-dicionario).

| # | Migration | O que faz |
| :---: | :--- | :--- |
| 1️⃣ | [`20260924120000_schema_inicial.sql`](./supabase/migrations/20260924120000_schema_inicial.sql) | Cria as 7 tabelas, PKs, FKs e índices, e liga o RLS |
| 2️⃣ | [`20260924120100_constraints_integridade.sql`](./supabase/migrations/20260924120100_constraints_integridade.sql) | NOT NULL, DEFAULT, UNIQUE, 33 CHECK, 7 FKs compostas e o gatilho `trg_alocacao_validar` |
| 3️⃣ | [`20260925120000_ajustes_schema_existente.sql`](./supabase/migrations/20260925120000_ajustes_schema_existente.sql) | Colunas novas em `ponto_rede`, `bolsa_hemocomponente`, `requisicao_hospitalar` e `alocacao`, status `UTILIZADA`, alocação ativa única por bolsa e gatilho genérico de `atualizado_em` |
| 4️⃣ | [`20260925120100_escopo_clinico.sql`](./supabase/migrations/20260925120100_escopo_clinico.sql) | Cria as 7 tabelas do escopo clínico (`usuario`, `paciente`, `remessa`, `campanha_doacao`, `agendamento_doacao`, `procedimento`, `procedimento_bolsa`) |
| 5️⃣ | [`20260925120200_views_painel.sql`](./supabase/migrations/20260925120200_views_painel.sql) | Cria as views `vw_estoque_por_tipo` e `vw_estoque_agrupado` usadas pelo Painel e pelo Estoque |
| 6️⃣ | [`20261015010623_papel_admin.sql`](./supabase/migrations/20261015010623_papel_admin.sql) | Inclui o papel `ADMIN` no `CHECK` de `usuario.papel` (PI3-183) |
| 7️⃣ | [`20261030120000_perfil_doador.sql`](./supabase/migrations/20261030120000_perfil_doador.sql) | Perfil do doador em `usuario`: e-mail (único), telefone e data de nascimento (PI3-203) |
| 8️⃣ | [`20261104120000_transferencia_bolsa.sql`](./supabase/migrations/20261104120000_transferencia_bolsa.sql) | Rastreabilidade das bolsas (PI3-236): `hospital_atual_id` e a tabela `transferencia_bolsa`, com de onde saíram, para onde foram, quem despachou e quem recebeu |

> ⚠️ Os prints abaixo foram tirados depois das duas primeiras migrations (7 tabelas); as migrations 3 a 8 ainda não têm print.
</details>

<details>
<summary>▶️🛡️ <b>Row Level Security (RLS)</b></summary>

| Acesso | Role | Resultado hoje |
| :--- | :--- | :--- |
| ☕ Backend (JDBC) | `postgres` | ✅ lê e escreve (ignora RLS) |
| 🌐 API REST com chave `anon` | `anon` | 🚫 bloqueado (RLS ligado, sem políticas) |
| 👤 Usuário logado via Supabase Auth | `authenticated` | 🚫 bloqueado até existirem políticas |
</details>

<details>
<summary>▶️🚀 <b>Como aplicar as migrations</b></summary>

**Opção A — SQL Editor:** abrir o projeto → **SQL Editor** → rodar as 8 migrations em ordem (pelo prefixo de data) → rodar `supabase/seed.sql` para carregar os dados de exemplo → criar os usuários de teste seguindo [`supabase/usuarios_de_teste.sql`](./supabase/usuarios_de_teste.sql) → conferir em **Table Editor**.

**Opção B — Supabase CLI:**

```bash
npx supabase login
npx supabase link --project-ref gilyfswvezmvtmlxgvrd
npx supabase db push
```

> O `db push` aplica só as migrations. A carga inicial ([`supabase/seed.sql`](./supabase/seed.sql)) é rodada pelo
> **SQL Editor**; o script é idempotente e pode ser executado mais de uma vez sem duplicar dados.
</details>

<details>
<summary>▶️📸 <b>Prints do Supabase</b></summary>

| Tabelas | Colunas de `bolsa_hemocomponente` |
| :---: | :---: |
| <img src="./supabase/img/Database%20Tables.png" width="400"/> | <img src="./supabase/img/Colunas_exemplo(bolsa_hemocomponente).png" width="400"/> |
| **Schema Visualizer** | **Migrations** |
| <img src="./supabase/img/Schema_Vizualizer.png" width="400"/> | <img src="./supabase/img/Migrations_no_supabase.png" width="400"/> |
| **Logs do Postgres** | **Projeto** |
| <img src="./supabase/img/logs_postgres.png" width="400"/> | <img src="./supabase/img/supabase.png" width="400"/> |
</details>

<h2 align="center" id="como-executar">🚀 Como Executar <br>
<img src="https://img.shields.io/badge/-Docker-111827?style=flat&logo=docker&logoColor=2496ED" height="22"/>
<img src="https://img.shields.io/badge/-npm_run-111827?style=flat&logo=npm&logoColor=CB3837" height="22"/>
<img src="https://img.shields.io/badge/Terminal-111827?style=flat&logo=gnubash&logoColor=white" height="22"/>
</h2>

<p align="center">
O jeito recomendado é o <b>Docker</b>: um comando sobe banco, API e site, sem instalar Java, Maven ou Postgres.
</p>

**Pré-requisitos:** Git, Docker Desktop (Compose 2.24+) e Node.js 20+. Java 21 e Maven 3.9 só no modo desenvolvimento.

<h4 align="left">⚡ Quickstart — Docker em 4 passos</h4>

**1. Clonar e criar o `.env`:**

```bash
git clone https://github.com/viictorpaes/Rotavital.git
cd Rotavital
cp .env.example .env # Windows (PowerShell): Copy-Item .env.example .env
```

**2. Preencher o `.env`:** o `DB_PASSWORD` de exemplo já serve. Para o login, preencha a `SUPABASE_KEY`; para o modo
nuvem, também a `SUPABASE_DB_PASSWORD` (peça os valores ao líder do grupo).

**3. Subir, escolhendo o banco:**

| | 🐳 Docker Local | ☁️ Docker + Supabase |
| :--- | :--- | :--- |
| **Comando** | `npm run docker:local` | `npm run docker:cloud` |
| **Banco** | Postgres 17 num container, criado com as migrations e o seed | Supabase, o mesmo da produção (⚠️ grava no banco do time) |
| **Use para** | desenvolver e testar sem medo | conferir com os dados reais |

```bash
npm run docker:local 
# ou: npm run docker:cloud
```

O `up` reconstrói sozinho as imagens do que mudou no código (`pull_policy: build`), então o que sobe é sempre o que está na
pasta. Não precisa rodar `docker:build` antes.

**4. Conferir:** `npm run docker:status` deve mostrar os containers `(healthy)`. Tudo passa por uma porta só: o **site** abre
em http://localhost:8080 (entre com um dos [usuários de teste](#usuarios-teste)), a **API** responde em
http://localhost:8080/api/v1 e a saúde em http://localhost:8080/actuator/health. A versão no ar aparece em
http://localhost:8080/versao.json.

<h4 align="left" id="comandos-npm">🧰 Comandos npm</h4>

| Comando | O que faz | Equivale a |
| :--- | :--- | :--- |
| `npm run docker:build` | Só gera as imagens, sem subir (o build do backend roda os testes) | `docker compose build` |
| `npm run docker:local` | Sobe db → backend → frontend | `docker compose -f compose.yaml up -d` |
| `npm run docker:cloud` | Sobe backend → frontend ligados ao Supabase | `docker compose -f compose.yaml -f compose.supabase.yaml up -d` |
| `npm run docker:status` | Lista os containers e se estão `(healthy)` | `docker compose ps` |
| `npm run docker:logs` | Acompanha os logs (`Ctrl+C` para sair) | `docker compose logs -f` |
| `npm run docker:down` | Para os containers; os dados ficam | `docker compose down` |
| `npm run docker:down:v` | ⚠️ Para e **apaga o banco local** | `docker compose down -v` |

<sub>Sem Node, rode o comando da última coluna. Os atalhos também funcionam dentro de <code>frontend/</code> (menos <code>build</code> e <code>logs</code>).</sub>

<h4 align="left" id="variaveis">🔑 Variáveis de ambiente: </h4>

| Arquivo | Variável | Para quê |
| :--- | :--- | :--- |
| `.env` (raiz) | `DB_NAME` · `DB_USER` · `DB_PASSWORD` | Banco do container (o exemplo já funciona) |
| | `SUPABASE_KEY` | Chave publicável do Supabase Auth; sem ela, login e cadastro de doador respondem `503` |
| | `SUPABASE_DB_PASSWORD` | Senha do banco do Supabase, só para o `docker:cloud` |
| | `GOOGLE_MAPS_API_KEY` | Opcional: Routes API do Google para o "Como chegar" ([como criar](#google-maps)); sem ela, a distância é estimada |
| | `GOOGLE_MAPS_BROWSER_KEY` | Opcional: mapa interativo do Google na tela de Rede ([como criar](#google-maps)); sem ela, o mapa é o OpenStreetMap |
| | `WEB_PORT` · `API_PORT` | Portas na sua máquina: o site, com a API em `/api/` (padrão `8080`), e o acesso direto ao backend, só para depurar (padrão `8082`) |
| | `COMPOSE_FILE` | Banco do `docker compose up` sem `-f` (`compose.yaml` ou `compose.yaml:compose.supabase.yaml`) |
| `backend/.env` | `SUPABASE_DB_PASSWORD` · `SUPABASE_KEY` · `GOOGLE_MAPS_API_KEY` | As mesmas, para o modo desenvolvimento ([modelo](./backend/.env.example)) |
| `frontend/.env.local` | `VITE_API_URL` · `VITE_API_TIMEOUT_MS` · `VITE_GOOGLE_MAPS_KEY` | Opcional: base da API (`/api/v1`), timeout (`10000` ms) e a chave do mapa da Rede ([modelo](./frontend/.env.example)) |

> [!WARNING]
> O `.env` fica fora do Git. A chave **`service_role`** do Supabase ignora o RLS: nunca vai para commit ou frontend.

<details id="google-maps">
<summary>▶️🗺️ <b>Google Maps — rota do doador (PI3-237) e mapa da Rede (PI3-236)</b> — chaves opcionais</summary>

No Portal do Doador, o botão **Como chegar** do cartão do paciente pede a localização do doador (com
consentimento, pela Geolocation API do navegador), mostra a distância de carro e o tempo de chegada com o trânsito
de agora e abre a navegação GPS no **Google Maps** ou no **Waze**. O destino são as coordenadas cadastradas do hospital
do paciente, resolvidas pelo backend (`POST /api/v1/campanhas/{id}/rota`).

| Peça | Onde |
| :--- | :--- |
| Localização do doador (PI3-261) | [`useGeolocalizacao.ts`](./frontend/src/hooks/useGeolocalizacao.ts) · [`geolocalizacao.ts`](./frontend/src/lib/geolocalizacao.ts) |
| Distância e tempo pela Routes API (PI3-262) | [`GoogleRoutes.java`](./backend/src/main/java/com/rotavital/google/GoogleRoutes.java) · [`RotaAteHospitalService.java`](./backend/src/main/java/com/rotavital/servico/RotaAteHospitalService.java) |
| Links do Google Maps e do Waze (PI3-263) | [`navegacao.ts`](./frontend/src/lib/navegacao.ts) · [`BotoesNavegacao.tsx`](./frontend/src/components/donations/BotoesNavegacao.tsx) |
| Modal "Como chegar" (PI3-237) | [`ModalComoChegar.tsx`](./frontend/src/components/donations/ModalComoChegar.tsx) |

**Criar a chave** (uma vez, por quem administra o projeto no Google Cloud):

1. [console.cloud.google.com](https://console.cloud.google.com) → crie ou escolha um projeto e ative o faturamento
   (a Routes API tem cota gratuita mensal; com trânsito, conta como SKU *Pro*).
2. *APIs e serviços → Biblioteca* → ative a **Routes API**.
3. *APIs e serviços → Credenciais → Criar credenciais → Chave de API* → em *Restrições de API*, deixe só a Routes API.
4. Coloque a chave em `GOOGLE_MAPS_API_KEY` no `.env` (Docker), no `backend/.env` (modo desenvolvimento) ou nas
   variáveis secretas do Render (produção). Ela fica só no backend e nunca chega ao navegador.

Sem a chave, ou se o Google não responder em 4 s, o "Como chegar" continua funcionando: a distância vira a linha reta
× 1,3 a 25 km/h e a tela avisa que é uma estimativa. Os botões do Google Maps e do Waze não usam a chave.

**Mapa da tela de Rede (PI3-258):** o mapa interativo vem da Maps JavaScript API, que roda no navegador. Por isso ele usa
uma **segunda chave**, feita para ficar exposta no site: a do backend nunca vai para o JavaScript.

1. *APIs e serviços → Biblioteca* → ative a **Maps JavaScript API**.
2. *Credenciais → Criar credenciais → Chave de API* → em *Restrições de aplicativos*, escolha **Sites** e liste
   `http://localhost:8080/*`, `http://localhost:5173/*` e o domínio da Vercel; em *Restrições de API*, deixe só a Maps
   JavaScript API.
3. Coloque a chave em `GOOGLE_MAPS_BROWSER_KEY` no `.env` (Docker), em `VITE_GOOGLE_MAPS_KEY` no `frontend/.env.local`
   (modo desenvolvimento) ou em `VITE_GOOGLE_MAPS_KEY` nas variáveis do projeto na Vercel (produção).

Sem essa chave, ou se o Google recusá-la, a tela de Rede usa o mapa do OpenStreetMap, com os mesmos marcadores.

**Rota no mapa (PI3-259):** o **Dijkstra** (`GET /api/v1/rotas`) decide por onde passar, sobre as conexões cadastradas. O
`GET /api/v1/rotas/trajeto` leva esse caminho para as ruas pela Routes API, com as paradas na ordem do Dijkstra, e devolve o
traçado (polyline), a distância e o tempo com trânsito. Usa a chave do backend, então não precisa de nada novo. Sem ela,
os números são os das conexões e o traçado vem do OSRM, sem trânsito; a tela mostra de onde vieram.

**Envio e rastreamento (PI3-236, PI3-260):** a tela lista as bolsas do estoque real por FEFO. O médico confirma o envio
(`POST /api/v1/transferencias`): a bolsa fica `EM_TRANSITO`, com a localização "Em transporte refrigerado → hospital", e
ganha uma linha do tempo (Origem, Em trânsito, Chegada estimada, Destino) que se atualiza sozinha a cada 15 s. As maletas
não têm GPS: o progresso é estimado pelo tempo da rota. A chegada só vale quando o médico confirma o recebimento, e aí
a bolsa fica `ENTREGUE`, com `hospital_atual_id` no hospital de destino.

> [!NOTE]
> O navegador só libera a localização em **HTTPS** ou em `localhost`. Para testar no celular, use o site publicado
> (Vercel) ou um túnel HTTPS; pelo IP da rede (`http://192.168…`) a localização é recusada e só os botões funcionam.
</details>

<details>
<summary>▶️💻 <b>Modo desenvolvimento (sem Docker)</b> — backend em <code>localhost:8080</code> e frontend em <code>localhost:5173</code></summary>

```bash
# Backend (ligado ao Supabase)
cp backend/.env.example backend/.env   # preencha SUPABASE_DB_PASSWORD e SUPABASE_KEY
cd backend && mvn spring-boot:run

# Frontend (em outro terminal; o Vite repassa /api para localhost:8080)
cd frontend && npm install && npm run dev
```

| Comando | Descrição |
| :--- | :--- |
| `mvn test` | Testes unitários (JUnit 5), sem banco |
| `mvn verify` | Unitários + integração e aceitação num Postgres do Testcontainers — precisa de Docker ([detalhes](#cdt-testes)) |
| `mvn test -Dtest=TesteFluxoTest` | Demonstração do domínio: estoque, requisição e alocação por FEFO/ABO-Rh |
| `mvn clean package` | Gera o `.jar` em `backend/target/` |
| `npm run build` · `npm run lint` | Build de produção (com checagem de tipos) e ESLint do frontend |

No Windows (PowerShell), troque `cp` por `Copy-Item` e `&&` por `;`.
</details>

<details>
<summary>▶️🩺 <b>Problemas comuns</b></summary>

| Sintoma | Solução |
| :--- | :--- |
| `Defina DB_PASSWORD no arquivo .env` | Faltou o `cp .env.example .env` |
| Login ou cadastro de doador responde `503` | Preencha a `SUPABASE_KEY` e rode `npm run docker:local` de novo |
| `port is already allocated` | Mude `WEB_PORT` ou `API_PORT` no `.env` |
| A tela parece de uma versão antiga | `npm run docker:local` reconstrói o que mudou; `curl localhost:8080/versao.json` mostra de quando é o build no ar. Se ele estiver certo, recarregue sem cache (`Cmd+Shift+R` · `Ctrl+Shift+R`) |
| Migration nova não aparece no banco local | `npm run docker:down:v` e suba de novo (apaga os dados locais) |
| Erro no `compose.supabase.yaml` | Atualize o Docker Desktop (Compose 2.24+) |
| O mapa da Rede abre no OpenStreetMap, mesmo com a `GOOGLE_MAPS_BROWSER_KEY` | O console do navegador diz o motivo (ex.: `RefererNotAllowedMapError`, `ApiNotActivatedMapError`) |
| "Como chegar" mostra "Estimativa pela distância em linha reta" | Preencha a `GOOGLE_MAPS_API_KEY` com a Routes API ativa; o log do backend diz o motivo (ex.: `403 PERMISSION_DENIED`) |
| "Como chegar" não pede a localização no celular | A página precisa estar em HTTPS ([detalhes](#google-maps)) |
</details>

<h2 align="center" id="api">🌐 Catálogo da API <br>
<img src="https://img.shields.io/badge/REST_·_%2Fapi%2Fv1-111827?style=flat&logo=springboot&logoColor=6DB33F" height="22"/>
<img src="https://img.shields.io/badge/-Swagger-111827?style=flat&logo=swagger&logoColor=85EA2D" height="22"/>
</h2>

<p align="center">
Base <code>http://localhost:8080/api/v1</code>, JSON nos dois sentidos. Os erros vêm no formato RFC 7807, com o motivo no
<code>detail</code>: 400 dado inválido · 401 sem login · 403 perfil sem permissão · 404 inexistente · 409 conflito ·
422 regra de negócio · 503 Supabase Auth fora.
</p>

**📖 Swagger:** abra o [`docs/openapi.yaml`](./docs/openapi.yaml) no [editor.swagger.io](https://editor.swagger.io)
(*File → Import URL* → `https://raw.githubusercontent.com/viictorpaes/Rotavital/main/docs/openapi.yaml`), no
Postman/Insomnia (*Import → File*) ou na sua máquina:

```bash
docker run --rm -p 8081:8080 -e SWAGGER_JSON=/spec/openapi.yaml -v "$(pwd)/docs:/spec" swaggerapi/swagger-ui
# abre em http://localhost:8081 — no PowerShell, troque $(pwd) por ${PWD}
```

<sub>O contrato ainda não descreve <code>/doadores</code>, <code>/hemocentros</code> e <code>/diagnostico</code>; a lista abaixo é a completa.</sub>

**🔐 Quem pode chamar:** os endpoints com 🔒 exigem o header `X-Usuario-Id` com o `id` que o login devolve (o site
manda sozinho). Sem o header → `401`; perfil errado → `403`.

<details>
<summary>▶️🔑 <b>Acesso e Doador</b> — <code>/acessos</code>, <code>/doadores</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `POST` | `/acessos` | livre | Login: `{email, senha}` → `{id, nome, tipoAcesso, telaInicial, menu, accessToken}` (HU‑01) |
| `POST` | `/doadores` | livre | Cadastro do doador: cria a conta no Supabase Auth e o perfil |
| `GET` · `PUT` | `/doadores/eu` | 🔒 doador | Ver e alterar o próprio perfil (HU‑10) |
</details>

<details>
<summary>▶️📦 <b>Estoque, Bolsas e Sugestão de Lote</b> — <code>/bancos/{bancoId}/…</code>, <code>/hemocomponentes</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `GET` | `/bancos/{bancoId}/estoque?tipoSanguineo=` | livre | Estoque do banco, com as bolsas (HU‑03) |
| `GET` | `/bancos/{bancoId}/estoque/resumo` | livre | Totais por tipo sanguíneo e por componente |
| `GET` | `/bancos/{bancoId}/alertas` | livre | Bolsas vencidas, perto de vencer ou fora da temperatura (HU‑02) |
| `GET` | `/bancos/{bancoId}/sugestao-lote?tipoComponente=&tipoSanguineo=&quantidade=` | livre | Lotes ABO/Rh compatíveis que vencem primeiro (HU‑04) |
| `GET` | `/hemocomponentes?bancoId=&tipoSanguineo=&tipoComponente=&status=&page=&size=` | livre | Lista paginada com filtros (20 por página) |
| `GET` | `/hemocomponentes/{id}` | livre | Uma bolsa |
| `POST` | `/hemocomponentes` | livre | Cadastra uma bolsa; `400`/`422` dizem o campo ou a regra |
| `PATCH` | `/hemocomponentes/{id}` | livre | Muda o status (`{"status":"RESERVADA"}`), só pelas transições permitidas (HU‑08) |
</details>

<details>
<summary>▶️🩺 <b>Pacientes</b> — <code>/pacientes</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `GET` | `/pacientes?status=&tipoSanguineo=&gravidade=&hospitalId=` | livre | Fila clínica: mais grave e mais antigo primeiro |
| `GET` | `/pacientes/{id}` | livre | Um paciente |
| `POST` · `PUT` · `DELETE` | `/pacientes` · `/pacientes/{id}` | 🔒 médico | Cadastrar, editar e remover |
</details>

<details>
<summary>▶️❤️ <b>Campanhas, Agendamentos e Hemocentros</b> — <code>/campanhas</code>, <code>/agendamentos</code>, <code>/hemocentros</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `GET` | `/campanhas?status=` | livre | Campanhas de doação (`ATIVA` ou `ENCERRADA`) |
| `GET` | `/campanhas/{id}` | livre | Uma campanha |
| `POST` · `PUT` | `/campanhas` · `/campanhas/{id}` | 🔒 médico | Publicar e editar (HU‑06) |
| `PATCH` | `/campanhas/{id}/encerramento` | 🔒 médico | Encerrar |
| `GET` | `/campanhas/{id}/agendamentos` | 🔒 médico ou admin | Quem agendou para a campanha |
| `POST` | `/campanhas/{id}/agendamentos` | 🔒 doador | Agendar `{bancoId, agendadoPara}`; um agendamento em aberto por doador (HU‑09) |
| `POST` | `/campanhas/{id}/rota` | 🔒 qualquer perfil | Hospital do paciente e, com `{latitude, longitude}`, distância e tempo pela Routes API do Google (PI3‑237) |
| `GET` | `/agendamentos/meus` | 🔒 doador | Minhas doações |
| `PATCH` | `/agendamentos/{id}` | 🔒 doador | Remarcar |
| `PATCH` | `/agendamentos/{id}/status` | 🔒 médico ou doador | `REALIZADO`, `CANCELADO` ou `FALTOU` |
| `GET` | `/hemocentros` · `/hemocentros/{id}` | livre | Hemocentros onde doar |
| `GET` | `/hemocentros/{id}/horarios?data=AAAA-MM-DD` | livre | Horários e vagas do dia |
</details>

<details>
<summary>▶️🚚 <b>Rede e Rotas</b> — <code>/pontos</code>, <code>/conexoes</code>, <code>/rotas</code>, <code>/transferencias</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `GET` | `/pontos` | livre | Hemocentro e hospitais da rede |
| `GET` | `/conexoes` | livre | Ligações entre os pontos, com distância e tempo |
| `GET` | `/rotas?origemId=&destinoId=&janelaEntregaLimite=` | livre | Menor caminho pelo grafo (Dijkstra) (HU‑07) |
| `GET` | `/rotas/trajeto?origemId=&destinoId=` | 🔒 médico ou admin | O caminho do Dijkstra levado para as ruas, com o traçado para o mapa (PI3‑259) |
| `GET` | `/transferencias?status=` | livre | Transferências de bolsas; `EM_TRANSITO` são as ativas (PI3‑236) |
| `POST` | `/transferencias` | 🔒 médico | Despacha a bolsa (`{bolsaId, destinoId}`): `EM_TRANSITO`, com a chegada prevista pela rota (PI3‑236) |
| `POST` | `/transferencias/{id}/recebimento` | 🔒 médico | Confirma a chegada: a bolsa fica `ENTREGUE`, no hospital de destino (PI3‑236) |
</details>

<details>
<summary>▶️🛠️ <b>Admin e Infraestrutura</b> — <code>/diagnostico</code>, <code>/benchmarks</code>, <code>/actuator</code></summary>

| Método | Endpoint | Quem | Para quê |
| :--- | :--- | :--- | :--- |
| `GET` | `/diagnostico` | livre (a tela é só do admin) | Conexão com o banco e últimas requisições HTTP |
| `GET` · `POST` | `/benchmarks/auditoria-telemetria` | livre | Auditoria de telemetria sequencial × paralela (Infraestrutura de Software) |
| `GET` | `/actuator/health` · `/actuator/info` | livre | Saúde (inclui o banco) e versão — fora do `/api/v1` |
</details>

<h4 align="left" id="usuarios-teste">👤 Usuários de teste</h4>

| Perfil | E-mail | Senha | Abre em |
| :--- | :--- | :--- | :--- |
| 🩺 Médico | `medico@rotavital.test` | `paguetti911` | `/painel` |
| ❤️ Doador | `doador@rotavital.test` | `franca911` | `/portal-doador` |
| 🛠️ Admin | `admin@rotavital.test` | `helema911` | `/admin` |

<sub>A senha é conferida no Supabase Auth, então o login precisa da <code>SUPABASE_KEY</code> no <code>.env</code>. Para criar outro doador, use <code>/cadastro-doador</code>.</sub>

**🧪 Chamadas prontas** (dados do [`seed.sql`](./supabase/seed.sql): banco `BS-01`, hospital `HOSP-01`, bolsas `CH-1042`, `CH-1061`, …):

```bash
curl localhost:8080/api/v1/bancos/BS-01/estoque
curl "localhost:8080/api/v1/bancos/BS-01/sugestao-lote?tipoComponente=HEMACIAS&tipoSanguineo=A_NEGATIVO&quantidade=2"
curl "localhost:8080/api/v1/rotas?origemId=BS-01&destinoId=HOSP-01"
curl -X POST localhost:8080/api/v1/acessos -H 'Content-Type: application/json' \
     -d '{"email":"medico@rotavital.test","senha":"paguetti911"}'
```

<sub>No Windows, use <code>curl.exe</code>. Mais exemplos, com status esperado × obtido, em <a href="./docs/evidencias/PI3-14_endpoints_rest.md"><code>docs/evidencias/</code></a>.</sub>

<h2 align="center" id="figma-vs-real">🖼️ Figma × Sistema Real <br>
<img src="https://img.shields.io/badge/Protótipo-111827?style=flat&logo=figma&logoColor=orange" height="22"/> vs <img src="https://img.shields.io/badge/Sistema_real-111827?style=flat&logo=react&logoColor=61DAFB" height="22"/>
</h2>

<p align="center">
O protótipo ao lado da mesma tela rodando de verdade (prints com Playwright, 1440×900, em 10 e 11/10/2026 —
<a href="./docs/evidencias/PI3-182_perfis_de_acesso.md">evidências do PI3-182</a>). Clique no nome do arquivo para ver o código.
</p>

| Tela | Figma | Sistema real |
| :--- | :---: | :---: |
| 🔐 **HU‑01 · Login**<br><sub>e-mail e senha no lugar dos cards; o print mostra o aviso de senha errada (PI3‑184/196)</sub><br><sub>[`PaginaLogin.tsx`](./frontend/src/pages/Login/PaginaLogin.tsx)</sub> | <img src="./img/FIGMA_Login_Medico.png" width="300"/> | <img src="./docs/evidencias/PI3-184/01_login_senha_errada.png" width="230"/> |
| 📊 **HU‑02 · Painel Operacional**<br><sub>[`PaginaPainel.tsx`](./frontend/src/pages/PainelOperacional/PaginaPainel.tsx)</sub> | <img src="./img/FIGMA_PainelOperacional.png" width="300"/> | <img src="./docs/evidencias/PI3-182/02_medico_entra.png" width="300"/> |
| 📦 **HU‑03 · Estoque**<br><sub>bolsas do banco <code>BS-01</code>; cadastro de bolsa no modal (PI3‑179)</sub><br><sub>[`PaginaEstoque.tsx`](./frontend/src/pages/Estoque/PaginaEstoque.tsx) · [`ModalNovaBolsa.tsx`](./frontend/src/components/estoque/ModalNovaBolsa.tsx)</sub> | <img src="./img/FIGMA_Estoque.png" width="300"/> | <img src="./docs/evidencias/PI3-182/09_admin_abre_estoque.png" width="300"/> |
| 🏥 **HU‑04 · Requisição (solicitar)**<br><sub>[`PaginaRequisicao.tsx`](./frontend/src/pages/Requisicao/PaginaRequisicao.tsx)</sub> | <img src="./img/FIGMA_Requisicao_Solicitar.png" width="300"/> | <img src="./img/real-requisicao_solicitar.png" width="300"/> |
| 🏥 **HU‑05 · Requisição (recebidas)** | <img src="./img/FIGMA_Requisicao_Recebidas.png" width="300"/> | <img src="./img/real-requisicao_recebidas.png" width="300"/> |
| 🏥 **HU‑06 · Requisição (campanha)** | <img src="./img/FIGMA_Requisicao_Campanha.png" width="300"/> | <img src="./img/real-requisicao_campanha.png" width="300"/> |
| 🗺️ **HU‑07 · Rede hospitalar**<br><sub>[`PaginaRede.tsx`](./frontend/src/pages/RedeHospitalar/PaginaRede.tsx)</sub> | <img src="./img/FIGMA_RedeHospitalar.png" width="300"/> | <img src="./img/real-rede_hospitalar.png" width="300"/> |
| 🩺 **HU‑08 · Pacientes (fila)**<br><sub>[`PaginaPacientes.tsx`](./frontend/src/pages/Pacientes/PaginaPacientes.tsx)</sub> | <img src="./img/FIGMA_Pacientes.png" width="300"/> | <img src="./img/real-pacientes.png" width="300"/> |
| 🩺 **HU‑08 · Concluir procedimento**<br><sub>[`ModalConcluirProcedimento.tsx`](./frontend/src/components/pacientes/ModalConcluirProcedimento.tsx)</sub> | <img src="./img/FIGMA_Pacientes_ConcluirProcedimento.png" width="300"/> | <img src="./img/real-pacientes_concluirprocedimento.png" width="300"/> |
| ❤️ **HU‑09 · Doações**<br><sub>[`PaginaDoacoes.tsx`](./frontend/src/pages/Doacoes/PaginaDoacoes.tsx)</sub> | <img src="./img/FIGMA_Doacoes.png" width="300"/> | <img src="./img/real-doacoes.png" width="300"/> |
| ❤️ **HU‑09 · Agende sua doação**<br><sub>[`ModalAgendarDoacao.tsx`](./frontend/src/components/donations/ModalAgendarDoacao.tsx)</sub> | <img src="./img/FIGMA_Doacoes_AgendeSuaDoacao.png" width="300"/> | <img src="./img/real-doacoes_agendesuadoacao.png" width="300"/> |
| 👤 **HU‑10 · Portal do Doador**<br><sub>[`PaginaPortalDoador.tsx`](./frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx)</sub> | <img src="./img/FIGMA_PortalDoDoador.png" width="300"/> | <img src="./docs/evidencias/PI3-182/05_doador_entra.png" width="300"/> |
| 🛠️ **Admin · Diagnóstico & Logs**<br><sub>só o perfil admin abre (<a href="./docs/PERFIS_DE_ACESSO.md">PI3‑182</a>)</sub><br><sub>[`PaginaAdmin.tsx`](./frontend/src/pages/Admin/PaginaAdmin.tsx)</sub> | *(criada depois do protótipo)* | <img src="./docs/evidencias/PI3-182/08_admin_entra.png" width="300"/> |

<sub>Os prints são de outubro; desde então Pacientes, Campanha e Doações passaram a gravar no banco.</sub>

<h2 align="center" id="documentacao">📚 Documentação <br>
<img src="https://img.shields.io/badge/Docs-111827?style=flat&logo=markdown&logoColor=white" height="22"/>
</h2>

<p align="center">
Índice completo dos documentos técnicos em <a href="./docs/DOCUMENTACAO.md"><code>docs/DOCUMENTACAO.md</code></a>.
</p>

| Documento | Descrição |
| :--- | :--- |
| 🏰 [`ARQUITETURA.md`](./docs/ARQUITETURA.md) | Componentes, ambientes, camadas, pacotes e fluxos de dados |
| 🧬 [`MODELO_DE_DOMINIO.md`](./docs/MODELO_DE_DOMINIO.md) | Classes do domínio (POO), grafo e Dijkstra |
| 🌐 [`CONTRATOS_DE_API.md`](./docs/CONTRATOS_DE_API.md) | Endpoints por módulo, padrões e erros |
| 📄 [`openapi.yaml`](./docs/openapi.yaml) | Contrato OpenAPI 3.0.3 |
| 📦 [`MODULOS.md`](./docs/MODULOS.md) | Contrato × domínio × banco, por módulo |
| 🗄️ [`DER.md`](./docs/DER.md) | 15 tabelas, 2 views e constraints |
| 🟢 [`SUPABASE.md`](./docs/SUPABASE.md) | Conexão, `.env`, RLS e prints |
| 🔐 [`PERFIS_DE_ACESSO.md`](./docs/PERFIS_DE_ACESSO.md) | Perfis (médico, doador, admin), telas e decisões do login |
| 🔌 [`LIGACOES_E_REDES.md`](./docs/LIGACOES_E_REDES.md) | Ligações e sub-redes (RSD) |
| 🗺️ [`DIAGRAMA_ROTAVITAL.md`](./docs/DIAGRAMA_ROTAVITAL.md) | Diagrama de arquitetura de rede (draw.io) |
| 🧩 [`INVENTARIO_COMPONENTES.md`](./docs/INVENTARIO_COMPONENTES.md) | Componentes ativos × planejados |
| ⚙️ [`RELATORIO_ATIVIDADE_PARALELISMO.md`](./docs/RELATORIO_ATIVIDADE_PARALELISMO.md) | Auditoria sequencial × paralela (Atividade - Infraestrutura de Software (SO)) |
| 🔁 [`TRADUCAO_C_JAVA_AV1.md`](./docs/TRADUCAO_C_JAVA_AV1.md) | Tradução comentada C ↔ Java: lista, fila e pilha (AV1) |
| 🧾 [`evidencias/`](./docs/evidencias/) | Chamadas reais aos endpoints e prints por tarefa do Jira |
| 🎬 [`ROTEIRO_MVP.md`](./docs/ROTEIRO_MVP.md) | Roteiro-exemplo dos screencasts e da apresentação final, ensaiado com o sistema rodando |
| 📕 [`arquitetura-rotavital-paes-silva.pdf`](./pdf/arquitetura-rotavital-paes-silva.pdf) | PDF final entregue - Atividade de Infraestrutura de Redes (RSD) |
| 🗺️ [`arquitetura-rotavital-paes-silva.drawio`](./docs/arquitetura-rotavital-paes-silva.drawio) | Arquivo-fonte do diagrama entregue junto com o PDF (RSD) |

<h2 align="center" id="container-tdd-deploy">🐳🧪🚀 Desafio do PI — Container, TDD e Deploy automatizados <br>
<img src="https://img.shields.io/badge/-Docker_·_TDD_·_GitHub_Actions-111827?style=flat&logo=githubactions&logoColor=2088FF" height="22"/>
</h2>

<p align="center">
Unidade 2 de <b>Infraestrutura de Software</b> (2026.2), com os mesmos <a href="#integrantes">integrantes</a> do PI.
A pergunta do desafio: <i>"se um colega clonar o repositório amanhã, ele consegue rodar, testar e publicar o sistema
sem pedir ajuda a ninguém?"</i>
</p>

| Entrega | Resumo | Status |
| :--- | :--- | :---: |
| 🐳 [**E1 · Container**](#cdt-como-rodar) | `docker compose up -d` sobe banco + API + site; o Dockerfile roda os testes antes de gerar a imagem | ✅ |
| 🧪 [**E2 · TDD**](#cdt-testes) | 3 histórias, 13 tarefas, 9 pares `test:` 🔴 → `feat:` 🟢; 208 unitários, 7 de integração e 13 cenários Gherkin | ✅ |
| 🚀 [**E3 · Deploy**](#cdt-pipeline) | GitHub Actions: testes → imagem → deploy (API no Render, site na Vercel), só na `main` e só com tudo verde | ⏳ falta ligar as contas |

> [!IMPORTANT]
> Falta o que só quem tem acesso às contas consegue fazer: proteger a `main`, criar a API no Render e o site na Vercel,
> cadastrar os segredos e colher as evidências. O passo a passo está em [Configuração única](#cdt-configuracao).

<p align="center">
<b>Seções pedidas no enunciado:</b>
<a href="#integrantes">Integrantes</a> ·
<a href="#cdt-como-rodar">Como rodar</a> ·
<a href="#cdt-testes">Testes</a> ·
<a href="#cdt-pipeline">Pipeline e deploy</a> ·
<a href="#cdt-decisoes">Decisões</a> ·
<a href="#cdt-ia">Uso de IA</a> ·
<a href="#cdt-jornada">Jornada TDD</a>
</p>

<h3 align="center" id="cdt-como-rodar">▶️ Como rodar — E1 · Container automatizado</h3>

Só precisa de **Git** e **Docker**. Java, Maven, Node e Postgres ficam dentro dos containers.

```bash
git clone https://github.com/viictorpaes/Rotavital.git && cd Rotavital
cp .env.example .env            # no Windows: Copy-Item .env.example .env — depois troque DB_PASSWORD
docker compose up -d            # compila o que mudou, roda os testes e sobe db → backend → frontend
docker compose ps               # os três precisam aparecer como (healthy)
```

| Serviço | O que é | Healthcheck · só sobe depois de | Endereço |
| :--- | :--- | :--- | :--- |
| 🐘 `db` | Postgres 17 com as migrations de `supabase/migrations` e o seed ([`db/`](./db/)), dados no volume `dados-postgres` | `pg_isready` | só na rede interna |
| ☕ `backend` | API Spring Boot ([`backend/Dockerfile`](./backend/Dockerfile), 2 estágios) | `/actuator/health` = `UP` (inclui o banco) · `db` | `localhost:8080/api/` (pelo Nginx) · `localhost:8082` (direto) |
| 🖥️ `frontend` | React no Nginx, com proxy `/api/` → `backend` ([`frontend/Dockerfile`](./frontend/Dockerfile)) | `GET /` · `backend` | `localhost:8080` |

`docker compose down` mantém os dados no volume; `docker compose down -v` apaga, e a próxima subida recria o banco com o seed.
Os mesmos passos têm atalhos `npm run docker:*` (ver [Como Executar](#como-executar)).

> [!NOTE]
> O Compose usa um Postgres próprio, então a API sobe sem a senha do Supabase e sem internet; em produção ela usa o
> Supabase. O **login** confere a senha no Supabase Auth: com a `SUPABASE_KEY` no `.env` e internet, funciona também no
> Compose (os usuários de teste já vêm no banco local); sem a chave, responde `503`.

<details>
<summary>📸 <b>Evidências do E1</b> — saídas reais do terminal (macOS + Docker Desktop 29.7)</summary>

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

Imagens finais, ambas sem root: `rotavital-backend` com 378 MB (usuário `rotavital`) e `rotavital-frontend` com 91 MB (usuário `101`).
</details>

<h3 align="center" id="cdt-testes">🧪 Testes — E2 · TDD</h3>

<h4 align="left">🗺️ Story map — backbone e Release 1</h4>

As colunas são o **backbone** (as atividades do médico e do doador, em ordem); as linhas são as releases. A Release 1
pegou, em cada atividade, a regra cujo erro machuca mais.

| | 🔐 Entrar | 📊 Painel | 📦 Estoque | 🏥 Requisitar | 🚚 Transportar | 🩺 Atender | ❤️ Doar |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Backbone** | entrar com o perfil | ver números e alertas | ver lotes por tipo | pedir hemocomponente | planejar a rota | concluir o procedimento | agendar a doação |
| ✅ **Fora do TDD** | HU‑01 · login no Supabase Auth (PI3‑183/184) | totais por tipo | HU‑03 · listagem e cadastro (PI3‑179/187) | — | HU‑07 rota (Dijkstra) | cadastro de pacientes (PI3‑175) | HU‑09/10 (mock) |
| 🟢 **Release 1 (TDD)** | — | **HU‑02** alertas | HU‑03 (caracterização) | **HU‑04** sugestão de lote | — | **HU‑08** baixa da bolsa | — |
| 🔵 **Release 2** | bloquear por perfil na API | HU‑F03 notificar alerta | HU‑05 recebimento | gravar alocação · HU‑06 | HU‑F04 rastreio · telemetria | ligar a baixa ao paciente | HU‑F06 elegibilidade |

<h4 align="left">🧩 Histórias → tarefas → red → green</h4>

- **HU‑04** — como médico, quero que o sistema sugira o lote compatível que vence primeiro, sem escolher na mão.
- **HU‑02** — como médico, quero ver no painel as bolsas vencidas, perto de vencer ou fora da temperatura.
- **HU‑08** — como médico, quero dar baixa na bolsa usada, para o estoque refletir o que foi consumido.

| HU | Tarefa | Regra | Teste (nível) | 🔴 `test:` → 🟢 `feat:` |
| :---: | :--- | :--- | :--- | :--- |
| 04 | T4.1 | ABO/Rh das hemácias (8 × 8): O− doa para todos, Rh+ nunca vai para Rh− | [`CompatibilidadeSanguineaTest`](./backend/src/test/java/com/rotavital/dominio/CompatibilidadeSanguineaTest.java) (unitário) | [`67d4c55`](https://github.com/viictorpaes/Rotavital/commit/67d4c55) 9 de 10 falham → [`1b9cd69`](https://github.com/viictorpaes/Rotavital/commit/1b9cd69) 69/69 |
| 04 | T4.2 | Plasma e crio: ABO invertido, Rh indiferente; plaquetas como hemácias | idem | [`9baea07`](https://github.com/viictorpaes/Rotavital/commit/9baea07) 8 falham → [`cbbd912`](https://github.com/viictorpaes/Rotavital/commit/cbbd912) 93/93 |
| 04 | T4.3 | Bolsa apta: disponível, na validade (vencer hoje vale) e na faixa de temperatura | [`BolsaHemocomponenteTest`](./backend/src/test/java/com/rotavital/dominio/BolsaHemocomponenteTest.java) (unitário) | [`e901034`](https://github.com/viictorpaes/Rotavital/commit/e901034) 3 de 9 → [`21c2312`](https://github.com/viictorpaes/Rotavital/commit/21c2312) 98/98 |
| 04 | T4.4 | Ordem: mesmo tipo primeiro (poupa o O−), FEFO, nunca incompatível | [`SugestaoDeLoteTest`](./backend/src/test/java/com/rotavital/dominio/SugestaoDeLoteTest.java) (unitário) | [`8c9fb6b`](https://github.com/viictorpaes/Rotavital/commit/8c9fb6b) 7 de 8 → [`e4d884b`](https://github.com/viictorpaes/Rotavital/commit/e4d884b) 106/106 |
| 04 | T4.5 | `GET /bancos/{bancoId}/sugestao-lote` | [`hu04_sugestao_de_lote.feature`](./backend/src/test/resources/features/hu04_sugestao_de_lote.feature) (aceitação, 6) | [`2dcd504`](https://github.com/viictorpaes/Rotavital/commit/2dcd504) 404 → [`547a00f`](https://github.com/viictorpaes/Rotavital/commit/547a00f) verdes |
| 02 | T2.1–T2.4 | Vencida ou fora da faixa = CRÍTICO; vence em 0 a 7 dias = ATENÇÃO; críticos primeiro | [`AlertasDoEstoqueTest`](./backend/src/test/java/com/rotavital/dominio/AlertasDoEstoqueTest.java) (unitário) | [`93980fa`](https://github.com/viictorpaes/Rotavital/commit/93980fa) 9 de 11 → [`5baca27`](https://github.com/viictorpaes/Rotavital/commit/5baca27) 117/117 |
| 02 | T2.5 | `GET /bancos/{bancoId}/alertas` | [`hu02_alertas_do_painel.feature`](./backend/src/test/resources/features/hu02_alertas_do_painel.feature) (aceitação, 2) | [`b82afdb`](https://github.com/viictorpaes/Rotavital/commit/b82afdb) 404 → [`591006a`](https://github.com/viictorpaes/Rotavital/commit/591006a) verdes |
| 08 | T8.1–T8.2 | 12 transições de status permitidas; bolsa vencida só pode ser descartada | [`MovimentacaoDaBolsaTest`](./backend/src/test/java/com/rotavital/dominio/MovimentacaoDaBolsaTest.java) (unitário) | [`60207da`](https://github.com/viictorpaes/Rotavital/commit/60207da) 29 de 40 → [`1dd60ba`](https://github.com/viictorpaes/Rotavital/commit/1dd60ba) 157/157 |
| 08 | T8.3 | `PATCH /hemocomponentes/{id}` grava no Postgres; recusa → 422 sem gravar | [`hu08_baixa_da_bolsa.feature`](./backend/src/test/resources/features/hu08_baixa_da_bolsa.feature) (aceitação, 3) + [`BaixaDaBolsaIT`](./backend/src/test/java/com/rotavital/integracao/BaixaDaBolsaIT.java) (integração, 4) | [`ec1c83d`](https://github.com/viictorpaes/Rotavital/commit/ec1c83d) 405 → [`f2d1e0f`](https://github.com/viictorpaes/Rotavital/commit/f2d1e0f) 157 + 20 verdes |

Cada `test:` foi commitado vermelho (o código só tinha a assinatura), e o `feat:` seguinte o deixou verde sem quebrar os
anteriores. Tarefas que compartilham o mesmo método viraram um único par.

> [!WARNING]
> Mescle o PR desta entrega com **"Create a merge commit"**: o *squash* junta tudo num commit só e apaga o red → green.

<h4 align="left">📶 Testes por nível</h4>

| Nível | Quantos | Como | Onde roda |
| :--- | :---: | :--- | :--- |
| 🧩 **Unitário** | **208** | JUnit 5, repositório falso e MockMvc. São 98 da Release 1, 59 que já existiam e 51 do login e do cadastro de bolsas, vindos da `main` | `mvn test`: 1º estágio do Dockerfile e esteira |
| 🔗 **Integração** | **7** | Testcontainers: Postgres 17 criado com as migrations do Supabase ([`EstoqueIT`](./backend/src/test/java/com/rotavital/integracao/EstoqueIT.java), [`BaixaDaBolsaIT`](./backend/src/test/java/com/rotavital/integracao/BaixaDaBolsaIT.java)) | `mvn verify`: esteira |
| ✅ **Aceitação** | **13** | Cucumber + Gherkin em português, chamando a API por HTTP ([`features/`](./backend/src/test/resources/features/)); os 2 cenários da HU‑03 são de caracterização | `mvn verify`: esteira |

<h4 align="left" id="cdt-risco">⚠️ Quais regras ganharam teste primeiro</h4>

| Ordem | Regra | Se der errado |
| :---: | :--- | :--- |
| 1️⃣ | Compatibilidade ABO/Rh | Reação hemolítica: o paciente pode morrer. E o código só aceitava bolsa do mesmo tipo |
| 2️⃣ | Bolsa apta; vencida só se descarta | Bolsa vencida ou com a cadeia fria rompida chega ao paciente |
| 3️⃣ | Baixa e transições de status | O estoque mente: bolsa usada duas vezes, descartada que volta |
| 4️⃣ | FEFO + mesmo tipo primeiro | Desperdício: bolsa vence na prateleira, O− gasto à toa |
| 5️⃣ | Alertas do painel | É aviso, não bloqueio: as regras acima já impedem o uso indevido |

<h4 align="left">🚧 O que ficou sem teste</h4>

- **Frontend:** sem testes; a esteira só garante que compila. O `npm run lint` tem 18 problemas herdados e ainda não é gate.
- **Login (veio da `main`):** tem testes unitários com o Supabase Auth simulado, mas ficou fora do red → green, e a API ainda não bloqueia por perfil.
- **Plaquetas:** seguem a regra das hemácias por precaução; falta validar com um hemoterapeuta.
- **Alocação gravada no banco:** a FK `fk_alocacao_bolsa_compativel` ainda exige o mesmo tipo (fica para a Release 2).
- **Produção:** o smoke test confere versão, saúde e site → API, mas não roda fluxos de negócio no Supabase.

<h3 align="center" id="cdt-pipeline">⚙️ Pipeline e deploy — E3 · Deploy automatizado</h3>

[`esteira.yml`](./.github/workflows/esteira.yml): **push ou PR → testes e build → imagens Docker → (só na `main`) GHCR →
deploy → smoke test.**

| Job | Quando | O que faz |
| :--- | :--- | :--- |
| ☕ Backend · testes | todo push e PR | `mvn verify` com os 3 níveis; um teste vermelho para a esteira aqui |
| 🖥️ Frontend · build | todo push e PR | `npm ci` + `npm run build` (checa os tipos) |
| 🐳 Imagens Docker | depois dos dois | builda os 2 Dockerfiles do E1; na `main`, publica a imagem da API no GHCR com a tag = sha |
| 🚀 Deploy · Render + Vercel | push na `main` | deploy hook do Render com a imagem do sha e `vercel deploy --prod` do site; o smoke test espera o sha novo em `/actuator/info` e no `/versao.json`, o health `UP` e o site chegando na API |
| ↩️ Rollback | manual (*Run workflow*) | reimplanta a imagem e o site de um sha anterior, sem recompilar |

**Fluxo de branch — GitHub Flow:** a `main` está sempre publicável; cada tarefa ganha uma branch, vira PR com a esteira
verde e, no merge, vai para produção. Os PRs #17 a #28 já seguiam isso. Na proteção da `main` (*Settings → Rules*), exigir
PR, os checks **Backend · testes**, **Frontend · build** e **Imagens Docker** e a branch atualizada, e bloquear *force push*.

| Segredo / configuração | Onde fica |
| :--- | :--- |
| `DB_PASSWORD` (Postgres local) | `.env` de cada um, no `.gitignore` (modelo em [`.env.example`](./.env.example)) |
| `SUPABASE_DB_PASSWORD`, `SUPABASE_KEY` | Variáveis do `rotavital-api` no **Render** ([`render.yaml`](./render.yaml)); sem a chave, o login responde `503` |
| `RENDER_DEPLOY_HOOK_API`, `VERCEL_TOKEN`, `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID` | GitHub → Environments → `producao` → Secrets |
| `API_URL`, `WEB_URL` (não são segredo) | GitHub → Environments → `producao` → Variables |
| Login no GHCR | `GITHUB_TOKEN` automático do Actions |

**Rollback:** ⭐ *Actions → Esteira CI/CD → Run workflow* com o sha completo da versão boa (`git log --format=%H`). Se o
GitHub estiver fora, use os painéis (Render: *Events → Rollback*; Vercel: *Deployments → Instant Rollback*). Depois,
um `git revert` do commit ruim via PR, para a `main` não continuar com ele.

<h4 align="left" id="cdt-evidencias-e3">📸 Evidências do E3</h4>

| Evidência | Link / print |
| :--- | :--- |
| 🟢 Esteira verde nesta branch (testes + imagens) | ✅ [execução 38563433392](https://github.com/viictorpaes/Rotavital/actions/runs/38563433392), commit `f2d1e0f`, 14/10/2026 |
| 🔴 Execução **vermelha** que barrou o merge | ⏳ *a preencher* (passo 2 abaixo) |
| 🟢 Execução **verde** que publicou na `main` | ⏳ *a preencher* (passo 8) |
| 🌐 **URL pública** | ⏳ `https://rotavital-web.vercel.app` *(confirmar o domínio)* |
| ↩️ **Rollback** executado | ⏳ *a preencher* (passo 9) |

<h4 align="left" id="cdt-configuracao">🛠️ Configuração única (quem tem acesso às contas)</h4>

<details>
<summary>▶️ Passo a passo para ligar o deploy e colher as evidências</summary>

1. **Branch vermelha de demonstração:** `git push origin 67d4c55:refs/heads/demo/esteira-vermelha` e abra um PR dela para
   a `main`. O `67d4c55` é o teste de ABO/Rh antes do código.
2. Proteja a `main` (regras acima). No PR de demonstração, o **Backend · testes** fica vermelho e o merge é bloqueado →
   📸 **print 🔴**. Feche o PR sem mesclar e apague a branch.
3. Abra o PR de `feature/container-tdd-deploy` → esteira verde → merge com **"Create a merge commit"**. Desta vez o deploy
   falha por falta dos segredos, como esperado.
4. **GHCR:** em *Packages*, deixe o `rotavital-backend` como **Public**.
5. **Render:** *New → Blueprint →* este repositório. O `render.yaml` cria o `rotavital-api` e pede a `SUPABASE_DB_PASSWORD`
   e a `SUPABASE_KEY`; se o serviço já existia, cadastre a chave em *Environment*. Copie o *Deploy Hook* e apague o
   `rotavital-web` antigo, se ele existir.
6. **Vercel:** dentro de `frontend/`, rode `npx vercel@64.0.2 login` e `npx vercel@64.0.2 link` (projeto novo
   `rotavital-web`, diretório `./`). O `orgId` e o `projectId` ficam em `.vercel/project.json`. Crie também um token.
7. Cadastre os segredos e as variáveis da tabela acima no environment `producao`. Se a API ganhar outro domínio, ajuste o
   rewrite em [`frontend/vercel.json`](./frontend/vercel.json).
8. Em *Actions*, abra a execução do passo 3 e clique em *Re-run failed jobs* → 📸 **print 🟢** + **URL pública**.
9. Depois do próximo merge, rode *Run workflow* com o sha anterior → 📸 **print ↩️**. Salve os prints em
   `docs/evidencias/container-tdd-deploy/` e preencha a tabela acima.
</details>

<h3 align="center" id="cdt-decisoes">🧭 Decisões</h3>

| Decisão | Escolha | Alternativas | Por quê |
| :--- | :--- | :--- | :--- |
| 🐳 Imagem base | `eclipse-temurin:21-jre-alpine` (API) e `nginx-unprivileged:1.30-alpine` (site), sem root | `openjdk`, JDK completo, `nginx:alpine` | Só o runtime, em Alpine: menos pacotes e menos vulnerabilidades (378 MB e 91 MB). A `openjdk` foi descontinuada. |
| 🏗️ Multi-stage | 1º estágio: o Maven compila e **roda os testes**; 2º: só o `.jar` | 1 estágio; build fora do Docker | "Compila e passa nos testes" vale em qualquer máquina, e a produção não carrega Maven nem JDK. |
| 🐘 Banco no Compose | Postgres 17 próprio, com as migrations do Supabase | Compose apontando para o Supabase; H2 | Sobe sem internet e sem a senha de ninguém, e o schema continua com uma fonte só. |
| 💾 Dados | Volume nomeado `dados-postgres` | Bind mount; sem volume | Os dados sobrevivem ao `docker compose down` (testado) e não sujam o repositório. |
| 🔌 Rede | Rede padrão do Compose, pelo nome do serviço (`db`, `backend`) | IPs fixos; `network_mode: host` | O DNS do Compose resolve os nomes, e o banco nem publica porta para fora. |
| ⏱️ Ordem de subida | `healthcheck` + `depends_on: service_healthy` | `depends_on` simples; `sleep` | A API só sobe com o banco pronto, e o site só com a API `UP`. |
| 🔑 Configuração | `.env` + `.env.example`; banco e porta por variável de ambiente | Senha no `application.properties` | Nenhum segredo no Git, e a mesma imagem roda no Compose e no Render. |
| 🧪 Teste primeiro | ABO/Rh → bolsa apta → baixa → FEFO → alertas | Começar pelo mais fácil | Ordem de risco ao paciente ([tabela](#cdt-risco)): um erro na compatibilidade põe uma vida em risco. |
| 🗄️ Banco nos testes | Testcontainers: Postgres 17 + as mesmas migrations | H2 em memória | Mesmo SQL de produção (CHECKs, gatilhos, views), igual na máquina e na esteira. |
| 🥒 Aceitação | Cucumber + Gherkin em português | JUnit com nomes dado/quando/então | Os cenários BDD da Entrega 01 passam a rodar de verdade, e o médico consegue lê-los. |
| ⚙️ CI/CD | GitHub Actions + GHCR, com a tag = sha do commit | GitLab CI, Jenkins; Docker Hub | O código já está no GitHub, o `GITHUB_TOKEN` autentica e a tag por sha permite rollback exato. |
| 🌿 Fluxo de branch | GitHub Flow | Git Flow, GitLab Flow | Grupo pequeno, uma versão no ar e deploy a cada merge. Git Flow seria cerimônia sem release agendada. |
| ☁️ Deploy | API no Render (por imagem, Blueprint); site na Vercel, com rewrite `/api/` → Render; banco no Supabase | Tudo no Render; Railway, Fly.io, Koyeb | O Render roda a imagem de graça e na região do Supabase. O site não funcionou no Render, e a Vercel o serve pela CDN. O rewrite evita CORS. |
| 🔐 Segredos | Hook e token no environment `producao` do GitHub; senha e chave do Supabase só no Render | No repositório ou no workflow | Cada segredo fica só onde é usado. |
| ↩️ Rollback | *Run workflow* com o sha anterior | Só `git revert` + rebuild | Volta em minutos, sem recompilar, e fica registrado. |

<h3 align="center" id="cdt-ia">🤖 Uso de IA</h3>

<p align="center">Permitido pelo enunciado e declarado aqui: <b>a IA rascunha; o grupo valida e assina.</b></p>

| Item | Detalhe |
| :--- | :--- |
| 🛠️ Ferramenta | **Claude Code** (Claude Opus 5.5, da Anthropic), na extensão do VS Code |
| ✍️ O que rascunhou | Dockerfiles, `compose.yaml`, `db/`, a esteira e os scripts de deploy, `render.yaml` e `vercel.json`; o story map e as tarefas; os testes e o código das HU‑04, HU‑02 e HU‑08; esta seção; o merge da `main` |
| 🔎 Como foi verificado | Cada `test:` falhou antes do `feat:`; `mvn verify` completo; `docker compose` do zero; `actionlint`; deploy e rollback testados contra um servidor que simula o Render e a Vercel |
| ⚠️ O que errou e foi corrigido | Script do banco num bind mount, que o macOS bloqueia (→ `db/Dockerfile`); `${var,,}`, que o bash do macOS não tem; rollback aceitando sha curto |
| 👥 O que fica com o grupo | Configurar as contas, colher os prints, conferir as regras clínicas (principalmente a das plaquetas) e apresentar |

<details>
<summary>▶️ <b>Prompts principais</b></summary>

1. *"com base em: Desafio do PI - Container TDD e Deploy automatizados - 2026.2.pdf [...] Três entregas no projeto do PI,
   cada decisão justificada no README [...] Se usarem TDD, expliquem como foi a jornada de desenvolvimento e as lições
   aprendidas; use o final da sessão do README principal p fzr essa atividade"*
2. *"USE O README"*, confirmado depois como *"tudo no README, depois da última seção"*.
3. Quando o `pom.xml` voltou ao estado anterior no meio do trabalho, a IA perguntou antes de reaplicar as dependências de
   teste; a resposta foi *"Não, pode reaplicar"*.
4. *"me ajude a trazer os updates da main pra cá, e reconfira se está tudo de acordo com o pdf"*
5. *"sinterize o h2 do desafio [...] e enxuge a parte dele no README.md pra não ficar TÃÃÃÃO complexo"*
</details>

<h3 align="center" id="cdt-jornada">🧭 Jornada TDD e lições aprendidas</h3>

1. **O story map mostrou onde testar primeiro:** em cada atividade, a regra que mais machuca se der errado.
2. **O teste expôs um gap:** o README prometia "FEFO + ABO/Rh", mas o código só aceitava bolsa do mesmo tipo. Documento
   sozinho não garante nada.
3. **Triangulação:** o primeiro código verde tratava tudo como hemácias; só o teste do plasma obrigou a separar a regra
   por componente.
4. **Fronteiras e relógio:** "a bolsa que vence hoje ainda vale?" virou um teste com nome próprio, e o `LocalDate.now()`
   virou um `Clock` injetado ("Dado que hoje é 01/10/2026").
5. **Banco de verdade:** os testes com o Postgres das migrations pegaram constraints que um mock não pega (ex.: plaquetas
   valem no máximo 7 dias).
6. **Ver o teste falhar:** nos 9 pares, o `test:` falhou pelo motivo certo antes do `feat:`. Teste que nunca falhou pode
   não testar nada.
7. **A IA rascunha, o grupo confere:** os erros dela (o bind mount, o `${var,,}`) só apareceram rodando, no container e na esteira.