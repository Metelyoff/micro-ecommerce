package com.ecommerce.payment_service.services;

import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.payment_service.dtos.PaymentDTO;
import com.ecommerce.payment_service.dtos.PaymentRequest;
import com.ecommerce.payment_service.entities.Status;
import com.ecommerce.payment_service.entities.Payment;

import java.util.Collection;
import java.util.UUID;

public interface PaymentServiceSpec {
    Collection<PaymentDTO> findAll();

    Collection<Payment> findAllByStatus(Status status);

    Collection<PaymentDTO> findAllByOrderId(UUID orderId);

    PaymentDTO findById(UUID id);

    PaymentDTO create(PaymentRequest paymentRequest);

    PaymentDTO pay(UUID id);

    boolean isExpired(Payment payment);

    PaymentDTO processExpiredPayment(Payment payment);

    void failById(UUID id);

    void failByOrderId(OutboxContext id);

    void cancelById(OutboxContext id);
}
