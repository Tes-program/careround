CREATE TABLE hospital_onboarding_request (
    id                      VARCHAR(36)  PRIMARY KEY,
    hospital_name           VARCHAR(255) NOT NULL,
    country_or_region       VARCHAR(120) NOT NULL,
    contact_email           VARCHAR(255) NOT NULL,
    contact_phone           VARCHAR(50),
    hospital_type           VARCHAR(80)  NOT NULL,
    estimated_beds          VARCHAR(40),
    primary_need            TEXT         NOT NULL,
    status                  VARCHAR(40)  NOT NULL,
    review_notes            TEXT,
    reviewed_by_user_id     VARCHAR(36),
    reviewed_at             DATETIME,
    provisioned_hospital_id VARCHAR(36),
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL
);

CREATE INDEX idx_onboarding_status_created
    ON hospital_onboarding_request (status, created_at);

CREATE INDEX idx_onboarding_contact_email
    ON hospital_onboarding_request (contact_email);

CREATE TABLE activation_token (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME     NOT NULL,
    used       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL
);

CREATE INDEX idx_activation_token_user
    ON activation_token (user_id);
