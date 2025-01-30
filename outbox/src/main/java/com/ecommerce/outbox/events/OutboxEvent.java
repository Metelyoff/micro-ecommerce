package com.ecommerce.outbox.events;

import com.ecommerce.outbox.core.OutboxContext;
import com.ecommerce.outbox.entities.OutboxEventStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OutboxEvent(
        @JsonProperty("id")
        String id,
        @JsonProperty("process_name")
        String processName,
        @JsonProperty("event_name")
        String eventName,
        @JsonProperty("context_id")
        String contextId,
        @JsonProperty("payload")
        String payload,
        @JsonProperty("status")
        OutboxEventStatus status,
        @JsonProperty("status_message")
        String statusMessage,
        @JsonProperty("created_at")
        String createdAt
) implements OutboxContext {

    @Override
    public String getContextId() {
        return contextId;
    }

}
