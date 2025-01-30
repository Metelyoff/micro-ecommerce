package com.ecommerce.order_service.services;

import com.ecommerce.order_service.dtos.OrderDTO;
import com.ecommerce.order_service.dtos.OrderRequest;
import com.ecommerce.outbox.core.OutboxContext;

import java.util.Collection;
import java.util.UUID;

public interface OrderServiceSpec {
    Collection<OrderDTO> findAll();
    OrderDTO findById(UUID id);
    OrderDTO create(OrderRequest order);
    void cancelByReason(OutboxContext id, String reason);
    void fail(OutboxContext id, String reason);
    OrderDTO confirmReservation(UUID id);
    OrderDTO confirmPayment(OutboxContext orderId, UUID paymentId, String statusDescription);
    void confirmPaidPayment(OutboxContext orderId);
}
