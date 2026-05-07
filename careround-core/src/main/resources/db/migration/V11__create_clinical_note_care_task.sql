CREATE TABLE clinical_note (
    id                      VARCHAR(36) NOT NULL PRIMARY KEY,
    patient_id              VARCHAR(36) NOT NULL,
    patient_round_review_id VARCHAR(36) NULL,
    author_id               VARCHAR(36) NOT NULL,
    note_type               VARCHAR(25) NOT NULL,
    content                 TEXT        NOT NULL,
    is_amended              BOOLEAN     NOT NULL DEFAULT FALSE,
    amended_by_id           VARCHAR(36) NULL,
    amended_at              DATETIME    NULL,
    created_at              DATETIME    NOT NULL,
    updated_at              DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE care_task (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id         VARCHAR(36)  NOT NULL,
    patient_id          VARCHAR(36)  NOT NULL,
    ward_id             VARCHAR(36)  NOT NULL,
    round_id            VARCHAR(36)  NULL,
    created_by_id       VARCHAR(36)  NOT NULL,
    assigned_to_id      VARCHAR(36)  NULL,
    assigned_to_role    VARCHAR(20)  NULL,
    task_type           VARCHAR(100) NOT NULL,
    source              VARCHAR(25)  NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT         NULL,
    priority            VARCHAR(15)  NOT NULL DEFAULT 'ROUTINE',
    window_start        DATETIME     NULL,
    window_end          DATETIME     NULL,
    status              VARCHAR(15)  NOT NULL DEFAULT 'PENDING',
    completed_by_id     VARCHAR(36)  NULL,
    completed_at        DATETIME     NULL,
    escalated_at        DATETIME     NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
