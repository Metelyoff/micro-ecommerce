package com.ecommerce.outbox;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableJpaRepositories(basePackages = "com.ecommerce.outbox.repositories")
@ComponentScan(basePackages = "com.ecommerce.outbox")
@EntityScan(basePackages = "com.ecommerce.outbox.entities")
public class OutboxAutoConfiguration {
}
