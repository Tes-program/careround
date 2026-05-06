-- ============================================================
-- V7: Handover, PatientHandoverNote
-- ============================================================

CREATE TABLE handover (
    id                 VARCHAR(36) NOT NULL,
    ward_id            VARCHAR(36) NOT NULL,
    outgoing_shift_id  VARCHAR(36) NOT NULL,
    incoming_shift_id  VARCHAR(36) NOT NULL,
    conducted_by_id    VARCHAR(36) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    general_notes      TEXT,
    completed_at       DATETIME,
    created_at         DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_handover_ward (ward_id),
    CONSTRAINT fk_handover_ward           FOREIGN KEY (ward_id)           REFERENCES ward  (id),
    CONSTRAINT fk_handover_outgoing_shift FOREIGN KEY (outgoing_shift_id) REFERENCES shift (id),
    CONSTRAINT fk_handover_incoming_shift FOREIGN KEY (incoming_shift_id) REFERENCES shift (id),
    CONSTRAINT fk_handover_conductor      FOREIGN KEY (conducted_by_id)   REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_handover_note (
    id                   VARCHAR(36) NOT NULL,
    handover_id          VARCHAR(36) NOT NULL,
    patient_id           VARCHAR(36) NOT NULL,
    status_summary       TEXT        NOT NULL,
    outstanding_task_ids TEXT,               -- comma-separated CareTask UUIDs
    urgency_flag         TINYINT(1)  NOT NULL DEFAULT 0,
    added_by_id          VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_phn_handover (handover_id),
    CONSTRAINT fk_phn_handover FOREIGN KEY (handover_id) REFERENCES handover (id) ON DELETE CASCADE,
    CONSTRAINT fk_phn_patient  FOREIGN KEY (patient_id)  REFERENCES patient  (id),
    CONSTRAINT fk_phn_author   FOREIGN KEY (added_by_id) REFERENCES users    (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
