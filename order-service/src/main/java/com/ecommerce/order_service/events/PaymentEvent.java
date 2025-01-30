package com.ecommerce.order_service.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentEvent(
        String id,
        String orderId,
        String status,
        String statusDescription,
        String expiredAt
) {
}
