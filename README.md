<h1 align="center"> Rotavital 💉🩸🏥🚑<br>
<img src="./img/RotaVital_logo.png" alt="Logo RotaVital" width="620"> <br>
<img src="https://img.shields.io/badge/-Java_21-020617?style=flat-square&logo=openjdk&logoColor=orange" height="19"/>
<img src="https://img.shields.io/badge/-Maven-020617?style=flat-square&logo=apachemaven&logoColor=C71A36" height="19"/>
<img src="https://img.shields.io/badge/-OpenAPI_3.0.3-020617?style=flat-square&logo=openapiinitiative&logoColor=6BA539" height="19"/>

[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg)](https://github.com/viictorpaes/Rotavital/blob/main/LICENSE)
![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-purple)
</h1>

<p align="center">
Sistema de logística de distribuição de hemocomponentes entre hospitais e bancos de sangue — estoque com
regras de validade, requisições hospitalares com alocação por <b>FEFO + compatibilidade ABO/Rh</b>,
roteirização de entregas e telemetria da cadeia fria.
</p>

<p align="center">
<a href="#integrantes">Integrantes</a> ·
<a href="#entregas">Entregas</a> ·
<a href="#arquitetura">Arquitetura</a> ·
<a href="#cdt-como-rodar">Como rodar</a> ·
<a href="#cdt-testes">Testes</a> ·
<a href="#cdt-pipeline">Pipeline e deploy</a> ·
<a href="#cdt-decisoes">Decisões</a> ·
<a href="#cdt-ia">Uso de IA</a> ·
<a href="#documentacao">Documentação</a>
</p>

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
  <img src="https://img.shields.io/badge/-Java_21-111827?style=flat-square&logo=openjdk&logoColor=orange"/>
  <img src="https://img.shields.io/badge/-Spring Boot-111827?style=flat-square&logo=springboot&logoColor=green"/>
  <img src="https://img.shields.io/badge/-Maven-111827?style=flat-square&logo=apachemaven&logoColor=C71A36"/>
  <img src="https://img.shields.io/badge/-JUnit_5-111827?style=flat-square&logo=junit5&logoColor=25A162"/>
  <img src="https://img.shields.io/badge/-Testcontainers-111827?style=flat-square&logo=testcontainers&logoColor=9B489A"/>
  <img src="https://img.shields.io/badge/-Cucumber_(Gherkin)-111827?style=flat-square&logo=cucumber&logoColor=23D96C"/>
  <img src="https://img.shields.io/badge/-C99-111827?style=flat-square&logo=c&logoColor=A8B9CC"/> <br>
  <img src="https://img.shields.io/badge/-React_18-111827?style=flat-square&logo=react&logoColor=61DAFB"/>
  <img src="https://img.shields.io/badge/-TypeScript-111827?style=flat-square&logo=typescript&logoColor=3178C6"/>
  <img src="https://img.shields.io/badge/-Vite-111827?style=flat-square&logo=vite&logoColor=646CFF"/>
  <img src="https://img.shields.io/badge/-Tailwind_CSS-111827?style=flat-square&logo=tailwindcss&logoColor=38BDF8"/>
  <img src="https://img.shields.io/badge/-Supabase-111827?style=flat-square&logo=supabase&logoColor=3ECF8E"/>
  <img src="https://img.shields.io/badge/-OpenAPI_3.0.3-111827?style=flat-square&logo=openapiinitiative&logoColor=6BA539"/> <br>
  <img src="https://img.shields.io/badge/-Docker-111827?style=flat-square&logo=docker&logoColor=2496ED"/>
  <img src="https://img.shields.io/badge/-Docker%20Compose-111827?style=flat-square&logo=docker&logoColor=2496ED"/>
  <img src="https://img.shields.io/badge/-GitHub_Actions-111827?style=flat-square&logo=githubactions&logoColor=2088FF"/>
  <img src="https://img.shields.io/badge/-Vercel-111827?style=flat-square&logo=vercel&logoColor=white"/>
  <img src="https://img.shields.io/badge/-Figma-111827?style=flat-square&logo=figma&logoColor=F24E1E"/> <br>
  <img src="https://img.shields.io/badge/-Git-111827?style=flat-square&logo=git&logoColor=F05032"/>
  <img src="https://img.shields.io/badge/-GitHub-111827?style=flat-square&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/-GitHub_Desktop-111827?style=flat-square&logo=github&logoColor=8250DF"/>
</p>

<h2 align="center" id="entregas">🚀 Entregas do Projeto Integrador <br>
<img src="https://img.shields.io/badge/Kickoff_→_Desafio_do_PI-111827?style=flat&logo=todoist&logoColor=E44332" height="22"/>
</h2>

| Entrega | O que foi entregue | Links |
| :--- | :--- | :--- |
| 📌 **Entrega 01** · 31/08 | Protótipo no Figma com 10 histórias de usuário, cada uma com 2 cenários BDD | [Figma](https://letter-raven-07178463.figma.site) · [Screencast 1](https://youtu.be/SRvAFkqi4Nc) · [Histórias](./docs/ENTREGAS_PI.md#-entrega-01) |
| 📌 **Entrega 02** · 21/09 | Backend Spring Boot e frontend React, com 5 histórias em código e Issue Tracker | [Screencast 2](https://youtu.be/3RlgYAGPyaE) · [Detalhes e issues](./docs/ENTREGAS_PI.md#-entrega-02) · [Figma × Sistema real](./docs/FIGMA_VS_REAL.md) |
| 🧬 **AV1** · Estruturas de dados | Lista, fila e pilha em C99, com a tradução comentada para Java | [`c/README.md`](./c/README.md) · [Tradução C ↔ Java](./docs/TRADUCAO_C_JAVA_AV1.md) |
| 🔌 **RSD** · Arquitetura e redes | Diagrama de arquitetura, ligações, sub-redes e PDF entregue | [PDF](./pdf/arquitetura-rotavital-paes-silva.pdf) · [Ligações e redes](./docs/LIGACOES_E_REDES.md) |
| ⚙️ **SO** · Paralelismo | Auditoria de telemetria sequencial × paralela | [Relatório](./docs/RELATORIO_ATIVIDADE_PARALELISMO.md) |
| 🐳 **Desafio do PI** · Unidade 2 | Container, TDD e deploy automatizados | [Seção abaixo](#container-tdd-deploy) · [Slides](./pdf/Rota%20Vital%20—%20Container,%20TDD%20e%20Deploy.pdf) |

<h2 align="center" id="arquitetura">🏰 Arquitetura <br>
<img src="https://img.shields.io/badge/Architecture-111827?style=flat-square&logo=instructure&logoColor=white" height="22"/>
</h2>

Três camadas: o site em React só conversa com a API `/api/v1`; a API em Spring Boot é a única que acessa o banco e
o Supabase Auth. As regras de negócio (FEFO, compatibilidade ABO/Rh, transições de status, grafo de rotas) ficam no
pacote `dominio`, sem dependência de Spring nem de banco, e por isso são testadas sem infraestrutura.

<pre>
Rotavital🩸/
├── backend <img src="https://img.shields.io/badge/-Java_21-111827?style=flat&logo=openjdk&logoColor=orange" height="18"/> <img src="https://img.shields.io/badge/-Spring_Boot_3.3-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/-Maven-111827?style=flat&logo=apachemaven&logoColor=C71A36" height="18"/>/
│   ├── pom.xml <img src="https://img.shields.io/badge/-Maven_POM-111827?style=flat&logo=apachemaven&logoColor=C71A36" height="18"/>
│   ├── Dockerfile <img src="https://img.shields.io/badge/-Multi--stage_·_roda_os_testes_no_1º_estágio-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   ├── .dockerignore <img src="https://img.shields.io/badge/-Docker_Ignore-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   ├── .env.example <img src="https://img.shields.io/badge/-Modelo_do_.env-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/>
│   ├── .gitignore <img src="https://img.shields.io/badge/-GitIgnore-111827?style=flat&logo=git&logoColor=F05032" height="18"/>
│   └── src <img src="https://img.shields.io/badge/-src-111827?style=flat-square&logo=openjdk&logoColor=orange" height="18"/>/
│       ├── main <img src="https://img.shields.io/badge/-main-111827?style=flat-square&logo=openjdk&logoColor=orange" height="18"/>/
│       │   ├── resources <img src="https://img.shields.io/badge/-Resources-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │   │   └── application.properties <img src="https://img.shields.io/badge/-Conexão_Supabase_(Session_Pooler)-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/>
│       │   └── java/com/rotavital <img src="https://img.shields.io/badge/-Pacote_raiz-111827?style=flat-square&logo=openjdk&logoColor=orange" height="18"/>/
│       │       ├── RotaVitalApplication.java <img src="https://img.shields.io/badge/-Entry_Point_·_carrega_.env-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       ├── api <img src="https://img.shields.io/badge/-REST_Controllers-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │       │   ├── AlertasDoEstoqueController.java <img src="https://img.shields.io/badge/-GET_%2Fbancos%2F%7BbancoId%7D%2Falertas-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑02-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── AcessoController.java <img src="https://img.shields.io/badge/-POST_%2Facessos-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑01-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── EstoqueController.java <img src="https://img.shields.io/badge/-GET_%2Fbancos%2F%7BbancoId%7D%2Festoque_·_%2Fresumo-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/-GET·POST_%2Fhemocomponentes_·_GET_%2F%7Bid%7D-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑03-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── MovimentacaoDeBolsaController.java <img src="https://img.shields.io/badge/-PATCH_%2Fhemocomponentes%2F%7Bid%7D-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑08-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── RotaController.java <img src="https://img.shields.io/badge/-GET_%2Fpontos_·_%2Fconexoes_·_%2Frotas-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑07-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── SugestaoDeLoteController.java <img src="https://img.shields.io/badge/-GET_%2Fbancos%2F%7BbancoId%7D%2Fsugestao--lote-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/> <img src="https://img.shields.io/badge/HU‑04-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── TratadorDeErros.java <img src="https://img.shields.io/badge/-Erros_RFC_7807_·_400_·_404_·_422-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   └── dto <img src="https://img.shields.io/badge/-DTO-111827?style=flat-square&logo=openapiinitiative&logoColor=6BA539" height="18"/>/
│       │       │       ├── acesso <img src="https://img.shields.io/badge/-Acesso-111827?style=flat-square&logo=keycloak&logoColor=4D4D4D" height="18"/>/
│       │       │       │   ├── AcessoDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── NovoAcessoRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   └── TipoAcesso.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │       ├── comum <img src="https://img.shields.io/badge/-Comum-111827?style=flat-square&logo=openjdk&logoColor=orange" height="18"/>/
│       │       │       │   └── ErroDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       ├── estoque <img src="https://img.shields.io/badge/-Estoque-111827?style=flat-square&logo=databricks&logoColor=FF3621" height="18"/>/
│       │       │       │   ├── AlertaDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── AlertasDoEstoqueDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── AtualizarStatusBolsaRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── BolsaHemocomponenteDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── EstoqueDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── NovaBolsaHemocomponenteRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── ResumoEstoqueDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── SugestaoDeLoteDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── TotalPorComponenteDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   └── TotalPorTipoSanguineoDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       ├── requisicao <img src="https://img.shields.io/badge/-Requisicao-111827?style=flat-square&logo=hospitalfederation&logoColor=E0234E" height="18"/>/
│       │       │       │   ├── AlocacaoDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── AtualizarStatusRequisicaoRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── NivelUrgencia.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │       │   ├── NovaRequisicaoRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   └── RequisicaoHospitalarDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       ├── rota <img src="https://img.shields.io/badge/-Rota-111827?style=flat-square&logo=googlemaps&logoColor=34A853" height="18"/>/
│       │       │       │   ├── ConexaoDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── PontoRedeDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   ├── RotaCalculadaDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │       │   └── TipoPontoRede.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │       └── telemetria <img src="https://img.shields.io/badge/-Telemetria-111827?style=flat-square&logo=grafana&logoColor=F46800" height="18"/>/
│       │       │           ├── LeituraTelemetriaDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │           ├── MonitoramentoEntregaDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │           └── RegistrarTelemetriaRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       ├── benchmark <img src="https://img.shields.io/badge/-Paralelismo_(threads)-111827?style=flat-square&logo=openjdk&logoColor=F46800" height="18"/>/
│       │       │   ├── controller <img src="https://img.shields.io/badge/-Controller-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │       │   │   └── BenchmarkController.java <img src="https://img.shields.io/badge/-%2Fbenchmarks%2Fauditoria--telemetria-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── dto <img src="https://img.shields.io/badge/-DTO-111827?style=flat-square&logo=openapiinitiative&logoColor=6BA539" height="18"/>/
│       │       │   │   ├── BenchmarkRequest.java <img src="https://img.shields.io/badge/Request-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │   │   ├── BenchmarkResponse.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │   │   └── ItemExecucaoDTO.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │   ├── model <img src="https://img.shields.io/badge/-Model-111827?style=flat-square&logo=openjdk&logoColor=3B82F6" height="18"/>/
│       │       │   │   ├── RegistroTelemetria.java <img src="https://img.shields.io/badge/Modelo-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   │   └── ResultadoAuditoria.java <img src="https://img.shields.io/badge/Modelo-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   └── service <img src="https://img.shields.io/badge/-Service-111827?style=flat-square&logo=openjdk&logoColor=F46800" height="18"/>/
│       │       │       └── AuditoriaTelemetriaService.java <img src="https://img.shields.io/badge/-Sequencial_×_Paralelo-111827?style=flat&logo=openjdk&logoColor=F46800" height="18"/>
│       │       ├── config <img src="https://img.shields.io/badge/-Configuração-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │       │   ├── CorsConfig.java <img src="https://img.shields.io/badge/-CORS_·_origens_dev_e_produção-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── RelogioConfig.java <img src="https://img.shields.io/badge/-Clock_no_fuso_de_Recife-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   └── HttpLoggingFilter.java <img src="https://img.shields.io/badge/-Log_das_requisições_HTTP-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       ├── diagnostico <img src="https://img.shields.io/badge/-Diagnóstico_(admin)-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │       │   ├── DiagnosticoController.java <img src="https://img.shields.io/badge/-GET_%2Fdiagnostico-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── DiagnosticoResponse.java <img src="https://img.shields.io/badge/Response-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│       │       │   ├── LogHttpItem.java <img src="https://img.shields.io/badge/Record-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   └── LogHttpService.java <img src="https://img.shields.io/badge/-Últimas_requisições_HTTP-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       ├── dominio <img src="https://img.shields.io/badge/-Domínio_(POO)-111827?style=flat-square&logo=openjdk&logoColor=purple" height="18"/>/
│       │       │   ├── PontoDeRede.java <img src="https://img.shields.io/badge/Interface-111827?style=flat&logo=openjdk&logoColor=22C55E" height="18"/>
│       │       │   ├── PontoDeRedeBase.java <img src="https://img.shields.io/badge/Classe_abstrata_·_herança_JPA-111827?style=flat&logo=openjdk&logoColor=22C55E" height="18"/>
│       │       │   ├── AlertaDeEstoque.java <img src="https://img.shields.io/badge/-Record_·_alertas_do_painel-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/> <img src="https://img.shields.io/badge/HU‑02-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── CompatibilidadeSanguinea.java <img src="https://img.shields.io/badge/-ABO%2FRh_por_componente-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/> <img src="https://img.shields.io/badge/HU‑04-111827?style=flat-square&logoColor=white" height="18"/>
│       │       │   ├── MovimentacaoInvalidaException.java <img src="https://img.shields.io/badge/-Exceção_·_transição_de_status-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │       │   ├── Endereco.java <img src="https://img.shields.io/badge/Value_Object-111827?style=flat&logo=openjdk&logoColor=A855F7" height="18"/>
│       │       │   ├── Hospital.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   ├── BancoDeSangue.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   ├── Estoque.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/> <img src="https://img.shields.io/badge/-FEFO-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │       │   ├── BolsaHemocomponente.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   ├── RequisicaoHospitalar.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/> <img src="https://img.shields.io/badge/-ABO%2FRh-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │       │   ├── RedeDistribuicao.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/> <img src="https://img.shields.io/badge/-Grafo_·_Dijkstra-111827?style=flat&logo=googlemaps&logoColor=34A853" height="18"/>
│       │       │   ├── Conexao.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   ├── RotaCalculada.java <img src="https://img.shields.io/badge/Entidade-111827?style=flat&logo=openjdk&logoColor=3B82F6" height="18"/>
│       │       │   ├── FilaRequisicoesHospitalares.java <img src="https://img.shields.io/badge/-Fila_FIFO_·_ArrayDeque-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/> <img src="https://img.shields.io/badge/-Equivalente_ao_C-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│       │       │   ├── PilhaOperacoes.java <img src="https://img.shields.io/badge/-Pilha_LIFO_·_ArrayDeque-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/> <img src="https://img.shields.io/badge/-Equivalente_ao_C-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│       │       │   ├── TipoComponente.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │   ├── TipoSanguineo.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │   ├── StatusBolsa.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       │   └── StatusRequisicao.java <img src="https://img.shields.io/badge/Enum-111827?style=flat&logo=openjdk&logoColor=FFD700" height="18"/>
│       │       ├── repositorio <img src="https://img.shields.io/badge/-Repositório_(Spring_Data)-111827?style=flat-square&logo=spring&logoColor=6DB33F" height="18"/>/
│       │       │   ├── BancoDeSangueRepository.java <img src="https://img.shields.io/badge/-JpaRepository-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── BolsaHemocomponenteRepository.java <img src="https://img.shields.io/badge/-JpaRepository-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── ConexaoRepository.java <img src="https://img.shields.io/badge/-JpaRepository-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   ├── PontoDeRedeRepository.java <img src="https://img.shields.io/badge/-JpaRepository-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │       │   └── ResumoEstoqueRepository.java <img src="https://img.shields.io/badge/-Consultas_às_views_do_painel-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│       │       └── servico <img src="https://img.shields.io/badge/-Service-111827?style=flat-square&logo=springboot&logoColor=6DB33F" height="18"/>/
│       │           ├── AlertasDoEstoqueService.java <img src="https://img.shields.io/badge/-%40Transactional_·_alertas-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │           ├── DadosInvalidosException.java <img src="https://img.shields.io/badge/-Exceção_·_400-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │           ├── EstoqueService.java <img src="https://img.shields.io/badge/-%40Transactional_·_estoque_·_bolsa_·_validação-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │           ├── MovimentacaoDeBolsaService.java <img src="https://img.shields.io/badge/-%40Transactional_·_baixa_da_bolsa-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │           ├── RecursoNaoEncontradoException.java <img src="https://img.shields.io/badge/-Exceção_·_404-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │           ├── RedeDistribuicaoService.java <img src="https://img.shields.io/badge/-%40Transactional_·_rede_do_banco-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │           ├── RegraDeNegocioException.java <img src="https://img.shields.io/badge/-Exceção_·_422-111827?style=flat&logo=openjdk&logoColor=E0234E" height="18"/>
│       │           ├── ResumoEstoqueService.java <img src="https://img.shields.io/badge/-%40Transactional_·_totais_do_estoque-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       │           └── SugestaoDeLoteService.java <img src="https://img.shields.io/badge/-%40Transactional_·_sugestão_de_lote-111827?style=flat&logo=springboot&logoColor=6DB33F" height="18"/>
│       └── test <img src="https://img.shields.io/badge/-JUnit_5-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/> <img src="https://img.shields.io/badge/-Testcontainers-111827?style=flat-square&logo=testcontainers&logoColor=9B489A" height="18"/> <img src="https://img.shields.io/badge/-Cucumber-111827?style=flat-square&logo=cucumber&logoColor=23D96C" height="18"/>/
│           ├── resources/features <img src="https://img.shields.io/badge/-Gherkin_em_português_·_1_por_história-111827?style=flat-square&logo=cucumber&logoColor=23D96C" height="18"/>/
│           │   ├── hu02_alertas_do_painel.feature
│           │   ├── hu03_estoque_por_tipo.feature
│           │   ├── hu04_sugestao_de_lote.feature
│           │   └── hu08_baixa_da_bolsa.feature
│           └── java/com/rotavital/
│           ├── aceitacao <img src="https://img.shields.io/badge/-Aceitação_(Cucumber_·_mvn_verify)-111827?style=flat-square&logo=cucumber&logoColor=23D96C" height="18"/>/
│           │   ├── AceitacaoIT.java <img src="https://img.shields.io/badge/-Suíte_·_relatório_em_target%2Fcucumber-111827?style=flat&logo=cucumber&logoColor=23D96C" height="18"/>
│           │   └── Passos*.java · Vocabulario.java <img src="https://img.shields.io/badge/-Dado%2FQuando%2FEntão_→_HTTP_+_SQL-111827?style=flat&logo=cucumber&logoColor=23D96C" height="18"/>
│           ├── integracao <img src="https://img.shields.io/badge/-Integração_(Postgres_real_·_mvn_verify)-111827?style=flat-square&logo=testcontainers&logoColor=9B489A" height="18"/>/
│           │   ├── EstoqueIT.java · BaixaDaBolsaIT.java <img src="https://img.shields.io/badge/-API_+_JPA_+_Postgres_17-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│           │   └── PostgresDeTeste.java · RelogioDeTeste.java · DadosDeTeste.java
│           ├── api <img src="https://img.shields.io/badge/-API_(MockMvc)-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/>/
│           │   └── ErrosDoEstoqueTest.java <img src="https://img.shields.io/badge/-%40Test_·_respostas_de_erro_400_·_404_·_422-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           ├── benchmark <img src="https://img.shields.io/badge/-Benchmark-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/>/
│           │   └── AuditoriaTelemetriaTest.java <img src="https://img.shields.io/badge/-%40Test_·_concorrência-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           ├── dominio <img src="https://img.shields.io/badge/-Domínio-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/>/
│           │   ├── AlertasDoEstoqueTest.java <img src="https://img.shields.io/badge/-%40Test_·_HU--02_alertas-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── BolsaHemocomponenteTest.java <img src="https://img.shields.io/badge/-%40Test_·_status_e_validade-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── CompatibilidadeSanguineaTest.java <img src="https://img.shields.io/badge/-%40Test_·_tabela_ABO%2FRh_8×8-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── MovimentacaoDaBolsaTest.java <img src="https://img.shields.io/badge/-%40Test_·_36_transições_de_status-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── SugestaoDeLoteTest.java <img src="https://img.shields.io/badge/-%40Test_·_mesmo_tipo_→_FEFO-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── EstoqueTest.java <img src="https://img.shields.io/badge/-%40Test_·_estoque_do_banco-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── FilaRequisicoesHospitalaresTest.java <img src="https://img.shields.io/badge/-%40Test_·_ordem_FIFO-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── PilhaOperacoesTest.java <img src="https://img.shields.io/badge/-%40Test_·_ordem_LIFO-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── PontoDeRedeTest.java <img src="https://img.shields.io/badge/-%40Test_·_herança_e_igualdade-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   ├── RedeDistribuicaoTest.java <img src="https://img.shields.io/badge/-%40Test_·_grafo_·_Dijkstra-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           │   └── TesteFluxoTest.java <img src="https://img.shields.io/badge/-%40Test_·_fluxo_FEFO_+_ABO%2FRh-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│           └── servico <img src="https://img.shields.io/badge/-Service-111827?style=flat-square&logo=junit5&logoColor=25A162" height="18"/>/
│               ├── EstoqueServiceTest.java <img src="https://img.shields.io/badge/-%40Test_·_estoque_e_busca_de_bolsa-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│               ├── RedeDistribuicaoServiceTest.java <img src="https://img.shields.io/badge/-%40Test_·_montagem_da_rede-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│               ├── RepositorioFalso.java <img src="https://img.shields.io/badge/-Repositório_falso_(sem_banco)-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│               ├── ResumoEstoqueServiceTest.java <img src="https://img.shields.io/badge/-%40Test_·_totais_do_resumo-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│               └── ValidacaoNovaBolsaTest.java <img src="https://img.shields.io/badge/-%40Test_·_validação_da_bolsa_nova-111827?style=flat&logo=junit5&logoColor=25A162" height="18"/>
│
├── c <img src="https://img.shields.io/badge/-C99-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/> <img src="https://img.shields.io/badge/-Estruturas_de_Dados_(AV1)-111827?style=flat-square&logo=c&logoColor=A8B9CC" height="18"/>/
│   ├── Makefile <img src="https://img.shields.io/badge/-make_·_make_run_·_make_clean-111827?style=flat&logo=gnu&logoColor=A8B9CC" height="18"/>
│   ├── README.md <img src="https://img.shields.io/badge/-Estruturas_·_testes_·_como_compilar-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── rotavital_tipos.h <img src="https://img.shields.io/badge/-Enums_·_structs_do_domínio-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── rotavital_tipos.c <img src="https://img.shields.io/badge/-Conversão_de_enums-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── lista_estoque.h <img src="https://img.shields.io/badge/-Lista_encadeada_·_estoque-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── lista_estoque.c <img src="https://img.shields.io/badge/-malloc_·_free_·_ponteiros-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── fila_requisicoes.h <img src="https://img.shields.io/badge/-Fila_FIFO_·_requisições-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── fila_requisicoes.c <img src="https://img.shields.io/badge/-Enfileirar_·_desenfileirar-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── pilha_operacoes.h <img src="https://img.shields.io/badge/-Pilha_LIFO_·_histórico-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   ├── pilha_operacoes.c <img src="https://img.shields.io/badge/-Empilhar_·_desempilhar-111827?style=flat&logo=c&logoColor=A8B9CC" height="18"/>
│   └── main.c <img src="https://img.shields.io/badge/-Testes_com_assert()-111827?style=flat&logo=c&logoColor=25A162" height="18"/>
│
├── frontend <img src="https://img.shields.io/badge/-React_18-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/> <img src="https://img.shields.io/badge/-TypeScript-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/> <img src="https://img.shields.io/badge/-Vite-111827?style=flat&logo=vite&logoColor=646CFF" height="18"/> <img src="https://img.shields.io/badge/-Tailwind_CSS-111827?style=flat&logo=tailwindcss&logoColor=38BDF8" height="18"/>/
│   ├── Dockerfile <img src="https://img.shields.io/badge/-Build_+_Nginx-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   ├── .dockerignore <img src="https://img.shields.io/badge/-Docker_Ignore-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   ├── .env.example <img src="https://img.shields.io/badge/-VITE__API__URL_·_timeout-111827?style=flat&logo=vite&logoColor=646CFF" height="18"/>
│   ├── nginx/default.conf.template <img src="https://img.shields.io/badge/-Proxy_%2Fapi_→_backend_·_%24%7BPORT%7D_·_%24%7BAPI__UPSTREAM%7D-111827?style=flat&logo=nginx&logoColor=009639" height="18"/>
│   ├── vercel.json <img src="https://img.shields.io/badge/-Site_na_Vercel_·_%2Fapi_→_Render_·_SPA-111827?style=flat&logo=vercel&logoColor=white" height="18"/>
│   ├── .gitignore <img src="https://img.shields.io/badge/-GitIgnore-111827?style=flat&logo=git&logoColor=F05032" height="18"/>
│   ├── index.html <img src="https://img.shields.io/badge/-HTML5-111827?style=flat&logo=html5&logoColor=E34F26" height="18"/>
│   ├── package.json <img src="https://img.shields.io/badge/-npm-111827?style=flat&logo=npm&logoColor=CB3837" height="18"/>
│   ├── vite.config.ts <img src="https://img.shields.io/badge/-Vite_Config-111827?style=flat&logo=vite&logoColor=646CFF" height="18"/>
│   ├── tailwind.config.ts <img src="https://img.shields.io/badge/-Tailwind_Config-111827?style=flat&logo=tailwindcss&logoColor=38BDF8" height="18"/>
│   ├── postcss.config.js <img src="https://img.shields.io/badge/-PostCSS-111827?style=flat&logo=postcss&logoColor=DD3A0A" height="18"/>
│   ├── eslint.config.js <img src="https://img.shields.io/badge/-ESLint-111827?style=flat&logo=eslint&logoColor=4B32C3" height="18"/>
│   ├── tsconfig.json <img src="https://img.shields.io/badge/-TS_Config-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│   ├── tsconfig.node.json <img src="https://img.shields.io/badge/-TS_Config_(Node)-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│   └── src <img src="https://img.shields.io/badge/-TSX_·_TS-111827?style=flat-square&logo=typescript&logoColor=3178C6" height="18"/>/
│       ├── main.tsx <img src="https://img.shields.io/badge/-Entry_Point-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       ├── App.tsx <img src="https://img.shields.io/badge/-App_Shell-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       ├── index.css <img src="https://img.shields.io/badge/-CSS-111827?style=flat&logo=css&logoColor=663399" height="18"/>
│       ├── vite-env.d.ts <img src="https://img.shields.io/badge/-Vite_Types-111827?style=flat&logo=vite&logoColor=646CFF" height="18"/>
│       ├── routes <img src="https://img.shields.io/badge/-React_Router-111827?style=flat-square&logo=reactrouter&logoColor=CA4245" height="18"/>/
│       │   └── index.tsx <img src="https://img.shields.io/badge/-AppRoutes-111827?style=flat&logo=reactrouter&logoColor=CA4245" height="18"/>
│       ├── context <img src="https://img.shields.io/badge/-Context_API-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   ├── ContextoAutenticacao.tsx <img src="https://img.shields.io/badge/-Auth_Provider-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/> <img src="https://img.shields.io/badge/HU‑01-111827?style=flat-square&logoColor=white" height="18"/>
│       │   ├── ContextoDados.tsx <img src="https://img.shields.io/badge/-Estoque_real_+_mocks-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       │   └── ContextoNotificacao.tsx <img src="https://img.shields.io/badge/-Notificações_globais-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       ├── components <img src="https://img.shields.io/badge/-Componentes-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   ├── layout <img src="https://img.shields.io/badge/-Layout-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   ├── LayoutPrincipal.tsx <img src="https://img.shields.io/badge/-Guarda_de_Rota-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       │   │   └── BarraLateral.tsx <img src="https://img.shields.io/badge/-Menu-111827?style=flat&logo=react&logoColor=61DAFB" height="18"/>
│       │   ├── painel <img src="https://img.shields.io/badge/-Painel-111827?style=flat-square&logo=react&logoColor=F59E0B" height="18"/> <img src="https://img.shields.io/badge/HU‑02-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── CartaoIndicador.tsx <img src="https://img.shields.io/badge/-Indicador-111827?style=flat&logo=react&logoColor=F59E0B" height="18"/>
│       │   │   ├── CaixaAvisos.tsx <img src="https://img.shields.io/badge/-Avisos-111827?style=flat&logo=react&logoColor=F59E0B" height="18"/>
│       │   │   └── AcoesRapidas.tsx <img src="https://img.shields.io/badge/-Atalhos-111827?style=flat&logo=react&logoColor=F59E0B" height="18"/>
│       │   ├── estoque <img src="https://img.shields.io/badge/-Estoque-111827?style=flat-square&logo=react&logoColor=FF3621" height="18"/> <img src="https://img.shields.io/badge/HU‑03-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── CartaoLote.tsx <img src="https://img.shields.io/badge/-Card-111827?style=flat&logo=react&logoColor=FF3621" height="18"/>
│       │   │   ├── FiltroTipoEstoque.tsx <img src="https://img.shields.io/badge/-Filtro-111827?style=flat&logo=react&logoColor=FF3621" height="18"/>
│       │   │   ├── ModalNovaBolsa.tsx <img src="https://img.shields.io/badge/-Cadastro-111827?style=flat&logo=react&logoColor=FF3621" height="18"/>
│       │   │   ├── PaginacaoEstoque.tsx <img src="https://img.shields.io/badge/-Paginação-111827?style=flat&logo=react&logoColor=FF3621" height="18"/>
│       │   │   └── SeletorFiltro.tsx <img src="https://img.shields.io/badge/-Filtro-111827?style=flat&logo=react&logoColor=FF3621" height="18"/>
│       │   ├── requisicao <img src="https://img.shields.io/badge/-Requisição-111827?style=flat-square&logo=react&logoColor=E0234E" height="18"/> <img src="https://img.shields.io/badge/HU‑04_·_05_·_06-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── FormularioRequisicao.tsx <img src="https://img.shields.io/badge/-Formulário-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   ├── ListaRecebimentos.tsx <img src="https://img.shields.io/badge/-Lista-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   ├── FormularioCampanha.tsx <img src="https://img.shields.io/badge/-Formulário-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   ├── SeletorUrgencia.tsx <img src="https://img.shields.io/badge/-Seletor-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   └── CampoFormulario.tsx <img src="https://img.shields.io/badge/-Campo-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   ├── rede <img src="https://img.shields.io/badge/-Rede-111827?style=flat-square&logo=react&logoColor=34A853" height="18"/> <img src="https://img.shields.io/badge/HU‑07-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── MapaRede.tsx <img src="https://img.shields.io/badge/-Mapa-111827?style=flat&logo=react&logoColor=34A853" height="18"/>
│       │   │   ├── ListaHospitais.tsx <img src="https://img.shields.io/badge/-Lista-111827?style=flat&logo=react&logoColor=34A853" height="18"/>
│       │   │   ├── PlanoTransporte.tsx <img src="https://img.shields.io/badge/-Plano-111827?style=flat&logo=react&logoColor=34A853" height="18"/>
│       │   │   └── PrioridadeEnvio.tsx <img src="https://img.shields.io/badge/-FEFO-111827?style=flat&logo=react&logoColor=34A853" height="18"/>
│       │   ├── pacientes <img src="https://img.shields.io/badge/-Pacientes-111827?style=flat-square&logo=react&logoColor=22C55E" height="18"/> <img src="https://img.shields.io/badge/HU‑08-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── CartaoPaciente.tsx <img src="https://img.shields.io/badge/-Card-111827?style=flat&logo=react&logoColor=22C55E" height="18"/>
│       │   │   └── ModalConcluirProcedimento.tsx <img src="https://img.shields.io/badge/-Modal-111827?style=flat&logo=react&logoColor=22C55E" height="18"/>
│       │   ├── donations <img src="https://img.shields.io/badge/-Doações-111827?style=flat-square&logo=react&logoColor=E0234E" height="18"/> <img src="https://img.shields.io/badge/HU‑09_·_HU‑10-111827?style=flat-square&logoColor=white" height="18"/>/
│       │   │   ├── CartaoPessoa.tsx <img src="https://img.shields.io/badge/-Card-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   ├── FiltroTipoSanguineo.tsx <img src="https://img.shields.io/badge/-Filtro-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   │   └── ModalAgendarDoacao.tsx <img src="https://img.shields.io/badge/-Modal-111827?style=flat&logo=react&logoColor=E0234E" height="18"/>
│       │   └── ui <img src="https://img.shields.io/badge/-UI_Kit-111827?style=flat-square&logo=react&logoColor=A855F7" height="18"/>/
│       │       ├── SeloTipoSanguineo.tsx <img src="https://img.shields.io/badge/-Badge-111827?style=flat&logo=react&logoColor=A855F7" height="18"/>
│       │       ├── PontoStatus.tsx <img src="https://img.shields.io/badge/-Status-111827?style=flat&logo=react&logoColor=A855F7" height="18"/>
│       │       └── EmBreve.tsx <img src="https://img.shields.io/badge/-Placeholder-111827?style=flat&logo=react&logoColor=A855F7" height="18"/>
│       ├── pages <img src="https://img.shields.io/badge/-Páginas-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   ├── Admin <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaAdmin.tsx <img src="https://img.shields.io/badge/-Diagnóstico_·_só_admin-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/> <img src="https://img.shields.io/badge/-API_real-3B82F6?style=flat-square" height="18"/>
│       │   ├── Login <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaLogin.tsx <img src="https://img.shields.io/badge/HU‑01-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   ├── PainelOperacional <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaPainel.tsx <img src="https://img.shields.io/badge/HU‑02-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   ├── Estoque <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaEstoque.tsx <img src="https://img.shields.io/badge/HU‑03-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/> <img src="https://img.shields.io/badge/-API_real-3B82F6?style=flat-square" height="18"/>
│       │   ├── Requisicao <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaRequisicao.tsx <img src="https://img.shields.io/badge/HU‑04_·_05_·_06-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   ├── RedeHospitalar <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaRede.tsx <img src="https://img.shields.io/badge/HU‑07-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   ├── Pacientes <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaPacientes.tsx <img src="https://img.shields.io/badge/HU‑08-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   ├── Doacoes <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   │   └── PaginaDoacoes.tsx <img src="https://img.shields.io/badge/HU‑09-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       │   └── PortalDoador <img src="https://img.shields.io/badge/-Tela-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │       └── PaginaPortalDoador.tsx <img src="https://img.shields.io/badge/HU‑10-111827?style=flat-square&logoColor=white" height="18"/> <img src="https://img.shields.io/badge/-Implementada-22C55E?style=flat-square" height="18"/>
│       ├── data <img src="https://img.shields.io/badge/-Mock_Data-111827?style=flat-square&logo=json&logoColor=yellow" height="18"/>/
│       │   ├── estoqueMock.ts <img src="https://img.shields.io/badge/-Lotes-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── pessoasMock.ts <img src="https://img.shields.io/badge/-Pessoas-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── redeMock.ts <img src="https://img.shields.io/badge/-Rede-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   └── requisicoesMock.ts <img src="https://img.shields.io/badge/-Requisições-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       ├── hooks <img src="https://img.shields.io/badge/-Hooks-111827?style=flat-square&logo=react&logoColor=61DAFB" height="18"/>/
│       │   └── useRotasDaRede.ts <img src="https://img.shields.io/badge/-Rotas_via_OSRM-111827?style=flat&logo=openstreetmap&logoColor=7EBC6F" height="18"/> <img src="https://img.shields.io/badge/HU‑07-111827?style=flat-square&logoColor=white" height="18"/>
│       ├── lib <img src="https://img.shields.io/badge/-Regras_de_tela-111827?style=flat-square&logo=typescript&logoColor=3178C6" height="18"/>/
│       │   ├── estoque.ts <img src="https://img.shields.io/badge/-Estoque-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── painel.ts <img src="https://img.shields.io/badge/-Painel-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── pacientes.ts <img src="https://img.shields.io/badge/-Pacientes-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── rede.ts <img src="https://img.shields.io/badge/-Rede-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── requisicao.ts <img src="https://img.shields.io/badge/-Requisição-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   ├── roteirizacao.ts <img src="https://img.shields.io/badge/-Roteirização-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   └── utilitarios.ts <img src="https://img.shields.io/badge/-Utilitários-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       ├── services <img src="https://img.shields.io/badge/-Integração_com_a_API-111827?style=flat-square&logo=typescript&logoColor=3178C6" height="18"/>/
│       │   ├── clienteHttp.ts <img src="https://img.shields.io/badge/-Fetch_·_timeout_·_interceptors-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       │   └── api.ts <img src="https://img.shields.io/badge/-Chamadas_a_%2Fapi%2Fv1-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│       └── types <img src="https://img.shields.io/badge/-TypeScript-111827?style=flat-square&logo=typescript&logoColor=3178C6" height="18"/>/
│           ├── dtos.ts <img src="https://img.shields.io/badge/-DTOs_do_backend-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│           └── index.ts <img src="https://img.shields.io/badge/-Domain_Types-111827?style=flat&logo=typescript&logoColor=3178C6" height="18"/>
│
├── docs <img src="https://img.shields.io/badge/Docs-111827?style=flat&logo=markdown&logoColor=blue" height="18"/>/
│   ├── DOCUMENTACAO.md <img src="https://img.shields.io/badge/Índice-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── COMO_EXECUTAR.md <img src="https://img.shields.io/badge/Como_executar_(Maven_·_npm)-111827?style=flat&logo=gnubash&logoColor=white" height="18"/>
│   ├── ESTRUTURA_DO_PROJETO.md <img src="https://img.shields.io/badge/Módulos_·_árvore_·_camadas-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── ENTREGAS_PI.md <img src="https://img.shields.io/badge/Entregas_01_e_02_·_HUs_·_BDD-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── FIGMA_VS_REAL.md <img src="https://img.shields.io/badge/Figma_×_Sistema_real-111827?style=flat&logo=figma&logoColor=F24E1E" height="18"/>
│   ├── DESAFIO_CONTAINER_TDD_DEPLOY.md <img src="https://img.shields.io/badge/Evidências_E1_·_configuração_do_deploy-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   ├── MODELO_DE_DOMINIO.md <img src="https://img.shields.io/badge/Modelo_de_Domínio-111827?style=flat&logo=markdown&logoColor=purple" height="18"/>
│   ├── CONTRATOS_DE_API.md <img src="https://img.shields.io/badge/Contratos_de_API-111827?style=flat&logo=markdown&logoColor=6BA539" height="18"/>
│   ├── openapi.yaml <img src="https://img.shields.io/badge/OpenAPI_3.0.3-111827?style=flat&logo=openapiinitiative&logoColor=6BA539" height="18"/>
│   ├── MODULOS.md <img src="https://img.shields.io/badge/Módulos-111827?style=flat&logo=markdown&logoColor=6BA539" height="18"/>
│   ├── DER.md <img src="https://img.shields.io/badge/DER_·_Constraints-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   ├── PERFIS_DE_ACESSO.md <img src="https://img.shields.io/badge/Perfis_de_Acesso_(PI3--182)-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── SUPABASE.md <img src="https://img.shields.io/badge/Detalhamento_do_Supabase-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/>
│   ├── LIGACOES_E_REDES.md <img src="https://img.shields.io/badge/Ligações_e_Redes-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── INVENTARIO_COMPONENTES.md <img src="https://img.shields.io/badge/Inventário_de_Componentes-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── DIAGRAMA_ROTAVITAL.md <img src="https://img.shields.io/badge/Diagrama_(texto)-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── arquitetura-rotavital-paes-silva.drawio <img src="https://img.shields.io/badge/Diagrama_de_Arquitetura_(entrega_RSD)-111827?style=flat&logo=diagramsdotnet&logoColor=F08705" height="18"/>
│   ├── arquitetura-rotavital-paes-silva.png <img src="https://img.shields.io/badge/-PNG_300%25-111827?style=flat-square&logo=diagramsdotnet&logoColor=F08705" height="18"/>
│   ├── REDES_SUBREDES_ROTAVITAL.svg <img src="https://img.shields.io/badge/Redes_e_Sub--redes-111827?style=flat&logo=svg&logoColor=FFB13B" height="18"/>
│   ├── MONTAGEM_PDF_FINAL.md <img src="https://img.shields.io/badge/Roteiro_do_PDF-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── RELATORIO_ATIVIDADE_PARALELISMO.md <img src="https://img.shields.io/badge/Relatório_de_Paralelismo-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   ├── TRADUCAO_C_JAVA_AV1.md <img src="https://img.shields.io/badge/Tradução_C_↔_Java_(AV1)-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│   └── evidencias <img src="https://img.shields.io/badge/-Evidências-111827?style=flat-square&logo=markdown&logoColor=white" height="18"/>/
│       ├── PI3-14_endpoints_rest.md <img src="https://img.shields.io/badge/-Endpoints_REST-111827?style=flat&logo=markdown&logoColor=6BA539" height="18"/>
│       ├── PI3-182_perfis_de_acesso.md <img src="https://img.shields.io/badge/-Perfis_de_acesso_·_navegação_real-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
│       ├── PI3-182 <img src="https://img.shields.io/badge/-9_prints_(Playwright)-111827?style=flat-square&logo=playwright&logoColor=2EAD33" height="18"/>/
│       ├── drawio(fluxo).png <img src="https://img.shields.io/badge/-PNG-111827?style=flat-square&logo=diagramsdotnet&logoColor=F08705" height="18"/>
│       └── drawio_atualizado.png <img src="https://img.shields.io/badge/-PNG-111827?style=flat-square&logo=diagramsdotnet&logoColor=F08705" height="18"/>
│
├── pdf <img src="https://img.shields.io/badge/-PDF-111827?style=flat-square&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>/
│   ├── arquitetura-rotavital-paes-silva.pdf <img src="https://img.shields.io/badge/-PDF_entregue_(RSD)-111827?style=flat&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>
│   ├── Entrega-Projetos-3-ADS-RSD.pdf <img src="https://img.shields.io/badge/-Explicação_da_entrega_(RSD)-111827?style=flat&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>
│   ├── Desafio do PI - Container TDD e Deploy automatizados - 2026.2.pdf <img src="https://img.shields.io/badge/-Enunciado_do_desafio-111827?style=flat&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>
│   └── Rota Vital — Container, TDD e Deploy.pdf <img src="https://img.shields.io/badge/-Slides_da_apresentação-111827?style=flat&logo=adobeacrobatreader&logoColor=EC1C24" height="18"/>
│
├── supabase <img src="https://img.shields.io/badge/-Supabase-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/> <img src="https://img.shields.io/badge/-PostgreSQL-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>/
│   ├── img <img src="https://img.shields.io/badge/-Prints_do_Supabase-111827?style=flat-square&logo=supabase&logoColor=3ECF8E" height="18"/>/
│   ├── migrations <img src="https://img.shields.io/badge/-5_migrations_·_14_tabelas_·_2_views-111827?style=flat-square&logo=postgresql&logoColor=4169E1" height="18"/>/
│   │   ├── 20260924120000_schema_inicial.sql <img src="https://img.shields.io/badge/-Tabelas_·_PK_·_FK_·_RLS-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   │   ├── 20260924120100_constraints_integridade.sql <img src="https://img.shields.io/badge/-NOT_NULL_·_UNIQUE_·_CHECK_·_Trigger-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   │   ├── 20260925120000_ajustes_schema_existente.sql <img src="https://img.shields.io/badge/-Ajustes_do_Schema-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   │   ├── 20260925120100_escopo_clinico.sql <img src="https://img.shields.io/badge/-Escopo_Clínico-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   │   └── 20260925120200_views_painel.sql <img src="https://img.shields.io/badge/-Views_do_Painel-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│   └── seed.sql <img src="https://img.shields.io/badge/-Dados_de_exemplo_·_idempotente-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│
├── db <img src="https://img.shields.io/badge/-Postgres_17_do_Compose-111827?style=flat-square&logo=postgresql&logoColor=4169E1" height="18"/>/
│   ├── Dockerfile <img src="https://img.shields.io/badge/-postgres:17--alpine_+_script-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
│   └── 00_rotavital.sh <img src="https://img.shields.io/badge/-aplica_supabase%2Fmigrations_e_o_seed_na_1ª_subida-111827?style=flat&logo=postgresql&logoColor=4169E1" height="18"/>
│
├── .github <img src="https://img.shields.io/badge/-GitHub_Actions-111827?style=flat-square&logo=githubactions&logoColor=2088FF" height="18"/>/
│   ├── workflows/esteira.yml <img src="https://img.shields.io/badge/-testes_→_imagens_→_deploy_→_smoke_test-111827?style=flat&logo=githubactions&logoColor=2088FF" height="18"/>
│   └── scripts <img src="https://img.shields.io/badge/-deploy.sh_·_resumo__testes.py-111827?style=flat&logo=gnubash&logoColor=white" height="18"/>
│
├── img <img src="https://img.shields.io/badge/Assets-green?style=flat&logo=image&logoColor=white" height="18"/>/
├── compose.yaml <img src="https://img.shields.io/badge/-db_·_backend_·_frontend_·_volume_·_healthcheck-111827?style=flat&logo=docker&logoColor=2496ED" height="18"/>
├── compose.supabase.yaml <img src="https://img.shields.io/badge/-Opcional_·_backend_no_banco_do_Supabase_(login)-111827?style=flat&logo=supabase&logoColor=3ECF8E" height="18"/>
├── .env.example <img src="https://img.shields.io/badge/-Variáveis_do_Compose_(sem_segredo)-111827?style=flat&logo=dotenv&logoColor=ECD53F" height="18"/>
├── render.yaml <img src="https://img.shields.io/badge/-Blueprint_do_Render_(API)-111827?style=flat&logo=render&logoColor=46E3B7" height="18"/>
├── package-lock.json <img src="https://img.shields.io/badge/-npm_lock-111827?style=flat&logo=npm&logoColor=CB3837" height="18"/>
├── .gitignore <img src="https://img.shields.io/badge/-GitIgnore-111827?style=flat&logo=git&logoColor=F05032" height="18"/>
├── LICENSE <img src="https://img.shields.io/badge/License-MIT-FF8C00?style=flat&logo=opensource&logoColor=white" height="18"/>
├── RotaVital.fig <img src="https://img.shields.io/badge/Figma-111827?style=flat&logo=figma&logoColor=red" height="18"/>
└── README.md <img src="https://img.shields.io/badge/-Markdown-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
</pre>

| Onde roda | Frontend | Backend | Banco | Login |
| :--- | :--- | :--- | :--- | :---: |
| 🐳 **Docker (padrão)** | Nginx · `localhost` (repassa `/api/`) | container · `localhost:8080` | Postgres 17 local, com as migrations e o seed | ❌ |
| 🐳🟢 **Docker + Supabase** | Nginx · `localhost` | container · `localhost:8080` | Supabase (o mesmo da produção), sem Postgres local | ✅ |
| 💻 **Sem Docker** | Vite · `localhost:5173` (proxy `/api`) | `mvn spring-boot:run` · `:8080` | Supabase | ✅ |
| ☁️ **Produção** | Vercel (rewrite `/api/` → Render) | Render, imagem do GHCR | Supabase | ✅ |

O login confere a senha no Supabase Auth e busca o perfil na tabela `usuario`, que só tem os usuários ligados ao Auth
no banco do Supabase; por isso ele não funciona com o Postgres local. A produção é atualizada pela
[esteira](#cdt-pipeline) a cada merge na `main`: testes → imagens → GHCR → Render e Vercel.

Pastas e o que cada camada faz hoje em [`ESTRUTURA_DO_PROJETO.md`](./docs/ESTRUTURA_DO_PROJETO.md); módulos em
[`MODULOS.md`](./docs/MODULOS.md); portas, protocolos e sub-redes em [`LIGACOES_E_REDES.md`](./docs/LIGACOES_E_REDES.md);
banco em [`SUPABASE.md`](./docs/SUPABASE.md) e [`DER.md`](./docs/DER.md).

<h2 align="center" id="cdt-como-rodar">▶️ Como Rodar <br>
<img src="https://img.shields.io/badge/-Docker-111827?style=flat&logo=docker&logoColor=2496ED" height="22"/>
</h2>

Só precisa de **Git** e **Docker**. Java, Maven, Node e Postgres ficam dentro dos containers.

```bash
git clone https://github.com/viictorpaes/Rotavital.git && cd Rotavital
cp .env.example .env            # no Windows: Copy-Item .env.example .env — depois troque DB_PASSWORD
docker compose up -d --build    # compila, roda os testes e sobe db → backend → frontend
docker compose ps               # os três precisam aparecer como (healthy)
```

| Serviço | O que é | Healthcheck · só sobe depois de | Endereço |
| :--- | :--- | :--- | :--- |
| 🐘 `db` | Postgres 17 com as migrations de `supabase/migrations` e o seed ([`db/`](./db/)), dados no volume `dados-postgres` | `pg_isready` | só na rede interna |
| ☕ `backend` | API Spring Boot ([`backend/Dockerfile`](./backend/Dockerfile), 2 estágios) | `/actuator/health` = `UP` (inclui o banco) · `db` | `localhost:8080` |
| 🖥️ `frontend` | React no Nginx, com proxy `/api/` → `backend` ([`frontend/Dockerfile`](./frontend/Dockerfile)) | `GET /` · `backend` | `localhost` |

`docker compose down` mantém os dados no volume; `docker compose down -v` apaga, e a próxima subida recria o banco com o seed.

Para usar o **Supabase** no lugar do Postgres local (é o único modo em que o login funciona, [por quê](#arquitetura)),
preencha `SUPABASE_KEY` e `SUPABASE_DB_PASSWORD` no `.env` e suba com o [`compose.supabase.yaml`](./compose.supabase.yaml):

```bash
docker compose down             # se o modo padrão estava no ar: senão o container db continua rodando
docker compose -f compose.yaml -f compose.supabase.yaml up -d --build
```

> [!WARNING]
> Nesse modo a API grava no **banco de produção**: o que for alterado pela tela ou pela API local aparece no Supabase.

Sem Docker, endpoints com `curl` e comandos para Windows: [`docs/COMO_EXECUTAR.md`](./docs/COMO_EXECUTAR.md). Saídas do
terminal que comprovam a subida (E1): [`docs/DESAFIO_CONTAINER_TDD_DEPLOY.md`](./docs/DESAFIO_CONTAINER_TDD_DEPLOY.md#e1-evidencias).

<h2 align="center" id="container-tdd-deploy">🐳🧪🚀 Desafio do PI — Container, TDD e Deploy automatizados <br>
<img src="https://img.shields.io/badge/-Docker_·_TDD_·_GitHub_Actions-111827?style=flat&logo=githubactions&logoColor=2088FF" height="22"/>
</h2>

<p align="center">
Unidade 2 de <b>Infraestrutura de Software</b> (2026.2): <i>"se um colega clonar o repositório amanhã, ele consegue
rodar, testar e publicar o sistema sem pedir ajuda a ninguém?"</i>
</p>

| Entrega | Resumo | Status |
| :--- | :--- | :---: |
| 🐳 [**E1 · Container**](#cdt-como-rodar) | `docker compose up -d --build` sobe banco + API + site; o Dockerfile roda os testes antes de gerar a imagem | ✅ |
| 🧪 [**E2 · TDD**](#cdt-testes) | 3 histórias, 13 tarefas, 9 pares `test:` 🔴 → `feat:` 🟢; 208 unitários, 7 de integração e 13 cenários Gherkin | ✅ |
| 🚀 [**E3 · Deploy**](#cdt-pipeline) | A esteira já roda na `main` desde o merge do PR #31; falta ligar Render e Vercel e proteger a `main` ([configuração única](./docs/DESAFIO_CONTAINER_TDD_DEPLOY.md#cdt-configuracao)) | ⏳ |

<h3 align="center" id="cdt-testes">🧪 Testes — E2 · TDD</h3>

**Story map:** as colunas são o backbone (as atividades do médico e do doador, em ordem). A Release 1 pegou, em cada
atividade, a regra cujo erro machuca mais.

| | 🔐 Entrar | 📊 Painel | 📦 Estoque | 🏥 Requisitar | 🚚 Transportar | 🩺 Atender | ❤️ Doar |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Backbone** | entrar com o perfil | ver números e alertas | ver lotes por tipo | pedir hemocomponente | planejar a rota | concluir o procedimento | agendar a doação |
| 🟢 **Release 1 (TDD)** | — | **HU‑02** alertas | HU‑03 (caracterização) | **HU‑04** sugestão de lote | — | **HU‑08** baixa da bolsa | — |
| 🔵 **Release 2** | bloquear por perfil na API | HU‑F03 notificar alerta | HU‑05 recebimento | gravar alocação · HU‑06 | HU‑F04 rastreio · telemetria | ligar a baixa ao paciente | HU‑F06 elegibilidade |

**Histórias → tarefas → red → green.** As três histórias são do médico: **HU‑04** receber a sugestão do lote compatível
que vence primeiro; **HU‑02** ver no painel as bolsas vencidas, perto de vencer ou fora da temperatura; **HU‑08** dar
baixa na bolsa usada. Cada `test:` foi commitado vermelho (o código só tinha a assinatura) e o `feat:` seguinte o deixou
verde sem quebrar os anteriores; tarefas do mesmo método viraram um par só.

<details>
<summary>🔴 → 🟢 <b>As 13 tarefas e os 9 pares de commits</b></summary>

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

</details>

| Nível | Quantos | Como | Onde roda |
| :--- | :---: | :--- | :--- |
| 🧩 **Unitário** | **208** | JUnit 5, repositório falso e MockMvc: 98 da Release 1, 59 que já existiam e 51 do login e do cadastro, vindos da `main` | `mvn test`: 1º estágio do Dockerfile e esteira |
| 🔗 **Integração** | **7** | Testcontainers com Postgres 17 e as migrations do Supabase ([`EstoqueIT`](./backend/src/test/java/com/rotavital/integracao/EstoqueIT.java), [`BaixaDaBolsaIT`](./backend/src/test/java/com/rotavital/integracao/BaixaDaBolsaIT.java)) | `mvn verify`: esteira |
| ✅ **Aceitação** | **13** | Cucumber + Gherkin em português, chamando a API por HTTP ([`features/`](./backend/src/test/resources/features/)); os 2 da HU‑03 são de caracterização | `mvn verify`: esteira |

<h4 align="left" id="cdt-risco">⚠️ Quais regras ganharam teste primeiro</h4>

| Ordem | Regra | Se der errado |
| :---: | :--- | :--- |
| 1️⃣ | Compatibilidade ABO/Rh | Reação hemolítica, que pode matar. E o código só aceitava bolsa do mesmo tipo |
| 2️⃣ | Bolsa apta; vencida só se descarta | Bolsa vencida ou com a cadeia fria rompida chega ao paciente |
| 3️⃣ | Baixa e transições de status | O estoque mente: bolsa usada duas vezes, descartada que volta |
| 4️⃣ | FEFO + mesmo tipo primeiro | Desperdício: bolsa vence na prateleira, O− gasto à toa |
| 5️⃣ | Alertas do painel | É aviso, não bloqueio: as regras acima já impedem o uso indevido |

**Sem teste:** o frontend (a esteira só garante que compila; o `npm run lint` tem 18 problemas herdados); o login, que
veio da `main` com unitários mas fora do red → green e sem bloqueio por perfil na API; a regra das plaquetas, que ainda
precisa de um hemoterapeuta; a alocação gravada no banco (a FK `fk_alocacao_bolsa_compativel` ainda exige o mesmo tipo);
e os fluxos de negócio em produção, já que o smoke test só confere versão, saúde e site → API.

<h3 align="center" id="cdt-pipeline">⚙️ Pipeline e deploy — E3 · Deploy automatizado</h3>

[`esteira.yml`](./.github/workflows/esteira.yml), a cada push e PR: **☕ Backend · testes** (`mvn verify`, os 3 níveis)
e **🖥️ Frontend · build** → **🐳 Imagens Docker** (os Dockerfiles do E1) → só na `main`: imagem da API no GHCR com
tag = sha → **🚀 Deploy** (API no Render, site na Vercel) → smoke test (sha novo em `/actuator/info` e `/versao.json`,
health `UP`, site chegando na API). Um teste vermelho para a esteira no primeiro job, e nada é publicado.

- **Fluxo de branch:** GitHub Flow. Cada tarefa numa branch, PR com a esteira verde, merge = deploy (PRs #17 a #31). A
  proteção da `main` (*Settings → Rules*, ainda a ligar) exige PR, os 3 checks acima e a branch atualizada, sem *force push*.
- **Segredos:** `.env` local, no `.gitignore`; senha e chave do Supabase só no Render; hook do Render e tokens da Vercel
  no environment `producao` do GitHub; o GHCR usa o `GITHUB_TOKEN`
  ([lista completa](./docs/DESAFIO_CONTAINER_TDD_DEPLOY.md#cdt-segredos)).
- **Rollback:** ⭐ *Actions → Esteira CI/CD → Run workflow* com o sha completo da versão boa (`git log --format=%H`):
  reimplanta a imagem e o site sem recompilar. Sem o GitHub, pelos painéis (Render: *Events → Rollback*; Vercel:
  *Instant Rollback*). Depois, `git revert` do commit ruim via PR.

<h4 align="left" id="cdt-evidencias-e3">📸 Evidências do E3</h4>

| Evidência | Link / print |
| :--- | :--- |
| 🟢 Esteira verde (testes + imagens) | ✅ [PR #31](https://github.com/viictorpaes/Rotavital/actions/runs/39512560086) (20/10) · [último push da branch](https://github.com/viictorpaes/Rotavital/actions/runs/40038937435) (23/10) |
| 🔴 Execução **vermelha** que barrou o merge | ⏳ *a preencher* ([passo 2 da configuração](./docs/DESAFIO_CONTAINER_TDD_DEPLOY.md#cdt-configuracao)) |
| 🟢 Execução **verde** que publicou na `main` | ⏳ passo 8. A [1ª execução na `main`](https://github.com/viictorpaes/Rotavital/actions/runs/39512621182) (merge do #31) passou nos testes e nas imagens e parou no deploy, ainda sem as contas ligadas |
| 🌐 **URL pública** | ⏳ `https://rotavital-web.vercel.app` *(confirmar o domínio)* |
| ↩️ **Rollback** executado | ⏳ *a preencher* (passo 9) |

<h3 align="center" id="cdt-decisoes">🧭 Decisões</h3>

| Decisão | Escolha | Alternativas | Por quê |
| :--- | :--- | :--- | :--- |
| 🐳 Imagem base | `eclipse-temurin:21-jre-alpine` (API) e `nginx-unprivileged:1.30-alpine` (site), sem root | `openjdk`, JDK completo, `nginx:alpine` | Só o runtime, em Alpine: menos pacotes vulneráveis (378 MB e 91 MB). A `openjdk` foi descontinuada. |
| 🏗️ Multi-stage | 1º estágio: Maven compila e **roda os testes**; 2º: só o `.jar` | 1 estágio; build fora do Docker | "Passa nos testes" vale em qualquer máquina, e a produção não leva Maven nem JDK. |
| 💾 Dados | Postgres 17 próprio, com as migrations do Supabase, no volume nomeado `dados-postgres` | Só o Supabase; H2; bind mount | Sobe sem internet e sem a senha de ninguém, e os dados sobrevivem ao `docker compose down` (testado). |
| 🔌 Rede | Rede padrão do Compose, pelo nome do serviço, com `depends_on: service_healthy` | IPs fixos; `network_mode: host`; `sleep` | O DNS do Compose resolve `db` e `backend`, o banco não publica porta, e cada serviço só sobe com o anterior pronto. |
| 🧪 Testes | ABO/Rh primeiro ([ordem de risco](#cdt-risco)); Testcontainers com as migrations; Gherkin em português | Começar pelo mais fácil; H2; JUnit dado/quando/então | Um erro na compatibilidade põe uma vida em risco; o banco dos testes é o SQL de produção; o médico consegue ler os cenários. |
| ⚙️ CI/CD | GitHub Actions + GHCR, tag = sha do commit | GitLab CI, Jenkins; Docker Hub | O código já está no GitHub, o `GITHUB_TOKEN` autentica e a tag por sha permite rollback exato. |
| 🌿 Fluxo de branch | GitHub Flow | Git Flow, GitLab Flow | Grupo pequeno, uma versão no ar e deploy a cada merge; Git Flow seria cerimônia sem release agendada. |
| ☁️ Deploy | API no Render (imagem, Blueprint); site na Vercel com rewrite `/api/` → Render | Tudo no Render; Railway, Fly.io | O Render roda a imagem de graça, na região do Supabase; o site não funcionou no Render, e a Vercel o serve pela CDN. O rewrite evita CORS. |
| 🔐 Segredos | `.env` fora do Git (modelo em `.env.example`); environment `producao` do GitHub; Supabase só no Render | No repositório ou no workflow | Cada segredo fica só onde é usado, e a mesma imagem roda no Compose e no Render. |
| ↩️ Rollback | *Run workflow* com o sha anterior | Só `git revert` + rebuild | Volta em minutos, sem recompilar, e fica registrado. |

<h3 align="center" id="cdt-ia">🤖 Uso de IA</h3>

<p align="center">Permitido pelo enunciado e declarado aqui: <b>a IA rascunha; o grupo valida e assina.</b></p>

| Item | Detalhe |
| :--- | :--- |
| 🛠️ Ferramenta | **Claude Code** (Claude Opus 5.5, da Anthropic), na extensão do VS Code |
| ✍️ O que rascunhou | Dockerfiles, `compose.yaml` e `compose.supabase.yaml`, `db/`, a esteira e os scripts de deploy; o story map e as tarefas; os testes e o código das HU‑04, HU‑02 e HU‑08; o merge da `main`; esta seção e a organização do README em `docs/` |
| 🔎 Como foi verificado | Cada `test:` falhou antes do `feat:`; `mvn verify`; `docker compose` do zero, nos dois modos; `actionlint`; deploy e rollback contra um servidor que simula o Render e a Vercel |
| ⚠️ O que errou e foi corrigido | Script do banco num bind mount, que o macOS bloqueia (→ `db/Dockerfile`); `${var,,}`, que o bash do macOS não tem; rollback aceitando sha curto |
| 👥 O que fica com o grupo | Ligar as contas, colher os prints, conferir as regras clínicas (principalmente a das plaquetas) e apresentar |

<details>
<summary>▶️ <b>Prompts principais</b></summary>

1. *"com base em: Desafio do PI - Container TDD e Deploy automatizados - 2026.2.pdf [...] Três entregas no projeto do PI,
   cada decisão justificada no README [...] Se usarem TDD, expliquem como foi a jornada de desenvolvimento e as lições
   aprendidas; use o final da sessão do README principal p fzr essa atividade"*
2. *"me ajude a trazer os updates da main pra cá, e reconfira se está tudo de acordo com o pdf"*
3. *"sinterize o h2 do desafio [...] e enxuge a parte dele no README.md pra não ficar TÃÃÃÃO complexo"*
4. *"limpe o README.md principal, e coloque as informações retiradas em outros arquivos na pasta docs/. deixe apenas o
   essencial nesse README.MD"*
5. *"me ajude a melhorar a arquitetura no README.md principal, e rode o docker com o supabase e me ajude a testar (sem
   banco local e com sync)"*; "sync" foi confirmado como *"Dados no Supabase"*, e o teste de gravação, como ida e volta.
6. *"ajuste no README a parte do desafio do PI, e encurte mais ele"*
</details>

<h3 align="center" id="cdt-jornada">🧭 Jornada TDD e lições aprendidas</h3>

1. **O story map mostrou onde testar primeiro:** em cada atividade, a regra que mais machuca se der errado.
2. **O teste expôs um gap:** o README prometia "FEFO + ABO/Rh", mas o código só aceitava bolsa do mesmo tipo.
3. **Triangulação:** o primeiro verde tratava tudo como hemácias; só o teste do plasma obrigou a separar por componente.
4. **Fronteiras e relógio:** "a bolsa que vence hoje ainda vale?" virou teste, e o `LocalDate.now()` virou um `Clock` injetado.
5. **Banco de verdade:** o Postgres das migrations pegou constraints que um mock não pega (plaquetas valem no máximo 7 dias).
6. **Ver o teste falhar:** nos 9 pares, o `test:` falhou pelo motivo certo antes do `feat:`; teste que nunca falhou pode não testar nada.

<h2 align="center" id="documentacao">📚 Documentação <br>
<img src="https://img.shields.io/badge/Docs-111827?style=flat&logo=markdown&logoColor=white" height="22"/>
</h2>

<p align="center">
Índice completo, com o que cada documento contém e quando ler, em <a href="./docs/DOCUMENTACAO.md"><code>docs/DOCUMENTACAO.md</code></a>.
</p>

| Documento | Descrição |
| :--- | :--- |
| 🚀 [`COMO_EXECUTAR.md`](./docs/COMO_EXECUTAR.md) | Rodar sem Docker, endpoints com exemplos `curl`, comandos do Maven e do npm |
| 🏰 [`ESTRUTURA_DO_PROJETO.md`](./docs/ESTRUTURA_DO_PROJETO.md) | Módulos e status, árvore do repositório e o que cada camada faz hoje |
| 📌 [`ENTREGAS_PI.md`](./docs/ENTREGAS_PI.md) | Entregas 01 e 02: histórias de usuário com BDD, screencasts e Issue Tracker |
| 🖼️ [`FIGMA_VS_REAL.md`](./docs/FIGMA_VS_REAL.md) | Cada tela do Figma ao lado do sistema rodando, com o código |
| 🐳 [`DESAFIO_CONTAINER_TDD_DEPLOY.md`](./docs/DESAFIO_CONTAINER_TDD_DEPLOY.md) | Evidências do E1 e configuração única do deploy |
| 🧬 [`MODELO_DE_DOMINIO.md`](./docs/MODELO_DE_DOMINIO.md) | Classes do domínio (POO), grafo e Dijkstra |
| 🌐 [`CONTRATOS_DE_API.md`](./docs/CONTRATOS_DE_API.md) · [`openapi.yaml`](./docs/openapi.yaml) | Endpoints por módulo, padrões e erros; contrato OpenAPI 3.0.3 |
| 📦 [`MODULOS.md`](./docs/MODULOS.md) | Contrato × domínio × banco, por módulo |
| 🟢 [`SUPABASE.md`](./docs/SUPABASE.md) · 🗄️ [`DER.md`](./docs/DER.md) | Banco: conexão, `.env`, migrations, RLS e prints; 14 tabelas, 2 views e constraints |
| 🔐 [`PERFIS_DE_ACESSO.md`](./docs/PERFIS_DE_ACESSO.md) | Perfis médico, doador e admin, e o que cada um vê (PI3-182) |
| 🔌 [`LIGACOES_E_REDES.md`](./docs/LIGACOES_E_REDES.md) · 🗺️ [`DIAGRAMA_ROTAVITAL.md`](./docs/DIAGRAMA_ROTAVITAL.md) | Ligações, sub-redes e diagrama de arquitetura (RSD) |
| 🧩 [`INVENTARIO_COMPONENTES.md`](./docs/INVENTARIO_COMPONENTES.md) | Componentes ativos × planejados |
| 🧾 [`evidencias/`](./docs/evidencias/) | Chamadas reais aos endpoints, navegação por perfil e login |
