package com.ecommerce.payment_service.dtos;

import com.ecommerce.payment_service.entities.Method;

import java.util.UUID;

public record PaymentRequest(
        Method method,
        double amount,
        UUID orderId
) {
}
