CREATE TABLE failed_notifications (
    id VARCHAR(36) PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    payload TEXT NOT NULL,
    failure_reason TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    resolved TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    resolved_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
