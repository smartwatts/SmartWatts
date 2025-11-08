package com.smartwatts.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@TestConfiguration
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///testdb",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.com.smartwatts=DEBUG"
})
public class IntegrationTestConfiguration {

    @Bean
    @Primary
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        
        container.start();
        return container;
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));
        
        container.start();
        return container;
    }

    @Bean
    public GenericContainer<?> eurekaContainer() {
        GenericContainer<?> container = new GenericContainer<>("openjdk:17-jre-slim")
                .withCommand("java", "-jar", "/app/service-discovery.jar")
                .withExposedPorts(8761)
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(3));
        
        container.start();
        return container;
    }
}





