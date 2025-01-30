package com.ecommerce.order_service.dtos;

import com.ecommerce.order_service.entities.OrderStatus;
import com.ecommerce.order_service.entities.PaymentMethod;
import com.ecommerce.outbox.core.OutboxContext;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record OrderDTO(
        UUID id,
        Set<OrderItemDTO> items,
        int totalItems,
        double totalPrice,
        OrderStatus status,
        String statusDescription,
        PaymentMethod paymentMethod,
        UUID paymentId,
        LocalDateTime createdAt
) implements OutboxContext {

    @Override
    public String getContextId() {
        return this.id.toString();
    }

}
