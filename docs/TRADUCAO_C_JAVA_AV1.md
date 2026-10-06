# Rota Vital — Tradução Comentada e Equivalência de Estruturas de Dados
## Projeto Integrador — Avaliação Unidade 1 (AV1 · Semanas 8–9)

---

## 📌 Sumário Executivo
Este documento formaliza a equivalência técnica e arquitetural entre as implementações em **Linguagem C** (com manipulação explícita de ponteiros e gerência manual de memória) e a sua **reimplementação em Java** (orientada a objetos e pronta para integração com Spring Boot) para o sistema **Rota Vital**.

### 🎯 Delimitação Rígida do Escopo da Unidade 1
Conforme as diretrizes pedagógicas da AV1:
* **Escopo Avaliado na Unidade 1:** Estruturas fundamentais de domínio — **Estoque de Hemocomponentes (Lista)**, **Requisições Hospitalares (Fila FIFO)** e **Histórico de Operações (Pilha LIFO)**, com suas operações primitivas de inserção, consulta, remoção e destruição de memória testadas.
* **Proibições da Unidade 1 (Reservadas para a Unidade 2):** Lógica de roteirização geográfica/redes (Dijkstra/grafos), ordenação FEFO (*First Expire, First Out*), tabelas Hash e regras complexas de compatibilidade cruzada ABO/Rh. Quaisquer módulos já presentes no ecossistema de microsserviços referentes a esses temas pertencem exclusivamente ao ciclo da Unidade 2.

---

## 🧬 1. Modelagem de Domínio e Justificativa das Estruturas

No contexto de uma rede logística de hemocentros e hospitais de emergência:

1. **Bolsas de Hemocomponentes & Estoque (`Lista`):**
   * *Justificativa:* O estoque de um banco de sangue armazena bolsas heterogêneas (diferentes tipos de hemocomponentes, tipos sanguíneos e datas de validade). Bolsas precisam ser inseridas dinamicamente a cada coleta, inspecionadas por ID ou filtradas por tipo, e retiradas em posições arbitrárias quando alocadas. A estrutura natural para armazenar esse volume elástico sem limite pré-fixado é a **Lista**.
2. **Requisições Hospitalares (`Fila FIFO`):**
   * *Justificativa:* Pedidos de emergência enviados pelos hospitais devem respeitar o princípio bioético e operacional de justiça e precedência temporal: o primeiro hospital a solicitar deve ter seu pedido avaliado e atendido primeiro (*First-In, First-Out*). A estrutura estrita que garante esse comportamento é a **Fila**.
3. **Histórico de Operações (`Pilha LIFO`):**
   * *Justificativa:* Ações executadas sobre o sistema (ex.: "criar requisição", "reservar bolsa", "cancelar solicitação") exigem rastreabilidade e capacidade de auditoria reversível (*undo*). A operação mais recente é a primeira a ser consultada para confirmação ou reversão (*Last-In, First-Out*). A estrutura canônica para essa política é a **Pilha**.

---

## 🔬 2. Análise Estrutural e Tradução Comentada por Estrutura

---

### Estrutura 1: Estoque de Hemocomponentes (Lista)

#### Código em C (`c/lista_estoque.h` e `c/lista_estoque.c`)
```c
typedef struct NoBolsa {
    BolsaHemocomponente dado;
    struct NoBolsa* proximo;
} NoBolsa;

typedef struct {
    NoBolsa* inicio;
    int tamanho;
} ListaEstoque;

bool lista_estoque_adicionar(ListaEstoque* lista, BolsaHemocomponente bolsa) {
    if (lista == NULL) return false;
    NoBolsa* novo = (NoBolsa*) malloc(sizeof(NoBolsa));
    if (novo == NULL) return false; // Falha de alocação de memória
    novo->dado = bolsa;
    novo->proximo = lista->inicio;
    lista->inicio = novo;
    lista->tamanho++;
    return true;
}

bool lista_estoque_remover(ListaEstoque* lista, const char* id, BolsaHemocomponente* removida) {
    if (lista == NULL || lista->inicio == NULL || id == NULL) return false;
    NoBolsa* atual = lista->inicio;
    NoBolsa* anterior = NULL;
    while (atual != NULL && strcmp(atual->dado.id, id) != 0) {
        anterior = atual;
        atual = atual->proximo;
    }
    if (atual == NULL) return false;
    if (anterior == NULL) lista->inicio = atual->proximo;
    else anterior->proximo = atual->proximo;
    if (removida != NULL) *removida = atual->dado;
    free(atual); // Liberação manual obrigatória
    lista->tamanho--;
    return true;
}
```

