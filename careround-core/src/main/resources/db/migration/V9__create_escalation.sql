CREATE TABLE escalation (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36) NOT NULL,
    patient_id      VARCHAR(36) NOT NULL,
    triggered_by_id VARCHAR(36) NULL,
    trigger_type    VARCHAR(30) NOT NULL,
    severity        VARCHAR(10) NOT NULL,
    assigned_to_id  VARCHAR(36) NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    notes           TEXT        NULL,
    resolved_at     DATETIME    NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
