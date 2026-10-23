<h1 align="center">
  Rota Vital — Documentação Técnica <br> Entregas 01 e 02 do Projeto Integrador <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸📌
</h1>

<p align="center">
<a href="../README.md">⬅️ Voltar ao README principal</a> ·
<a href="DOCUMENTACAO.md">📚 Índice da documentação</a> ·
<a href="FIGMA_VS_REAL.md">🖼️ Figma × Sistema Real</a>
</p>

> Histórias de usuário (com os cenários BDD), protótipo, screencasts e Issue Tracker das duas primeiras entregas do PI. Este conteúdo ficava no README principal; lá ficou só o resumo com os links.

<p align="center">
Cada entrega do Projeto Integrador tem sua própria seção abaixo, com link direto para os artefatos
produzidos (histórias de usuário, protótipo, screencasts). Datas conforme o roadmap do 3º semestre.
</p>

<h2 id="-entrega-01" align="center">📌 Entrega 01 — 31/08 <br>
<a href="https://letter-raven-07178463.figma.site" target="_blank"><img src="https://img.shields.io/badge/-Figma-111827?style=flat-square&logo=figma&logoColor=purple" height="20"/></a>
<a href="https://youtu.be/SRvAFkqi4Nc" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_1-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Status-Concluída-22C55E?style=flat" height="20"/>
</h2>