#### Código em Java (`com.rotavital.dominio.Estoque` e `BancoDeSangue`)
```java
public class Estoque {
    private final BancoDeSangue bancoDeSangue;

    public Estoque(BancoDeSangue bancoDeSangue) {
        this.bancoDeSangue = bancoDeSangue;
    }

    private List<BolsaHemocomponente> bolsas() {
        return bancoDeSangue.bolsas();
    }

    public void adicionarBolsa(BolsaHemocomponente bolsa) {
        bolsas().add(bolsa);
    }

    public List<BolsaHemocomponente> buscarDisponiveis(TipoComponente tipoComponente, TipoSanguineo tipoSanguineo) {
        return bolsas().stream()
            .filter(BolsaHemocomponente::estaDisponivel)
            .filter(b -> b.getTipoComponente() == tipoComponente)
            .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
            .collect(Collectors.toList());
    }
}
```

#### 📝 Justificativa e Equivalência Amarrada ao Código Real (Opção B)
> **Parágrafo de Justificativa:**
> Na versão em C, a lista de estoque é implementada através de nós encadeados (`NoBolsa`) alocados individualmente na Heap via chamadas explícitas a `malloc(sizeof(NoBolsa))`, encadeando o ponteiro `novo->proximo = lista->inicio` para inserção em $O(1)$ e percorrendo os ponteiros sequencialmente para remoção por ID com liberação obrigatória através de `free(atual)`. Já na reimplementação em Java, optou-se pela **Opção B de engenharia de software e POO**: ao invés de expor manipulações primitivas de nós, a classe `Estoque` atua como um agregador de domínio de alto nível que delega a coleção interna para o `BancoDeSangue`, utilizando a interface canônica `java.util.List<BolsaHemocomponente>`. Essa decisão de design é fundamental para permitir que o estoque seja diretamente persistido e hidratado pelo ORM (JPA/Hibernate) no ecossistema Spring Boot, eliminando o acoplamento rígido de nós encadeados na camada relacional. Enquanto em C o desenvolvedor é o único responsável por desarmar vazamentos de memória percorrendo a lista com `free()`, em Java a JVM delega o ciclo de vida das bolsas ao *Garbage Collector*, preservando idêntica semântica de inserção, busca e remoção protegida por encapsulamento de métodos de negócio.

| Operação | Implementação em C | Reimplementação em Java | Complexidade Temporal |
| :--- | :--- | :--- | :---: |
| **Inicialização** | `lista_estoque_criar()` com `malloc(sizeof(ListaEstoque))` | Construtor `new Estoque(bancoDeSangue)` | $O(1)$ |
| **Inserção** | Alocação de `NoBolsa` e redirecionamento de `inicio` | `adicionarBolsa(bolsa)` que delega para `bolsas().add(bolsa)` | $O(1)$ |
| **Busca/Filtro** | Laço `while (atual != NULL)` comparando strings/enums | Pipeline de Streams funcional com predicados `.filter()` | $O(n)$ |
| **Remoção** | Costura de ponteiros `anterior->proximo = atual->proximo` + `free()` | Remoção na coleção do banco de sangue gerenciada pelo JPA | $O(n)$ |
| **Destruição** | `lista_estoque_destruir()` com laço de `free()` em cada nó | Coleta automática pelo Garbage Collector da JVM | $O(n)$ no C / Automático no Java |

---

### Estrutura 2: Requisições Hospitalares (Fila FIFO)

