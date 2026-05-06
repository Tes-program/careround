-- ============================================================
-- V10: Escalation, OutboxEvent
-- ============================================================

CREATE TABLE escalation (
    id               VARCHAR(36) NOT NULL,
    hospital_id      VARCHAR(36) NOT NULL,
    patient_id       VARCHAR(36) NOT NULL,
    triggered_by_id  VARCHAR(36),            -- NULL for system-generated
    trigger_type     VARCHAR(25) NOT NULL,
    severity         VARCHAR(10) NOT NULL,
    assigned_to_id   VARCHAR(36),
    status           VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    notes            TEXT,
    resolved_at      DATETIME,
    created_at       DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_escalation_hospital_status (hospital_id, status),
    KEY idx_escalation_patient         (patient_id),
    KEY idx_escalation_created         (created_at),
    CONSTRAINT fk_esc_hospital   FOREIGN KEY (hospital_id)     REFERENCES hospital (id) ON DELETE CASCADE,
    CONSTRAINT fk_esc_patient    FOREIGN KEY (patient_id)      REFERENCES patient  (id),
    CONSTRAINT fk_esc_trigger    FOREIGN KEY (triggered_by_id) REFERENCES users    (id) ON DELETE SET NULL,
    CONSTRAINT fk_esc_assignee   FOREIGN KEY (assigned_to_id)  REFERENCES users    (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Transactional Outbox — the critical reliability guarantee
-- OutboxPoller reads published=0 rows, publishes to Kafka, then sets published=1.
-- Never delete rows from this table (use a retention job in production).
CREATE TABLE outbox_event (
    id           VARCHAR(36)  NOT NULL,
    hospital_id  VARCHAR(36)  NOT NULL,
    event_type   VARCHAR(60)  NOT NULL,
    payload      TEXT         NOT NULL,
    published    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL,
    published_at DATETIME,
    PRIMARY KEY (id),
    KEY idx_outbox_unpublished (published, created_at)   -- hot path for poller
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
