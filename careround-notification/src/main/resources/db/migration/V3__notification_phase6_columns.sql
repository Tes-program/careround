ALTER TABLE notifications
    ADD COLUMN hospital_id VARCHAR(36) NULL AFTER event_type,
    ADD COLUMN recipient_id VARCHAR(36) NULL AFTER hospital_id,
    ADD COLUMN recipient_type VARCHAR(20) NULL AFTER recipient_id,
    ADD COLUMN channel VARCHAR(20) NULL AFTER recipient_type,
    ADD COLUMN subject VARCHAR(255) NULL AFTER channel,
    ADD COLUMN body TEXT NULL AFTER subject,
    ADD COLUMN correlation_id VARCHAR(100) NULL AFTER body,
    MODIFY COLUMN payload LONGTEXT NULL,
    MODIFY COLUMN sent_at DATETIME NULL;

CREATE INDEX idx_notifications_correlation
    ON notifications(correlation_id);

CREATE INDEX idx_notifications_correlation_recipient_channel
    ON notifications(correlation_id, recipient_id, channel);

ALTER TABLE failed_notifications
    ADD COLUMN topic VARCHAR(255) NULL AFTER event_type,
    ADD COLUMN hospital_id VARCHAR(36) NULL AFTER topic,
    ADD COLUMN correlation_id VARCHAR(100) NULL AFTER hospital_id;
