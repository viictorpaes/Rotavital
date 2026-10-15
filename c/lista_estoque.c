#include <stdlib.h>
#include <string.h>
#include "lista_estoque.h"

ListaEstoque* lista_estoque_criar(void) {
    ListaEstoque* lista = (ListaEstoque*) malloc(sizeof(ListaEstoque));
    if (lista != NULL) {
        lista->inicio = NULL;
        lista->tamanho = 0;
    }
    return lista;
}

bool lista_estoque_adicionar(ListaEstoque* lista, BolsaHemocomponente bolsa) {
    if (lista == NULL) {
        return false;
    }

    NoBolsa* novo = (NoBolsa*) malloc(sizeof(NoBolsa));
    if (novo == NULL) {
        return false; // Falha na alocação de memória (malloc)
    }

    novo->dado = bolsa;
    novo->proximo = lista->inicio; // Insere na cabeça da lista
    lista->inicio = novo;
    lista->tamanho++;

    return true;
}

BolsaHemocomponente* lista_estoque_buscar_por_id(const ListaEstoque* lista, const char* id) {
    if (lista == NULL || id == NULL) {
        return NULL;
    }

    NoBolsa* atual = lista->inicio;
    while (atual != NULL) {
        if (strcmp(atual->dado.id, id) == 0) {
            return &(atual->dado);
        }
        atual = atual->proximo;
    }

    return NULL;
}

bool lista_estoque_remover(ListaEstoque* lista, const char* id, BolsaHemocomponente* removida) {
    if (lista == NULL || lista->inicio == NULL || id == NULL) {
        return false;
    }

    NoBolsa* atual = lista->inicio;
    NoBolsa* anterior = NULL;

    while (atual != NULL && strcmp(atual->dado.id, id) != 0) {
        anterior = atual;
        atual = atual->proximo;
    }

    if (atual == NULL) {
        return false; // Elemento não encontrado
    }

    // Se for o primeiro nó
    if (anterior == NULL) {
        lista->inicio = atual->proximo;
    } else {
        anterior->proximo = atual->proximo;
    }

    if (removida != NULL) {
        *removida = atual->dado;
    }

    free(atual); // Liberação explícita da memória alocada
    lista->tamanho--;

    return true;
}

int lista_estoque_filtrar_disponiveis(const ListaEstoque* lista, 
                                     TipoComponente componente, 
                                     TipoSanguineo sangue, 
                                     BolsaHemocomponente* buffer_saida, 
                                     int capacidade_max) {
    if (lista == NULL || buffer_saida == NULL || capacidade_max <= 0) {
        return 0;
    }

    int encontrados = 0;
    NoBolsa* atual = lista->inicio;

    while (atual != NULL && encontrados < capacidade_max) {
        if (atual->dado.status == STATUS_BOLSA_DISPONIVEL &&
            atual->dado.tipo_componente == componente &&
            atual->dado.tipo_sanguineo == sangue) {
            buffer_saida[encontrados++] = atual->dado;
        }
        atual = atual->proximo;
    }

    return encontrados;
}

int lista_estoque_tamanho(const ListaEstoque* lista) {
    return (lista != NULL) ? lista->tamanho : 0;
}

bool lista_estoque_esta_vazia(const ListaEstoque* lista) {
    return (lista == NULL || lista->inicio == NULL);
}

void lista_estoque_destruir(ListaEstoque* lista) {
    if (lista == NULL) {
        return;
    }

    NoBolsa* atual = lista->inicio;
    while (atual != NULL) {
        NoBolsa* proximo = atual->proximo;
        free(atual); // Libera nó por nó
        atual = proximo;
    }

    free(lista); // Libera o descritor da lista
}

