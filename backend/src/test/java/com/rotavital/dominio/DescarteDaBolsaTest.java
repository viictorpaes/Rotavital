package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// PI3-243: o descarte guarda motivo padronizado, observação e data, e a bolsa nunca mais volta ao uso.
public class DescarteDaBolsaTest
{
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 10, 1, 12, 0, 0, 0, ZoneOffset.ofHours(-3));

    private BancoDeSangue banco;

    private BolsaHemocomponente bolsa(int diasAteVencer)
    {
        return new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                HOJE.minusDays(10), HOJE.plusDays(diasAteVencer), "LOTE-0001", 450.0, 4.0, "R1", banco);
    }

    @BeforeEach
    public void criarBanco()
    {
        banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.05, -34.88));
    }

    @Test
    public void descarteRegistraMotivoObservacaoEData()
    {
        BolsaHemocomponente bolsa = bolsa(20);

        bolsa.descartar(CategoriaDescarte.BOLSA_DANIFICADA, "Bolsa rompida na centrífuga", AGORA);

        Assertions.assertEquals(StatusBolsa.DESCARTADA, bolsa.getStatus());
        Assertions.assertEquals(CategoriaDescarte.BOLSA_DANIFICADA, bolsa.getCategoriaDescarte());
        Assertions.assertEquals("Bolsa rompida na centrífuga", bolsa.getMotivoDescarte());
        Assertions.assertEquals(AGORA, bolsa.getDataDescarte());
    }

    @Test
    public void validadeExpiradaValeParaBolsaVencida()
    {
        BolsaHemocomponente vencida = bolsa(-1);

        vencida.descartar(CategoriaDescarte.VALIDADE_EXPIRADA, null, AGORA);

        Assertions.assertEquals(StatusBolsa.DESCARTADA, vencida.getStatus());
        Assertions.assertNull(vencida.getMotivoDescarte());
    }

    @Test
    public void validadeExpiradaERecusadaParaBolsaNaValidade()
    {
        // Vence hoje: ainda vale, então não está "expirada".
        BolsaHemocomponente bolsa = bolsa(0);

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> bolsa.descartar(CategoriaDescarte.VALIDADE_EXPIRADA, null, AGORA));

        Assertions.assertEquals("A bolsa CH-1042 vence em 01/10/2026: o motivo \"Validade expirada\" só vale para bolsa vencida.",
                erro.getMessage());
        Assertions.assertEquals(StatusBolsa.DISPONIVEL, bolsa.getStatus());
        Assertions.assertNull(bolsa.getCategoriaDescarte());
        Assertions.assertNull(bolsa.getDataDescarte());
    }

    @Test
    public void bolsaUtilizadaNaoPodeSerDescartada()
    {
        BolsaHemocomponente bolsa = bolsa(20);
        bolsa.alterarStatus(StatusBolsa.RESERVADA, HOJE);
        bolsa.alterarStatus(StatusBolsa.UTILIZADA, HOJE);

        MovimentacaoInvalidaException erro = Assertions.assertThrows(MovimentacaoInvalidaException.class,
                () -> bolsa.descartar(CategoriaDescarte.OUTRO, "Teste", AGORA));

        Assertions.assertEquals("A bolsa CH-1042 está UTILIZADA e não pode passar para DESCARTADA.", erro.getMessage());
    }

    @Test
    public void bolsaDescartadaNaoPodeSerReservadaNemUsadaNemEnviada()
    {
        BolsaHemocomponente bolsa = bolsa(20);
        bolsa.descartar(CategoriaDescarte.TEMPERATURA_FORA_DO_PADRAO, null, AGORA);

        for (StatusBolsa destino : new StatusBolsa[] { StatusBolsa.RESERVADA, StatusBolsa.UTILIZADA,
                StatusBolsa.EM_TRANSITO, StatusBolsa.DISPONIVEL })
        {
            Assertions.assertThrows(MovimentacaoInvalidaException.class, () -> bolsa.alterarStatus(destino, HOJE));
        }
        Assertions.assertFalse(bolsa.estaAptaParaUso(HOJE));
        Assertions.assertEquals(StatusBolsa.DESCARTADA, bolsa.getStatus());
    }
}
