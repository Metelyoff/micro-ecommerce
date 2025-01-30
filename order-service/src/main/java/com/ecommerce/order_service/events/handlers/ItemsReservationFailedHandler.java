package com.ecommerce.order_service.events.handlers;

import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ItemsReservationFailedHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "ItemReservationService.reserve";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.FAILED;

    private final OrderServiceSpec orderService;

    public ItemsReservationFailedHandler(
            OrderServiceSpec orderService
    ) {
        this.orderService = orderService;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        OutboxContext orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) () -> id)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent contextId is null"));

        orderService.fail(orderId, outboxEvent.statusMessage());
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
