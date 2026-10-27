package com.rotavital.integracao;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Relógio que o teste ajusta ("Dado que hoje é 01/10/2026"): regras de validade dependem do dia,
 * e um teste que usa a data real passa hoje e quebra daqui a uma semana.
 * É @Primary: fica no lugar do relógio do sistema que a aplicação usa em produção.
 */
@TestConfiguration(proxyBeanMethods = false)
public class RelogioDeTeste
{
    @Bean
    @Primary
    public RelogioAjustavel relogioAjustavel()
    {
        return new RelogioAjustavel();
    }

    public static class RelogioAjustavel extends Clock
    {
        private static final ZoneId FUSO = ZoneId.of("America/Recife");

        // volatile: o teste ajusta numa thread e a requisição HTTP lê em outra (a do Tomcat).
        private volatile Instant agora = meioDia(LocalDate.of(2026, 10, 1));

        public void ajustarPara(LocalDate dia)
        {
            agora = meioDia(dia);
        }

        private static Instant meioDia(LocalDate dia)
        {
            return dia.atTime(12, 0).atZone(FUSO).toInstant();
        }

        @Override
        public ZoneId getZone()
        {
            return FUSO;
        }

        @Override
        public Clock withZone(ZoneId fuso)
        {
            return Clock.fixed(agora, fuso);
        }

        @Override
        public Instant instant()
        {
            return agora;
        }
    }
}