#### Código em C (`c/fila_requisicoes.h` e `c/fila_requisicoes.c`)
```c
typedef struct NoRequisicao {
    RequisicaoHospitalar dado;
    struct NoRequisicao* proximo;
} NoRequisicao;

typedef struct {
    NoRequisicao* inicio;
    NoRequisicao* fim;
    int tamanho;
} FilaRequisicoes;

bool fila_requisicoes_enfileirar(FilaRequisicoes* fila, RequisicaoHospitalar req) {
    if (fila == NULL) return false;
    NoRequisicao* novo = (NoRequisicao*) malloc(sizeof(NoRequisicao));
    if (novo == NULL) return false;
    novo->dado = req;
    novo->proximo = NULL;
    if (fila->fim == NULL) {
        fila->inicio = novo;
        fila->fim = novo;
    } else {
        fila->fim->proximo = novo;
        fila->fim = novo;
    }
    fila->tamanho++;
    return true;
}

bool fila_requisicoes_desenfileirar(FilaRequisicoes* fila, RequisicaoHospitalar* req_saida) {
    if (fila == NULL || fila->inicio == NULL) return false;
    NoRequisicao* no_removido = fila->inicio;
    if (req_saida != NULL) *req_saida = no_removido->dado;
    fila->inicio = no_removido->proximo;
    if (fila->inicio == NULL) fila->fim = NULL;
    free(no_removido);
    fila->tamanho--;
    return true;
}
```

#### Código em Java (`com.rotavital.dominio.FilaRequisicoesHospitalares`)
```java
public class FilaRequisicoesHospitalares {
    private final Queue<RequisicaoHospitalar> requisicoes;

    public FilaRequisicoesHospitalares() {
        this.requisicoes = new ArrayDeque<>();
    }

    public void adicionar(RequisicaoHospitalar requisicao) {
        requisicoes.add(Objects.requireNonNull(requisicao, "A requisição não pode ser nula."));
    }

    public Optional<RequisicaoHospitalar> consultarProxima() {
        return Optional.ofNullable(requisicoes.peek());
    }

    public Optional<RequisicaoHospitalar> retirarProxima() {
        return Optional.ofNullable(requisicoes.poll());
    }

    public List<RequisicaoHospitalar> listar() {
        return List.copyOf(requisicoes);
    }
}
```

#### 📝 Justificativa e Equivalência Amarrada ao Código Real (Opção B)
> **Parágrafo de Justificativa:**
> Na versão em C, a garantia de tempo constante $O(1)$ na inserção (*enqueue*) e na retirada (*dequeue*) da fila de requisições é alcançada mantendo dois ponteiros explícitos no descritor: `inicio` (para retirada na cabeça) e `fim` (para encadeamento na cauda). Cada nó `NoRequisicao` é alocado individualmente na Heap e descartado com `free(no_removido)`. Na versão em Java, a classe `FilaRequisicoesHospitalares` encapsula rigorosamente essa mesma invariante temporal e semântica FIFO utilizando internamente um `ArrayDeque`, a estrutura canônica de fila de maior desempenho da biblioteca padrão Java. Optou-se por essa abordagem em POO para evitar o consumo excessivo de memória decorrente da sobrecarga de cabeçalhos de objetos de nós encadeados na JVM, garantindo simultaneamente segurança contra nulos através do uso moderno de `Optional<RequisicaoHospitalar>` e `Objects.requireNonNull()`. Para proteger o estado interno contra mutações indevidas por chamadores externos no Spring Boot, o método `listar()` devolve uma cópia imutável gerada por `List.copyOf(requisicoes)`, preservando a integridade da fila sem abrir mão da performance nativa.

| Operação | Implementação em C | Reimplementação em Java | Complexidade Temporal |
| :--- | :--- | :--- | :---: |
| **Inicialização** | `fila_requisicoes_criar()` com `inicio = NULL, fim = NULL` | Construtor `new FilaRequisicoesHospitalares()` com `new ArrayDeque<>()` | $O(1)$ |
| **Enfileirar (Enqueue)** | Aloca `NoRequisicao`, costura em `fim->proximo`, avança `fim` | `adicionar(requisicao)` que chama `requisicoes.add(...)` | $O(1)$ |
| **Consultar Início (Peek)** | `fila_requisicoes_consultar_proxima()` lê `inicio->dado` | `consultarProxima()` que retorna `Optional.ofNullable(requisicoes.peek())` | $O(1)$ |
| **Desenfileirar (Dequeue)**| Move `inicio = inicio->proximo` e executa `free(no_removido)` | `retirarProxima()` que executa `requisicoes.poll()` dentro de `Optional` | $O(1)$ |
| **Destruição** | `fila_requisicoes_destruir()` liberando cada nó em laço | Objeto elegível ao Garbage Collector ao perder referência | $O(n)$ no C / Automático no Java |

