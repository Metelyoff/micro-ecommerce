package com.ecommerce.payment_service.events.handlers;

import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.payment_service.events.OrderEvent;
import com.ecommerce.payment_service.services.PaymentServiceSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderConfirmationPaymentFailed implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.confirmPayment";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.FAILED;

    private final PaymentServiceSpec paymentServiceSpec;
    private final ObjectMapper objectMapper;

    public OrderConfirmationPaymentFailed(
            PaymentServiceSpec paymentServiceSpec,
            ObjectMapper objectMapper
    ) {
        this.paymentServiceSpec = paymentServiceSpec;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {
        Optional.ofNullable(outboxEvent)
                .map(OutboxEvent::payload)
                .map(this::parseOrderEvent)
                .map(OrderEvent::paymentId)
                .map(UUID::fromString)
                .ifPresentOrElse(paymentServiceSpec::failById, () -> paymentServiceSpec.failByOrderId(outboxEvent));
    }

    private OrderEvent parseOrderEvent(String outboxEventPayload) {
        try {
            return objectMapper.readValue(outboxEventPayload, OrderEvent.class);
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
