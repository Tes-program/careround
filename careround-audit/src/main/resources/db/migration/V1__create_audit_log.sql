CREATE TABLE audit_log (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_type      VARCHAR(100) NOT NULL,
    hospital_id     VARCHAR(36)  NOT NULL,
    correlation_id  VARCHAR(36)  NULL,
    payload         LONGTEXT     NULL,
    processed_at    DATETIME     NOT NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    INDEX idx_audit_hospital_type_created (hospital_id, event_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
