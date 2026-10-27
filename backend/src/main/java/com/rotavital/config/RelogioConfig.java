package com.rotavital.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Relógio no fuso do hemocentro (Recife). Os serviços recebem o Clock para os testes fixarem "agora".
@Configuration
public class RelogioConfig
{
    public static final ZoneId FUSO = ZoneId.of("America/Recife");

    @Bean
    public Clock relogio()
    {
        return Clock.system(FUSO);
    }
}
