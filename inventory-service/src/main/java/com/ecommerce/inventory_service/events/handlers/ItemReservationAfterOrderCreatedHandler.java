package com.ecommerce.inventory_service.events.handlers;

import com.ecommerce.inventory_service.dtos.ReservedItemDTO;
import com.ecommerce.inventory_service.events.OrderEvent;
import com.ecommerce.inventory_service.services.ItemReservationServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemReservationAfterOrderCreatedHandler implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.create";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private final ItemReservationServiceSpec itemReservationServiceSpec;
    private final ObjectMapper objectMapper;

    public ItemReservationAfterOrderCreatedHandler(
            ItemReservationServiceSpec itemReservationServiceSpec,
            ObjectMapper objectMapper
    ) {
        this.itemReservationServiceSpec = itemReservationServiceSpec;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        OrderEvent orderEvent = Optional.ofNullable(outboxEvent)
                .map(OutboxEvent::payload)
                .map(this::parseOrderEvent)
                .orElseThrow(() -> new IllegalArgumentException("Order event is null"));

        OutboxContext orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("Context id is null"));

        Set<ReservedItemDTO> orderItemsToReserve = Optional.ofNullable(orderEvent.items())
                .stream()
                .flatMap(Collection::stream)
                .map(item -> transform(item, orderId.getContextId()))
                .collect(Collectors.toSet());

        itemReservationServiceSpec.reserve(orderId, orderItemsToReserve);
    }

    private OrderEvent parseOrderEvent(String outboxEventPayload) {
        try {
            return objectMapper.readValue(outboxEventPayload, OrderEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ReservedItemDTO transform(OrderEvent.OrderItemDTO orderItemEvent, String orderId) {
        return new ReservedItemDTO(
                null,
                UUID.fromString(orderItemEvent.productId()),
                UUID.fromString(orderId),
                orderItemEvent.price(),
                orderItemEvent.quantity(),
                null,
                null
        );
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
