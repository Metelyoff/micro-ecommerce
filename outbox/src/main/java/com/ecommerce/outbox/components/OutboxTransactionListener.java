package com.ecommerce.outbox.components;

import com.ecommerce.outbox.entities.OutboxEvent;
import com.ecommerce.outbox.services.OutboxEventServiceSpec;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class OutboxTransactionListener {

    private final OutboxEventServiceSpec outboxEventService;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OutboxTransactionListener.class);

    // TODO: just for demo purpose, it's not scalable solution
    private static final Set<UUID> HANDLED_EVENTS = new HashSet<>();

    private static final String EVENT_ALREADY_HANDLED = "Event already handled: {}";
    private static final String EVENT_PUBLISHED = "Event published: {}";

    public OutboxTransactionListener(OutboxEventServiceSpec outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleFailedReservationEvent(OutboxEvent event) {
        LOG.info("Async handling failed event: {}", event);
        processEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSuccessfulReservationEvent(OutboxEvent event) {
        LOG.info("Handling successful event: {}", event);
        processEvent(event);
    }

    private void processEvent(OutboxEvent event) {
        if (HANDLED_EVENTS.contains(event.getId())) {
            LOG.info(EVENT_ALREADY_HANDLED, event);
            return;
        }
        HANDLED_EVENTS.add(outboxEventService.publish(event).getId());
        LOG.info(EVENT_PUBLISHED, event);
    }

}