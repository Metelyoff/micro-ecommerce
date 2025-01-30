package com.ecommerce.order_service.events.handlers;

import com.ecommerce.order_service.events.PaymentEvent;
import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentExpiredHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "PaymentService.expiredPayment";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final OrderServiceSpec orderService;
    private final ObjectMapper objectMapper;

    public PaymentExpiredHandler(
            OrderServiceSpec orderService,
            ObjectMapper objectMapper
    ) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        PaymentEvent paymentEvent = Optional.ofNullable(outboxEvent)
                .map(OutboxEvent::payload)
                .map(this::parsePaymentEvent)
                .orElseThrow(() -> new IllegalArgumentException("Payment payload is null"));

        orderService.cancelByReason(outboxEvent, paymentEvent.statusDescription());
    }

    private PaymentEvent parsePaymentEvent(String s) {
        try {
            return objectMapper.readValue(s, PaymentEvent.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
