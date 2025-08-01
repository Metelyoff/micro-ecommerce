package com.ecommerce.inventory.events.handlers;

import com.ecommerce.inventory.services.ItemReservationService;
import com.ecommerce.outbox.core.AbstractOutboxEventHandler;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class OrderFailsProcessed extends AbstractOutboxEventHandler {

    private final ItemReservationService itemReservationService;

    public OrderFailsProcessed(final ItemReservationService itemReservationService) {
        super("OrderService.fail", OutboxEventStatus.PROCESSED);
        this.itemReservationService = itemReservationService;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        var orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent aggregateId required to cancel reservation."));

        var reservedOrderItems = itemReservationService.findReservedItemsByOrderId(orderId);

        if (reservedOrderItems.isEmpty()) {
            log.info("No reserved items found for order id: {}", orderId);
        } else {
            log.info("Canceling reserved items for order id: {}", orderId);
            itemReservationService.cancelReservation(orderId);
        }
    }

}
