package com.ecommerce.payment_service.repositories;

import com.ecommerce.outbox.repositories.OutboxEventRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public interface PaymentOutboxRepository extends OutboxEventRepository {
    boolean existsByContextIdAndEventName(String contextId, String eventName);
}
