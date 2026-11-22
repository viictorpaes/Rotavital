<h1 align="center"> Rota Vital — F. Plano de refatoração, TDD e SDD 🛠️<br>
<img src="../../img/RotaVital_logo.png" alt="Logo RotaVital" width="320"> <br>
<img src="https://img.shields.io/badge/-Refatoração-020617?style=flat-square&logo=openjdk&logoColor=orange" height="19"/>
<img src="https://img.shields.io/badge/-JaCoCo-020617?style=flat-square&logo=junit5&logoColor=25A162" height="19"/>
<img src="https://img.shields.io/badge/-ArchUnit-020617?style=flat-square&logo=junit5&logoColor=25A162" height="19"/>
<img src="https://img.shields.io/badge/-SDD-020617?style=flat-square&logo=markdown&logoColor=white" height="19"/>

![Seção](https://img.shields.io/badge/seção-F_(devolutiva)-purple)
![Entrega](https://img.shields.io/badge/Parte_3-01%2F12-red)
</h1>

> [!NOTE]
> Esta seção não está no enunciado: ela responde aos **cinco pontos da devolutiva de 17/11** e diz o que a Parte 3 entrega
> em **01/12/2026**.

<h2 align="center" id="f1-devolutiva">💬 F.1 Onde cada ponto da devolutiva é respondido</h2>

| # | Ponto da devolutiva | Resposta | Onde |
| :---: | :--- | :--- | :--- |
| 1 | DDD a sério e **domínio rico**, sem entidades anêmicas | Diagnóstico das classes anêmicas; agregado `Bolsa` com 6 objetos de valor e 8 invariantes; regras saem do `EstoqueService` e vão para o domínio | [B.1](B-modelo-tatico.md#b1-diagnostico), [B.2](B-modelo-tatico.md#b2-agregado), [B.3](B-modelo-tatico.md#b3-invariantes) |
| 2 | **Camadas e Clean Architecture** | Três camadas com portas e adaptadores; domínio em Java puro; ArchUnit na esteira | [C.2.1](C-tres-camadas.md#c21-clean-architecture), [ADR 0002](adr/0002-dominio-puro-clean-architecture.md) |
| 3 | **O que muda efetivamente** (e o que sai em 01/12) | Tabela antes → depois e o escopo fechado da entrega | [F.2](#f2-o-que-muda) |
| 4 | **TDD**: cobertura atual e planejada | Cobertura medida com JaCoCo e as metas por camada | [F.3](#f3-tdd) |
| 5 | **SDD** para ajudar nos itens anteriores | Especificação da HU-11 antes do código: spec, plano e tarefas | [F.4](#f4-sdd), [`docs/specs/HU-11-…`](../specs/HU-11-reserva-de-bolsa-apta/spec.md) |

<h2 align="center" id="f2-o-que-muda">🔧 F.2 O que muda efetivamente</h2>

| # | Hoje | Depois | Entrega |
| :---: | :--- | :--- | :---: |
| 1 | Pacotes por camada técnica (`api`, `servico`, `dominio`, `repositorio`) | Estoque em `com.rotavital.estoque.{apresentacao, negocio.aplicacao, negocio.dominio, dados}` | **01/12** |
| 2 | `BolsaHemocomponente` com `@Entity`, `@Table`, `Persistable` | `Bolsa` em Java puro + `BolsaEntity` e `BolsaMapper` em `dados` | **01/12** |
| 3 | Prazo de validade, volume e faixa do sensor no `EstoqueService.conferirRegras` | Objetos de valor `Validade`, `Volume`, `Temperatura` e `CodigoDaBolsa`, criados por TDD | **01/12** |
| 4 | `reservar()` e `descartar()` sem conferência (porta dos fundos) | Removidos; `reservarPara(requisicao, hoje)` confere a aptidão (HU-11, invariante I3) | **01/12** |
| 5 | Mudança de status não deixa histórico | Entidade `Movimentacao` dentro do agregado + migration `movimentacao_bolsa` | **01/12** |
| 6 | `EstoqueService`, `MovimentacaoDeBolsaService`, `CorrecaoDeBolsaService` | Casos de uso `RegistrarBolsa`, `ReservarBolsa`, `DescartarBolsa`, `CorrigirBolsa` + porta `BolsaRepository` | **01/12** |
| 7 | Nada impede o domínio de importar JPA | Teste ArchUnit no `mvn verify`: `..negocio.dominio..` não importa `jakarta.persistence`, `org.hibernate` nem `org.springframework` | **01/12** |
| 8 | Sem medição de cobertura na esteira | JaCoCo no `pom.xml`, relatório unitário + integração, com metas que quebram o build | **01/12** |
| 9 | HU-11 não existe | Cenário Gherkin da [B.5](B-modelo-tatico.md#b5-gherkin) rodando no `AceitacaoIT` | **01/12** |
| 10 | `TransferenciaDeBolsa` muda o status da bolsa direto (regra 6 da C.2) | Distribuição pede ao serviço aberto do Estoque; `BolsaDescartada` cancela a transferência | Depois |
| 11 | Requisição só no `openapi.yaml` | Contexto Requisição persistido, com a saga em versão monolito (eventos internos do Spring) | Depois |
| 12 | `Paciente` anêmico + `ValidadorPaciente` no serviço | Objetos de valor `Cpf`, `DataDeNascimento`, `Gravidade` no domínio do Atendimento | Depois |
| 13 | Frontend sem nenhum teste | Vitest nas telas de Estoque (cadastro, descarte, filtros) | Depois |

**Ordem de trabalho para 01/12:** primeiro os objetos de valor (testes primeiro), depois a raiz `Bolsa`, depois os
casos de uso com repositório em memória e, por último, o adaptador JPA. Os testes de integração que já existem
(`EstoqueIT`, `BaixaDaBolsaIT`, `TransferenciaIT` e as features das HU-02, 03, 04 e 08) **não podem mudar de
resultado**: são a rede de segurança da refatoração. A mudança de pacote não muda tabela nenhuma; a única migration é a
tabela `movimentacao_bolsa`, da entidade `Movimentacao`, que guarda a requisição como texto (`R-001`) até o contexto
Requisição ser persistido.

<h2 align="center" id="f3-tdd">🧪 F.3 TDD: cobertura atual e planejada</h2>

**Cobertura atual**, medida pelo squad em 22/11/2026 com JaCoCo 0.8.12 sobre os **430 testes unitários** (`mvn test`).
Os 32 testes de integração e aceitação, que rodam com Postgres no `mvn verify`, ainda não entram na conta:

| Pacote | Linhas cobertas | Ramos cobertos |
| :--- | ---: | ---: |
| `dominio` | **93,8 %** (794 linhas) | 93,7 % |
| `servico` | **94,3 %** (759 linhas) | 87,4 % |
| `supabase` / `google` | 78,4 % / 86,7 % | 64,6 % / 72,7 % |
| `api` (controllers) | 45,6 % | 48,1 % |
| `config`, `diagnostico`, `benchmark.controller` | 43,3 % / 0 % / 0 % | — |
| **Backend total** | **77,5 %** (1.963 de 2.534 linhas) | **75,3 %** |
| Frontend | **0 %** (nenhum teste) | — |

**Cobertura planejada** (o JaCoCo quebra o build abaixo da meta):

| Escopo | Meta em 01/12 | Meta no fim do semestre |
| :--- | ---: | ---: |
| `estoque.negocio.dominio` | ≥ 95 % de linhas e ≥ 90 % de ramos | mantém |
| `estoque.negocio.aplicacao` | ≥ 90 % de linhas | mantém |
| Backend total (unitário + integração) | ≥ 80 % de linhas | ≥ 85 % |
| Frontend | — | ≥ 40 % nas telas de Estoque |

**Como o TDD entra na refatoração:** cada objeto de valor nasce de um teste que falha. O primeiro de `Validade`, por
exemplo, é "plaqueta coletada em 01/10 com validade em 09/10 é recusada (8 dias, o máximo é 7)". Depois vem o código
mínimo que passa e, por último, a refatoração. As mensagens de erro dos testes atuais do `CadastroDeBolsaTest` viram os
critérios dos testes novos, então o comportamento visto pelo usuário não muda.

<h2 align="center" id="f4-sdd">📐 F.4 SDD: especificação antes do código</h2>

No Spec-Driven Development, a **especificação é o artefato principal**: o código e os testes saem dela e são
conferidos contra ela no PR. Cada história da Parte 3 ganha uma pasta em `docs/specs/` com três arquivos curtos:

| Arquivo | O que tem | Ajuda em qual ponto da devolutiva |
| :--- | :--- | :--- |
| `spec.md` | Problema, linguagem ubíqua, **invariantes**, critérios de aceite, contrato da API (trecho do `openapi.yaml`) e o Gherkin | 1: as regras ficam escritas no vocabulário do domínio antes de existir classe |
| `plan.md` | Em que contexto e em que camada cada peça fica, e que porta ou adaptador é novo | 2: a regra de dependência é decidida no papel |
| `tasks.md` | Tarefas pequenas na ordem do TDD (teste → código → refatoração), cada uma com o teste que a fecha | 3 e 4: o plano de refatoração vira tarefas com teste |

O exemplo completo é a [HU-11](../specs/HU-11-reserva-de-bolsa-apta/spec.md). A especificação também é o que entregamos
a uma ferramenta de IA quando ela ajuda a escrever código: ela recebe a spec e as tarefas, não um pedido solto, e o PR só
entra se os testes da spec passarem.

<p align="center">
<a href="E-cenarios-e-orcamento.md">⬅️ E. Cenários e orçamento</a> · <a href="README.md">Índice</a>
</p>
