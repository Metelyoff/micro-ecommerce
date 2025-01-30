package com.ecommerce.outbox.repositories;

import com.ecommerce.outbox.entities.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    boolean existsByContextIdAndEventName(String contextId, String eventName);
}
