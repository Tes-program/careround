-- ============================================================
-- V6: ShiftSchedule, Shift
-- ============================================================

CREATE TABLE shift_schedule (
    id            VARCHAR(36) NOT NULL,
    hospital_id   VARCHAR(36) NOT NULL,
    ward_id       VARCHAR(36),            -- NULL = applies to all wards
    shift_type    VARCHAR(10) NOT NULL,
    start_time    TIME        NOT NULL,
    end_time      TIME        NOT NULL,
    days_of_week  VARCHAR(50) NOT NULL,   -- e.g. 'MON,TUE,WED,THU,FRI'
    is_active     TINYINT(1)  NOT NULL DEFAULT 1,
    created_at    DATETIME    NOT NULL,
    updated_at    DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_hospital FOREIGN KEY (hospital_id) REFERENCES hospital (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_ward     FOREIGN KEY (ward_id)     REFERENCES ward     (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shift (
    id                 VARCHAR(36) NOT NULL,
    ward_id            VARCHAR(36) NOT NULL,
    shift_schedule_id  VARCHAR(36),
    type               VARCHAR(10) NOT NULL,
    start_time         DATETIME    NOT NULL,
    end_time           DATETIME    NOT NULL,
    lead_doctor_id     VARCHAR(36),
    nurse_in_charge_id VARCHAR(36),
    status             VARCHAR(25) NOT NULL DEFAULT 'PENDING_ASSIGNMENT',
    assigned_at        DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uq_shift_ward_type_start (ward_id, type, start_time),
    KEY idx_shift_status (status),
    CONSTRAINT fk_shift_ward      FOREIGN KEY (ward_id)           REFERENCES ward           (id),
    CONSTRAINT fk_shift_schedule  FOREIGN KEY (shift_schedule_id) REFERENCES shift_schedule (id) ON DELETE SET NULL,
    CONSTRAINT fk_shift_doctor    FOREIGN KEY (lead_doctor_id)    REFERENCES users           (id) ON DELETE SET NULL,
    CONSTRAINT fk_shift_nurse     FOREIGN KEY (nurse_in_charge_id)REFERENCES users           (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
