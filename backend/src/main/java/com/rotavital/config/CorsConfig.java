package com.rotavital.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer
{
    @Value("${cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://localhost:3000,http://localhost}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition", "Location")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
