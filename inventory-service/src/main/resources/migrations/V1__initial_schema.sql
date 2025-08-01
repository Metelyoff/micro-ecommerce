CREATE TABLE IF NOT EXISTS items
(
    id         UUID PRIMARY KEY,

    name       VARCHAR(255)   NOT NULL,
    quantity   INTEGER        NOT NULL,
    image      VARCHAR(2048)  NULL,
    price      DECIMAL(10, 2) NOT NULL,

    created    TIMESTAMP      NOT NULL,
    created_by VARCHAR(255)   NOT NULL,
    updated    TIMESTAMP      NOT NULL,
    updated_by VARCHAR(255)   NOT NULL,
    version    INTEGER        NULL
);

CREATE TABLE IF NOT EXISTS reserved_items
(
    id          UUID PRIMARY KEY,

    order_id    UUID         NOT NULL,
    item_id     UUID         NOT NULL,
    quantity    INTEGER      NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    reserved_at TIMESTAMP    NOT NULL,

    created     TIMESTAMP    NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated     TIMESTAMP    NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    version     INTEGER      NULL
);

CREATE INDEX idx_order_id ON reserved_items(order_id);
CREATE INDEX idx_item_id ON reserved_items(item_id);

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
    version             INTEGER      NULL,
    CONSTRAINT uq_context_id_event_name UNIQUE (context_id, event_name)
);

CREATE INDEX idx_context_id ON outbox_events(context_id);
CREATE INDEX idx_status ON outbox_events(status);
CREATE INDEX idx_context_id_status ON outbox_events(context_id, status);
CREATE INDEX idx_context_id_event_name ON outbox_events(context_id, event_name);