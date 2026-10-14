package com.rotavital.integracao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * A aplicação inteira (Tomcat numa porta livre, controllers, serviços, JPA) contra o Postgres
 * do Testcontainers. Mesma configuração para os *IT e para a aceitação (Cucumber): o Spring
 * reaproveita o contexto e o container entre eles.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.show-sql=false")
@Import({ PostgresDeTeste.class, RelogioDeTeste.class })
public @interface TesteDeIntegracao
{
}
