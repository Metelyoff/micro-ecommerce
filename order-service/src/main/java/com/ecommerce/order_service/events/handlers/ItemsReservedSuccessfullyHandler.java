package com.ecommerce.order_service.events.handlers;

import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ItemsReservedSuccessfullyHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "ItemReservationService.reserve";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final OrderServiceSpec orderService;

    public ItemsReservedSuccessfullyHandler(
            OrderServiceSpec orderService
    ) {
        this.orderService = orderService;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        UUID orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(UUID::fromString)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent aggregateId is null"));

        orderService.confirmReservation(orderId);

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
