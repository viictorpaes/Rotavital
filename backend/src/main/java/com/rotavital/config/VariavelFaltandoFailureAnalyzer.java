package com.rotavital.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * PI3-308: variável de ambiente que a configuração usa sem valor padrão (ex.: SUPABASE_DB_PASSWORD
 * em spring.datasource.password) e que não foi definida. Diz qual é e onde colocar.
 */
public class VariavelFaltandoFailureAnalyzer extends AbstractFailureAnalyzer<IllegalArgumentException>
{
    private static final Pattern PLACEHOLDER = Pattern.compile("Could not resolve placeholder '([^']+)'");

    @Override
    protected FailureAnalysis analyze(Throwable falhaCompleta, IllegalArgumentException causa)
    {
        Matcher variavel = PLACEHOLDER.matcher(causa.getMessage() == null ? "" : causa.getMessage());
        if (!variavel.find())
        {
            return null;
        }
        String nome = variavel.group(1);
        return new FailureAnalysis(
                "Falta a variável de ambiente " + nome + ", usada na configuração do backend.",
                "Defina " + nome + " no backend/.env para rodar com mvn spring-boot:run, ou no .env da raiz para o "
                        + "Docker. O arquivo .env.example mostra todas as variáveis.",
                causa);
    }
}
