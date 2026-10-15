#ifndef FILA_REQUISICOES_H
#define FILA_REQUISICOES_H

#include <stdbool.h>
#include "rotavital_tipos.h"

/* -------------------------------------------------------------
 * ESTRUTURA: FILA DINÂMICA (FIFO) DE REQUISIÇÕES HOSPITALARES
 * 
 * Equivalência no domínio Java:
 * - com.rotavital.dominio.FilaRequisicoesHospitalares
 * - Encapsulamento de requisições hospitalares em ordem estrita de chegada
 * ------------------------------------------------------------- */

typedef struct NoRequisicao {
    RequisicaoHospitalar dado;
    struct NoRequisicao* proximo;
} NoRequisicao;

typedef struct {
    NoRequisicao* inicio;
    NoRequisicao* fim;
    int tamanho;
} FilaRequisicoes;

/* Cria e inicializa a fila dinamicamente via malloc */
FilaRequisicoes* fila_requisicoes_criar(void);

/* Enfileira no final da fila (Enqueue - O(1)) */
bool fila_requisicoes_enfileirar(FilaRequisicoes* fila, RequisicaoHospitalar req);

/* Remove do início da fila (Dequeue - O(1)) e libera o nó com free */
bool fila_requisicoes_desenfileirar(FilaRequisicoes* fila, RequisicaoHospitalar* req_saida);

/* Consulta o elemento na cabeça da fila sem removê-lo (Peek - O(1)) */
bool fila_requisicoes_consultar_proxima(const FilaRequisicoes* fila, RequisicaoHospitalar* req_saida);

/* Retorna o tamanho atual da fila */
int fila_requisicoes_tamanho(const FilaRequisicoes* fila);

/* Verifica se a fila está vazia */
bool fila_requisicoes_esta_vazia(const FilaRequisicoes* fila);

/* Libera todos os nós da fila com free() e a estrutura da fila */
void fila_requisicoes_destruir(FilaRequisicoes* fila);

#endif /* FILA_REQUISICOES_H */

