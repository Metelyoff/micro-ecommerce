package com.ecommerce.outbox.transformers;

public interface OutboxTransactionPayloadTransformer<T> {
    String transform(T from);
}
