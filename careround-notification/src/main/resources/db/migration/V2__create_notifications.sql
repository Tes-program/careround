CREATE TABLE notifications (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_type      VARCHAR(100) NOT NULL,
    payload         LONGTEXT     NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    failure_reason  TEXT         NULL,
    sent_at         DATETIME     NOT NULL,
    retry_count     INT          NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_notifications_status_created
    ON notifications(status, created_at);

CREATE INDEX idx_notifications_event_type
    ON notifications(event_type);
