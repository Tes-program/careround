-- ============================================================
-- V1: Hospital, SystemConfiguration, Users
-- NOTE: Department created in V2 (it depends on users for HOD FK).
--       Users table created here because SystemConfiguration needs
--       hospital first, and department/ward need users.
-- ============================================================

CREATE TABLE hospital (
    id              VARCHAR(36)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    address         TEXT         NOT NULL,
    contact_email   VARCHAR(255) NOT NULL,
    contact_phone   VARCHAR(50),
    created_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hospital_email (contact_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_configuration (
    id                            VARCHAR(36) NOT NULL,
    hospital_id                   VARCHAR(36) NOT NULL,
    news_amber_threshold          INT         NOT NULL DEFAULT 5,
    news_red_threshold            INT         NOT NULL DEFAULT 7,
    task_overdue_grace_minutes    INT         NOT NULL DEFAULT 30,
    round_notifications_enabled   TINYINT(1)  NOT NULL DEFAULT 1,
    nok_notification_enabled      TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sysconfig_hospital (hospital_id),
    CONSTRAINT fk_sysconfig_hospital FOREIGN KEY (hospital_id) REFERENCES hospital (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- users (reserved word avoided by naming table 'users')
CREATE TABLE users (
    id            VARCHAR(36)  NOT NULL,
    hospital_id   VARCHAR(36)  NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    department_id VARCHAR(36),              -- FK added in V2 after department exists
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_hospital_email (hospital_id, email),
    CONSTRAINT fk_user_hospital FOREIGN KEY (hospital_id) REFERENCES hospital (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
