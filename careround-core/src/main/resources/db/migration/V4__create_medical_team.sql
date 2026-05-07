CREATE TABLE medical_team (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36)  NOT NULL,
    name            VARCHAR(255) NOT NULL,
    consultant_id   VARCHAR(36)  NOT NULL,
    department_id   VARCHAR(36)  NOT NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_team_ward (
    medical_team_id VARCHAR(36) NOT NULL,
    ward_id         VARCHAR(36) NOT NULL,
    assigned_at     DATETIME    NOT NULL,
    PRIMARY KEY (medical_team_id, ward_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_team_member (
    medical_team_id VARCHAR(36) NOT NULL,
    user_id         VARCHAR(36) NOT NULL,
    joined_at       DATETIME    NOT NULL,
    PRIMARY KEY (medical_team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_team_invite (
    id              VARCHAR(36) NOT NULL PRIMARY KEY,
    hospital_id     VARCHAR(36) NOT NULL,
    medical_team_id VARCHAR(36) NOT NULL,
    invited_user_id VARCHAR(36) NOT NULL,
    invited_by_id   VARCHAR(36) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at      DATETIME    NOT NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
