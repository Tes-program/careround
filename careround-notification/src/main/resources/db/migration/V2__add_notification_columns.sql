ALTER TABLE failed_notifications
    ADD COLUMN hospital_id   VARCHAR(36)  NULL,
    ADD COLUMN correlation_id VARCHAR(100) NULL,
    ADD COLUMN topic          VARCHAR(255) NULL;
