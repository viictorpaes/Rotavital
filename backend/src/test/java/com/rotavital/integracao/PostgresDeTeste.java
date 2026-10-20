package com.rotavital.integracao;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Postgres de verdade para os testes de integração e aceitação, criado pelo Testcontainers.
 * Usa a mesma imagem base e o mesmo script do docker compose (db/00_rotavital.sh), que aplica
 * as migrations de supabase/migrations: o teste valida o schema que vai para produção.
 * Sem o seed: cada teste cria só os dados de que precisa.
 *
 * O @ServiceConnection troca a conexão do application.properties (Supabase) pela deste container.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresDeTeste
{
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres()
    {
        // Caminhos relativos a backend/, onde o Maven roda os testes.
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                .withCopyFileToContainer(MountableFile.forHostPath("../db/00_rotavital.sh", 0755),
                        "/docker-entrypoint-initdb.d/00_rotavital.sh")
                .withCopyFileToContainer(MountableFile.forHostPath("../supabase/migrations"),
                        "/rotavital/migrations");
    }
}
