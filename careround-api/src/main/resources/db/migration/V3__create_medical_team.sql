-- ============================================================
-- V3: MedicalTeam, MedicalTeamWard, MedicalTeamMember, MedicalTeamInvite
-- ============================================================

CREATE TABLE medical_team (
    id             VARCHAR(36)  NOT NULL,
    hospital_id    VARCHAR(36)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    consultant_id  VARCHAR(36)  NOT NULL,
    department_id  VARCHAR(36)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_team_hospital    FOREIGN KEY (hospital_id)   REFERENCES hospital    (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_consultant  FOREIGN KEY (consultant_id) REFERENCES users       (id),
    CONSTRAINT fk_team_department  FOREIGN KEY (department_id) REFERENCES department  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_ward (
    medical_team_id  VARCHAR(36) NOT NULL,
    ward_id          VARCHAR(36) NOT NULL,
    assigned_at      DATETIME    NOT NULL,
    PRIMARY KEY (medical_team_id, ward_id),
    CONSTRAINT fk_tmw_team FOREIGN KEY (medical_team_id) REFERENCES medical_team (id) ON DELETE CASCADE,
    CONSTRAINT fk_tmw_ward FOREIGN KEY (ward_id)         REFERENCES ward          (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_member (
    medical_team_id  VARCHAR(36) NOT NULL,
    user_id          VARCHAR(36) NOT NULL,
    joined_at        DATETIME    NOT NULL,
    PRIMARY KEY (medical_team_id, user_id),
    CONSTRAINT fk_tmm_team FOREIGN KEY (medical_team_id) REFERENCES medical_team (id) ON DELETE CASCADE,
    CONSTRAINT fk_tmm_user FOREIGN KEY (user_id)         REFERENCES users         (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_team_invite (
    id               VARCHAR(36) NOT NULL,
    hospital_id      VARCHAR(36) NOT NULL,
    medical_team_id  VARCHAR(36) NOT NULL,
    invited_user_id  VARCHAR(36) NOT NULL,
    invited_by_id    VARCHAR(36) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at       DATETIME    NOT NULL,
    created_at       DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_invite_user_status (invited_user_id, status),
    CONSTRAINT fk_invite_hospital   FOREIGN KEY (hospital_id)     REFERENCES hospital    (id) ON DELETE CASCADE,
    CONSTRAINT fk_invite_team       FOREIGN KEY (medical_team_id) REFERENCES medical_team(id) ON DELETE CASCADE,
    CONSTRAINT fk_invite_invitee    FOREIGN KEY (invited_user_id) REFERENCES users        (id) ON DELETE CASCADE,
    CONSTRAINT fk_invite_inviter    FOREIGN KEY (invited_by_id)   REFERENCES users        (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
