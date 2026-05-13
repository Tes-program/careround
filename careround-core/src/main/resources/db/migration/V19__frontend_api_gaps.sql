CREATE TABLE password_reset_token (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  DATETIME    NOT NULL,
    used_at     DATETIME    NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    INDEX idx_password_reset_token_hash_used (token_hash, used_at),
    INDEX idx_password_reset_user (hospital_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_read_receipt (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36)  NOT NULL,
    user_id         VARCHAR(36)  NOT NULL,
    notification_id VARCHAR(120) NOT NULL,
    read_at         DATETIME     NOT NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    UNIQUE KEY uk_notification_read_user (hospital_id, user_id, notification_id),
    INDEX idx_notification_read_user (hospital_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE clinical_note
    ADD COLUMN vitals_id VARCHAR(36) NULL AFTER patient_round_review_id,
    ADD INDEX idx_clinical_note_vitals (vitals_id);
