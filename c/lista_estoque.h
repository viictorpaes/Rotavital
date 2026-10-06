#ifndef LISTA_ESTOQUE_H
#define LISTA_ESTOQUE_H

#include <stdbool.h>
#include "rotavital_tipos.h"

/* -------------------------------------------------------------
 * ESTRUTURA: LISTA ENCADEADA SIMPLES DE BOLSAS DE ESTOQUE
 * 
 * Equivalência no domínio Java:
 * - com.rotavital.dominio.Estoque
 * - Gerenciamento de coleções de com.rotavital.dominio.BolsaHemocomponente
 * ------------------------------------------------------------- */

typedef struct NoBolsa {
    BolsaHemocomponente dado;
    struct NoBolsa* proximo;
} NoBolsa;

typedef struct {
    NoBolsa* inicio;
    int tamanho;
} ListaEstoque;

/* Cria e inicializa a lista alocando memória dinamicamente via malloc */
ListaEstoque* lista_estoque_criar(void);

/* Insere uma nova bolsa no início da lista encadeada (O(1)) */
bool lista_estoque_adicionar(ListaEstoque* lista, BolsaHemocomponente bolsa);

/* Busca linear por ID percorrendo os ponteiros proximo (O(n)) */
BolsaHemocomponente* lista_estoque_buscar_por_id(const ListaEstoque* lista, const char* id);

/* Remove um nó da lista por ID ajustando os ponteiros e liberando memória com free (O(n)) */
bool lista_estoque_remover(ListaEstoque* lista, const char* id, BolsaHemocomponente* removida);

/* Filtra bolsas disponíveis por tipo de componente e tipo sanguíneo */
int lista_estoque_filtrar_disponiveis(const ListaEstoque* lista, 
                                     TipoComponente componente, 
                                     TipoSanguineo sangue, 
                                     BolsaHemocomponente* buffer_saida, 
                                     int capacidade_max);

/* Retorna a quantidade de elementos na lista */
int lista_estoque_tamanho(const ListaEstoque* lista);

/* Verifica se a lista está vazia */
bool lista_estoque_esta_vazia(const ListaEstoque* lista);

/* Libera todos os nós individualmente com free() e a estrutura descritora */
void lista_estoque_destruir(ListaEstoque* lista);

#endif /* LISTA_ESTOQUE_H */

