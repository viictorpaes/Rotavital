#include <stdlib.h>
#include "fila_requisicoes.h"

FilaRequisicoes* fila_requisicoes_criar(void) {
    FilaRequisicoes* fila = (FilaRequisicoes*) malloc(sizeof(FilaRequisicoes));
    if (fila != NULL) {
        fila->inicio = NULL;
        fila->fim = NULL;
        fila->tamanho = 0;
    }
    return fila;
}

bool fila_requisicoes_enfileirar(FilaRequisicoes* fila, RequisicaoHospitalar req) {
    if (fila == NULL) {
        return false;
    }

    NoRequisicao* novo = (NoRequisicao*) malloc(sizeof(NoRequisicao));
    if (novo == NULL) {
        return false; // Falha no malloc
    }

    novo->dado = req;
    novo->proximo = NULL;

    if (fila->fim == NULL) {
        // Fila anteriormente vazia: início e fim apontam para o novo nó
        fila->inicio = novo;
        fila->fim = novo;
    } else {
        // Encadeia no final da fila e atualiza o ponteiro fim
        fila->fim->proximo = novo;
        fila->fim = novo;
    }

    fila->tamanho++;
    return true;
}

bool fila_requisicoes_desenfileirar(FilaRequisicoes* fila, RequisicaoHospitalar* req_saida) {
    if (fila == NULL || fila->inicio == NULL) {
        return false; // Fila vazia ou nula
    }

    NoRequisicao* no_removido = fila->inicio;

    if (req_saida != NULL) {
        *req_saida = no_removido->dado;
    }

    fila->inicio = no_removido->proximo;

    // Se o início virou NULL, a fila esvaziou; atualiza o fim para NULL
    if (fila->inicio == NULL) {
        fila->fim = NULL;
    }

    free(no_removido); // Liberação de memória do nó desenfileirado
    fila->tamanho--;

    return true;
}

bool fila_requisicoes_consultar_proxima(const FilaRequisicoes* fila, RequisicaoHospitalar* req_saida) {
    if (fila == NULL || fila->inicio == NULL || req_saida == NULL) {
        return false;
    }

    *req_saida = fila->inicio->dado;
    return true;
}

int fila_requisicoes_tamanho(const FilaRequisicoes* fila) {
    return (fila != NULL) ? fila->tamanho : 0;
}

bool fila_requisicoes_esta_vazia(const FilaRequisicoes* fila) {
    return (fila == NULL || fila->inicio == NULL);
}

void fila_requisicoes_destruir(FilaRequisicoes* fila) {
    if (fila == NULL) {
        return;
    }

    NoRequisicao* atual = fila->inicio;
    while (atual != NULL) {
        NoRequisicao* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }

    free(fila);
}

