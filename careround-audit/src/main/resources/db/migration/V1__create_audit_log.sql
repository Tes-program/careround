CREATE TABLE audit_log (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    entity_id VARCHAR(36),
    actor_id VARCHAR(36),
    correlation_id VARCHAR(36) NOT NULL,
    payload TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_audit_hospital_date (hospital_id, created_at),
    INDEX idx_audit_correlation (correlation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
