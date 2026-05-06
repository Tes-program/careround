-- ============================================================
-- V9: ClinicalNote (immutable), CareTask
-- ============================================================

CREATE TABLE clinical_note (
    id                       VARCHAR(36) NOT NULL,
    patient_id               VARCHAR(36) NOT NULL,
    patient_round_review_id  VARCHAR(36),         -- NULL for standalone notes
    author_id                VARCHAR(36) NOT NULL,
    note_type                VARCHAR(25) NOT NULL,
    content                  TEXT        NOT NULL,
    is_amended               TINYINT(1)  NOT NULL DEFAULT 0,
    amended_by_id            VARCHAR(36),
    amended_at               DATETIME,
    created_at               DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_note_patient_time  (patient_id, created_at),
    CONSTRAINT fk_note_patient   FOREIGN KEY (patient_id)              REFERENCES patient           (id),
    CONSTRAINT fk_note_review    FOREIGN KEY (patient_round_review_id) REFERENCES patient_round_review (id) ON DELETE SET NULL,
    CONSTRAINT fk_note_author    FOREIGN KEY (author_id)               REFERENCES users             (id),
    CONSTRAINT fk_note_amender   FOREIGN KEY (amended_by_id)           REFERENCES users             (id)
    -- No DELETE constraint — clinical notes are never deleted
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE care_task (
    id               VARCHAR(36)  NOT NULL,
    patient_id       VARCHAR(36)  NOT NULL,
    ward_id          VARCHAR(36)  NOT NULL,
    round_id         VARCHAR(36),              -- NULL for NURSING_CARE_PLAN tasks
    created_by_id    VARCHAR(36)  NOT NULL,
    assigned_to_id   VARCHAR(36),
    assigned_to_role VARCHAR(20),
    task_type        VARCHAR(100),
    source           VARCHAR(25)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    priority         VARCHAR(15)  NOT NULL DEFAULT 'ROUTINE',
    window_start     DATETIME,
    window_end       DATETIME,
    status           VARCHAR(15)  NOT NULL DEFAULT 'PENDING',
    completed_by_id  VARCHAR(36),
    completed_at     DATETIME,
    escalated_at     DATETIME,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task_ward_status   (ward_id, status),
    KEY idx_task_patient       (patient_id),
    KEY idx_task_assigned      (assigned_to_id),
    KEY idx_task_window_end    (window_end, status),
    CONSTRAINT fk_task_patient    FOREIGN KEY (patient_id)     REFERENCES patient (id),
    CONSTRAINT fk_task_ward       FOREIGN KEY (ward_id)        REFERENCES ward    (id),
    CONSTRAINT fk_task_round      FOREIGN KEY (round_id)       REFERENCES round   (id) ON DELETE SET NULL,
    CONSTRAINT fk_task_creator    FOREIGN KEY (created_by_id)  REFERENCES users   (id),
    CONSTRAINT fk_task_assignee   FOREIGN KEY (assigned_to_id) REFERENCES users   (id) ON DELETE SET NULL,
    CONSTRAINT fk_task_completer  FOREIGN KEY (completed_by_id)REFERENCES users   (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
