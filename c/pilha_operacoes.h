#ifndef PILHA_OPERACOES_H
#define PILHA_OPERACOES_H

#include <stdbool.h>
#include "rotavital_tipos.h"

/* -------------------------------------------------------------
 * ESTRUTURA: PILHA DINÂMICA (LIFO) DE HISTÓRICO DE OPERAÇÕES
 * 
 * Equivalência no domínio Java:
 * - com.rotavital.dominio.PilhaOperacoes<T>
 * - Rastreamento e auditoria reversível de eventos
 * ------------------------------------------------------------- */

typedef struct NoOperacao {
    OperacaoHistorico dado;
    struct NoOperacao* proximo;
} NoOperacao;

typedef struct {
    NoOperacao* topo;
    int tamanho;
} PilhaOperacoes;

/* Cria e inicializa a pilha dinamicamente via malloc */
PilhaOperacoes* pilha_operacoes_criar(void);

/* Empilha no topo da pilha (Push - O(1)) */
bool pilha_operacoes_empilhar(PilhaOperacoes* pilha, OperacaoHistorico op);

/* Desempilha do topo da pilha (Pop - O(1)) e libera a memória do nó com free */
bool pilha_operacoes_desempilhar(PilhaOperacoes* pilha, OperacaoHistorico* op_saida);

/* Consulta a operação no topo da pilha sem desempilhar (Peek - O(1)) */
bool pilha_operacoes_consultar_topo(const PilhaOperacoes* pilha, OperacaoHistorico* op_saida);

/* Retorna o tamanho atual da pilha */
int pilha_operacoes_tamanho(const PilhaOperacoes* pilha);

/* Verifica se a pilha está vazia */
bool pilha_operacoes_esta_vazia(const PilhaOperacoes* pilha);

/* Libera todos os nós da pilha com free() e a estrutura da pilha */
void pilha_operacoes_destruir(PilhaOperacoes* pilha);

#endif /* PILHA_OPERACOES_H */

