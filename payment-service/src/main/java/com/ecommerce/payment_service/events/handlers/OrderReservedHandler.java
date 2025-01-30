package com.ecommerce.payment_service.events.handlers;

import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.payment_service.dtos.PaymentRequest;
import com.ecommerce.payment_service.entities.Method;
import com.ecommerce.payment_service.events.OrderEvent;
import com.ecommerce.payment_service.services.PaymentServiceSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderReservedHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.confirmReservation";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final PaymentServiceSpec paymentServiceSpec;
    private final ObjectMapper objectMapper;

    public OrderReservedHandler(
            PaymentServiceSpec paymentServiceSpec,
            ObjectMapper objectMapper
    ) {
        this.paymentServiceSpec = paymentServiceSpec;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        OrderEvent orderEvent = Optional.ofNullable(outboxEvent)
                .map(OutboxEvent::payload)
                .map(this::parseOrderEvent)
                .orElseThrow(() -> new IllegalArgumentException("Order event is null"));

        PaymentRequest paymentRequest = new PaymentRequest(
                Method.valueOf(orderEvent.paymentMethod()),
                orderEvent.totalPrice(),
                UUID.fromString(orderEvent.id())
        );

        paymentServiceSpec.create(paymentRequest);
    }

    private OrderEvent parseOrderEvent(String outboxEventPayload) {
        try {
            return objectMapper.readValue(outboxEventPayload, OrderEvent.class);
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
