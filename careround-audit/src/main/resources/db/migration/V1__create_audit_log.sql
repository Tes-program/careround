CREATE TABLE audit_log (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_id       VARCHAR(36)  NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    hospital_id    VARCHAR(36)  NOT NULL,
    correlation_id VARCHAR(36),
    payload        LONGTEXT     NOT NULL,
    received_at    DATETIME     NOT NULL,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL,
    CONSTRAINT uk_audit_event_id UNIQUE (event_id)
);
CREATE INDEX idx_audit_hospital_event ON audit_log(hospital_id, event_type, received_at);
