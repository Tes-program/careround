CREATE TABLE handovers (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    ward_id VARCHAR(36) NOT NULL,
    outgoing_shift_id VARCHAR(36) NOT NULL,
    incoming_shift_id VARCHAR(36) NOT NULL,
    conducted_by_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    general_notes TEXT,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_handover_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_handover_ward FOREIGN KEY (ward_id) REFERENCES wards(id),
    CONSTRAINT fk_handover_outgoing FOREIGN KEY (outgoing_shift_id) REFERENCES shifts(id),
    CONSTRAINT fk_handover_incoming FOREIGN KEY (incoming_shift_id) REFERENCES shifts(id),
    CONSTRAINT fk_handover_conductor FOREIGN KEY (conducted_by_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_handover_notes (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    handover_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    status_summary TEXT,
    outstanding_task_ids TEXT,
    urgency_flag TINYINT(1) NOT NULL DEFAULT 0,
    added_by_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_phn_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_phn_handover FOREIGN KEY (handover_id) REFERENCES handovers(id),
    CONSTRAINT fk_phn_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_phn_author FOREIGN KEY (added_by_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
