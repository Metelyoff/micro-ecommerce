package com.ecommerce.payment_service.events.handlers;

import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.payment_service.services.PaymentServiceSpec;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.cancel";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final PaymentServiceSpec paymentServiceSpec;

    public OrderCancelledHandler(
            PaymentServiceSpec paymentServiceSpec
    ) {
        this.paymentServiceSpec = paymentServiceSpec;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {
        paymentServiceSpec.cancelById(outboxEvent);
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
