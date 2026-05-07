CREATE TABLE hospital (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    address         TEXT         NULL,
    contact_email   VARCHAR(255) NOT NULL,
    contact_phone   VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    UNIQUE KEY uq_hospital_email (contact_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_configuration (
    id                              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id                     VARCHAR(36) NOT NULL,
    news_amber_threshold            INT         NOT NULL DEFAULT 5,
    news_red_threshold              INT         NOT NULL DEFAULT 7,
    task_overdue_grace_minutes      INT         NOT NULL DEFAULT 30,
    round_notifications_enabled     BOOLEAN     NOT NULL DEFAULT TRUE,
    nok_notification_enabled        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at                      DATETIME    NOT NULL,
    updated_at                      DATETIME    NOT NULL,
    UNIQUE KEY uq_system_config_hospital (hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
