package com.rotavital.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que registra no terminal todas as requisições HTTP recebidas e respondidas
 * pela API do Rota Vital, incluindo método, rota, status HTTP e tempo de resposta.
 *
 * Roda logo depois do filtro de observação do Spring Boot (ServerHttpObservationFilter, de ordem
 * HIGHEST_PRECEDENCE + 1): assim a linha é escrita com o trace da requisição aberto, e o padrão
 * de log da observabilidade (LOGGING_PATTERN_CORRELATION) inclui o traceId que o Grafana liga ao Tempo.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class HttpLoggingFilter extends OncePerRequestFilter
{
    private static final Logger LOG = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
    HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        long inicio = System.currentTimeMillis();
        String metodo = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : " ";

        // Ignora requisições de pre-flight OPTIONS do CORS e os healthchecks do Docker/Render
        // (/actuator, a cada poucos segundos) para não poluir o terminal
        if ("OPTIONS".equalsIgnoreCase(metodo) || uri.startsWith("/actuator"))
        {
            filterChain.doFilter(request, response);
            return;
        }

        try
        {
            filterChain.doFilter(request, response);
        }

        finally
        {
            long duracao = System.currentTimeMillis() - inicio;
            int status = response.getStatus();

            String icone;

            if (status >= 200 && status < 300)
            {
                icone = "✅";
            }

            else if (status >= 400 && status < 500)
            {
                icone = "⚠️";
            }

            else if (status >= 500)
            {
                icone = "❌";
            }
            
            else
            {
                icone = "ℹ️";
            }

            LOG.info(String.format("%s [HTTP %d] %-6s %s%s | %d ms",
                    icone, status, metodo, uri, query, duracao));
        }
    }
}