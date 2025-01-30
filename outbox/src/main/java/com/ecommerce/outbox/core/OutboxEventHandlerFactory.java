package com.ecommerce.outbox.core;

import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OutboxEventHandlerFactory {

    private final Map<String, OutboxEventHandler> handlerMap = new ConcurrentHashMap<>();

    public OutboxEventHandlerFactory(List<OutboxEventHandler> handlers) {
        for (OutboxEventHandler handler : handlers) {
            handlerMap.put(resolveHandlerKey(handler), handler);
        }
    }

    public OutboxEventHandler getHandler(OutboxEvent event) throws UnsupportedOperationException {
        String key = resolveHandlerKey(event);
        if (handlerMap.containsKey(key)) {
            return handlerMap.get(key);
        }
        throw new UnsupportedOperationException("No handler found for: " + key);
    }

    public String resolveHandlerKey(OutboxEvent event) {
        return String.format("%s.%s", event.eventName(), event.status());
    }

    private String resolveHandlerKey(OutboxEventHandler handler) {
        return String.format("%s.%s", handler.eventName(), handler.status());
    }

}
