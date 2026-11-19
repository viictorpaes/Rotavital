<h1 align="center"> Rota Vital — Planejamento da remodelagem 🏗️<br>
<img src="../../img/RotaVital_logo.png" alt="Logo RotaVital" width="420"> <br>
<img src="https://img.shields.io/badge/-DDD-020617?style=flat-square&logo=openjdk&logoColor=orange" height="19"/>
<img src="https://img.shields.io/badge/-Três_camadas-020617?style=flat-square&logo=springboot&logoColor=6DB33F" height="19"/>
<img src="https://img.shields.io/badge/-Diagramas_Mermaid-020617?style=flat-square&logo=mermaid&logoColor=FF3670" height="19"/>
<img src="https://img.shields.io/badge/-ADRs-020617?style=flat-square&logo=markdown&logoColor=white" height="19"/>

![Disciplina](https://img.shields.io/badge/disciplina-Infraestrutura_de_Software-orange)
![Trabalho](https://img.shields.io/badge/trabalho-U2_·_Parte_2-purple)
![Status](https://img.shields.io/badge/status-pré--entrega_parcial-yellow)
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
| 4 | [🗃️ O que ainda vai entrar](#a-entrar) | Arquivos previstos para esta pasta |
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
| 🧩 **A.** Remodelagem do domínio com DDD | 0,6 | [`A-remodelagem-ddd.md`](A-remodelagem-ddd.md) | <img src="https://img.shields.io/badge/Parcial-EAB308?style=flat-square" height="18"/> pré-entrega de 17/11 |
| 🧠 **B.** Modelo tático de um contexto core | 0,4 | — | <img src="https://img.shields.io/badge/Proposta-6B7280?style=flat-square" height="18"/> agregado `Bolsa` do Estoque ([A.6](A-remodelagem-ddd.md#a6-devolutiva)) |
| 🧱 **C.** Arquitetura em três camadas | 0,5 | [`C-tres-camadas.md`](C-tres-camadas.md) | <img src="https://img.shields.io/badge/Parcial-EAB308?style=flat-square" height="18"/> pré-entrega de 17/11 |
| 🧭 **D.** Projeção com microsserviços | 0,3 | — (ADRs em `adr/`) | <img src="https://img.shields.io/badge/24%2F11-6B7280?style=flat-square" height="18"/> entrega final |
| 💰 **E.** Três cenários com orçamento | 0,7 | — (planilha e diagramas aqui) | <img src="https://img.shields.io/badge/24%2F11-6B7280?style=flat-square" height="18"/> entrega final |

<h2 align="center" id="prazos">📅 Prazos <br>
<img src="https://img.shields.io/badge/17%2F11_→_24%2F11-111827?style=flat&logo=googleclassroom&logoColor=white" height="22"/>
</h2>

| Marco | Data | O que entra |
| :--- | :--- | :--- |
| 📌 Pré-entrega | 17/11/2026, até 19h15 | Seções A e C parciais, sem nota |
| 🏁 Entrega final | 24/11/2026, até 19h15 | Documento em PDF (até 12 páginas), slides e apresentação |

<h2 align="center" id="a-entrar">🗃️ O que ainda vai entrar nesta pasta <br>
<img src="https://img.shields.io/badge/Em_andamento-111827?style=flat&logo=markdown&logoColor=white" height="22"/>
</h2>

| Arquivo | Seção |
| :--- | :---: |
| 📝 `adr/0001-decomposicao.md` e `adr/0002-…md` | D |
| 📊 Diagramas por cenário e a planilha de orçamento | E |
| 🥒 O cenário Gherkin da Seção B, que a Parte 3 executa junto com os que já estão em [`backend/src/test/resources/features`](../../backend/src/test/resources/features) | B |

<h2 align="center" id="uso-de-ia">🤖 Uso de IA <br>
<img src="https://img.shields.io/badge/PO--CESAR--021-111827?style=flat&logo=anthropic&logoColor=white" height="22"/>
</h2>

<p align="center">Uso conforme a <b>PO-CESAR-021</b> (categoria Condicionado). Apenas dados sintéticos do Rota Vital.</p>

| Item | Detalhe |
| :--- | :--- |
| 🛠️ Ferramenta | **Claude Code** (Anthropic) |
| ✍️ Finalidade | Rascunho das Seções A e C a partir do código do repositório: levantamento das classes por contexto, das dependências entre pacotes e das integrações já isoladas por interface; revisão da coerência entre glossário, mapa de contextos e eventos |
| 🔎 Como foi validado | Cada classe, contagem e relação citada foi conferida no código (`backend/src/main/java/com/rotavital`) e nos documentos [`ARQUITETURA.md`](../ARQUITETURA.md) e [`MODELO_DE_DOMINIO.md`](../MODELO_DE_DOMINIO.md) |
| 👥 O que fica com o squad | A classificação dos subdomínios e os pontos da seção [A.6](A-remodelagem-ddd.md#a6-devolutiva) são decisões do squad e serão revistos após a devolutiva |
