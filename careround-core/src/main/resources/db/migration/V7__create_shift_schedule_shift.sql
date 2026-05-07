CREATE TABLE shift_schedules (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    ward_id VARCHAR(36),
    shift_type VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days_of_week VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_shiftsched_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_shiftsched_ward FOREIGN KEY (ward_id) REFERENCES wards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shifts (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    ward_id VARCHAR(36) NOT NULL,
    shift_schedule_id VARCHAR(36),
    type VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    lead_doctor_id VARCHAR(36),
    nurse_in_charge_id VARCHAR(36),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_ASSIGNMENT',
    assigned_at DATETIME,
    UNIQUE KEY uk_shift_ward_type_start (ward_id, type, start_time),
    CONSTRAINT fk_shift_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_shift_ward FOREIGN KEY (ward_id) REFERENCES wards(id),
    CONSTRAINT fk_shift_schedule FOREIGN KEY (shift_schedule_id) REFERENCES shift_schedules(id),
    CONSTRAINT fk_shift_lead_doctor FOREIGN KEY (lead_doctor_id) REFERENCES users(id),
    CONSTRAINT fk_shift_nurse FOREIGN KEY (nurse_in_charge_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
