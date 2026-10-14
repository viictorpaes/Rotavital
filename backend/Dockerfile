# ===================================================================
# Rota Vital — imagem do backend (Spring Boot, Java 21), em dois estágios.
# O 1º compila e roda os testes; o 2º leva só o .jar para um JRE enxuto.
# ===================================================================

# 1. Estágio de build: Maven + JDK 21 (só existe durante o build, não vai para produção)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código: essa camada fica em cache
# enquanto o pom.xml não muda, e o rebuild depois de mexer em src/ leva segundos.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# package compila e roda os testes unitários (surefire): se um falhar, a imagem não é gerada.
# Os de integração e aceitação (*IT, failsafe) sobem um Postgres com Testcontainers e
# rodam na esteira (mvn verify), onde há Docker disponível.
COPY src ./src
RUN mvn -B package

# 2. Estágio de execução: só o JRE 21 em Alpine (~200 MB a menos que a imagem com Maven e JDK)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário sem privilégios: se a aplicação for comprometida, o processo não é root no container.
RUN addgroup -S rotavital && adduser -S rotavital -G rotavital

COPY --from=build /app/target/rotavital-backend-*.jar app.jar

# Versão (sha do commit) que a esteira grava na imagem; aparece em /actuator/info,
# e é por ela que o smoke test confere que o deploy publicou o commit certo.
ARG APP_VERSAO=local
ENV APP_VERSAO=${APP_VERSAO}

USER rotavital

# Porta padrão do Spring Boot; no Render a porta vem da variável PORT (server.port=${PORT:8080}).
EXPOSE 8080

# MaxRAMPercentage: a JVM respeita o limite de memória do container (512 MB no plano gratuito do Render).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
