package com.ecommerce.order_service.entities;

import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED,
    RESERVED,
    PENDING_FOR_PAY,
    PAID,
    COMPLETED,
    FAILED,
    CANCELLED;

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isPending() {
        return this == CREATED || this == RESERVED || this == PENDING_FOR_PAY;
    }
}
