package com.rotavital.config;

import java.io.IOException;

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
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        long inicio = System.currentTimeMillis();
        String metodo = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        // Ignora requisições de pre-flight OPTIONS do CORS para não poluir o terminal
        if ("OPTIONS".equalsIgnoreCase(metodo))
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

            System.out.printf("%s [HTTP %d] %-6s %s%s | %d ms%n",
                    icone, status, metodo, uri, query, duracao);
        }
    }
}

