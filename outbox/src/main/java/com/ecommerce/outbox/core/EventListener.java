package com.ecommerce.outbox.core;

public interface EventListener<T> {
    void onEvent(T event);
}
