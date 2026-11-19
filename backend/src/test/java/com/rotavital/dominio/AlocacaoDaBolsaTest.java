package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// HU-04 — A bolsa reservada por uma requisição passa a mostrar a ala de destino, e leva essa
// ala junto no transporte e no recebimento pela tela de Rede.
public class AlocacaoDaBolsaTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 4);
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 4, 10, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final Trajeto TRAJETO = new Trajeto(9.4, 21, FonteDoTrajeto.GOOGLE_ROUTES);

    private final BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central",
            new Endereco("Av. Central, 100", -8.0578, -34.8829));
    private final Hospital clinicas = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235", -8.0512, -34.9478));
    private final Hospital portugues = new Hospital("real-portugues", "Real Hospital Português",
            new Endereco("Av. Agamenon Magalhães, 4760", -8.0447, -34.8990));
    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, null, null);

    private BolsaHemocomponente bolsa(double temperatura, LocalDate validade)
    {
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                validade.minusDays(30), validade, "LOTE-1", 450, temperatura, "R1 · P3 · N2", hemope);
    }

    @Test
    public void localizacaoDoDestinoTemHospitalAlaELeito()
    {
        Assertions.assertEquals("Hospital das Clínicas de PE · UTI Adulto · Leito 04",
                AlaHospitalar.UTI_ADULTO.localizacaoNo(clinicas, " 04 "));
        Assertions.assertEquals("Hospital das Clínicas de PE · Centro Cirúrgico",
                AlaHospitalar.CENTRO_CIRURGICO.localizacaoNo(clinicas, "  "));
    }

    @Test
    public void alocarReservaEMudaALocalizacaoParaODestino()
    {
        BolsaHemocomponente bolsa = bolsa(4.0, HOJE.plusDays(10));

        bolsa.alocarPara(AlaHospitalar.UTI_ADULTO.localizacaoNo(clinicas, "04"), HOJE);

        Assertions.assertEquals(StatusBolsa.RESERVADA, bolsa.getStatus());
        Assertions.assertEquals("Hospital das Clínicas de PE · UTI Adulto · Leito 04", bolsa.getLocalizacao());
        Assertions.assertTrue(bolsa.estaReservadaPara(clinicas));
        Assertions.assertFalse(bolsa.estaReservadaPara(portugues));
    }

    @Test
    public void bolsaVencidaOuForaDaFaixaNaoEhAlocada()
    {
        BolsaHemocomponente vencida = bolsa(4.0, HOJE.minusDays(1));
        BolsaHemocomponente quente = bolsa(9.0, HOJE.plusDays(10));
        String destino = AlaHospitalar.EMERGENCIA.localizacaoNo(clinicas, null);

        Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> vencida.alocarPara(destino, HOJE));
        Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> quente.alocarPara(destino, HOJE));

        Assertions.assertEquals(StatusBolsa.DISPONIVEL, vencida.getStatus());
        Assertions.assertEquals("R1 · P3 · N2", vencida.getLocalizacao());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, quente.getStatus());
        Assertions.assertEquals("R1 · P3 · N2", quente.getLocalizacao());
    }

    @Test
    public void bolsaJaAlocadaNaoEhAlocadaDeNovo()
    {
        BolsaHemocomponente bolsa = bolsa(4.0, HOJE.plusDays(10));
        bolsa.alocarPara(AlaHospitalar.UTI_ADULTO.localizacaoNo(clinicas, "04"), HOJE);

        Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> bolsa.alocarPara(AlaHospitalar.ONCOLOGIA.localizacaoNo(portugues, null), HOJE));

        Assertions.assertEquals("Hospital das Clínicas de PE · UTI Adulto · Leito 04", bolsa.getLocalizacao());
    }

    @Test
    public void bolsaAlocadaViajaERecebeComAAlaDoPedido()
    {
        BolsaHemocomponente bolsa = bolsa(4.0, HOJE.plusDays(10));
        bolsa.alocarPara(AlaHospitalar.UTI_NEONATAL.localizacaoNo(clinicas, "12"), HOJE);

        TransferenciaDeBolsa transferencia = TransferenciaDeBolsa.despachar(bolsa, clinicas, TRAJETO, medico, AGORA);

        Assertions.assertEquals(StatusBolsa.EM_TRANSITO, bolsa.getStatus());
        Assertions.assertEquals("Em transporte refrigerado → Hospital das Clínicas de PE · UTI Neonatal · Leito 12",
                bolsa.getLocalizacao());

        transferencia.receber(medico, AGORA.plusMinutes(25));

        Assertions.assertEquals(StatusBolsa.ENTREGUE, bolsa.getStatus());
        Assertions.assertEquals(clinicas, bolsa.getHospitalAtual());
        Assertions.assertEquals("Hospital das Clínicas de PE · UTI Neonatal · Leito 12", bolsa.getLocalizacao());
    }

    @Test
    public void bolsaAlocadaParaUmHospitalNaoSaiParaOutro()
    {
        BolsaHemocomponente bolsa = bolsa(4.0, HOJE.plusDays(10));
        bolsa.alocarPara(AlaHospitalar.MATERNIDADE.localizacaoNo(clinicas, null), HOJE);

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> TransferenciaDeBolsa.despachar(bolsa, portugues, TRAJETO, medico, AGORA));

        Assertions.assertTrue(erro.getMessage().contains("RESERVADA"), erro.getMessage());
        Assertions.assertEquals("Hospital das Clínicas de PE · Maternidade", bolsa.getLocalizacao());
    }
}
