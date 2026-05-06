-- ============================================================
-- V8: Round, PatientRoundReview
-- ============================================================

CREATE TABLE round (
    id              VARCHAR(36) NOT NULL,
    ward_id         VARCHAR(36) NOT NULL,
    medical_team_id VARCHAR(36) NOT NULL,
    shift_id        VARCHAR(36) NOT NULL,
    round_type      VARCHAR(20) NOT NULL,
    lead_doctor_id  VARCHAR(36) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_time  DATETIME,
    started_at      DATETIME,
    completed_at    DATETIME,
    team_members    TEXT,                -- comma-separated User UUIDs
    created_at      DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_round_ward_status  (ward_id, status),
    KEY idx_round_team         (medical_team_id),
    CONSTRAINT fk_round_ward    FOREIGN KEY (ward_id)         REFERENCES ward         (id),
    CONSTRAINT fk_round_team    FOREIGN KEY (medical_team_id) REFERENCES medical_team (id),
    CONSTRAINT fk_round_shift   FOREIGN KEY (shift_id)        REFERENCES shift        (id),
    CONSTRAINT fk_round_doctor  FOREIGN KEY (lead_doctor_id)  REFERENCES users        (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_round_review (
    id                    VARCHAR(36) NOT NULL,
    round_id              VARCHAR(36) NOT NULL,
    patient_id            VARCHAR(36) NOT NULL,
    reviewed_by_id        VARCHAR(36) NOT NULL,
    review_order          INT         NOT NULL,
    news_score_at_review  INT         NOT NULL,
    clinical_status       VARCHAR(20) NOT NULL,
    was_examined          TINYINT(1)  NOT NULL DEFAULT 1,
    management_plan       TEXT,
    discharge_assessment  VARCHAR(25) NOT NULL DEFAULT 'NONE',
    notified_next_of_kin  TINYINT(1)  NOT NULL DEFAULT 0,
    reviewed_at           DATETIME    NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_review_round_patient (round_id, patient_id),
    KEY idx_review_patient (patient_id),
    CONSTRAINT fk_review_round    FOREIGN KEY (round_id)       REFERENCES round   (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_patient  FOREIGN KEY (patient_id)     REFERENCES patient (id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewed_by_id) REFERENCES users   (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
