package com.ecommerce.inventory_service.events.handlers;

import com.ecommerce.inventory_service.services.ItemReservationServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderCanceledHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.cancelByReason";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final ItemReservationServiceSpec itemReservationServiceSpec;

    public OrderCanceledHandler(ItemReservationServiceSpec itemReservationServiceSpec) {
        this.itemReservationServiceSpec = itemReservationServiceSpec;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        OutboxContext orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent aggregateId required to cancel reservation."));

        itemReservationServiceSpec.cancelReservation(orderId);
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
