package com.ecommerce.inventory_service.events.handlers;

import com.ecommerce.inventory_service.dtos.ReservedItemDTO;
import com.ecommerce.inventory_service.services.ItemReservationServiceSpec;
import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.ecommerce.outbox.events.OutboxEvent;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class OrderFailsProcessed implements OutboxEventHandler {

    public static final String EVENT_TYPE = "OrderService.fail";
    public static final OutboxEventStatus EVENT_STATUS = OutboxEventStatus.PROCESSED;

    private static final Logger LOG = Logger.getLogger(OrderFailsProcessed.class.getName());

    private final ItemReservationServiceSpec itemReservationServiceSpec;

    public OrderFailsProcessed(ItemReservationServiceSpec itemReservationServiceSpec) {
        this.itemReservationServiceSpec = itemReservationServiceSpec;
    }

    @Override
    public void handleEvent(OutboxEvent outboxEvent) {

        OutboxContext orderId = Optional.ofNullable(outboxEvent.getContextId())
                .map(id -> (OutboxContext) id::toString)
                .orElseThrow(() -> new IllegalArgumentException("OutboxEvent aggregateId required to cancel reservation."));

        Collection<ReservedItemDTO> reservedOrderItems = itemReservationServiceSpec.findReservedItemsByOrderId(orderId);
        if (reservedOrderItems.isEmpty()) {
            LOG.info("No reserved items found for order id: " + orderId);
        } else {
            LOG.info("Canceling reserved items for order id: " + orderId);
            itemReservationServiceSpec.cancelReservation(orderId);
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
