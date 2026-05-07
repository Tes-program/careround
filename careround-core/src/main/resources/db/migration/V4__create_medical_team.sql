CREATE TABLE medical_teams (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    consultant_id VARCHAR(36) NOT NULL,
    department_id VARCHAR(36) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_medical_team_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_medical_team_consultant FOREIGN KEY (consultant_id) REFERENCES users(id),
    CONSTRAINT fk_medical_team_department FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_wards (
    medical_team_id VARCHAR(36) NOT NULL,
    ward_id VARCHAR(36) NOT NULL,
    assigned_at DATETIME NOT NULL,
    PRIMARY KEY (medical_team_id, ward_id),
    CONSTRAINT fk_mtw_team FOREIGN KEY (medical_team_id) REFERENCES medical_teams(id),
    CONSTRAINT fk_mtw_ward FOREIGN KEY (ward_id) REFERENCES wards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_members (
    medical_team_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    joined_at DATETIME NOT NULL,
    PRIMARY KEY (medical_team_id, user_id),
    CONSTRAINT fk_mtm_team FOREIGN KEY (medical_team_id) REFERENCES medical_teams(id),
    CONSTRAINT fk_mtm_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_invites (
    id VARCHAR(36) PRIMARY KEY,
    hospital_id VARCHAR(36) NOT NULL,
    medical_team_id VARCHAR(36) NOT NULL,
    invited_user_id VARCHAR(36) NOT NULL,
    invited_by_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_mti_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_mti_team FOREIGN KEY (medical_team_id) REFERENCES medical_teams(id),
    CONSTRAINT fk_mti_invited_user FOREIGN KEY (invited_user_id) REFERENCES users(id),
    CONSTRAINT fk_mti_invited_by FOREIGN KEY (invited_by_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
