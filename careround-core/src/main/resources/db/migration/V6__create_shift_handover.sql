CREATE TABLE shift (
    id                  VARCHAR(36) NOT NULL PRIMARY KEY,
    ward_id             VARCHAR(36) NOT NULL,
    shift_schedule_id   VARCHAR(36) NULL,
    type                VARCHAR(10) NOT NULL,
    start_time          DATETIME    NOT NULL,
    end_time            DATETIME    NOT NULL,
    lead_doctor_id      VARCHAR(36) NULL,
    nurse_in_charge_id  VARCHAR(36) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING_ASSIGNMENT',
    assigned_at         DATETIME    NULL,
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME    NOT NULL,
    UNIQUE KEY uq_shift_ward_type_start (ward_id, type, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE handover (
    id                  VARCHAR(36) NOT NULL PRIMARY KEY,
    ward_id             VARCHAR(36) NOT NULL,
    outgoing_shift_id   VARCHAR(36) NOT NULL,
    incoming_shift_id   VARCHAR(36) NOT NULL,
    conducted_by_id     VARCHAR(36) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    general_notes       TEXT        NULL,
    completed_at        DATETIME    NULL,
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient_handover_note (
    id                      VARCHAR(36) NOT NULL PRIMARY KEY,
    handover_id             VARCHAR(36) NOT NULL,
    patient_id              VARCHAR(36) NOT NULL,
    status_summary          TEXT        NULL,
    outstanding_task_ids    TEXT        NULL,
    urgency_flag            BOOLEAN     NOT NULL DEFAULT FALSE,
    added_by_id             VARCHAR(36) NOT NULL,
    created_at              DATETIME    NOT NULL,
    updated_at              DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
