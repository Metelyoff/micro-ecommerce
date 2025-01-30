package com.ecommerce.outbox.services;

import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventService implements OutboxEventServiceSpec {

    private final OutboxEventRepository outboxEventRepository;

    private static final Logger LOG = LoggerFactory.getLogger(OutboxEventService.class);

    public OutboxEventService(
            OutboxEventRepository outboxEventRepository
    ) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public OutboxEvent publish(OutboxEvent event) {
        LOG.debug("Publishing event: {}", event);
        return outboxEventRepository.save(event);
    }

}