---

### Estrutura 3: Histórico de Operações (Pilha LIFO)

#### Código em C (`c/pilha_operacoes.h` e `c/pilha_operacoes.c`)
```c
typedef struct NoOperacao {
    OperacaoHistorico dado;
    struct NoOperacao* proximo;
} NoOperacao;

typedef struct {
    NoOperacao* topo;
    int tamanho;
} PilhaOperacoes;

bool pilha_operacoes_empilhar(PilhaOperacoes* pilha, OperacaoHistorico op) {
    if (pilha == NULL) return false;
    NoOperacao* novo = (NoOperacao*) malloc(sizeof(NoOperacao));
    if (novo == NULL) return false;
    novo->dado = op;
    novo->proximo = pilha->topo; // Encadeia o topo anterior
    pilha->topo = novo;          // Novo nó se torna o topo
    pilha->tamanho++;
    return true;
}

bool pilha_operacoes_desempilhar(PilhaOperacoes* pilha, OperacaoHistorico* op_saida) {
    if (pilha == NULL || pilha->topo == NULL) return false;
    NoOperacao* no_removido = pilha->topo;
    if (op_saida != NULL) *op_saida = no_removido->dado;
    pilha->topo = no_removido->proximo;
    free(no_removido);
    pilha->tamanho--;
    return true;
}
```

#### Código em Java (`com.rotavital.dominio.PilhaOperacoes<T>`)
```java
public class PilhaOperacoes<T> {
    private final Deque<T> operacoes;

    public PilhaOperacoes() {
        this.operacoes = new ArrayDeque<>();
    }

    public void registrar(T operacao) {
        operacoes.push(Objects.requireNonNull(operacao, "A operação não pode ser nula."));
    }

    public Optional<T> consultarMaisRecente() {
        return Optional.ofNullable(operacoes.peek());
    }

    public Optional<T> removerMaisRecente() {
        return Optional.ofNullable(operacoes.poll());
    }

    public List<T> listar() {
        return List.copyOf(operacoes);
    }
}
```

#### 📝 Justificativa e Equivalência Amarrada ao Código Real (Opção B)
> **Parágrafo de Justificativa:**
> Na implementação em C, a política LIFO da pilha é garantida pela manipulação direta do ponteiro de cabeça `topo`: a inserção (`push`) aloca dinamicamente `NoOperacao`, atribui `novo->proximo = pilha->topo` e atualiza `topo = novo` em $O(1)$. A retirada (`pop`) extrai os dados, aponta `topo` para `topo->proximo` e libera a memória do nó precedente via `free()`. Na versão em Java, a classe `PilhaOperacoes<T>` parametriza o tipo de dado por meio de *Generics* e implementa a interface `java.util.Deque`, cuja semântica formal com os métodos `push()` e `poll()` espelha exatamente a mesma mecânica de pilha LIFO sem permitir acessos indexados aleatórios. A justificativa para o uso do `ArrayDeque` em vez de uma pilha manual de nós vinculados ancora-se nas recomendações oficiais da documentação do Java (`java.util.Stack` é uma classe legada e depreciada por sincronização desnecessária, enquanto `ArrayDeque` provê alocação contígua e localidade espacial de cache muito superior à fragmentação de ponteiros na memória).

| Operação | Implementação em C | Reimplementação em Java | Complexidade Temporal |
| :--- | :--- | :--- | :---: |
| **Inicialização** | `pilha_operacoes_criar()` com `topo = NULL` | Construtor `new PilhaOperacoes<>()` instanciando `new ArrayDeque<>()` | $O(1)$ |
| **Empilhar (Push)** | Aloca `NoOperacao`, aponta para `topo` e atualiza `topo = novo` | `registrar(op)` invocando `operacoes.push(...)` | $O(1)$ |
| **Consultar Topo (Peek)** | `pilha_operacoes_consultar_topo()` acessando `topo->dado` | `consultarMaisRecente()` retornando `Optional.ofNullable(operacoes.peek())` | $O(1)$ |
| **Desempilhar (Pop)** | Move `topo = topo->proximo` e invoca `free(no_removido)` | `removerMaisRecente()` invocando `operacoes.poll()` em `Optional` | $O(1)$ |
| **Destruição** | `pilha_operacoes_destruir()` desalocando nós via laço | Desalocação automática via Garbage Collector | $O(n)$ no C / Automático no Java |

