#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "rotavital_tipos.h"
#include "lista_estoque.h"
#include "fila_requisicoes.h"
#include "pilha_operacoes.h"

void testar_lista_estoque(void) {
    printf("\n==================================================\n");
    printf("[TESTE 1] Lista Encadeada de Estoque (Bolsas)\n");
    printf("==================================================\n");

    ListaEstoque* estoque = lista_estoque_criar();
    assert(estoque != NULL);
    assert(lista_estoque_esta_vazia(estoque));
    assert(lista_estoque_tamanho(estoque) == 0);

    BolsaHemocomponente b1 = {
        .id = "CH-1042",
        .tipo_componente = COMPONENTE_HEMACIAS,
        .tipo_sanguineo = SANGUE_O_POSITIVO,
        .data_coleta = "2026-09-25",
        .data_validade = "2026-11-05",
        .lote = "LOTE-CH1042",
        .volume_ml = 450.0,
        .temperatura_celsius = 4.0,
        .banco_origem_id = "BS-01",
        .status = STATUS_BOLSA_DISPONIVEL
    };

    BolsaHemocomponente b2 = {
        .id = "PL-2030",
        .tipo_componente = COMPONENTE_PLAQUETAS,
        .tipo_sanguineo = SANGUE_A_POSITIVO,
        .data_coleta = "2026-10-01",
        .data_validade = "2026-10-06",
        .lote = "LOTE-PL2030",
        .volume_ml = 300.0,
        .temperatura_celsius = 22.0,
        .banco_origem_id = "BS-01",
        .status = STATUS_BOLSA_DISPONIVEL
    };

    BolsaHemocomponente b3 = {
        .id = "CH-1043",
        .tipo_componente = COMPONENTE_HEMACIAS,
        .tipo_sanguineo = SANGUE_O_POSITIVO,
        .data_coleta = "2026-09-26",
        .data_validade = "2026-11-06",
        .lote = "LOTE-CH1043",
        .volume_ml = 450.0,
        .temperatura_celsius = 4.0,
        .banco_origem_id = "BS-01",
        .status = STATUS_BOLSA_RESERVADA
    };

    // 1. Inserção
    assert(lista_estoque_adicionar(estoque, b1));
    assert(lista_estoque_adicionar(estoque, b2));
    assert(lista_estoque_adicionar(estoque, b3));
    assert(lista_estoque_tamanho(estoque) == 3);
    assert(!lista_estoque_esta_vazia(estoque));
    printf("-> 3 bolsas inseridas com sucesso. Tamanho = %d\n", lista_estoque_tamanho(estoque));

    // 2. Consulta
    BolsaHemocomponente* encontrada = lista_estoque_buscar_por_id(estoque, "CH-1042");
    assert(encontrada != NULL);
    assert(strcmp(encontrada->id, "CH-1042") == 0);
    assert(encontrada->tipo_componente == COMPONENTE_HEMACIAS);
    assert(encontrada->tipo_sanguineo == SANGUE_O_POSITIVO);
    printf("-> Consulta por ID 'CH-1042': Encontrada (%s, %s, %s)\n",
           encontrada->id,
           tipo_componente_para_string(encontrada->tipo_componente),
           tipo_sanguineo_para_string(encontrada->tipo_sanguineo));

    // 3. Filtragem de disponíveis
    BolsaHemocomponente buffer[10];
    int disp = lista_estoque_filtrar_disponiveis(estoque, COMPONENTE_HEMACIAS, SANGUE_O_POSITIVO, buffer, 10);
    assert(disp == 1);
    assert(strcmp(buffer[0].id, "CH-1042") == 0);
    printf("-> Filtragem por disponíveis (HEMACIAS / O+): %d encontrada(s)\n", disp);

    // 4. Remoção
    BolsaHemocomponente removida;
    assert(lista_estoque_remover(estoque, "PL-2030", &removida));
    assert(strcmp(removida.id, "PL-2030") == 0);
    assert(lista_estoque_tamanho(estoque) == 2);
    assert(lista_estoque_buscar_por_id(estoque, "PL-2030") == NULL);
    printf("-> Remoção de 'PL-2030': Sucesso. Novo tamanho = %d\n", lista_estoque_tamanho(estoque));

    // 5. Destruição e liberação de memória
    lista_estoque_destruir(estoque);
    printf("-> Memória da lista de estoque completamente liberada via free(). [OK]\n");
}

