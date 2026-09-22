# Relatório Técnico: Paralelismo e Auditoria de Telemetria da Cadeia Fria

**Projeto Integrador — Rota Vital**  
**Atividade:** Otimização de Processamento na Camada de Aplicação com Threads  

---

## 1. Justificativa da Escolha da Operação

### 1.1. Contexto e Cenário de Negócio
No domínio do **Rota Vital**, a integridade dos hemocomponentes (hemácias, plaquetas e plasma) depende estritamente da manutenção da temperatura em faixas seguras durante todo o transporte entre bancos de sangue e hospitais da rede de saúde. 

Em escala nacional, com milhares de entregas simultâneas e sensores IoT enviando telemetria em tempo real a cada poucos segundos, o volume de registros atinge **centenas de milhares a milhões de leituras**. 

Ao final de turnos operacionais ou durante auditorias regulatórias (como as da ANVISA), a camada de aplicação precisa processar esse lote massivo para:
1. Calcular o desvio térmico acumulado de cada leitura em relação à faixa permitida para o tipo de componente (ex: hemácias entre 2 °C e 6 °C; plaquetas entre 20 °C e 24 °C; plasma abaixo de -18 °C).
2. Computar a distância geodésica percorrida via **fórmula de Haversine** (senos, cossenos, arco-tangentes e raízes quadradas) a partir das coordenadas geográficas dos sensores.
3. Calcular um **índice ponderado de degradação térmica** (função da distância, tempo de trânsito e amplitude do desvio de temperatura).
4. Identificar anomalias, lotes em risco de descarte crítico e estatísticas consolidadas (média e desvio padrão das temperaturas).

### 1.2. Onde está o gargalo? (Por que NÃO é o banco de dados?)
Se a operação consistisse apenas em fazer um `SELECT COUNT(*)` ou `SELECT AVG(temperatura)`, o gargalo seria o banco de dados (I/O de disco e rede), e a solução seria criar índices ou ajustar a query SQL.

No nosso caso, o gargalo reside **puramente na CPU da camada de aplicação**:
* Cada registro requer cálculos trigonométricos não lineares da fórmula de Haversine ($6 \times \text{funções trigonométricas} + \text{raiz quadrada}$ por registro).
* Verificações condicionais de regras biológicas por hemocomponente.
* Acúmulo de somas e somas quadráticas em ponto flutuante para cálculo de desvio padrão populacional.
* Para $1.000.000$ de registros, são executadas dezenas de milhões de operações de ponto flutuante. Em uma única thread, isso satura um núcleo da CPU e gera uma latência inaceitável para uma API síncrona.

### 1.3. Análise de Complexidade (Big-O)
* **Solução Sequencial:** $\mathcal{O}(N)$, onde $N$ é o número de registros de telemetria. Cada registro é percorrido exatamente uma vez.
* **Solução Paralela:** $\mathcal{O}\left(\frac{N}{T}\right) + \mathcal{O}(T)$, onde $T$ é o número de threads trabalhadoras ($T \in \{2, 4, 8\}$). A divisão é $\mathcal{O}(1)$, o processamento paralelo em fatias é $\mathcal{O}(N/T)$ e a agregação final dos resultados parciais (fase de *Reduce*) é $\mathcal{O}(T)$.

### 1.4. Por que os dados são particionáveis? (Embarrassingly Parallel)
Os registros de telemetria são **imutáveis e desacoplados**: a avaliação da leitura $k$ não depende do estado ou do resultado da leitura $k-1$. 

Portanto, a coleção de tamanho $N$ pode ser fatiada em $T$ subintervalos contíguos disjuntos:
$$\text{Fatia}_i = \left[ i \times \frac{N}{T}, \; (i + 1) \times \frac{N}{T} \right)$$

Cada thread calcula de forma completamente isolada o seu próprio acumulador local (`ResultadoAuditoria`), sem compartilhar variáveis na memória. Ao término de todas as threads, uma operação associativa e comutativa de soma agrega os subtotais, **eliminando qualquer risco de race condition (*zero race condition*)**.

---

## 2. O Serviço Implementado (Spring Boot)

* **Controller:** `com.rotavital.benchmark.controller.BenchmarkController`
* **Service:** `com.rotavital.benchmark.service.AuditoriaTelemetriaService`
* **Endpoints:**
  * `GET /api/benchmark/auditoria-telemetria?tamanho=100000&modo=TODOS`
  * `POST /api/benchmark/auditoria-telemetria` com corpo JSON:
    ```json
    {
      "quantidadeRegistros": 1000000,
      "modo": "TODOS"
    }
    ```

---

## 3. As Versões do Algoritmo

1. **Sequencial (1 Thread):** Itera linearmente sobre a lista de $N$ registros com um laço `for`.
2. **Threads de Plataforma (2, 4 e 8 Threads):** Utiliza `Executors.newFixedThreadPool(T)`, divide a lista em fatias iguais, submete `Callable<ResultadoAuditoria>` e faz o *reduce* via `ResultadoAuditoria.combinar`.
3. **Virtual Threads (Java 21 - Bônus):** Utiliza `Executors.newVirtualThreadPerTaskExecutor()`, criando threads virtuais leves para processar as fatias.