---

## ⚖️ 3. Síntese Comparativa dos Paradigmas de Programação

A tabela abaixo resume as distinções fundamentais entre as duas linguagens aplicadas às estruturas do **Rota Vital**:

| Dimensão Técnica | Versão em C (Estruturada) | Reimplementação em Java (POO / Spring) |
| :--- | :--- | :--- |
| **Alocação de Memória** | Manual na Heap via `malloc()` e cálculo explícito de bytes com `sizeof`. | Automática na Heap gerenciada pela JVM via operador `new`. |
| **Desalocação de Memória** | Obrigatória e manual via `free()`. Risco de *Memory Leak* ou *Dangling Pointer*. | Automática via *Garbage Collector* com contagem e marcação de alcançabilidade. |
| **Encadeamento** | Explícito através de ponteiros de máquina (`struct No* proximo`). | Implícito através de referências de objetos gerenciadas pelo runtime. |
| **Tratamento de Nulos** | Verificação manual defensiva de ponteiro (`if (ptr == NULL)`). | `Optional<T>` e `Objects.requireNonNull()`, eliminando `NullPointerException`. |
| **Encapsulamento** | Divisão conceitual entre arquivos de cabeçalho (`.h`) e código (`.c`). | Modificadores de visibilidade (`private`, `final`) e imutabilidade (`List.copyOf`). |
| **Extensibilidade** | Structs concretas com enums inteiros. | Tipagem genérica (`<T>`) e enums ricos com métodos de domínio. |
| **Prontidão de Integração** | Funções de biblioteca para serem ligadas estaticamente ou dinamicamente. | Classes prontas para injeção de dependência e consumo por `@Service` do Spring Boot. |

---

## 🧪 4. Validação e Testes Automatizados

Ambas as versões possuem suítes de testes automatizados equivalentes que comprovam o funcionamento das operações básicas (inserir, consultar, remover):

1. **Testes da versão em C (`c/main.c`):**
   * Validações formais com macro `assert()` cobrindo:
     * Inserção de bolsas, busca por ID, filtro de disponíveis e remoção de bolsas no estoque.
     * Enfileiramento e desenfileiramento estrito em ordem FIFO com checagem de esvaziamento.
     * Empilhamento e desempilhamento estrito em ordem LIFO com checagem de esvaziamento.
     * Invocação de `destruir()` em todas as estruturas para certificar que a memória foi integralmente desalocada.
2. **Testes da versão em Java (JUnit 5):**
   * [`EstoqueTest.java`](../backend/src/test/java/com/rotavital/dominio/EstoqueTest.java): Valida inserção, filtragem por tipo de componente/sanguíneo e sincronia com o banco de sangue.
   * [`FilaRequisicoesHospitalaresTest.java`](../backend/src/test/java/com/rotavital/dominio/FilaRequisicoesHospitalaresTest.java): Valida ordem FIFO estrita, imutabilidade da listagem exposta e rejeição de entradas nulas.
   * [`PilhaOperacoesTest.java`](../backend/src/test/java/com/rotavital/dominio/PilhaOperacoesTest.java): Valida ordem LIFO estrita, comportamento em pilha vazia com `Optional.empty()` e imutabilidade de histórico.

---

## 🚀 5. Instruções de Compilação e Execução

### Versão em C
Para compilar e rodar a suíte de testes em C com GCC:
```bash
cd c
gcc -Wall -Wextra -std=c99 -pedantic main.c rotavital_tipos.c lista_estoque.c fila_requisicoes.c pilha_operacoes.c -o rotavital_ed
./rotavital_ed
```

### Versão em Java
Para rodar os testes unitários das estruturas no backend:
```bash
cd backend
mvn test -Dtest="EstoqueTest,FilaRequisicoesHospitalaresTest,PilhaOperacoesTest,BolsaHemocomponenteTest"
```

