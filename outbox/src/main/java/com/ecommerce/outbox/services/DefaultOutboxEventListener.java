package com.ecommerce.outbox.services;

import com.ecommerce.outbox.core.EventListener;
import com.ecommerce.outbox.core.OutboxEventHandler;
import com.ecommerce.outbox.core.OutboxEventHandlerFactory;
import com.ecommerce.outbox.events.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DefaultOutboxEventListener implements EventListener<OutboxEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultOutboxEventListener.class);

    private final OutboxEventHandlerFactory outboxEventHandlerFactory;

    // TODO: just for demo purpose, it's not scalable solution
    private static final Set<String> HANDLED_EVENTS = new HashSet<>();

    public DefaultOutboxEventListener(
            OutboxEventHandlerFactory outboxEventHandlerFactory
    ) {
        this.outboxEventHandlerFactory = outboxEventHandlerFactory;
    }

    @KafkaListener(topics = "#{'${spring.application.outbox.listener.topics}'.split(',')}", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void onEvent(OutboxEvent event) {
        LOG.info("Received message: {}", event);
        try {
            if (HANDLED_EVENTS.contains(event.id())) {
                LOG.warn("Event already processed: {}", event);
                return;
            }
            OutboxEventHandler handler = outboxEventHandlerFactory.getHandler(event);
            handler.handleEvent(event);
            HANDLED_EVENTS.add(event.id());
            LOG.info("Event processed: {}", event);
        } catch (Exception e) {
            LOG.error("Error processed message: ", e);
            throw e;
        }
    }

}
