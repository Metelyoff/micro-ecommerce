package com.ecommerce.outbox.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_context_id", columnList = "context_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_context_id_status", columnList = "context_id, status"),
                @Index(name = "idx_context_id_event_name", columnList = "context_id, event_name"),
                @Index(name = "idx_version", columnList = "version")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_context_id_event_name", columnNames = {"context_id", "event_name"})
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Size(max = 255)
    @Column(name = "context_id", nullable = false)
    private String contextId;

    @Size(max = 255)
    @Column(name = "process_name", nullable = false)
    private String processName;

    @Size(max = 255)
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    @Size(max = 255)
    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "processed_timestamp")
    private long processedTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Version
    private int version;

    public OutboxEvent() {
    }

    public OutboxEvent(
            String contextId,
            String processName,
            String eventName,
            String payload,
            OutboxEventStatus status,
            String statusMessage,
            long processedTimestamp
    ) {
        this.contextId = contextId;
        this.eventName = eventName;
        this.processName = processName;
        this.payload = payload;
        this.status = status;
        this.statusMessage = statusMessage;
        this.processedTimestamp = processedTimestamp;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxEventStatus status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public long getProcessedTimestamp() {
        return processedTimestamp;
    }

    public void setProcessedTimestamp(long processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEvent that = (OutboxEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id=" + id +
                ", contextId='" + contextId + '\'' +
                ", processName='" + processName + '\'' +
                ", eventName='" + eventName + '\'' +
                ", payload='" + payload + '\'' +
                ", status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                ", processedTimestamp=" + processedTimestamp +
                ", createdAt=" + createdAt +
                ", version=" + version +
                '}';
    }

}
