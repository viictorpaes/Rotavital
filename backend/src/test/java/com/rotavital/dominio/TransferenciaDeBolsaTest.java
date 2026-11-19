package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Saída e chegada de uma bolsa pela tela de Rede (PI3-236).
public class TransferenciaDeBolsaTest
{
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 4, 10, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final Trajeto TRAJETO = new Trajeto(9.4, 21, FonteDoTrajeto.GOOGLE_ROUTES);

    private final BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital clinicas = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);

    private BolsaHemocomponente bolsa;

    private BolsaHemocomponente bolsaA(double temperatura, LocalDate validade)
    {
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                validade.minusDays(30), validade, "LOTE-1", 450, temperatura, "R1 · P3 · N2", hemope);
    }

    @BeforeEach
    public void bolsaDisponivel()
    {
        bolsa = bolsaA(4.0, LocalDate.of(2026, 11, 20));
    }

    @Test
    public void despacharTiraABolsaDoEstoqueERegistraOTrajeto()
    {
        TransferenciaDeBolsa transferencia = TransferenciaDeBolsa.despachar(bolsa, clinicas, TRAJETO, medico, AGORA);

        Assertions.assertEquals(StatusBolsa.EM_TRANSITO, bolsa.getStatus());
        Assertions.assertEquals("Em transporte refrigerado → Hospital das Clínicas de PE", bolsa.getLocalizacao());
        Assertions.assertNull(bolsa.getHospitalAtual(), "Em trânsito, ela ainda não está em hospital nenhum.");

        Assertions.assertEquals(StatusTransferencia.EM_TRANSITO, transferencia.getStatus());
        Assertions.assertEquals(hemope, transferencia.getOrigem());
        Assertions.assertEquals(clinicas, transferencia.getDestino());
        Assertions.assertEquals(9.4, transferencia.getDistanciaKm());
        Assertions.assertEquals(FonteDoTrajeto.GOOGLE_ROUTES, transferencia.getFonteDoTrajeto());
        Assertions.assertEquals(AGORA, transferencia.getDespachadaEm());
        Assertions.assertEquals(AGORA.plusMinutes(21), transferencia.getChegadaPrevistaEm());
        Assertions.assertEquals(medico, transferencia.getDespachadaPor());
    }

    @Test
    public void receberPoeABolsaNoHospitalDeDestino()
    {
        TransferenciaDeBolsa transferencia = TransferenciaDeBolsa.despachar(bolsa, clinicas, TRAJETO, medico, AGORA);

        transferencia.receber(medico, AGORA.plusMinutes(25));

        Assertions.assertEquals(StatusTransferencia.RECEBIDA, transferencia.getStatus());
        Assertions.assertEquals(AGORA.plusMinutes(25), transferencia.getRecebidaEm());
        Assertions.assertEquals(medico, transferencia.getRecebidaPor());
        Assertions.assertEquals(StatusBolsa.ENTREGUE, bolsa.getStatus());
        Assertions.assertEquals(clinicas, bolsa.getHospitalAtual());
        Assertions.assertEquals("Hospital das Clínicas de PE", bolsa.getLocalizacao());
    }

    @Test
    public void naoRecebeDuasVezes()
    {
        TransferenciaDeBolsa transferencia = TransferenciaDeBolsa.despachar(bolsa, clinicas, TRAJETO, medico, AGORA);
        transferencia.receber(medico, AGORA.plusMinutes(25));

        Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> transferencia.receber(medico, AGORA.plusMinutes(26)));
    }

    @Test
    public void bolsaQueVenceuNoCaminhoNaoEhRecebida()
    {
        BolsaHemocomponente vencendoHoje = bolsaA(4.0, AGORA.toLocalDate());
        TransferenciaDeBolsa transferencia = TransferenciaDeBolsa.despachar(vencendoHoje, clinicas, TRAJETO, medico, AGORA);

        Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> transferencia.receber(medico, AGORA.plusDays(1)));

        Assertions.assertEquals(StatusTransferencia.EM_TRANSITO, transferencia.getStatus());
        Assertions.assertNull(vencendoHoje.getHospitalAtual());
    }

    @Test
    public void bolsaIndisponivelNaoSai()
    {
        bolsa.reservar();

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> TransferenciaDeBolsa.despachar(bolsa, clinicas, TRAJETO, medico, AGORA));

        Assertions.assertTrue(erro.getMessage().contains("RESERVADA"), erro.getMessage());
    }

    @Test
    public void bolsaVencidaNaoSaiEContinuaNoEstoque()
    {
        BolsaHemocomponente vencida = bolsaA(4.0, AGORA.toLocalDate().minusDays(1));

        Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> TransferenciaDeBolsa.despachar(vencida, clinicas, TRAJETO, medico, AGORA));

        Assertions.assertEquals(StatusBolsa.DISPONIVEL, vencida.getStatus());
        Assertions.assertEquals("R1 · P3 · N2", vencida.getLocalizacao());
    }

    @Test
    public void bolsaForaDaFaixaNaoSai()
    {
        BolsaHemocomponente quente = bolsaA(9.0, LocalDate.of(2026, 11, 20));

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> TransferenciaDeBolsa.despachar(quente, clinicas, TRAJETO, medico, AGORA));

        Assertions.assertTrue(erro.getMessage().contains("fora da faixa"), erro.getMessage());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, quente.getStatus());
    }
}
