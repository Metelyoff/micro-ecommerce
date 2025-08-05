package com.ecommerce.inventory;

import org.junit.jupiter.api.TestInstance;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
            .withReuse(true);

    private static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka"))
            .withEnv("KAFKA_KRAFT_MODE", "true")
            .withEnv("KAFKA_CFG_NODE_ID", "0")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "0@localhost:9093")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093")
            .withEnv("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withExposedPorts(9092)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
            .withReuse(true);

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

}
