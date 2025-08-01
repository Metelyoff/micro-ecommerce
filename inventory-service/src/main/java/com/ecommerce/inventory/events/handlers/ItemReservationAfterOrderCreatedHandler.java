package com.ecommerce.inventory.events.handlers;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.inventory.events.OrderEvent;
import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.outbox.core.AbstractOutboxEventHandler;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import com.ecommerce.outbox.transformers.OutboxPayloadToTypeTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemReservationAfterOrderCreatedHandler extends AbstractOutboxEventHandler {

    private final ItemReservationService itemReservationService;
    private final OutboxPayloadToTypeTransformer<OrderEvent> transformer;

    public ItemReservationAfterOrderCreatedHandler(
            final ItemReservationService itemReservationService,
            final OutboxPayloadToTypeTransformer<OrderEvent> transformer
    ) {
        super("OrderService.create", OutboxEventStatus.PROCESSED);
        this.itemReservationService = itemReservationService;
        this.transformer = transformer;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        var orderEvent = Optional.ofNullable(outboxEvent)
                .map(OutboxEvent::payload)
                .map(transformer::transform)
                .orElseThrow(() -> new IllegalArgumentException("Order event is null"));

        var orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("Context id is null"));

        var orderItemsToReserve = Optional.ofNullable(orderEvent.items())
                .stream()
                .flatMap(Collection::stream)
                .map(item -> transform(item, orderId.getContextId()))
                .collect(Collectors.toSet());

        itemReservationService.reserve(orderId, orderItemsToReserve);
    }

    private ReservedItemDTO transform(OrderEvent.OrderItemDTO orderItemEvent, String orderId) {
        return new ReservedItemDTO(
                UUID.fromString(orderItemEvent.productId()),
                UUID.fromString(orderId),
                orderItemEvent.price(),
                orderItemEvent.quantity(),
                null,
                null
        );
    }

}
