package com.ecommerce.inventory.services;

import com.ecommerce.inventory.dtos.ReservedItemDTO;
import com.ecommerce.outbox.core.OutboxContext;

import java.util.Collection;

public interface ItemReservationService {
    void reserve(OutboxContext orderId, Collection<ReservedItemDTO> reservedItems);
    void cancelReservation(OutboxContext orderId);
    Collection<ReservedItemDTO> findReservedItemsByOrderId(OutboxContext orderId);
}
