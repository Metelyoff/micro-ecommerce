package com.ecommerce.inventory_service.events.handlers;

import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class OrderConfirmedReservation implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.confirmReservation";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OrderConfirmedReservation.class);

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {
        LOG.info("OK, order confirmed by event {}", outboxEvent);
    }

    @Override
    public String eventName() {
        return EVENT_TYPE;
    }

    @Override
    public OutboxEventStatus status() {
        return EVENT_STATUS;
    }

}
