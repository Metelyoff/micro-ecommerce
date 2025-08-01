package com.ecommerce.inventory.events;

import com.ecommerce.outbox.transformers.OutboxPayloadToTypeTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformersConfiguration {
    @Bean
    public OutboxPayloadToTypeTransformer<OrderEvent> orderEventOutboxPayloadTransformer(final ObjectMapper objectMapper) {
        return new OutboxPayloadToTypeTransformer<>(objectMapper, new TypeReference<>() {
        });
    }
}
