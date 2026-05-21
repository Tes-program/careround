CREATE TABLE hospital (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    code          VARCHAR(20)  NOT NULL,
    address       TEXT,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT uk_hospital_code UNIQUE (code),
    CONSTRAINT uk_hospital_email UNIQUE (contact_email)
);

CREATE TABLE system_configuration (
    id                            VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id                   VARCHAR(36) NOT NULL,
    acuity_amber_threshold        INT         NOT NULL DEFAULT 5,
    acuity_red_threshold          INT         NOT NULL DEFAULT 7,
    task_overdue_reminder_minutes INT         NOT NULL DEFAULT 10,
    task_escalation_minutes       INT         NOT NULL DEFAULT 20,
    push_notifications_enabled    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at                    DATETIME    NOT NULL,
    updated_at                    DATETIME    NOT NULL,
    CONSTRAINT uk_sysconfig_hospital UNIQUE (hospital_id)
);

CREATE TABLE users (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id   VARCHAR(36)  NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    fcm_token     VARCHAR(512),
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT uk_users_hospital_email UNIQUE (hospital_id, email)
);
CREATE INDEX idx_users_hospital_id ON users(hospital_id);

CREATE TABLE refresh_tokens (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    hospital_id VARCHAR(36)  NOT NULL,
    token       VARCHAR(512) NOT NULL,
    expires_at  DATETIME     NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT uk_refresh_token UNIQUE (token)
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