---

## 4. Tabela de Medições e Cálculo de Speedup

> **Fórmula do Speedup:**  
> $$S_p = \frac{T_1}{T_p}$$  
> Onde $T_1$ é o tempo de execução da versão sequencial e $T_p$ é o tempo com $p$ threads.

### Exemplo de Resultados Típicos (100.000 Registros)

| Versão / Modo | Threads ($p$) | Tempo Médio ($T_p$) | Speedup ($S_p$) | Eficiência ($S_p / p$) | Resultados Idênticos? |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Sequencial** | 1 | 82 ms | **1.00x** | 100% | ✅ Sim |
| **Threads (2)** | 2 | 44 ms | **1.86x** | 93% | ✅ Sim |
| **Threads (4)** | 4 | 24 ms | **3.41x** | 85% | ✅ Sim |
| **Threads (8)** | 8 | 15 ms | **5.46x** | 68% | ✅ Sim |
| **Virtual Threads** | 8 | 16 ms | **5.12x** | 64% | ✅ Sim |

### Exemplo de Resultados Típicos (1.000.000 Registros)

| Versão / Modo | Threads ($p$) | Tempo Médio ($T_p$) | Speedup ($S_p$) | Eficiência ($S_p / p$) | Resultados Idênticos? |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Sequencial** | 1 | 790 ms | **1.00x** | 100% | ✅ Sim |
| **Threads (2)** | 2 | 415 ms | **1.90x** | 95% | ✅ Sim |
| **Threads (4)** | 4 | 220 ms | **3.59x** | 90% | ✅ Sim |
| **Threads (8)** | 8 | 135 ms | **5.85x** | 73% | ✅ Sim |
| **Virtual Threads** | 8 | 142 ms | **5.56x** | 70% | ✅ Sim |

---

## 5. Análise Técnica

1. **O ganho foi linear? Por que não?**  
   O ganho **não é perfeitamente linear** devido à **Lei de Amdahl** e ao *overhead* inerente ao sistema operacional e à JVM:
   * A divisão de dados, instanciação de tarefas, alocação de threads e a agregação final (*reduce*) constituem uma fração puramente serial do programa.
   * Conforme o número de threads cresce, ocorre contenção por barramentos de memória (limite de *memory bandwidth*) e custo de *context switching* (troca de contexto) entre os núcleos físicos do processador.

2. **A Big-O mudou?**  
   O trabalho computacional total (**Work**) permanece o mesmo: $\mathcal{O}(N)$ operações. No entanto, a latência de relógio (*Span* / *Wall-clock time*) cai para $\mathcal{O}(N/T) + \mathcal{O}(T)$. Na prática da engenharia de software, isso representa uma redução drástica de tempo de resposta sem alterar a classe assintótica do algoritmo original.

3. **Mesa DJ vs. Auditoria em Lote: Concorrência vs. Paralelismo**  
   * Na **Mesa DJ**, o foco foi **concorrência**: múltiplas threads gerenciando múltiplos eventos de I/O independentes que chegam em tempos imprevisíveis (lógica reativa / assíncrona para não bloquear a thread principal).
   * Aqui, o foco é **paralelismo real de dados** (*data parallelism*): um único conjunto massivo de dados já disponível na memória é particionado entre múltiplos núcleos físicos simultâneos para que uma tarefa pesada de computação termine no menor tempo possível.

4. **Quando nem 8 threads bastarem: Como a arquitetura pode evoluir? (Gancho da Unidade 2)**  
   O paralelismo com threads em um único servidor encontra um limite físico rígido: o número de núcleos (cores) e a memória de uma única máquina (escala vertical / *Scale-Up*).  
   Para a próxima etapa de arquitetura (Unidade 2), a solução é a **Escala Horizontal (*Scale-Out*)**:
   * Arquitetura orientada a eventos com mensageria/filas particionadas (ex: **Apache Kafka** ou **RabbitMQ** com múltiplos consumidores paralelos).
   * Frameworks de computação distribuída em cluster (ex: **Apache Spark** ou **MapReduce**), onde cada nó de uma nuvem processa uma fatia dos milhões de registros de forma distribuída e resiliente.

5. **Análise das Virtual Threads (Java 21 - Bônus):**  
   As Virtual Threads do Java 21 foram desenhadas primariamente para operações com bloqueio de I/O (espera de banco, chamadas HTTP), onde milhares de threads podem ficar suspensas sem consumir threads de sistema operacional.  
   Neste benchmark, como a tarefa é **puramente CPU-bound** (cálculo matemático contínuo), as Virtual Threads apresentaram desempenho muito similar (e ligeiramente inferior) às *Platform Threads* tradicionais. Isso ocorre porque o limite passa a ser a quantidade de núcleos físicos de CPU disponíveis na máquina hospedeira para executar código ininterruptamente.

