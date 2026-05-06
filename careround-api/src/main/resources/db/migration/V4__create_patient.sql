-- ============================================================
-- V4: Patient
-- ============================================================

CREATE TABLE patient (
    id                      VARCHAR(36)  NOT NULL,
    hospital_id             VARCHAR(36)  NOT NULL,
    ward_id                 VARCHAR(36),
    bed_number              VARCHAR(20),
    medical_team_id         VARCHAR(36),
    admitting_consultant_id VARCHAR(36),
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    date_of_birth           DATE         NOT NULL,
    gender                  VARCHAR(20),
    hospital_number         VARCHAR(50)  NOT NULL,
    admission_date          DATETIME     NOT NULL,
    admission_type          VARCHAR(20)  NOT NULL,
    primary_diagnosis       TEXT,
    specialty_required      VARCHAR(100),
    acuity_level            VARCHAR(20)  NOT NULL DEFAULT 'LOW',
    news_score              INT          NOT NULL DEFAULT 0,
    is_discharge_ready      TINYINT(1)   NOT NULL DEFAULT 0,
    estimated_discharge_date DATE,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'ADMITTED',
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uq_patient_hospital_number (hospital_number),
    KEY idx_patient_ward           (ward_id),
    KEY idx_patient_team           (medical_team_id),
    KEY idx_patient_acuity_news    (acuity_level, news_score),
    CONSTRAINT fk_patient_hospital    FOREIGN KEY (hospital_id)             REFERENCES hospital     (id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_ward        FOREIGN KEY (ward_id)                 REFERENCES ward          (id) ON DELETE SET NULL,
    CONSTRAINT fk_patient_team        FOREIGN KEY (medical_team_id)         REFERENCES medical_team  (id) ON DELETE SET NULL,
    CONSTRAINT fk_patient_consultant  FOREIGN KEY (admitting_consultant_id) REFERENCES users         (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
