CREATE TABLE round (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36) NOT NULL,
    ward_id         VARCHAR(36) NOT NULL,
    medical_team_id VARCHAR(36) NOT NULL,
    shift_id        VARCHAR(36) NOT NULL,
    round_type      VARCHAR(20) NOT NULL,
    lead_doctor_id  VARCHAR(36) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_time  DATETIME    NULL,
    started_at      DATETIME    NULL,
    completed_at    DATETIME    NULL,
    team_members    TEXT        NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient_round_review (
    id                      VARCHAR(36) NOT NULL PRIMARY KEY,
    round_id                VARCHAR(36) NOT NULL,
    patient_id              VARCHAR(36) NOT NULL,
    reviewed_by_id          VARCHAR(36) NOT NULL,
    review_order            INT         NOT NULL,
    news_score_at_review    INT         NULL,
    clinical_status         VARCHAR(20) NOT NULL,
    was_examined            BOOLEAN     NOT NULL DEFAULT FALSE,
    management_plan         TEXT        NULL,
    discharge_assessment    VARCHAR(20) NOT NULL DEFAULT 'NONE',
    notified_next_of_kin    BOOLEAN     NOT NULL DEFAULT FALSE,
    reviewed_at             DATETIME    NOT NULL,
    created_at              DATETIME    NOT NULL,
    updated_at              DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
