CREATE TABLE on_call_rotation (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36) NOT NULL,
    department_id   VARCHAR(36) NOT NULL,
    ward_id         VARCHAR(36) NULL,
    doctor_id       VARCHAR(36) NOT NULL,
    role            VARCHAR(30) NOT NULL,
    start_time      DATETIME    NOT NULL,
    end_time        DATETIME    NOT NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shift_schedule (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36) NOT NULL,
    ward_id         VARCHAR(36) NULL,
    shift_type      VARCHAR(10) NOT NULL,
    start_time      TIME        NOT NULL,
    end_time        TIME        NOT NULL,
    days_of_week    VARCHAR(50) NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
