package com.rotavital;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RotaVitalApplication
{
    public static void main(String[] args)
    {
        carregarArquivoEnv();
        SpringApplication.run(RotaVitalApplication.class, args);
    }

    private static void carregarArquivoEnv()
    {
        List<Path> possiveisCaminhos = List.of
        (
            Paths.get(".env"),
            Paths.get("Rotavital/backend/.env"),
            Paths.get("backend/.env"),
            Paths.get("Rotavital/.env"),
            Paths.get("../backend/.env"),
            Paths.get("../../backend/.env")
        );

        for (Path caminho : possiveisCaminhos)
        {
            if (Files.exists(caminho))
            {
                try
                {
                    List<String> linhas = Files.readAllLines(caminho);
                    for (String linha : linhas)
                    {
                        linha = linha.trim();
                        if (!linha.isEmpty() && !linha.startsWith("#") && linha.contains("="))
                        {
                            int separador = linha.indexOf('=');
                            String chave = linha.substring(0, separador).trim();
                            String valor = linha.substring(separador + 1).trim();

                            if (System.getProperty(chave) == null && System.getenv(chave) == null)
                            {
                                System.setProperty(chave, valor);
                            }
                        }
                    }
                    System.out.println("📄 Arquivo .env carregado de: " + caminho.toAbsolutePath());
                    break;
                }
                catch (IOException ignored) 
                {}
            }
        }
    }

    @Bean
    CommandLineRunner testarConexaoBanco(DataSource dataSource)
    {
        return args ->
        {
            try (Connection conn = dataSource.getConnection())
            {
                System.out.println("=================================================");
                System.out.println("✅ CONECTADO AO SUPABASE COM SUCESSO!");
                System.out.println("Catálogo: " + conn.getCatalog());
                System.out.println("Schema:   " + conn.getSchema());
                System.out.println("=================================================");
            }
            catch (Exception e)
            {
                System.err.println("=================================================");
                System.err.println("❌ FALHA AO CONECTAR COM SUPABASE: " + e.getMessage());
                System.err.println("=================================================");
            }
        };
    }
}