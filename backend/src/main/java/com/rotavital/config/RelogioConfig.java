package com.rotavital.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelogioConfig
{
    // "Hoje" das regras de validade é o dia nos hemocentros (Recife), não o do servidor
    // (o Render roda em UTC: depois das 21h em Recife já seria amanhã). Nos testes, o
    // RelogioDeTeste fica no lugar deste para fixar o dia.
    @Bean
    public Clock relogio()
    {
        return Clock.system(ZoneId.of("America/Recife"));
    }
}
