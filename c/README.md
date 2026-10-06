# Rota Vital — Módulo de Estruturas de Dados em C (AV1)

Este diretório contém a implementação original em linguagem C das estruturas de dados fundamentais do domínio **Rota Vital** para a entrega da **Unidade 1 (AV1) do Projeto Integrador**:

1. **Estoque de Hemocomponentes:** Lista Encadeada Simples com alocação manual (`malloc`/`free`) e manipulação explícita de ponteiros.
2. **Requisições Hospitalares:** Fila Dinâmica (Queue FIFO) com ponteiros para início e fim.
3. **Histórico de Operações:** Pilha Dinâmica (Stack LIFO) com ponteiro para o topo.

---

## 📁 Arquitetura dos Arquivos

* `rotavital_tipos.h` e `rotavital_tipos.c`: Definição dos enums (`TipoComponente`, `TipoSanguineo`, `StatusBolsa`, `StatusRequisicao`) e das structs de domínio (`BolsaHemocomponente`, `RequisicaoHospitalar`, `OperacaoHistorico`).
* `lista_estoque.h` e `lista_estoque.c`: Estrutura de Lista Encadeada (`NoBolsa`, `ListaEstoque`) e operações fundamentais (`criar`, `adicionar`, `buscar_por_id`, `remover`, `filtrar_disponiveis`, `destruir`).
* `fila_requisicoes.h` e `fila_requisicoes.c`: Estrutura de Fila FIFO (`NoRequisicao`, `FilaRequisicoes`) e operações fundamentais (`criar`, `enfileirar`, `desenfileirar`, `consultar_proxima`, `destruir`).
* `pilha_operacoes.h` e `pilha_operacoes.c`: Estrutura de Pilha LIFO (`NoOperacao`, `PilhaOperacoes`) e operações fundamentais (`criar`, `empilhar`, `desempilhar`, `consultar_topo`, `destruir`).
* `main.c`: Programa de testes automatizados com `assert()` validando todas as operações das três estruturas e garantindo que não haja vazamentos de memória (memory leaks).
* `Makefile`: Automação da compilação com flags `-Wall -Wextra -std=c99 -pedantic`.

---

## 🚀 Como Compilar e Executar

### Usando GCC (Linux / macOS / MinGW no Windows)
```bash
gcc -Wall -Wextra -std=c99 -pedantic main.c rotavital_tipos.c lista_estoque.c fila_requisicoes.c pilha_operacoes.c -o rotavital_ed
./rotavital_ed
```

### Usando Makefile
```bash
make
make run
```

