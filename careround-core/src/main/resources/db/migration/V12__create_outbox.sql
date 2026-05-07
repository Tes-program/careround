CREATE TABLE outbox_event (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36)  NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         LONGTEXT     NOT NULL,
    published       BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at    DATETIME     NULL,
    correlation_id  VARCHAR(36)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    INDEX idx_outbox_published_created (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
