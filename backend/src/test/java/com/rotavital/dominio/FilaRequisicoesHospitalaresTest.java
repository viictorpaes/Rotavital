package com.rotavital.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class FilaRequisicoesHospitalaresTest
{
    @Test
    void adicionaEProcessaRequisicoesEmOrdemFifo()
    {
        Hospital hospital = criarHospital();
        FilaRequisicoesHospitalares fila = new FilaRequisicoesHospitalares();
        RequisicaoHospitalar primeira = criarRequisicao(hospital);
        RequisicaoHospitalar segunda = criarRequisicao(hospital);

        fila.adicionar(primeira);
        fila.adicionar(segunda);

        assertSame(primeira, fila.consultarProxima().orElseThrow());
        assertSame(primeira, fila.retirarProxima().orElseThrow());
        assertSame(segunda, fila.consultarProxima().orElseThrow());
        assertSame(segunda, fila.retirarProxima().orElseThrow());
        assertTrue(fila.consultarProxima().isEmpty());
        assertTrue(fila.retirarProxima().isEmpty());
    }

    @Test
    void listagemNaoPermiteAlterarFila()
    {
        Hospital hospital = criarHospital();
        FilaRequisicoesHospitalares fila = new FilaRequisicoesHospitalares();
        RequisicaoHospitalar requisicao = criarRequisicao(hospital);
        fila.adicionar(requisicao);

        List<RequisicaoHospitalar> requisicoes = fila.listar();

        assertThrows(UnsupportedOperationException.class, requisicoes::clear);
        assertEquals(List.of(requisicao), fila.listar());
    }

    @Test
    void naoPermiteAdicionarRequisicaoNula()
    {
        FilaRequisicoesHospitalares fila = new FilaRequisicoesHospitalares();

        assertThrows(NullPointerException.class, () -> fila.adicionar(null));
        assertTrue(fila.listar().isEmpty());
    }

    @Test
    void hospitalRegistraRequisicoesNaFilaSemExporColecaoInterna()
    {
        Hospital hospital = criarHospital();
        RequisicaoHospitalar primeira = hospital.solicitar(
                TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 2);
        RequisicaoHospitalar segunda = hospital.solicitar(
                TipoComponente.PLASMA, TipoSanguineo.A_NEGATIVO, 1);

        assertSame(hospital, primeira.getHospital());
        assertSame(hospital, segunda.getHospital());
        assertEquals(StatusRequisicao.PENDENTE, primeira.getStatus());
        assertEquals(StatusRequisicao.PENDENTE, segunda.getStatus());
        assertEquals(List.of(primeira, segunda), hospital.getRequisicoes());
        assertSame(primeira, hospital.consultarProximaRequisicao().orElseThrow());
        assertSame(primeira, hospital.retirarProximaRequisicao().orElseThrow());
        assertSame(segunda, hospital.consultarProximaRequisicao().orElseThrow());
        assertFalse(hospital.getRequisicoes().contains(primeira));
        assertThrows(UnsupportedOperationException.class, () -> hospital.getRequisicoes().clear());
    }

    private Hospital criarHospital()
    {
        return new Hospital(
                "HOSP-01",
                "Hospital das Clinicas",
                new Endereco("Rua das Flores, 500 - Recife/PE", -8.0476, -34.8770));
    }

    private RequisicaoHospitalar criarRequisicao(Hospital hospital)
    {
        return hospital.solicitar(TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 1);
    }
}
