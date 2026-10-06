#ifndef ROTAVITAL_TIPOS_H
#define ROTAVITAL_TIPOS_H

#include <stdbool.h>

#define MAX_ID_LEN 64
#define MAX_STR_LEN 128
#define MAX_LOTE_LEN 64
#define MAX_DATA_LEN 11 // Formato YYYY-MM-DD

/* -------------------------------------------------------------
 * ENUMS DE DOMÍNIO
 * Espelham os Enums do Java:
 * - com.rotavital.dominio.TipoComponente
 * - com.rotavital.dominio.TipoSanguineo
 * - com.rotavital.dominio.StatusBolsa
 * - com.rotavital.dominio.StatusRequisicao
 * ------------------------------------------------------------- */

typedef enum {
    COMPONENTE_HEMACIAS = 0,
    COMPONENTE_PLASMA,
    COMPONENTE_PLAQUETAS,
    COMPONENTE_CRIOPRECIPITADO
} TipoComponente;

typedef enum {
    SANGUE_A_POSITIVO = 0,
    SANGUE_A_NEGATIVO,
    SANGUE_B_POSITIVO,
    SANGUE_B_NEGATIVO,
    SANGUE_AB_POSITIVO,
    SANGUE_AB_NEGATIVO,
    SANGUE_O_POSITIVO,
    SANGUE_O_NEGATIVO
} TipoSanguineo;

typedef enum {
    STATUS_BOLSA_DISPONIVEL = 0,
    STATUS_BOLSA_RESERVADA,
    STATUS_BOLSA_EM_TRANSITO,
    STATUS_BOLSA_DESCARTADA
} StatusBolsa;

typedef enum {
    STATUS_REQ_PENDENTE = 0,
    STATUS_REQ_ALOCADA,
    STATUS_REQ_ATENDIDA,
    STATUS_REQ_CANCELADA
} StatusRequisicao;

/* -------------------------------------------------------------
 * STRUCTS DE DOMÍNIO
 * ------------------------------------------------------------- */

typedef struct {
    char id[MAX_ID_LEN];
    TipoComponente tipo_componente;
    TipoSanguineo tipo_sanguineo;
    char data_coleta[MAX_DATA_LEN];
    char data_validade[MAX_DATA_LEN];
    char lote[MAX_LOTE_LEN];
    double volume_ml;
    double temperatura_celsius;
    char banco_origem_id[MAX_ID_LEN];
    StatusBolsa status;
} BolsaHemocomponente;

typedef struct {
    char id[MAX_ID_LEN];
    char hospital_id[MAX_ID_LEN];
    char hospital_nome[MAX_STR_LEN];
    TipoComponente tipo_componente;
    TipoSanguineo tipo_sanguineo;
    int quantidade;
    char data_solicitacao[MAX_DATA_LEN];
    StatusRequisicao status;
} RequisicaoHospitalar;

typedef struct {
    char id[MAX_ID_LEN];
    char tipo_operacao[MAX_STR_LEN];  // Ex: "CRIAR_REQUISICAO", "RESERVAR_BOLSA"
    char detalhes[MAX_STR_LEN];
    char timestamp[MAX_STR_LEN];
} OperacaoHistorico;

/* Funções utilitárias de apresentação */
const char* tipo_componente_para_string(TipoComponente tipo);
const char* tipo_sanguineo_para_string(TipoSanguineo tipo);
const char* status_bolsa_para_string(StatusBolsa status);
const char* status_requisicao_para_string(StatusRequisicao status);

#endif /* ROTAVITAL_TIPOS_H */

