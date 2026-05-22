CREATE TABLE prescription (
    id                   VARCHAR(36)  NOT NULL PRIMARY KEY,
    patient_id           VARCHAR(36)  NOT NULL,
    hospital_id          VARCHAR(36)  NOT NULL,
    clinical_note_id     VARCHAR(36),
    drug_name            VARCHAR(255) NOT NULL,
    dose                 VARCHAR(50)  NOT NULL,
    route                VARCHAR(50)  NOT NULL,
    frequency_string     VARCHAR(100) NOT NULL,
    frequency_hours      INT          NOT NULL,
    total_doses          INT          NOT NULL,
    start_time           DATETIME     NOT NULL,
    administration_times LONGTEXT     NOT NULL,
    confirmed_by_id      VARCHAR(36)  NOT NULL,
    confirmed_at         DATETIME     NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at           DATETIME     NOT NULL,
    updated_at           DATETIME     NOT NULL
);
CREATE INDEX idx_prescription_patient ON prescription(patient_id);
CREATE INDEX idx_prescription_hospital_patient ON prescription(hospital_id, patient_id);

CREATE TABLE medication_chart (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    patient_id      VARCHAR(36) NOT NULL,
    hospital_id     VARCHAR(36) NOT NULL,
    prescription_id VARCHAR(36) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    nurse_notes     TEXT,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
);
CREATE INDEX idx_chart_patient ON medication_chart(patient_id, status);

CREATE TABLE medication_task (
    id                    VARCHAR(36) NOT NULL PRIMARY KEY,
    medication_chart_id   VARCHAR(36) NOT NULL,
    patient_id            VARCHAR(36) NOT NULL,
    hospital_id           VARCHAR(36) NOT NULL,
    ward_id               VARCHAR(36) NOT NULL,
    assigned_nurse_id     VARCHAR(36),
    scheduled_time        DATETIME    NOT NULL,
    status                VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    completed_at          DATETIME,
    completed_by_id       VARCHAR(36),
    actual_dose_given     VARCHAR(50),
    pre_reminder_sent_at  DATETIME,
    overdue_alert_sent_at DATETIME,
    created_at            DATETIME    NOT NULL,
    updated_at            DATETIME    NOT NULL
);
CREATE INDEX idx_task_hospital_status_time ON medication_task(hospital_id, status, scheduled_time);
CREATE INDEX idx_task_ward_status ON medication_task(ward_id, status, scheduled_time);
CREATE INDEX idx_task_nurse_status ON medication_task(assigned_nurse_id, status, scheduled_time);
CREATE INDEX idx_task_reminder_window ON medication_task(status, scheduled_time, pre_reminder_sent_at);