| Artefato | Descrição | Link |
| :--- | :--- | :--- |
| 🎨 Protótipo low-Fi (Figma Make) | 10 histórias navegadas e confirmadas ponta a ponta (HU‑01 a HU‑10) | [Abrir protótipo](https://letter-raven-07178463.figma.site) |
| 🎬 Screencast (YouTube) | Demonstração do protótipo, explicando cada história implementada | [Assistir vídeo](https://youtu.be/SRvAFkqi4Nc) |

<h3 align="center">📋 Histórias de Usuário — Rota Vital 🩸<br>
Login, Estoque, Requisições, Rede Hospitalar, Pacientes e <b>Doações</b></h3>

> **Formato:** Como `[papel]`, quero `[funcionalidade]`, para que `[benefício]`.

<p align="center">
<img src="https://img.shields.io/badge/HUs%20Implementadas-10-brightgreen?style=flat-square&logo=checkmarx&logoColor=white"/>
<img src="https://img.shields.io/badge/HUs%20Futuras-6-blue?style=flat-square&logo=rocket&logoColor=white"/>
</p>

<h4 align="left">🖥️ Telas do Protótipo ↔ Histórias</h4>

| # | Tela do Figma | Papel | HUs |
| :---: | :--- | :--- | :--- |
| 1 | 🔐 Login / Seleção de acesso | Médico e Doador | HU‑01 |
| 2 | 📊 Painel Operacional (Início) | Médico | HU‑02 |
| 3 | 📦 Estoque por tipo sanguíneo | Médico | HU‑03 |
| 4 | 🏥 Requisição — Solicitar / Recebidas / Campanha | Médico | HU‑04, HU‑05, HU‑06 |
| 5 | 🚚 Rede hospitalar (mapa + plano de transporte) | Médico | HU‑07 |
| 6 | 🩺 Pacientes | Médico | HU‑08 |
| 7 | ❤️ Doações (pessoas que precisam + agendar) | Médico e Doador | HU‑09 |
| 8 | 👤 Portal do Doador | Doador | HU‑10 |

> Parte da lógica por trás de algumas telas (FEFO, compatibilidade ABO/Rh, grafo de rotas) já existe no
> backend Java — ver [`docs/MODULOS.md`](MODULOS.md). O que ainda é só protótipo está anotado como
> "Nível de código" em cada história abaixo; isso não é o foco desta entrega, só uma referência rápida.

<h4 align="left">✅ Implementadas</h4>

**🔐 Login e Painel**

| # | História |
| :---: | :--- |
| HU‑01 | Como **usuário do RotaVital (médico ou doador)**, quero escolher meu tipo de acesso e informar meu nome na tela inicial, para que eu entre direto na experiência certa para o meu papel, sem um cadastro completo. |
| HU‑02 | Como **médico**, quero ver um painel com números rápidos e uma caixa de avisos com alertas críticos em tempo real, para que eu identifique pacientes em risco e problemas de estoque sem navegar por todas as telas. |

**📦 Estoque**

| # | História |
| :---: | :--- |
| HU‑03 | Como **médico**, quero consultar o estoque agrupado por tipo sanguíneo, com o detalhe de cada lote (validade, temperatura e localização física), para que eu saiba exatamente quais lotes estão em risco de vencer ou fora da faixa de temperatura ideal. |

**🏥 Requisição**

| # | História |
| :---: | :--- |
| HU‑04 | Como **médico**, quero preencher uma requisição informando hemocomponente, tipo sanguíneo, ala de destino, quantidade, paciente e urgência, para que o sistema me sugira automaticamente o lote certo pela regra FEFO. |
| HU‑05 | Como **médico**, quero registrar o recebimento físico de hemocomponentes vindos de outras instituições, para que essas unidades sejam somadas automaticamente ao estoque, sem lançamento manual. |
| HU‑06 | Como **médico**, quero publicar uma campanha de doação para um paciente específico, para que doadores voluntários o vejam nas telas de Doações e Pacientes e se cadastrem para ajudar diretamente. |

**🚚 Rede Hospitalar**

| # | História |
| :---: | :--- |
| HU‑07 | Como **médico**, quero visualizar o mapa de hospitais conectados, escolher um destino e ver o plano de transporte com a rota calculada e as exigências da cadeia fria, para que a entrega siga pelo caminho mais eficiente, no veículo certo e na temperatura ideal. |

**🩺 Pacientes**

| # | História |
| :---: | :--- |
| HU‑08 | Como **médico**, quero, ao concluir o atendimento de um paciente, informar se o hemocomponente usado veio do estoque interno (com baixa automática por FEFO) ou de uma doação externa (sem baixa), para que o estoque reflita com exatidão o que foi realmente consumido. |

**❤️ Doações**

| # | História |
| :---: | :--- |
| HU‑09 | Como **médico ou doador**, quero visualizar as pessoas que precisam de doação — filtráveis por tipo sanguíneo — e agendar uma doação para uma delas, para que doadores compatíveis sejam direcionados a quem precisa com mais urgência. |
| HU‑10 | Como **doador**, quero acessar, após o login, uma versão simplificada da tela de Doações, sem o menu operacional do hospital, para que minha experiência seja direta: ver quem precisa e agendar minha doação. |

<h4 align="left">🚀 Podem Ser Implementadas no Futuro</h4>

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

<h4 align="left">📋 Resumo</h4>

| Status | Quantidade |
| :--- | :---: |
| ✅ Implementadas | 10 histórias |
| 🚀 Futuras | 6 histórias |
| **Total** | **16 histórias** |

<h4 align="left">🔍 Detalhamento:</h4>

<p align="center">
Cada história implementada abaixo traz o detalhamento completo — ator, ação, benefício, a tela do protótipo
onde ela vive, uma nota (curta) do que já existe em código, e <b>2 cenários de validação</b> em BDD com os
dados reais vistos no protótipo. Clique em <b>▶️</b> + no emoji da tela para expandir.
</p>

<details>
<summary>▶️🔐 <b>HU‑01</b> — Login e seleção de tipo de acesso</summary>

| Campo | Detalhe |
| :--- | :--- |
| **Ator** | Usuário do sistema (médico ou doador) |
| **Quero** | Escolher meu tipo de acesso ("Médico — acesso hospitalar completo" ou "Doador — portal de doações") e informar meu nome |
| **Para que** | Eu veja a tela adequada ao meu papel sem precisar de um cadastro completo |
| **Tela do Figma** | Tela inicial "Cesar Life · Estoque Inteligente" — cartões `Médico`/`Doador`, campo `Nome`, botão `Entrar` |
| **Nível de código** | Desde a PI3-183/184, a entrada é por **e-mail e senha**: `POST /api/v1/acessos` confere as credenciais no Supabase Auth e devolve o perfil (médico, doador ou admin) e a tela inicial. Os cards do protótipo saíram da tela; ver [`PERFIS_DE_ACESSO.md`](PERFIS_DE_ACESSO.md) |

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

<h2 id="-entrega-02" align="center">📌 Entrega 02 — 21/09 <br>
<img src="https://img.shields.io/badge/-Spring_Boot-111827?style=flat-square&logo=springboot&logoColor=green" height="20"/>
<img src="https://img.shields.io/badge/-React-111827?style=flat-square&logo=react&logoColor=61DAFB" height="20"/>
<a href="https://youtu.be/3RlgYAGPyaE" target="_blank"><img src="https://img.shields.io/badge/-ScreenCast_2-111827?style=flat-square&logo=youtube&logoColor=red" height="20"/></a>
<img src="https://img.shields.io/badge/Status-Concluída-22C55E?style=flat" height="20"/>
</h2>

<p align="center">
Segunda entrega do Projeto Integrador — sai do protótipo Figma e liga o domínio a código rodando de
verdade: back-end em <b>Spring Boot</b> (HU‑01, HU‑03 — vindas do <a href="https://github.com/viictorpaes/Rotavital/pull/2">PR #2</a>,
já mesclado na <code>main</code>) e front-end em <b>React</b> (HU‑01, HU‑09, HU‑10), somando 5 histórias
com código real, acima do mínimo de 2 exigido. Screencast de uso do sistema e explicação do código
publicado no <a href="https://youtu.be/3RlgYAGPyaE" target="_blank">YouTube</a>, fechando a entrega.
</p>

| Requisito da entrega | Descrição | Status |
| :--- | :--- | :---: |
| ✅ Histórias implementadas | Mínimo de 2 HUs em código real (não só Figma) | <img src="https://img.shields.io/badge/5%2F2-22C55E?style=flat-square" height="18"/> **Feito** |
| 🔁 Versionamento atuante | Commits de código direto na `main`, no mínimo semanais | <img src="https://img.shields.io/badge/Commits_semanais_na_main-22C55E?style=flat-square" height="18"/> **Feito** — PRs #2 e #10 mesclados na `main` em 14/09, com commits também em 23/08, 09/09 e 12/09, mantendo cadência semanal |
| 🐞 Issue/Bug Tracker | GitHub Issues atualizado todas as semanas + print no README | <img src="https://img.shields.io/badge/5_abertas_%2F_2_fechadas-22C55E?style=flat-square" height="18"/> **Feito** — 7 issues registradas no GitHub (label `bug`), 2 já fechadas (#2 e #5), com prints na seção [Issue/Bug Tracker](#issue-tracker) abaixo |

| Artefato | Descrição | Link |
| :--- | :--- | :--- |
| 🎬 Screencast — Uso do sistema | Aplicação Spring Boot rodando, explicando as HUs implementadas | [Assistir vídeo](https://youtu.be/3RlgYAGPyaE) |
| 💻 Screencast — Explicação do código | Código da aplicação Spring Boot e das HUs implementadas | [Assistir vídeo](https://youtu.be/3RlgYAGPyaE) |

<h3 align="center">📝 Histórias Implementadas — Entrega 02</h3>

<p align="center">
Resumo em formato <b>post-it</b> (como visto em sala) das histórias já rodando em código — Spring Boot
no backend, React no frontend. O detalhamento completo (ator, tela, BDD) de cada HU está na
<a href="#-entrega-01">Entrega 01</a> acima.
</p>

> 🟨 **HU‑01** — Como **usuário (médico ou doador)**, preciso escolher meu tipo de acesso e informar meu
> nome, para entrar direto na experiência certa para o meu papel, sem um cadastro completo.
>
> ✅ Frontend — [`PaginaLogin.tsx`](../frontend/src/pages/Login/PaginaLogin.tsx)
> ✅ Backend — `POST /api/v1/acessos` via [`AcessoController.java`](../backend/src/main/java/com/rotavital/api/AcessoController.java) (PR #2, mesclado)

> 🟨 **HU‑03** — Como **médico**, preciso consultar o estoque por tipo sanguíneo, com validade, temperatura
> e localização de cada lote, para saber quais estão em risco de vencer ou fora da faixa ideal.
>
> ✅ Backend — `GET /api/v1/bancos/{bancoId}/estoque?tipoSanguineo=` via [`EstoqueController.java`](../backend/src/main/java/com/rotavital/api/EstoqueController.java) (PR #2, mesclado)
> ✅ Frontend — [`PaginaEstoque.tsx`](../frontend/src/pages/Estoque/PaginaEstoque.tsx), entregue depois da Entrega 02; hoje lista as bolsas de `GET /api/v1/hemocomponentes`, com filtros de tipo sanguíneo, componente e status e paginação (PI3-180), e o botão **Nova bolsa** cadastra pelo `POST /api/v1/hemocomponentes`, mostrando no formulário os erros 400/422 da API (PI3-179; ver [Figma × Sistema Real](FIGMA_VS_REAL.md))

> 🟨 **HU‑09** — Como **médico ou doador**, preciso visualizar as pessoas que precisam de doação — filtráveis
> por tipo sanguíneo — e agendar uma doação, para direcionar doadores compatíveis a quem precisa com urgência.
>
> ✅ Frontend — [`PaginaDoacoes.tsx`](../frontend/src/pages/Doacoes/PaginaDoacoes.tsx) + [`ModalAgendarDoacao.tsx`](../frontend/src/components/donations/ModalAgendarDoacao.tsx)

> 🟨 **HU‑10** — Como **doador**, preciso acessar uma versão simplificada da tela de Doações, sem o menu
> operacional do hospital, para uma experiência direta: ver quem precisa e agendar minha doação.
>
> ✅ Frontend — [`PaginaPortalDoador.tsx`](../frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx)

<h3 align="center" id="issue-tracker">🐛 Issue/Bug Tracker — Entrega 02 <br>
<img src="https://img.shields.io/badge/5_Issues_Abertas-red?style=flat-square&logo=github&logoColor=white" height="20"/>
<img src="https://img.shields.io/badge/2_Issues_Fechadas-22C55E?style=flat-square&logo=github&logoColor=white" height="20"/>
</h3>

<p align="center">
7 issues registradas no <a href="https://github.com/viictorpaes/Rotavital/issues" target="_blank">GitHub Issues</a> do repositório,
todas com a label <code>bug</code>, registrando gaps reais encontrados entre o protótipo Figma, a documentação
técnica (<code>docs/</code>) e o código que de fato roda hoje (backend Spring Boot + frontend React). As Issues #2 e #5
já foram fechadas; as demais viram trabalho de entrada da Entrega 03.
</p>

<p align="center">
<img src="../img/issues_abertas.png" width="800"/>
<img src="../img/issues_fechadas.png" width="800"/>
</p>

| Imagem | Título | Descrição |
| :---: | :--- | :--- |
| <img src="../img/issue-tracker_issue1-testes-junit_1.png" width="260"/> | 🔴 Issue #1 — Testes JUnit 5 + CI (contexto) | Cobertura de testes em 0%: o projeto já tem `junit-jupiter` no `pom.xml`, mas `TesteFluxo.java` ainda usa `main()` em vez de `@Test`. Tarefas para convertê-lo, adicionar `spring-boot-starter-test` e cobrir `AcessoController`/`EstoqueController`. |
| <img src="../img/issue-tracker_issue1-testes-junit_2.png" width="260"/> | 🔴 Issue #1 — Testes JUnit 5 + CI (cenários e pipeline) | Matriz mínima de cenários de teste por endpoint (`POST /api/v1/acessos`, `GET /api/v1/bancos/{id}/estoque`) e pipeline de CI sugerido, bloqueando o merge quando `mvn test` falha. |
| <img src="../img/issue2_done.png" width="260"/> | ✅ Issue #2 — Screencasts da Entrega 02 (fechada) | Checklist para gravar e publicar os 2 vídeos exigidos nesta entrega (uso do sistema rodando e explicação do código) concluído; vídeos publicados no YouTube e issue fechada no GitHub. |
| <img src="../img/issue-tracker_issue3-estoque.png" width="260"/> | 🔴 Issue #3 — Implementar tela de Estoque (HU‑03) | O backend já expõe `GET /api/v1/bancos/{bancoId}/estoque?tipoSanguineo=`; falta a `PaginaEstoque.tsx` sair do placeholder `EmBreve` e consumir esse endpoint de verdade. |
| <img src="../img/issue-tracker_issue4-integracao-backend.png" width="260"/> | 🔴 Issue #4 — Integrar frontend ao backend real | Hoje o frontend roda 100% sobre dados mockados (`pessoasMock.ts`, login só em memória). Plano para criar uma camada `api.ts`, trocar login/estoque por chamadas HTTP reais e configurar CORS no Spring Boot. |
| <img src="../img/issue-tracker_issue5-docs-roteirizacao_1.png" width="260"/> | ✅ Issue #5 — Docs desatualizados sobre Roteirização (fechada) | `docs/MODULOS.md` classifica o módulo de Roteirização como "só lat/long, sem grafo", mas o código já implementa o grafo/Dijkstra (`RedeDistribuicao`, `Conexao`, `RotaCalculada`) usado no protótipo. |
| <img src="../img/issue-tracker_issue5-docs-roteirizacao_2.png" width="260"/> | ✅ Issue #5 — Docs desatualizados sobre Roteirização (resolução) | Resolvida com o novo `RotaController`, que expõe `GET /api/v1/pontos`, `GET /api/v1/conexoes` e `GET /api/v1/rotas?origemId=&destinoId=` (o `POST /rotas/calcular` proposto virou consulta `GET` idempotente), e com o `docs/MODULOS.md` marcando o módulo como implementado (grafo + Dijkstra). |
| <img src="../img/issue-tracker_issue6-painel-operacional.png" width="260"/> | 🟠 Issue #6 — Implementar Painel Operacional (HU‑02) | Escopo e cenários BDD para tirar a `PaginaPainel.tsx` do placeholder `EmBreve` e implementar os cards de "Números Rápidos" e a caixa de avisos com alertas críticos. |
| <img src="../img/issue-tracker_issue7-doador-campanha.png" width="260"/> | 🟠 Issue #7 — Modelar Doador e Campanha de Doação | Diagrama de classes proposto (`Doador`, `CampanhaDoacao`, `AgendamentoDoacao`) — hoje não existe conceito de doador nem de campanha pública no domínio, o que bloqueia versões não mockadas de HU‑06, HU‑09 e HU‑10. |
