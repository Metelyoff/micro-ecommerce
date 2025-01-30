package com.ecommerce.order_service.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySSEService<T> implements SSEService<T> {

    private final Map<UUID, Sinks.Many<T>> sinks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> completed = new ConcurrentHashMap<>();

    @Override
    public Flux<T> subscribe(UUID id) {
        if (Boolean.TRUE.equals(completed.get(id))) {
            return Flux.empty();
        }
        Sinks.Many<T> sink = sinks.computeIfAbsent(id, key -> Sinks.many().multicast().onBackpressureBuffer());
        return sink.asFlux();
    }

//    @Override
//    public Flux<T> subscribe(UUID id) {
//        Sinks.Many<T> sink = sinks.compute(id, (key, existingSink) -> {
//            if (existingSink == null || completed.getOrDefault(id, false)) {
//                completed.remove(id);
//                return Sinks.many().multicast().onBackpressureBuffer();
//            }
//            return existingSink;
//        });
//        return sink.asFlux();
//    }

    @Override
    public Sinks.Many<T> getSink(UUID id) {
        return sinks.get(id);
    }

    @Override
    public void emit(UUID id, T event) {
        Sinks.Many<T> sink = sinks.get(id);
        if (sink != null) {
            sink.tryEmitNext(event);
        }
    }

    @Override
    public void complete(UUID id) {
        Sinks.Many<T> sink = sinks.remove(id);
        if (sink != null) {
            sink.tryEmitComplete();
        }
        completed.put(id, true);
    }

}
