CREATE TABLE handover_note (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    patient_id  VARCHAR(36) NOT NULL,
    hospital_id VARCHAR(36) NOT NULL,
    author_id   VARCHAR(36) NOT NULL,
    content     TEXT        NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL
);
CREATE INDEX idx_handover_note_patient ON handover_note(patient_id, created_at);

CREATE TABLE processed_event (
    event_id     VARCHAR(36) NOT NULL PRIMARY KEY,
    processed_at DATETIME    NOT NULL
);
