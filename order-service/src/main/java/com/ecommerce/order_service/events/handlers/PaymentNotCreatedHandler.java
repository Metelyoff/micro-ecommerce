package com.ecommerce.order_service.events.handlers;

import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Service;

@Service
public class PaymentNotCreatedHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "PaymentService.create";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.FAILED;

    private final OrderServiceSpec orderService;

    public PaymentNotCreatedHandler(
            OrderServiceSpec orderService
    ) {
        this.orderService = orderService;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {
        orderService.fail(outboxEvent, outboxEvent.statusMessage());
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
