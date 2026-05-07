CREATE TABLE escalations (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    triggered_by_id VARCHAR(36),
    trigger_type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    assigned_to_id VARCHAR(36),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    notes TEXT,
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_escalation_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_escalation_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_escalation_triggerer FOREIGN KEY (triggered_by_id) REFERENCES users(id),
    CONSTRAINT fk_escalation_assignee FOREIGN KEY (assigned_to_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    payload TEXT NOT NULL,
    published TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    published_at DATETIME,
    CONSTRAINT fk_outbox_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
