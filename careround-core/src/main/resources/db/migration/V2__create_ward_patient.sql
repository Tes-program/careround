CREATE TABLE ward (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id VARCHAR(36)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    specialty   VARCHAR(100),
    total_beds  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL
);
CREATE INDEX idx_ward_hospital_id ON ward(hospital_id);

CREATE TABLE patient (
    id                       VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id              VARCHAR(36)  NOT NULL,
    ward_id                  VARCHAR(36),
    bed_number               VARCHAR(20),
    first_name               VARCHAR(100) NOT NULL,
    last_name                VARCHAR(100) NOT NULL,
    date_of_birth            DATE         NOT NULL,
    gender                   VARCHAR(20),
    hospital_number          VARCHAR(50)  NOT NULL,
    admission_date           DATETIME     NOT NULL,
    admission_type           VARCHAR(20)  NOT NULL,
    primary_diagnosis        TEXT,
    acuity_color             VARCHAR(10)  NOT NULL DEFAULT 'GREEN',
    status                   VARCHAR(15)  NOT NULL DEFAULT 'ADMITTED',
    estimated_discharge_date DATE,
    created_at               DATETIME     NOT NULL,
    updated_at               DATETIME     NOT NULL,
    CONSTRAINT uk_patient_hospital_number UNIQUE (hospital_number)
);
CREATE INDEX idx_patient_hospital_ward ON patient(hospital_id, ward_id);
CREATE INDEX idx_patient_hospital_acuity ON patient(hospital_id, ward_id, acuity_color);
