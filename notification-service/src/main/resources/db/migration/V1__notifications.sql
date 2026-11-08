CREATE TABLE t_notifications (
    event_id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(36) NOT NULL,
    email VARCHAR(254) NOT NULL,
    total VARCHAR(64) NOT NULL,
    next_attempt_at TIMESTAMP(6) NOT NULL,
    sent_at TIMESTAMP(6) NULL,
    attempts INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_notification_pending ON t_notifications(sent_at, next_attempt_at);
