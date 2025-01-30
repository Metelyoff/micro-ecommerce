package com.ecommerce.inventory_service.services;

import com.ecommerce.inventory_service.dtos.ReservedItemDTO;
import com.ecommerce.outbox.core.OutboxContext;

import java.util.Collection;

public interface ItemReservationServiceSpec {
    void reserve(OutboxContext orderId, Collection<ReservedItemDTO> reservedItems);
    void cancelReservation(OutboxContext orderId);
    Collection<ReservedItemDTO> findReservedItemsByOrderId(OutboxContext orderId);
}
