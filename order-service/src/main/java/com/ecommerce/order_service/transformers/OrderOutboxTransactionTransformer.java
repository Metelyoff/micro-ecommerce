package com.ecommerce.order_service.transformers;

import com.ecommerce.order_service.dtos.OrderDTO;
import com.ecommerce.outbox.transformers.OutboxTransactionPayloadTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxTransactionTransformer implements OutboxTransactionPayloadTransformer<OrderDTO> {

    private final ObjectMapper objectMapper;

    public OrderOutboxTransactionTransformer(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String transform(OrderDTO t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
