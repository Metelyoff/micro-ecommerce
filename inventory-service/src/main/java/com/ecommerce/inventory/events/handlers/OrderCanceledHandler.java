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
public class OrderCanceledHandler extends AbstractOutboxEventHandler {

    private final ItemReservationService itemReservationService;

    public OrderCanceledHandler(final ItemReservationService itemReservationService) {
        super("OrderService.cancelByReason", OutboxEventStatus.PROCESSED);
        this.itemReservationService = itemReservationService;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        var orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent aggregateId required to cancel reservation."));

        itemReservationService.cancelReservation(orderId);
    }

}
