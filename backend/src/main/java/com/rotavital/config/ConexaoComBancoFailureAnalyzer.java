package com.rotavital.config;

import java.sql.SQLException;
import java.util.Locale;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * PI3-308: quando o backend não consegue entrar no banco na inicialização (senha errada ou vazia,
 * banco fora do ar, endereço errado), explica o motivo provável e o que conferir, no lugar da pilha
 * do driver. A mensagem do driver vai junto; a senha nunca aparece nela.
 */
public class ConexaoComBancoFailureAnalyzer extends AbstractFailureAnalyzer<SQLException>
{
    @Override
    protected FailureAnalysis analyze(Throwable falhaCompleta, SQLException causa)
    {
        String mensagem = causa.getMessage() == null ? "" : causa.getMessage();
        String minuscula = mensagem.toLowerCase(Locale.ROOT);

        if (minuscula.contains("password") || minuscula.contains("authentication"))
        {
            return new FailureAnalysis(
                    "O backend não conseguiu entrar no banco de dados: usuário ou senha recusados (" + mensagem + ").",
                    "No modo desenvolvimento, confira SUPABASE_DB_PASSWORD no backend/.env. No Docker, confira "
                            + "DB_PASSWORD no .env da raiz e recrie o banco local se a senha mudou (npm run docker:down:v).",
                    causa);
        }
        if (minuscula.contains("refused") || minuscula.contains("unknownhost") || minuscula.contains("timed out")
                || minuscula.contains("timeout") || minuscula.contains("connection attempt failed"))
        {
            return new FailureAnalysis(
                    "O backend não conseguiu chegar ao banco de dados (" + mensagem + ").",
                    "Confira se o banco está no ar e se o endereço em SPRING_DATASOURCE_URL (ou o do Supabase em "
                            + "application.properties) está certo. No Docker, rode npm run docker:status e veja se o "
                            + "serviço db está healthy.",
                    causa);
        }
        return null;
    }
}
