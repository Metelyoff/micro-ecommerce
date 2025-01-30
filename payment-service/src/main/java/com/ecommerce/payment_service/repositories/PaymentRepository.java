package com.ecommerce.payment_service.repositories;

import com.ecommerce.payment_service.entities.Status;
import com.ecommerce.payment_service.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    <T> Collection<T> findAllProjectedBy(Class<T> type);

    <T> Collection<T> findAllByOrderId(UUID orderId, Class<T> type);

    <T> T findById(UUID id, Class<T> type);

    Collection<Payment> findAllByStatus(Status status);

    Collection<Payment> findAllByOrderId(UUID orderId);
}
