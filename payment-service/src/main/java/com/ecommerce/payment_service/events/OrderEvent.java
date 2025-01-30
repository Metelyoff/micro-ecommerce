package com.ecommerce.payment_service.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEvent(
        String id,
        double totalPrice,
        int totalItems,
        String status,
        String statusDescription,
        String paymentMethod,
        String paymentId,
        String createdAt
) {
}