CREATE TABLE patient_vitals (
    id                      VARCHAR(36)     NOT NULL PRIMARY KEY,
    patient_id              VARCHAR(36)     NOT NULL,
    recorded_by_id          VARCHAR(36)     NOT NULL,
    heart_rate              INT             NULL,
    respiratory_rate        INT             NULL,
    oxygen_saturation       DECIMAL(5,2)    NULL,
    systolic_bp             INT             NULL,
    temperature             DECIMAL(4,1)    NULL,
    consciousness_level     VARCHAR(20)     NULL,
    news_score              INT             NOT NULL,
    recorded_at             DATETIME        NOT NULL,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE next_of_kin (
    id                          VARCHAR(36)  NOT NULL PRIMARY KEY,
    patient_id                  VARCHAR(36)  NOT NULL,
    name                        VARCHAR(255) NOT NULL,
    relationship                VARCHAR(100) NULL,
    phone                       VARCHAR(50)  NULL,
    email                       VARCHAR(255) NULL,
    preferred_contact_method    VARCHAR(10)  NOT NULL DEFAULT 'SMS',
    is_emergency_contact        BOOLEAN      NOT NULL DEFAULT FALSE,
    notification_consent        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at                  DATETIME     NOT NULL,
    updated_at                  DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
