package com.ecommerce.order_service.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

public interface SSEService<T> {
    Flux<T> subscribe(UUID id);
    Sinks.Many<T> getSink(UUID id);
    void emit(UUID id, T event);
    void complete(UUID id);
}
