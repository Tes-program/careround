ALTER TABLE audit_log
    ADD COLUMN kafka_topic VARCHAR(255) NULL,
    ADD COLUMN kafka_partition INT NULL,
    ADD COLUMN kafka_offset BIGINT NULL,
    ADD COLUMN received_at DATETIME NULL;

CREATE INDEX idx_audit_correlation
    ON audit_log(correlation_id);
