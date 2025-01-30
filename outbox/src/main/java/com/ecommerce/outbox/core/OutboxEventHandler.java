package com.ecommerce.outbox.core;

import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;

public interface OutboxEventHandler {
    void handleEvent(OutboxEvent event);
    String eventName();
    OutboxEventStatus status();
}
