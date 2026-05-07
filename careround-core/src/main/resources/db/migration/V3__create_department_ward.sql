CREATE TABLE department (
    id                      VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id             VARCHAR(36)  NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    head_of_department_id   VARCHAR(36)  NULL,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ward (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    specialty       VARCHAR(100) NULL,
    total_beds      INT          NOT NULL DEFAULT 0,
    supervisor_id   VARCHAR(36)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
