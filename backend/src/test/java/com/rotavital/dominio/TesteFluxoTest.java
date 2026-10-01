package com.rotavital.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TesteFluxoTest
{
    @Test
    void alocaBolsaCompativelComMenorPrazoDeValidade()
    {
        BancoDeSangue banco = criarBanco();
        Hospital hospital = criarHospital();
        BolsaHemocomponente maisProximaDoVencimento = criarBolsa(
                banco, "BOLSA-001", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.of(2030, 11, 15));
        BolsaHemocomponente maisDistanteDoVencimento = criarBolsa(
                banco, "BOLSA-002", TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                LocalDate.of(2030, 12, 10));
        BolsaHemocomponente incompativel = criarBolsa(
                banco, "BOLSA-003", TipoComponente.PLASMA, TipoSanguineo.O_POSITIVO,
                LocalDate.of(2030, 11, 1));
        banco.getEstoque().adicionarBolsa(maisProximaDoVencimento);
        banco.getEstoque().adicionarBolsa(maisDistanteDoVencimento);
        banco.getEstoque().adicionarBolsa(incompativel);

        RequisicaoHospitalar requisicao = hospital.solicitar(
                TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 1);

        assertSame(hospital, requisicao.getHospital());
        assertEquals(StatusRequisicao.PENDENTE, requisicao.getStatus());
        List<BolsaHemocomponente> compativeis = banco.getEstoque().buscarDisponiveis(
                requisicao.getTipoComponente(), requisicao.getTipoSanguineo());
        BolsaHemocomponente escolhida = compativeis.stream()
                .min((a, b) -> a.getDataValidade().compareTo(b.getDataValidade()))
                .orElseThrow();

        escolhida.reservar();
        requisicao.marcarComoAlocada();

        assertSame(maisProximaDoVencimento, escolhida);
        assertEquals(StatusBolsa.RESERVADA, escolhida.getStatus());
        assertEquals(StatusBolsa.DISPONIVEL, maisDistanteDoVencimento.getStatus());
        assertEquals(StatusBolsa.DISPONIVEL, incompativel.getStatus());
        assertEquals(StatusRequisicao.ALOCADA, requisicao.getStatus());
    }

    @Test
    void mantemRequisicaoPendenteQuandoNaoHaBolsaCompativel()
    {
        BancoDeSangue banco = criarBanco();
        Hospital hospital = criarHospital();
        RequisicaoHospitalar requisicao = hospital.solicitar(
                TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO, 1);

        assertTrue(banco.getEstoque().buscarDisponiveis(
                requisicao.getTipoComponente(), requisicao.getTipoSanguineo()).isEmpty());
        assertEquals(StatusRequisicao.PENDENTE, requisicao.getStatus());
        assertSame(requisicao, hospital.consultarProximaRequisicao().orElseThrow());
    }

    private BancoDeSangue criarBanco()
    {
        return new BancoDeSangue(
                "BS-01",
                "Hemope Central",
                new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829));
    }

    private Hospital criarHospital()
    {
        return new Hospital(
                "HOSP-01",
                "Hospital das Clinicas",
                new Endereco("Rua das Flores, 500 - Recife/PE", -8.0476, -34.8770));
    }

    private BolsaHemocomponente criarBolsa(
            BancoDeSangue banco,
            String id,
            TipoComponente componente,
            TipoSanguineo tipoSanguineo,
            LocalDate validade)
    {
        return new BolsaHemocomponente(
                id,
                componente,
                tipoSanguineo,
                validade.minusDays(10),
                validade,
                450.0,
                4.0,
                "R1 · P1 · N1",
                banco);
    }
}
