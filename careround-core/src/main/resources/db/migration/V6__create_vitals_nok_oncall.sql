CREATE TABLE patient_vitals (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    recorded_by_id VARCHAR(36) NOT NULL,
    heart_rate INT,
    respiratory_rate INT,
    oxygen_saturation DECIMAL(5,2),
    systolic_bp INT,
    temperature DECIMAL(4,1),
    consciousness_level VARCHAR(50) NOT NULL,
    news_score INT NOT NULL,
    recorded_at DATETIME NOT NULL,
    CONSTRAINT fk_vitals_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_vitals_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_vitals_recorded_by FOREIGN KEY (recorded_by_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE next_of_kin (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    preferred_contact_method VARCHAR(50) NOT NULL DEFAULT 'SMS',
    is_emergency_contact TINYINT(1) NOT NULL DEFAULT 0,
    notification_consent TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_nok_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_nok_patient FOREIGN KEY (patient_id) REFERENCES patients(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE on_call_rotations (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    department_id VARCHAR(36) NOT NULL,
    ward_id VARCHAR(36),
    doctor_id VARCHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_oncall_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_oncall_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_oncall_ward FOREIGN KEY (ward_id) REFERENCES wards(id),
    CONSTRAINT fk_oncall_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
