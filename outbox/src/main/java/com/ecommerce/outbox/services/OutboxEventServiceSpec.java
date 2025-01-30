package com.ecommerce.outbox.services;

import com.ecommerce.outbox.entities.OutboxEvent;

public interface OutboxEventServiceSpec {
    OutboxEvent publish(OutboxEvent event);
}
