<h1 align="center"> Rota Vital — Planejamento da remodelagem 🏗️<br>
<img src="../../img/RotaVital_logo.png" alt="Logo RotaVital" width="420"> <br>
<img src="https://img.shields.io/badge/-DDD-020617?style=flat-square&logo=openjdk&logoColor=orange" height="19"/>
<img src="https://img.shields.io/badge/-Três_camadas-020617?style=flat-square&logo=springboot&logoColor=6DB33F" height="19"/>
<img src="https://img.shields.io/badge/-Diagramas_Mermaid-020617?style=flat-square&logo=mermaid&logoColor=FF3670" height="19"/>
<img src="https://img.shields.io/badge/-ADRs-020617?style=flat-square&logo=markdown&logoColor=white" height="19"/>

![Disciplina](https://img.shields.io/badge/disciplina-Infraestrutura_de_Software-orange)
![Trabalho](https://img.shields.io/badge/trabalho-U2_·_Parte_2-purple)
![Status](https://img.shields.io/badge/status-entrega_final_24%2F11-green)
</h1>

<p align="center">
DDD, arquitetura em três camadas, projeção com microsserviços e três cenários com orçamento.<br>
ADS · CESAR School · 2026.2 · Repositório: <a href="https://github.com/viictorpaes/Rotavital">github.com/viictorpaes/Rotavital</a>
</p>

> [!NOTE]
> **Onde fica cada coisa.** O enunciado chama esta pasta de `docs/arquitetura/`; no nosso repositório ela é
> **`docs/arquitetura_SO/`**. Os diagramas, a planilha de orçamento e os ADRs
> (`docs/arquitetura_SO/adr/0001-decomposicao.md` e seguintes) ficam todos aqui.

<h2 align="center" id="visao-geral">🧭 Visão Geral <br>
<img src="https://img.shields.io/badge/Sumário-111827?style=flat&logo=bookstack&logoColor=white" height="22"/>
</h2>

| # | Seção | O que você encontra |
| :---: | :--- | :--- |
| 1 | [👥 Squad](#squad) | Quem faz o trabalho |
| 2 | [🗂️ Seções do trabalho](#secoes) | As cinco seções, o peso de cada uma e onde estão |
| 3 | [📅 Prazos](#prazos) | Pré-entrega e entrega final |
| 4 | [🗃️ Arquivos desta pasta](#a-entrar) | Diagramas, planilha, ADRs, specs e o PDF |
| 5 | [🤖 Uso de IA](#uso-de-ia) | Ferramenta, finalidade e como foi validado |

<h2 align="center" id="squad">👥 Squad do Projeto Integrador <br>
<img src="https://img.shields.io/badge/Squad-111827?style=flat&logo=googleclassroom&logoColor=white" height="22"/>
</h2>

<ul>
  <li><b>Victor José Paes e Silva (Líder 👑)</b></li>
  <li>Eduardo de Souza Cavalcanti Junior</li>
  <li>Felipe Franca Alves de Lima</li>
  <li>Helamã Leone de Lima Procídio</li>
  <li>João Pedro Arruda Guimarães</li>
  <li>Júlia Oliveira Veríssimo</li>
  <li>Lucas Paguetti Pereira</li>
  <li>Tiago Luiz Moreira de Vasconcelos</li>
</ul>

<p align="center">Links do LinkedIn e do GitHub de cada um em <a href="../../README.md#integrantes">Integrantes do Projeto</a>, no README principal.</p>

<h2 align="center" id="secoes">🗂️ Seções do trabalho <br>
<img src="https://img.shields.io/badge/Peso_total-2,5-111827?style=flat&logo=todoist&logoColor=E44332" height="22"/>
</h2>

| Seção | Peso | Documento | Status |
| :--- | :---: | :--- | :--- |
| 🧩 **A.** Remodelagem do domínio com DDD | 0,6 | [`A-remodelagem-ddd.md`](A-remodelagem-ddd.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |
| 🧠 **B.** Modelo tático do Estoque (agregado `Bolsa`) | 0,4 | [`B-modelo-tatico.md`](B-modelo-tatico.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |
| 🧱 **C.** Três camadas com Clean Architecture | 0,5 | [`C-tres-camadas.md`](C-tres-camadas.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |
| 🧭 **D.** Projeção com microsserviços | 0,3 | [`D-microsservicos.md`](D-microsservicos.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |
| 💰 **E.** Três cenários com orçamento | 0,7 | [`E-cenarios-e-orcamento.md`](E-cenarios-e-orcamento.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |
| 🛠️ **F.** Plano de refatoração, TDD e SDD (devolutiva de 17/11) | — | [`F-plano-de-refatoracao.md`](F-plano-de-refatoracao.md) | <img src="https://img.shields.io/badge/Final-16A34A?style=flat-square" height="18"/> |

<h2 align="center" id="prazos">📅 Prazos <br>
<img src="https://img.shields.io/badge/17%2F11_→_24%2F11_→_01%2F12-111827?style=flat&logo=googleclassroom&logoColor=white" height="22"/>
</h2>

| Marco | Data | O que entra |
| :--- | :--- | :--- |
| 📌 Pré-entrega | 17/11/2026, até 19h15 | Seções A e C parciais, sem nota (feita) |
| 🏁 Entrega final da Parte 2 | 24/11/2026, até 19h15 | Documento em PDF (até 12 páginas), slides e apresentação |
| 🧱 Parte 3 | 01/12/2026 | Refatoração do Estoque descrita na [Seção F](F-plano-de-refatoracao.md#f2-o-que-muda) |

<h2 align="center" id="a-entrar">🗃️ Arquivos desta pasta <br>
<img src="https://img.shields.io/badge/-Markdown_·_Mermaid_·_XLSX-111827?style=flat&logo=markdown&logoColor=white" height="22"/>
</h2>

| Arquivo | Seção |
| :--- | :---: |
| 📝 [`adr/0001-decomposicao.md`](adr/0001-decomposicao.md), [`adr/0002-dominio-puro-clean-architecture.md`](adr/0002-dominio-puro-clean-architecture.md), [`adr/0003-especificacao-e-teste-antes-do-codigo.md`](adr/0003-especificacao-e-teste-antes-do-codigo.md) | D |
| 📊 [`orcamento/orcamento-cenarios.xlsx`](orcamento/orcamento-cenarios.xlsx) (com fórmulas) e [`orcamento/orcamento.csv`](orcamento/orcamento.csv) | E |
| 🗺️ Diagramas Mermaid (mapa de contextos, agregado, camadas, saga, três cenários) dentro de cada `.md` | A–E |
| 🥒 [`HU-11-reserva-de-bolsa-apta.feature`](HU-11-reserva-de-bolsa-apta.feature): o cenário da Seção B, que a Parte 3 executa | B |
| 📐 [`../specs/HU-11-reserva-de-bolsa-apta/`](../specs/HU-11-reserva-de-bolsa-apta/spec.md): exemplo de SDD (spec, plano, tarefas) | F |
| 📄 [`Rota-Vital-U2-Parte2-Planejamento.pdf`](Rota-Vital-U2-Parte2-Planejamento.pdf): o documento entregue no Classroom | Todas |

<h2 align="center" id="uso-de-ia">🤖 Uso de IA <br>
<img src="https://img.shields.io/badge/PO--CESAR--021-111827?style=flat&logo=anthropic&logoColor=white" height="22"/>
</h2>

<p align="center">Uso conforme a <b>PO-CESAR-021</b> (categoria Condicionado). Apenas dados sintéticos do Rota Vital.</p>

| Item | Detalhe |
| :--- | :--- |
| 🛠️ Ferramenta | **Claude** (Anthropic), pelo Claude Code e pelo app Claude |
| ✍️ Finalidade | Rascunho das Seções A a F a partir do código do repositório: levantamento das classes por contexto, das classes anêmicas e das regras que estão no serviço; medição da cobertura com JaCoCo; contas da Lei de Little; montagem da planilha, dos diagramas Mermaid, dos ADRs e do PDF |
| 🔎 Como foi validado | Cada classe, contagem e relação citada foi conferida no código (`backend/src/main/java/com/rotavital`). A cobertura saiu do relatório do JaCoCo 0.8.12 rodando os 430 testes unitários. As contas foram recalculadas pelas fórmulas da planilha. **Os preços não são sugestão da IA:** cada um foi lido na página oficial do provedor em 22/11/2026, e o link está na planilha para o squad conferir |
| 👥 O que fica com o squad | Decisões de arquitetura (ADRs), premissas de carga e a explicação de cada parte na apresentação |
| 🔒 Dados | Apenas os dados sintéticos do Rota Vital |
