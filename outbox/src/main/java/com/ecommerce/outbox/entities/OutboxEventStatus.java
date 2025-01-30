package com.ecommerce.outbox.entities;

public enum OutboxEventStatus {
    PENDING,
    FAILED,
    PROCESSED
}
