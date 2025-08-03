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
