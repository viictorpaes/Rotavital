package com.rotavital.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * PI3-308: com ddl-auto=validate, um banco sem a última migration impede o backend de subir e o
 * Spring mostra uma pilha enorme. Este analisador troca isso por uma explicação curta, na tela
 * "APPLICATION FAILED TO START": qual coluna ou tabela falta e o que fazer.
 */
public class SchemaDesatualizadoFailureAnalyzer extends AbstractFailureAnalyzer<SchemaManagementException>
{
    private static final Pattern COLUNA = Pattern.compile("missing column \\[([^\\]]+)\\] in table \\[([^\\]]+)\\]");
    private static final Pattern TABELA = Pattern.compile("missing table \\[([^\\]]+)\\]");

    @Override
    protected FailureAnalysis analyze(Throwable falhaCompleta, SchemaManagementException causa)
    {
        return new FailureAnalysis(
                "O banco de dados não está igual ao que o código espera: " + traduzir(causa.getMessage()) + ".",
                "Aplique no banco que o backend está usando as migrations de supabase/migrations que ainda não "
                        + "rodaram nele. No Supabase, compare com a lista de migrations do projeto; no Docker local, "
                        + "recrie o banco com: npm run docker:down:v, docker compose build db e npm run docker:local.",
                causa);
    }

    static String traduzir(String mensagem)
    {
        if (mensagem == null)
        {
            return "a validação do schema falhou";
        }
        Matcher coluna = COLUNA.matcher(mensagem);
        if (coluna.find())
        {
            return "falta a coluna " + coluna.group(1) + " na tabela " + coluna.group(2);
        }
        Matcher tabela = TABELA.matcher(mensagem);
        if (tabela.find())
        {
            return "falta a tabela " + tabela.group(1);
        }
        return mensagem;
    }
}
