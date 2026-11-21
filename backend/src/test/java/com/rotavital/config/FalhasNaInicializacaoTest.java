package com.rotavital.config;

import java.sql.SQLException;

import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

// PI3-308: as falhas comuns ao subir o backend viram uma explicação curta com o que fazer.
public class FalhasNaInicializacaoTest
{
    // A falha real chega embrulhada em várias exceções do Spring; o analisador acha a causa no meio delas.
    private static RuntimeException embrulhar(Throwable causa)
    {
        return new RuntimeException("Error creating bean with name 'entityManagerFactory'",
                new IllegalStateException("Unable to build Hibernate SessionFactory", causa));
    }

    @Test
    public void colunaFaltandoDizQualColunaEQualTabela()
    {
        FailureAnalysis analise = new SchemaDesatualizadoFailureAnalyzer().analyze(embrulhar(new SchemaManagementException(
                "Schema-validation: missing column [motivo_descarte] in table [bolsa_hemocomponente]")));

        Assertions.assertEquals("O banco de dados não está igual ao que o código espera: falta a coluna motivo_descarte "
                + "na tabela bolsa_hemocomponente.", analise.getDescription());
        Assertions.assertTrue(analise.getAction().contains("supabase/migrations"));
    }

    @Test
    public void tabelaFaltandoDizQualTabela()
    {
        FailureAnalysis analise = new SchemaDesatualizadoFailureAnalyzer().analyze(embrulhar(
                new SchemaManagementException("Schema-validation: missing table [agendamento_doacao]")));

        Assertions.assertTrue(analise.getDescription().contains("falta a tabela agendamento_doacao"));
    }

    @Test
    public void senhaRecusadaApontaParaAVariavelDaSenha()
    {
        FailureAnalysis analise = new ConexaoComBancoFailureAnalyzer().analyze(embrulhar(
                new SQLException("FATAL: password authentication failed for user \"postgres\"")));

        Assertions.assertTrue(analise.getDescription().contains("usuário ou senha recusados"));
        Assertions.assertTrue(analise.getAction().contains("SUPABASE_DB_PASSWORD"));
    }

    @Test
    public void bancoForaDoArApontaParaOEndereco()
    {
        FailureAnalysis analise = new ConexaoComBancoFailureAnalyzer().analyze(embrulhar(
                new SQLException("Connection to localhost:5432 refused. Check that the hostname and port are correct")));

        Assertions.assertTrue(analise.getDescription().startsWith("O backend não conseguiu chegar ao banco de dados"));
        Assertions.assertTrue(analise.getAction().contains("SPRING_DATASOURCE_URL"));
    }

    @Test
    public void outroErroDeSqlFicaParaOSpringExplicar()
    {
        Assertions.assertNull(new ConexaoComBancoFailureAnalyzer().analyze(embrulhar(
                new SQLException("ERROR: syntax error at or near \"selec\""))));
    }

    @Test
    public void variavelFaltandoDizONomeDaVariavel()
    {
        FailureAnalysis analise = new VariavelFaltandoFailureAnalyzer().analyze(embrulhar(new IllegalArgumentException(
                "Could not resolve placeholder 'SUPABASE_DB_PASSWORD' in value \"${SUPABASE_DB_PASSWORD}\"")));

        Assertions.assertEquals("Falta a variável de ambiente SUPABASE_DB_PASSWORD, usada na configuração do backend.",
                analise.getDescription());
        Assertions.assertTrue(analise.getAction().contains("backend/.env"));
    }

    @Test
    public void outroIllegalArgumentNaoEhVariavelFaltando()
    {
        Assertions.assertNull(new VariavelFaltandoFailureAnalyzer().analyze(embrulhar(
                new IllegalArgumentException("Latitude fora do intervalo"))));
    }
}
