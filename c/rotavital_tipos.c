#include "rotavital_tipos.h"

const char* tipo_componente_para_string(TipoComponente tipo) {
    switch (tipo) {
        case COMPONENTE_HEMACIAS: return "HEMACIAS";
        case COMPONENTE_PLASMA: return "PLASMA";
        case COMPONENTE_PLAQUETAS: return "PLAQUETAS";
        case COMPONENTE_CRIOPRECIPITADO: return "CRIOPRECIPITADO";
        default: return "DESCONHECIDO";
    }
}

const char* tipo_sanguineo_para_string(TipoSanguineo tipo) {
    switch (tipo) {
        case SANGUE_A_POSITIVO: return "A+";
        case SANGUE_A_NEGATIVO: return "A-";
        case SANGUE_B_POSITIVO: return "B+";
        case SANGUE_B_NEGATIVO: return "B-";
        case SANGUE_AB_POSITIVO: return "AB+";
        case SANGUE_AB_NEGATIVO: return "AB-";
        case SANGUE_O_POSITIVO: return "O+";
        case SANGUE_O_NEGATIVO: return "O-";
        default: return "DESCONHECIDO";
    }
}

const char* status_bolsa_para_string(StatusBolsa status) {
    switch (status) {
        case STATUS_BOLSA_DISPONIVEL: return "DISPONIVEL";
        case STATUS_BOLSA_RESERVADA: return "RESERVADA";
        case STATUS_BOLSA_EM_TRANSITO: return "EM_TRANSITO";
        case STATUS_BOLSA_DESCARTADA: return "DESCARTADA";
        default: return "DESCONHECIDO";
    }
}

const char* status_requisicao_para_string(StatusRequisicao status) {
    switch (status) {
        case STATUS_REQ_PENDENTE: return "PENDENTE";
        case STATUS_REQ_ALOCADA: return "ALOCADA";
        case STATUS_REQ_ATENDIDA: return "ATENDIDA";
        case STATUS_REQ_CANCELADA: return "CANCELADA";
        default: return "DESCONHECIDO";
    }
}

