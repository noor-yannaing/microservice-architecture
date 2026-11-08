ALTER TABLE t_orders
    MODIFY order_number VARCHAR(36) NOT NULL,
    MODIFY sku_code VARCHAR(64) NOT NULL,
    MODIFY price DECIMAL(19,2) NOT NULL,
    MODIFY quantity INT NOT NULL,
    ADD customer_id VARCHAR(128) NOT NULL DEFAULT 'legacy',
    ADD idempotency_key VARCHAR(128) NULL,
    ADD email VARCHAR(254) NOT NULL DEFAULT 'legacy@example.invalid',
    ADD status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    ADD created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD next_attempt_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD attempts INT NOT NULL DEFAULT 0;
UPDATE t_orders SET idempotency_key = order_number;
ALTER TABLE t_orders MODIFY idempotency_key VARCHAR(128) NOT NULL;
ALTER TABLE t_orders ADD CONSTRAINT uk_order_number UNIQUE (order_number);
ALTER TABLE t_orders ADD CONSTRAINT uk_order_idempotency UNIQUE (customer_id, idempotency_key);
ALTER TABLE t_orders ADD CONSTRAINT ck_order_quantity CHECK (quantity > 0);
CREATE INDEX idx_orders_pending ON t_orders(status, next_attempt_at);
CREATE TABLE t_outbox (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(36) NOT NULL UNIQUE,
    occurred_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6) NULL,
    CONSTRAINT fk_outbox_order FOREIGN KEY (order_number) REFERENCES t_orders(order_number)
);
CREATE INDEX idx_outbox_unpublished ON t_outbox(published_at, occurred_at);
