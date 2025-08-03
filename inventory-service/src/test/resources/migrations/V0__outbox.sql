CREATE TABLE IF NOT EXISTS outbox_events
(
    id                  UUID PRIMARY KEY,
    context_id          VARCHAR(255) NOT NULL,
    process_name        VARCHAR(255) NOT NULL,
    event_name          VARCHAR(255) NOT NULL,
    payload             TEXT         NULL,
    status              VARCHAR(255) NOT NULL,
    status_message      VARCHAR(255) NULL,
    processed_timestamp BIGINT       NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    version             INTEGER      NULL
);

CREATE INDEX IF NOT EXISTS idx_context_id ON outbox_events(context_id);
CREATE INDEX IF NOT EXISTS idx_event_name ON outbox_events(event_name);
CREATE INDEX IF NOT EXISTS idx_process_name ON outbox_events(process_name);
CREATE INDEX IF NOT EXISTS idx_status ON outbox_events(status);