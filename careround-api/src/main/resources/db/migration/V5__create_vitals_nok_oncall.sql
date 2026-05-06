-- ============================================================
-- V5: PatientVitals, NextOfKin, OnCallRotation
-- ============================================================

CREATE TABLE patient_vitals (
    id                   VARCHAR(36)    NOT NULL,
    patient_id           VARCHAR(36)    NOT NULL,
    recorded_by_id       VARCHAR(36)    NOT NULL,
    heart_rate           INT            NOT NULL,
    respiratory_rate     INT            NOT NULL,
    oxygen_saturation    DECIMAL(5,2)   NOT NULL,
    systolic_bp          INT            NOT NULL,
    temperature          DECIMAL(4,1)   NOT NULL,
    consciousness_level  VARCHAR(20)    NOT NULL,
    news_score           INT            NOT NULL,
    recorded_at          DATETIME       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_vitals_patient_time (patient_id, recorded_at),
    CONSTRAINT fk_vitals_patient     FOREIGN KEY (patient_id)     REFERENCES patient (id) ON DELETE CASCADE,
    CONSTRAINT fk_vitals_recorder    FOREIGN KEY (recorded_by_id) REFERENCES users   (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE next_of_kin (
    id                        VARCHAR(36)  NOT NULL,
    patient_id                VARCHAR(36)  NOT NULL,
    name                      VARCHAR(150) NOT NULL,
    relationship              VARCHAR(80)  NOT NULL,
    phone                     VARCHAR(30),
    email                     VARCHAR(255),
    preferred_contact_method  VARCHAR(10)  NOT NULL DEFAULT 'SMS',
    is_emergency_contact      TINYINT(1)   NOT NULL DEFAULT 0,
    notification_consent      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_nok_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE on_call_rotation (
    id             VARCHAR(36) NOT NULL,
    hospital_id    VARCHAR(36) NOT NULL,
    department_id  VARCHAR(36) NOT NULL,
    ward_id        VARCHAR(36),
    doctor_id      VARCHAR(36) NOT NULL,
    role           VARCHAR(30) NOT NULL,
    start_time     DATETIME    NOT NULL,
    end_time       DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_oncall_dept_role_time (department_id, role, start_time, end_time),
    CONSTRAINT fk_oncall_hospital    FOREIGN KEY (hospital_id)   REFERENCES hospital   (id) ON DELETE CASCADE,
    CONSTRAINT fk_oncall_department  FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_oncall_ward        FOREIGN KEY (ward_id)       REFERENCES ward       (id) ON DELETE SET NULL,
    CONSTRAINT fk_oncall_doctor      FOREIGN KEY (doctor_id)     REFERENCES users      (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
