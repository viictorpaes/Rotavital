<h1 align="center">
  Rota Vital — Documentação Técnica <br> Estrutura do Projeto <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸🏰
</h1>

<p align="center">
<a href="../README.md">⬅️ Voltar ao README principal</a> ·
<a href="DOCUMENTACAO.md">📚 Índice da documentação</a> ·
<a href="MODULOS.md">📦 Módulos em detalhe</a>
</p>

> Módulos implementados, árvore comentada do repositório e o que cada camada (backend, frontend, C e banco) faz hoje.

<h2 align="left" id="modulos">📦 1. Módulos e status</h2>

<p align="center">
4 módulos — Estoque, Requisições e Alocação, Roteirização e Telemetria — catalogados em detalhe, com
endpoints, classes de domínio e tabelas do banco, em <a href="MODULOS.md"><code>docs/MODULOS.md</code></a>.
</p>

| Módulo | Endpoints implementados | Domínio | Status |
| :--- | :--- | :--- | :---: |
| 📦 Estoque e Hemocomponentes | `GET /api/v1/bancos/{bancoId}/estoque` · `/resumo` · `/sugestao-lote` · `/alertas`, `GET`/`PATCH /api/v1/hemocomponentes/{id}` | `Estoque`, `BolsaHemocomponente`, `CompatibilidadeSanguinea`, `AlertaDeEstoque` | ⚠️ Parcial |
| 🏥 Requisições e Alocação | — (só contrato) | `Hospital`, `RequisicaoHospitalar` | ⚠️ Sem controller |
| 🚚 Roteirização e Logística | `GET /api/v1/pontos`, `/conexoes`, `/rotas` | `RedeDistribuicao` (grafo + Dijkstra), `Conexao`, `RotaCalculada` | ✅ Implementado |
| 🌡️ Telemetria e Cadeia Fria | — (só contrato + tabelas) | *(nenhuma)* | ⚠️ Gap de domínio |
| 🔑 Acesso (HU‑01) | `POST /api/v1/acessos` | *(fora do domínio)* | ✅ Implementado |
| 📊 Benchmarks (paralelismo) | `GET`/`POST /api/v1/benchmarks/auditoria-telemetria` | `com.rotavital.benchmark` | ✅ Implementado |

<h2 align="left" id="arvore">🌳 2. Árvore do repositório</h2>

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
│   ├── ESTRUTURA_DO_PROJETO.md <img src="https://img.shields.io/badge/Este_arquivo-111827?style=flat&logo=markdown&logoColor=white" height="18"/>
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

<h2 align="left" id="notas">📝 3. O que cada camada faz hoje</h2>

> [!NOTE]
> **Backend:** Spring Boot 3.3 (Java 21) com 8 controllers — `AcessoController`, `EstoqueController`,
> `RotaController`, `BenchmarkController`, `DiagnosticoController` (`GET /api/v1/diagnostico`, usado pela tela
> de admin) e os três que nasceram por TDD no [Desafio do PI](../README.md#container-tdd-deploy): `SugestaoDeLoteController`
> (HU‑04), `AlertasDoEstoqueController` (HU‑02) e `MovimentacaoDeBolsaController` (HU‑08). Os controllers de domínio leem do **banco (Supabase)** via JPA: bolsas pelo `EstoqueService`,
> totais do estoque pelo `ResumoEstoqueService` (views do painel) e rede/rotas pelo `RedeDistribuicaoService`;
> não há mais dados fixos em memória. Os erros saem no formato RFC 7807 pelo `TratadorDeErros`, com a
> explicação no `detail` (dado inválido → 400, recurso inexistente → 404, regra de negócio → 422). O CORS fica em
> `config/CorsConfig` e o `HttpLoggingFilter` registra cada requisição HTTP. Requisições e Telemetria têm
> contrato em `openapi.yaml`, mas nenhum controller. O domínio também tem `FilaRequisicoesHospitalares` (FIFO)
> e `PilhaOperacoes` (LIFO), as versões Java das estruturas do módulo em C.
>
> **Frontend:** as 10 histórias da Entrega 01 (**HU‑01** a **HU‑10**) têm tela em React. O **login (HU‑01)**, com e-mail e
> senha em `POST /acessos` (PI3-183/184), o **Estoque (HU‑03)**, com filtros e
> paginação em `GET /hemocomponentes` e cadastro de bolsa em `POST /hemocomponentes`, e a tela de **Admin** já consomem a API `/api/v1` por `src/services/api.ts` (cliente em `clienteHttp.ts`, erros
> exibidos pelo `ContextoNotificacao`); as demais telas continuam sobre dados **mockados**
> (`src/data/*Mock.ts` + `ContextoDados`). A roteirização da HU‑07 consulta o OSRM público e cai nos valores
> mockados se ele não responder. O restante da integração segue na Issue #4.
>
> **Estruturas de dados em C (AV1):** `c/` reimplementa em C99 a lista encadeada do estoque, a fila FIFO de
> requisições e a pilha LIFO do histórico de operações, com alocação manual (`malloc`/`free`) e testes com
> `assert()` em `main.c` (`make run`), sem vazamento de memória. Estruturas, testes e como compilar em
> [`c/README.md`](../c/README.md); a tradução comentada C ↔ Java está em
> [`docs/TRADUCAO_C_JAVA_AV1.md`](TRADUCAO_C_JAVA_AV1.md).
>
> **Banco:** Supabase (PostgreSQL), conectado pelo Session Pooler (`application.properties`, senha em
> `backend/.env`). As 5 migrations em `supabase/migrations/` criam **14 tabelas e 2 views**, com PKs, FKs,
> constraints de integridade, gatilhos e RLS ligado. DER e catálogo de constraints em
> [`docs/DER.md`](DER.md); conexão, variáveis, RLS e prints em [`docs/SUPABASE.md`](SUPABASE.md).
