CREATE TABLE patient_vitals (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    patient_id          VARCHAR(36)  NOT NULL,
    hospital_id         VARCHAR(36)  NOT NULL,
    recorded_by_id      VARCHAR(36)  NOT NULL,
    heart_rate          INT,
    respiratory_rate    INT,
    oxygen_saturation   DECIMAL(5,2),
    systolic_bp         INT,
    temperature         DECIMAL(4,1),
    consciousness_level VARCHAR(20),
    computed_score      INT          NOT NULL,
    acuity_color        VARCHAR(10)  NOT NULL,
    recorded_at         DATETIME     NOT NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL
);
CREATE INDEX idx_vitals_patient_time ON patient_vitals(patient_id, recorded_at DESC);

CREATE TABLE clinical_note (
    id                     VARCHAR(36)  NOT NULL PRIMARY KEY,
    patient_id             VARCHAR(36)  NOT NULL,
    hospital_id            VARCHAR(36)  NOT NULL,
    author_id              VARCHAR(36)  NOT NULL,
    note_type              VARCHAR(30)  NOT NULL,
    content                LONGTEXT     NOT NULL,
    raw_transcription      LONGTEXT,
    is_ai_generated        BOOLEAN      NOT NULL DEFAULT FALSE,
    confirmed_by_doctor_at DATETIME,
    ai_model_used          VARCHAR(100),
    created_at             DATETIME     NOT NULL,
    updated_at             DATETIME     NOT NULL
);
CREATE INDEX idx_note_patient ON clinical_note(patient_id, created_at);
CREATE INDEX idx_note_hospital_patient ON clinical_note(hospital_id, patient_id, created_at);
