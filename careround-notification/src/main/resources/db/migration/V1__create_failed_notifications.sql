CREATE TABLE failed_notifications (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_type      VARCHAR(100) NOT NULL,
    payload         LONGTEXT     NULL,
    error_message   TEXT         NULL,
    failed_at       DATETIME     NOT NULL,
    retry_count     INT          NOT NULL DEFAULT 0,
    resolved        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
