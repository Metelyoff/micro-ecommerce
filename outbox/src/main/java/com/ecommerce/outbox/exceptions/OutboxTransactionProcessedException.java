package com.ecommerce.outbox.exceptions;

public class OutboxTransactionProcessedException extends RuntimeException {
    public OutboxTransactionProcessedException(String message) {
        super(message);
    }
    public OutboxTransactionProcessedException(String message, Throwable cause) {
        super(message, cause);
    }
}
