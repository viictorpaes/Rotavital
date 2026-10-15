#include <stdlib.h>
#include "pilha_operacoes.h"

PilhaOperacoes* pilha_operacoes_criar(void) {
    PilhaOperacoes* pilha = (PilhaOperacoes*) malloc(sizeof(PilhaOperacoes));
    if (pilha != NULL) {
        pilha->topo = NULL;
        pilha->tamanho = 0;
    }
    return pilha;
}

bool pilha_operacoes_empilhar(PilhaOperacoes* pilha, OperacaoHistorico op) {
    if (pilha == NULL) {
        return false;
    }

    NoOperacao* novo = (NoOperacao*) malloc(sizeof(NoOperacao));
    if (novo == NULL) {
        return false; // Falha no malloc
    }

    novo->dado = op;
    novo->proximo = pilha->topo; // Encadeia o topo anterior como próximo
    pilha->topo = novo;          // Novo nó passa a ser o topo
    pilha->tamanho++;

    return true;
}

bool pilha_operacoes_desempilhar(PilhaOperacoes* pilha, OperacaoHistorico* op_saida) {
    if (pilha == NULL || pilha->topo == NULL) {
        return false; // Pilha vazia ou nula
    }

    NoOperacao* no_removido = pilha->topo;

    if (op_saida != NULL) {
        *op_saida = no_removido->dado;
    }

    pilha->topo = no_removido->proximo; // Atualiza o topo para o próximo elemento
    free(no_removido);                  // Libera a memória do nó desempilhado
    pilha->tamanho--;

    return true;
}

bool pilha_operacoes_consultar_topo(const PilhaOperacoes* pilha, OperacaoHistorico* op_saida) {
    if (pilha == NULL || pilha->topo == NULL || op_saida == NULL) {
        return false;
    }

    *op_saida = pilha->topo->dado;
    return true;
}

int pilha_operacoes_tamanho(const PilhaOperacoes* pilha) {
    return (pilha != NULL) ? pilha->tamanho : 0;
}

bool pilha_operacoes_esta_vazia(const PilhaOperacoes* pilha) {
    return (pilha == NULL || pilha->topo == NULL);
}

void pilha_operacoes_destruir(PilhaOperacoes* pilha) {
    if (pilha == NULL) {
        return;
    }

    NoOperacao* atual = pilha->topo;
    while (atual != NULL) {
        NoOperacao* proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }

    free(pilha);
}

