CREATE TABLE platform_operator (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(40)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    UNIQUE KEY uk_platform_operator_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hospital_onboarding_request (
    id                      VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_name           VARCHAR(255) NOT NULL,
    country_or_region       VARCHAR(120) NOT NULL,
    contact_email           VARCHAR(255) NOT NULL,
    contact_phone           VARCHAR(50)  NULL,
    hospital_type           VARCHAR(80)  NOT NULL,
    estimated_beds          VARCHAR(40)  NULL,
    primary_need            TEXT         NOT NULL,
    status                  VARCHAR(40)  NOT NULL,
    review_notes            TEXT         NULL,
    reviewed_by_user_id     VARCHAR(36)  NULL,
    reviewed_at             DATETIME     NULL,
    provisioned_hospital_id VARCHAR(36)  NULL,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_onboarding_status_created
    ON hospital_onboarding_request(status, created_at);

CREATE INDEX idx_onboarding_contact_email
    ON hospital_onboarding_request(contact_email);

CREATE TABLE account_activation_token (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    token_hash  VARCHAR(64) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    hospital_id VARCHAR(36) NOT NULL,
    expires_at  DATETIME    NOT NULL,
    used_at     DATETIME    NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    UNIQUE KEY uk_activation_token_hash (token_hash),
    INDEX idx_activation_user (user_id),
    INDEX idx_activation_hospital (hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
