package com.rotavital.aceitacao;

import com.rotavital.integracao.TesteDeIntegracao;

import io.cucumber.spring.CucumberContextConfiguration;

// Os cenários rodam contra a aplicação inteira, igual aos *IT (mesmo contexto e mesmo Postgres).
@CucumberContextConfiguration
@TesteDeIntegracao
public class ConfiguracaoDaAceitacao
{
}
