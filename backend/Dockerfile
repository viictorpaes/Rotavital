# 1. Estagio de compilacao (Maven + OpenJDK 21)
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copia pom.xml e faz download das dependencias (camada em cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia codigo-fonte e compila gerando o JAR
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Estagio de execucao (JRE 21 leve)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar compilado do estagio anterior
COPY --from=build /app/target/*.jar app.jar

# Porta padrao do Spring Boot
EXPOSE 8080

# Execucao do Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]