void testar_fila_requisicoes(void) {
    printf("\n==================================================\n");
    printf("[TESTE 2] Fila FIFO de Requisições Hospitalares\n");
    printf("==================================================\n");

    FilaRequisicoes* fila = fila_requisicoes_criar();
    assert(fila != NULL);
    assert(fila_requisicoes_esta_vazia(fila));
    assert(fila_requisicoes_tamanho(fila) == 0);

    RequisicaoHospitalar req1 = {
        .id = "REQ-001",
        .hospital_id = "HOSP-01",
        .hospital_nome = "Hospital da Restauracao",
        .tipo_componente = COMPONENTE_HEMACIAS,
        .tipo_sanguineo = SANGUE_O_POSITIVO,
        .quantidade = 2,
        .data_solicitacao = "2026-10-06",
        .status = STATUS_REQ_PENDENTE
    };

    RequisicaoHospitalar req2 = {
        .id = "REQ-002",
        .hospital_id = "HOSP-02",
        .hospital_nome = "Hospital das Clinicas",
        .tipo_componente = COMPONENTE_PLASMA,
        .tipo_sanguineo = SANGUE_A_NEGATIVO,
        .quantidade = 1,
        .data_solicitacao = "2026-10-06",
        .status = STATUS_REQ_PENDENTE
    };

    // 1. Enfileirar (Enqueue)
    assert(fila_requisicoes_enfileirar(fila, req1));
    assert(fila_requisicoes_enfileirar(fila, req2));
    assert(fila_requisicoes_tamanho(fila) == 2);
    printf("-> 2 requisições enfileiradas. Tamanho = %d\n", fila_requisicoes_tamanho(fila));

    // 2. Consultar Início (Peek)
    RequisicaoHospitalar proxima;
    assert(fila_requisicoes_consultar_proxima(fila, &proxima));
    assert(strcmp(proxima.id, "REQ-001") == 0);
    printf("-> Consultar próxima (Peek): %s (%s, %s)\n",
           proxima.id, proxima.hospital_nome, tipo_componente_para_string(proxima.tipo_componente));

    // 3. Desenfileirar em Ordem FIFO (Dequeue)
    RequisicaoHospitalar atendida1;
    assert(fila_requisicoes_desenfileirar(fila, &atendida1));
    assert(strcmp(atendida1.id, "REQ-001") == 0);
    printf("-> Desenfileirada 1a (FIFO): %s. Restantes: %d\n", atendida1.id, fila_requisicoes_tamanho(fila));

    RequisicaoHospitalar atendida2;
    assert(fila_requisicoes_desenfileirar(fila, &atendida2));
    assert(strcmp(atendida2.id, "REQ-002") == 0);
    printf("-> Desenfileirada 2a (FIFO): %s. Restantes: %d\n", atendida2.id, fila_requisicoes_tamanho(fila));

    assert(fila_requisicoes_esta_vazia(fila));
    assert(!fila_requisicoes_desenfileirar(fila, &proxima)); // Fila vazia

    // 4. Destruição
    fila_requisicoes_destruir(fila);
    printf("-> Memória da fila de requisições liberada com sucesso via free(). [OK]\n");
}

void testar_pilha_operacoes(void) {
    printf("\n==================================================\n");
    printf("[TESTE 3] Pilha LIFO de Histórico de Operações\n");
    printf("==================================================\n");

    PilhaOperacoes* pilha = pilha_operacoes_criar();
    assert(pilha != NULL);
    assert(pilha_operacoes_esta_vazia(pilha));
    assert(pilha_operacoes_tamanho(pilha) == 0);

    OperacaoHistorico op1 = {
        .id = "OP-101",
        .tipo_operacao = "CRIAR_REQUISICAO",
        .detalhes = "Requisicao REQ-001 criada para Hospital da Restauracao",
        .timestamp = "2026-10-06 08:30:00"
    };

    OperacaoHistorico op2 = {
        .id = "OP-102",
        .tipo_operacao = "RESERVAR_BOLSA",
        .detalhes = "Bolsa CH-1043 reservada",
        .timestamp = "2026-10-06 08:35:10"
    };

    // 1. Empilhar (Push)
    assert(pilha_operacoes_empilhar(pilha, op1));
    assert(pilha_operacoes_empilhar(pilha, op2));
    assert(pilha_operacoes_tamanho(pilha) == 2);
    printf("-> 2 operações empilhadas. Tamanho = %d\n", pilha_operacoes_tamanho(pilha));

    // 2. Consultar Topo (Peek)
    OperacaoHistorico topo;
    assert(pilha_operacoes_consultar_topo(pilha, &topo));
    assert(strcmp(topo.id, "OP-102") == 0);
    printf("-> Operação mais recente no topo (Peek): %s - %s\n", topo.id, topo.tipo_operacao);

    // 3. Desempilhar em Ordem LIFO (Pop)
    OperacaoHistorico desempilhada1;
    assert(pilha_operacoes_desempilhar(pilha, &desempilhada1));
    assert(strcmp(desempilhada1.id, "OP-102") == 0);
    printf("-> Desempilhada 1a (LIFO): %s (%s). Restantes: %d\n",
           desempilhada1.id, desempilhada1.tipo_operacao, pilha_operacoes_tamanho(pilha));

    OperacaoHistorico desempilhada2;
    assert(pilha_operacoes_desempilhar(pilha, &desempilhada2));
    assert(strcmp(desempilhada2.id, "OP-101") == 0);
    printf("-> Desempilhada 2a (LIFO): %s (%s). Restantes: %d\n",
           desempilhada2.id, desempilhada2.tipo_operacao, pilha_operacoes_tamanho(pilha));

    assert(pilha_operacoes_esta_vazia(pilha));
    assert(!pilha_operacoes_desempilhar(pilha, &topo)); // Pilha vazia

    // 4. Destruição
    pilha_operacoes_destruir(pilha);
    printf("-> Memória da pilha de operações liberada com sucesso via free(). [OK]\n");
}

int main(void) {
    printf("##################################################\n");
    printf("   ROTA VITAL - ESTRUTURAS DE DADOS EM C (AV1)    \n");
    printf("   Projeto Integrador - Unidade 1                 \n");
    printf("##################################################\n");

    testar_lista_estoque();
    testar_fila_requisicoes();
    testar_pilha_operacoes();

    printf("\n>>> TODOS OS TESTES EM C EXECUTADOS COM SUCESSO! <<<\n");
    return EXIT_SUCCESS;
}

