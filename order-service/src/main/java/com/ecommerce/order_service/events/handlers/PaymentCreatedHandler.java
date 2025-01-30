package com.ecommerce.order_service.events.handlers;

import com.ecommerce.order_service.events.PaymentEvent;
import com.ecommerce.order_service.services.OrderServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentCreatedHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "PaymentService.create";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final OrderServiceSpec orderService;
    private final ObjectMapper objectMapper;

    public PaymentCreatedHandler(
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
                .orElseThrow(() -> new IllegalArgumentException("Payment event is null"));

        orderService.confirmPayment(
                paymentEvent::orderId,
                UUID.fromString(paymentEvent.id()),
                paymentEvent.statusDescription()
        );

    }

    private PaymentEvent parsePaymentEvent(String s) {
        try {
            return objectMapper.readValue(s, PaymentEvent.class);
        } catch (JsonProcessingException e) {
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
