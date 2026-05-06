-- ============================================================
-- V2: Department, Ward + backfill FK on users.department_id
-- ============================================================

CREATE TABLE department (
    id                      VARCHAR(36)  NOT NULL,
    hospital_id             VARCHAR(36)  NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    head_of_department_id   VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_dept_hospital FOREIGN KEY (hospital_id) REFERENCES hospital (id) ON DELETE CASCADE,
    CONSTRAINT fk_dept_hod      FOREIGN KEY (head_of_department_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ward (
    id            VARCHAR(36)  NOT NULL,
    hospital_id   VARCHAR(36)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    specialty     VARCHAR(100),
    total_beds    INT          NOT NULL,
    supervisor_id VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_ward_hospital    FOREIGN KEY (hospital_id)   REFERENCES hospital (id) ON DELETE CASCADE,
    CONSTRAINT fk_ward_supervisor  FOREIGN KEY (supervisor_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Backfill: now that department exists, add FK constraint on users
ALTER TABLE users
    ADD CONSTRAINT fk_user_department
    FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE SET NULL;
