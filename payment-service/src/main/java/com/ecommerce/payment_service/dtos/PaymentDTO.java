package com.ecommerce.payment_service.dtos;

import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.payment_service.entities.Method;
import com.ecommerce.payment_service.entities.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDTO(
        UUID id,
        double amount,
        Method method,
        Status status,
        UUID orderId,
        String statusDescription,
        LocalDateTime expiredAt,
        LocalDateTime createdAt
) implements OutboxContext {
    @Override
    public String getContextId() {
        return this.orderId.toString();
    }
}
