-- ============================================================================
-- CareRound Seed Data Script - Testing Data
-- ============================================================================
-- This script populates the database with realistic test data for:
-- - Hospital and system configuration
-- - Departments, Wards
-- - Users with all roles
-- - Medical Teams
-- - Shift Schedules
-- - On-Call Rotations
-- - Test Patients
--
-- Run this after Flyway migrations complete: mysql careround_core < seed.sql
-- ============================================================================

USE careround_core;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE patient_round_review;
TRUNCATE TABLE clinical_note;
TRUNCATE TABLE care_task;
TRUNCATE TABLE patient_handover_note;
TRUNCATE TABLE handover;
TRUNCATE TABLE round;
TRUNCATE TABLE escalation;
TRUNCATE TABLE patient_vitals;
TRUNCATE TABLE next_of_kin;
TRUNCATE TABLE shift;
TRUNCATE TABLE on_call_rotation;
TRUNCATE TABLE medical_team_invite;
TRUNCATE TABLE medical_team_member;
TRUNCATE TABLE medical_team_ward;
TRUNCATE TABLE medical_team;
TRUNCATE TABLE shift_schedule;
TRUNCATE TABLE patient;
TRUNCATE TABLE ward;
TRUNCATE TABLE department;
TRUNCATE TABLE system_configuration;
TRUNCATE TABLE hospital;

-- ============================================================================
-- 1. HOSPITAL AND SYSTEM CONFIGURATION
-- ============================================================================

INSERT INTO hospital (id, created_at, updated_at, name, address, contact_email, contact_phone)
VALUES (
    'hosp-001',
    NOW(),
    NOW(),
    'St. Mary''s Teaching Hospital',
    '123 Medical Street, London, UK',
    'admin@stmarys.nhs.uk',
    '+44-20-7946-0958'
);

INSERT INTO system_configuration (id, created_at, updated_at, hospital_id, news_amber_threshold, news_red_threshold, task_overdue_grace_minutes, round_notifications_enabled, nok_notification_enabled)
VALUES (
    'sysconf-001',
    NOW(),
    NOW(),
    'hosp-001',
    5,
    7,
    30,
    TRUE,
    TRUE
);

-- ============================================================================
-- 2. DEPARTMENTS
-- ============================================================================

INSERT INTO department (id, created_at, updated_at, hospital_id, name, head_of_department_id)
VALUES 
    ('dept-001', NOW(), NOW(), 'hosp-001', 'General Medicine', NULL),
    ('dept-002', NOW(), NOW(), 'hosp-001', 'Cardiology', NULL),
    ('dept-003', NOW(), NOW(), 'hosp-001', 'Surgery', NULL);

-- ============================================================================
-- 3. USERS - Create all roles
-- ============================================================================

-- Admin User
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES (
    'user-admin-001',
    NOW(),
    NOW(),
    'hosp-001',
    'Alice',
    'Administrator',
    'alice.admin@stmarys.nhs.uk',
    -- Password: admin123 (hashed with bcrypt)
    '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
    'ADMIN',
    NULL,
    TRUE
);

-- Consultant Users (General Medicine and Cardiology)
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES 
    (
        'user-cons-001',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. James',
        'Harrison',
        'james.harrison@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'CONSULTANT',
        'dept-001',
        TRUE
    ),
    (
        'user-cons-002',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Sarah',
        'Kumar',
        'sarah.kumar@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'CONSULTANT',
        'dept-002',
        TRUE
    ),
    (
        'user-cons-003',
        NOW(),
        NOW(),
        'hosp-001',
        'Mr. Michael',
        'O''Brien',
        'michael.obrien@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'CONSULTANT',
        'dept-003',
        TRUE
    );

-- Registrar Users
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES 
    (
        'user-reg-001',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Emma',
        'Wilson',
        'emma.wilson@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'REGISTRAR',
        'dept-001',
        TRUE
    ),
    (
        'user-reg-002',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Priya',
        'Patel',
        'priya.patel@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'REGISTRAR',
        'dept-002',
        TRUE
    );

-- Junior Doctor Users
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES 
    (
        'user-jd-001',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Tom',
        'Anderson',
        'tom.anderson@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'JUNIOR_DOCTOR',
        'dept-001',
        TRUE
    ),
    (
        'user-jd-002',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Lisa',
        'Chen',
        'lisa.chen@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'JUNIOR_DOCTOR',
        'dept-002',
        TRUE
    ),
    (
        'user-jd-003',
        NOW(),
        NOW(),
        'hosp-001',
        'Dr. Amir',
        'Hassan',
        'amir.hassan@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'JUNIOR_DOCTOR',
        'dept-003',
        TRUE
    );

-- Nurse Users
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES 
    (
        'user-nurse-001',
        NOW(),
        NOW(),
        'hosp-001',
        'Sister',
        'Rachel',
        'rachel.nurse@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'NURSE',
        'dept-001',
        TRUE
    ),
    (
        'user-nurse-002',
        NOW(),
        NOW(),
        'hosp-001',
        'Staff Nurse',
        'David',
        'david.nurse@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'NURSE',
        'dept-002',
        TRUE
    ),
    (
        'user-nurse-003',
        NOW(),
        NOW(),
        'hosp-001',
        'Charge Nurse',
        'Margaret',
        'margaret.nurse@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'NURSE',
        'dept-003',
        TRUE
    );

-- Ward Supervisor Users
INSERT INTO users (id, created_at, updated_at, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active)
VALUES 
    (
        'user-ws-001',
        NOW(),
        NOW(),
        'hosp-001',
        'Karen',
        'Thompson',
        'karen.thompson@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'WARD_SUPERVISOR',
        'dept-001',
        TRUE
    ),
    (
        'user-ws-002',
        NOW(),
        NOW(),
        'hosp-001',
        'Robert',
        'Brown',
        'robert.brown@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'WARD_SUPERVISOR',
        'dept-002',
        TRUE
    ),
    (
        'user-ws-003',
        NOW(),
        NOW(),
        'hosp-001',
        'Victoria',
        'Smith',
        'victoria.smith@stmarys.nhs.uk',
        '$2a$10$HUaEJW3Cq3lKKqxJz4cXJOqz5NzN5y5Z5zN5y5Z5zN5y5Z5zN5y5z',
        'WARD_SUPERVISOR',
        'dept-003',
        TRUE
    );

-- ============================================================================
-- 4. WARDS
-- ============================================================================

INSERT INTO ward (id, created_at, updated_at, hospital_id, name, specialty, total_beds, supervisor_id)
VALUES 
    ('ward-001', NOW(), NOW(), 'hosp-001', 'General Medicine Ward A', 'General Medicine', 28, 'user-ws-001'),
    ('ward-002', NOW(), NOW(), 'hosp-001', 'General Medicine Ward B', 'General Medicine', 24, 'user-ws-001'),
    ('ward-003', NOW(), NOW(), 'hosp-001', 'Cardiology Ward', 'Cardiology', 20, 'user-ws-002'),
    ('ward-004', NOW(), NOW(), 'hosp-001', 'Surgical Ward', 'Surgery', 22, 'user-ws-003');

-- ============================================================================
-- 5. MEDICAL TEAMS
-- ============================================================================

-- General Medicine Team (led by Dr. James Harrison)
INSERT INTO medical_team (id, created_at, updated_at, hospital_id, name, consultant_id, department_id)
VALUES 
    ('team-001', NOW(), NOW(), 'hosp-001', 'GM Team A', 'user-cons-001', 'dept-001'),
    ('team-002', NOW(), NOW(), 'hosp-001', 'Cardiology Team A', 'user-cons-002', 'dept-002'),
    ('team-003', NOW(), NOW(), 'hosp-001', 'Surgery Team A', 'user-cons-003', 'dept-003');

-- Assign medical teams to wards
INSERT INTO medical_team_ward (medical_team_id, ward_id, assigned_at)
VALUES 
    ('team-001', 'ward-001', NOW()),
    ('team-001', 'ward-002', NOW()),
    ('team-002', 'ward-003', NOW()),
    ('team-003', 'ward-004', NOW());

-- Add team members (composite PK - no base entity)
INSERT INTO medical_team_member (medical_team_id, user_id, joined_at)
VALUES 
    ('team-001', 'user-cons-001', NOW()),
    ('team-001', 'user-reg-001', NOW()),
    ('team-001', 'user-jd-001', NOW()),
    ('team-001', 'user-jd-002', NOW()),
    ('team-002', 'user-cons-002', NOW()),
    ('team-002', 'user-reg-002', NOW()),
    ('team-002', 'user-jd-002', NOW()),
    ('team-003', 'user-cons-003', NOW()),
    ('team-003', 'user-jd-003', NOW());

-- ============================================================================
-- 6. SHIFT SCHEDULES (automated shift generation)
-- ============================================================================

-- Day shifts: 07:00-19:00, Monday-Friday for General Medicine Wards
INSERT INTO shift_schedule (id, created_at, updated_at, hospital_id, ward_id, shift_type, start_time, end_time, days_of_week, is_active)
VALUES 
    ('schedule-001', NOW(), NOW(), 'hosp-001', 'ward-001', 'DAY', '07:00:00', '19:00:00', 'MON,TUE,WED,THU,FRI', TRUE),
    ('schedule-002', NOW(), NOW(), 'hosp-001', 'ward-001', 'NIGHT', '19:00:00', '07:00:00', 'MON,TUE,WED,THU,FRI', TRUE),
    ('schedule-003', NOW(), NOW(), 'hosp-001', 'ward-002', 'DAY', '07:00:00', '19:00:00', 'MON,TUE,WED,THU,FRI', TRUE),
    ('schedule-004', NOW(), NOW(), 'hosp-001', 'ward-002', 'NIGHT', '19:00:00', '07:00:00', 'MON,TUE,WED,THU,FRI', TRUE),
    ('schedule-005', NOW(), NOW(), 'hosp-001', 'ward-003', 'DAY', '08:00:00', '20:00:00', 'MON,TUE,WED,THU,FRI', TRUE),
    ('schedule-006', NOW(), NOW(), 'hosp-001', 'ward-004', 'DAY', '07:30:00', '19:30:00', 'MON,TUE,WED,THU,FRI', TRUE);

-- ============================================================================
-- 7. ON-CALL ROTATIONS (current week simulation)
-- ============================================================================

-- General Medicine on-call (Mon-Fri this week)
INSERT INTO on_call_rotation (id, created_at, updated_at, hospital_id, department_id, ward_id, doctor_id, role, start_time, end_time)
VALUES 
    ('oncall-001', NOW(), NOW(), 'hosp-001', 'dept-001', NULL, 'user-reg-001', 'REGISTRAR_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
    ('oncall-002', NOW(), NOW(), 'hosp-001', 'dept-001', NULL, 'user-cons-001', 'CONSULTANT_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
    ('oncall-003', NOW(), NOW(), 'hosp-001', 'dept-002', NULL, 'user-reg-002', 'REGISTRAR_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
    ('oncall-004', NOW(), NOW(), 'hosp-001', 'dept-002', NULL, 'user-cons-002', 'CONSULTANT_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
    ('oncall-005', NOW(), NOW(), 'hosp-001', 'dept-003', NULL, 'user-jd-003', 'REGISTRAR_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY)),
    ('oncall-006', NOW(), NOW(), 'hosp-001', 'dept-003', NULL, 'user-cons-003', 'CONSULTANT_ON_CALL', DATE_ADD(NOW(), INTERVAL 0 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY));

-- ============================================================================
-- 8. SHIFTS (manual creation for today and tomorrow)
-- ============================================================================

-- Day shifts for Ward A (today and tomorrow)
INSERT INTO shift (id, created_at, updated_at, hospital_id, ward_id, shift_schedule_id, type, start_time, end_time, lead_doctor_id, nurse_in_charge_id, status, assigned_at)
VALUES 
    (
        'shift-001',
        NOW(),
        NOW(),
        'hosp-001',
        'ward-001',
        'schedule-001',
        'DAY',
        DATE_ADD(CURDATE(), INTERVAL 7 HOUR),
        DATE_ADD(CURDATE(), INTERVAL 19 HOUR),
        'user-cons-001',
        'user-nurse-001',
        'ACTIVE',
        NOW()
    ),
    (
        'shift-002',
        NOW(),
        NOW(),
        'hosp-001',
        'ward-001',
        'schedule-001',
        'DAY',
        TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'),
        TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'),
        NULL,
        NULL,
        'PENDING_ASSIGNMENT',
        NULL
    );

-- ============================================================================
-- 9. HANDOVER, ROUND, REVIEWS AND NOTES
-- ============================================================================

INSERT INTO handover (id, created_at, updated_at, hospital_id, ward_id, outgoing_shift_id, incoming_shift_id, conducted_by_id, status, general_notes, completed_at)
VALUES (
    'handover-001',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-001',
    'shift-001',
    'shift-002',
    'user-cons-001',
    'PENDING',
    'Seeded handover for testing shift change workflows.',
    NULL
);

INSERT INTO patient_handover_note (id, created_at, updated_at, handover_id, patient_id, status_summary, outstanding_task_ids, urgency_flag, added_by_id)
VALUES 
    ('hnote-001', NOW(), NOW(), 'handover-001', 'pt-001', 'Stable overnight. Continue antibiotics.', 'task-001', FALSE, 'user-jd-001'),
    ('hnote-002', NOW(), NOW(), 'handover-001', 'pt-002', 'More breathless this morning, monitor closely.', 'task-002', TRUE, 'user-jd-002');

INSERT INTO round (id, created_at, updated_at, hospital_id, ward_id, medical_team_id, shift_id, round_type, lead_doctor_id, status, scheduled_time, started_at, completed_at, team_members)
VALUES (
    'round-001',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-001',
    'team-001',
    'shift-001',
    'MORNING',
    'user-cons-001',
    'COMPLETED',
    DATE_ADD(NOW(), INTERVAL -2 HOUR),
    DATE_ADD(NOW(), INTERVAL -90 MINUTE),
    DATE_ADD(NOW(), INTERVAL -30 MINUTE),
    'user-cons-001,user-reg-001,user-jd-001,user-jd-002'
);

INSERT INTO patient_round_review (id, created_at, updated_at, round_id, patient_id, reviewed_by_id, review_order, news_score_at_review, clinical_status, was_examined, management_plan, discharge_assessment, notified_next_of_kin, reviewed_at)
VALUES 
    (
        'review-001',
        NOW(),
        NOW(),
        'round-001',
        'pt-001',
        'user-cons-001',
        1,
        2,
        'IMPROVING',
        TRUE,
        'Continue current treatment and observe for discharge tomorrow.',
        'NONE',
        FALSE,
        DATE_ADD(NOW(), INTERVAL -75 MINUTE)
    ),
    (
        'review-002',
        NOW(),
        NOW(),
        'round-001',
        'pt-002',
        'user-cons-001',
        2,
        5,
        'DETERIORATING',
        TRUE,
        'Escalate respiratory support and review blood gases.',
        'POSSIBLE',
        TRUE,
        DATE_ADD(NOW(), INTERVAL -60 MINUTE)
    );

INSERT INTO clinical_note (id, created_at, updated_at, patient_id, patient_round_review_id, author_id, note_type, content, is_amended, amended_by_id, amended_at)
VALUES 
    (
        'note-001',
        NOW(),
        NOW(),
        'pt-001',
        'review-001',
        'user-jd-001',
        'ROUND_NOTE',
        'Reviewed on morning ward round. Breathing improved and tolerating oral intake.',
        FALSE,
        NULL,
        NULL
    ),
    (
        'note-002',
        NOW(),
        NOW(),
        'pt-002',
        'review-002',
        'user-jd-002',
        'PROGRESS_NOTE',
        'Shortness of breath persists. Escalation discussed with cardiology registrar.',
        FALSE,
        NULL,
        NULL
    ),
    (
        'note-003',
        NOW(),
        NOW(),
        'pt-005',
        NULL,
        'user-cons-001',
        'ADMISSION_NOTE',
        'Admitted with acute gastroenteritis. Hydration plan started.',
        FALSE,
        NULL,
        NULL
    );

-- ============================================================================
-- 10. TEST PATIENTS (admitted to various wards with different acuity levels)
-- ============================================================================

-- Patient 1: Low acuity, stable in GM Ward A
INSERT INTO patient (id, created_at, updated_at, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, status)
VALUES (
    'pt-001',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-001',
    'A1',
    'team-001',
    'user-cons-001',
    'John',
    'Smith',
    '1954-05-15',
    'M',
    'HN-0001',
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    'ELECTIVE',
    'Pneumonia - improving',
    'General Medicine',
    'LOW',
    2,
    FALSE,
    'ADMITTED'
);

-- Patient 2: Medium acuity (AMBER), GM Ward B
INSERT INTO patient (id, created_at, updated_at, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, status)
VALUES (
    'pt-002',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-002',
    'B5',
    'team-001',
    'user-cons-001',
    'Mary',
    'Johnson',
    '1960-12-22',
    'F',
    'HN-0002',
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    'EMERGENCY',
    'COPD exacerbation',
    'General Medicine',
    'MEDIUM',
    5,
    FALSE,
    'ADMITTED'
);

-- Patient 3: High acuity (RED), Cardiology Ward
INSERT INTO patient (id, created_at, updated_at, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, status)
VALUES (
    'pt-003',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-003',
    'C2',
    'team-002',
    'user-cons-002',
    'Robert',
    'Williams',
    '1948-08-10',
    'M',
    'HN-0003',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'EMERGENCY',
    'Acute myocardial infarction',
    'Cardiology',
    'HIGH',
    8,
    FALSE,
    'DETERIORATING'
);

-- Patient 4: Low acuity, Surgical Ward
INSERT INTO patient (id, created_at, updated_at, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, status)
VALUES (
    'pt-004',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-004',
    'D3',
    'team-003',
    'user-cons-003',
    'Patricia',
    'Brown',
    '1965-03-30',
    'F',
    'HN-0004',
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    'ELECTIVE',
    'Cholecystectomy - post-op day 5',
    'Surgery',
    'LOW',
    1,
    TRUE,
    'DISCHARGE_READY'
);

-- Patient 5: Another low acuity for testing ward round
INSERT INTO patient (id, created_at, updated_at, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, status)
VALUES (
    'pt-005',
    NOW(),
    NOW(),
    'hosp-001',
    'ward-001',
    'A2',
    'team-001',
    'user-cons-001',
    'Angela',
    'Davis',
    '1972-07-18',
    'F',
    'HN-0005',
    NOW(),
    'EMERGENCY',
    'Acute gastroenteritis',
    'General Medicine',
    'LOW',
    3,
    FALSE,
    'ADMITTED'
);

-- ============================================================================
-- 11. PATIENT VITALS (for NEWS2 score testing)
-- ============================================================================

-- Patient 1 vitals (good)
INSERT INTO patient_vitals (id, created_at, updated_at, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at)
VALUES (
    'vitals-001',
    NOW(),
    NOW(),
    'pt-001',
    'user-nurse-001',
    72,
    18,
    98.5,
    125,
    37.2,
    'ALERT',
    2,
    NOW()
);

-- Patient 2 vitals (AMBER - elevated respirations)
INSERT INTO patient_vitals (id, created_at, updated_at, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at)
VALUES (
    'vitals-002',
    NOW(),
    NOW(),
    'pt-002',
    'user-nurse-001',
    95,
    24,
    93.0,
    138,
    38.1,
    'ALERT',
    5,
    NOW()
);

-- Patient 3 vitals (RED - critical)
INSERT INTO patient_vitals (id, created_at, updated_at, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at)
VALUES (
    'vitals-003',
    NOW(),
    NOW(),
    'pt-003',
    'user-nurse-002',
    128,
    28,
    89.0,
    92,
    39.5,
    'VOICE',
    12,
    NOW()
);

-- ============================================================================
-- 12. NEXT OF KIN
-- ============================================================================

INSERT INTO next_of_kin (id, created_at, updated_at, patient_id, name, relationship, phone, email, preferred_contact_method, is_emergency_contact, notification_consent)
VALUES 
    ('nok-001', NOW(), NOW(), 'pt-001', 'Michael Smith', 'Son', '+44-20-7123-4567', 'michael@example.com', 'BOTH', TRUE, TRUE),
    ('nok-002', NOW(), NOW(), 'pt-002', 'David Johnson', 'Husband', '+44-20-7234-5678', 'david@example.com', 'EMAIL', TRUE, TRUE),
    ('nok-003', NOW(), NOW(), 'pt-003', 'Anne Williams', 'Daughter', '+44-20-7345-6789', 'anne@example.com', 'SMS', TRUE, TRUE),
    ('nok-004', NOW(), NOW(), 'pt-004', 'James Brown', 'Husband', '+44-20-7456-7890', 'james@example.com', 'BOTH', FALSE, TRUE);

-- ============================================================================
-- 13. ESCALATIONS (for testing escalation workflow)
-- ============================================================================

INSERT INTO escalation (id, created_at, updated_at, hospital_id, patient_id, triggered_by_id, trigger_type, severity, assigned_to_id, status, notes, resolved_at)
VALUES (
    'esc-001',
    NOW(),
    NOW(),
    'hosp-001',
    'pt-003',
    'user-nurse-002',
    'HIGH_NEWS_SCORE',
    'RED',
    'user-cons-002',
    'OPEN',
    'Patient deteriorating rapidly - RED NEWS score of 12',
    NULL
);

-- ============================================================================
-- 14. CARE TASKS (for testing task workflow)
-- ============================================================================

-- Nursing care task
INSERT INTO care_task (id, created_at, updated_at, hospital_id, patient_id, ward_id, round_id, created_by_id, assigned_to_id, assigned_to_role, task_type, source, title, description, priority, window_start, window_end, status, completed_by_id, completed_at, escalated_at)
VALUES (
    'task-001',
    NOW(),
    NOW(),
    'hosp-001',
    'pt-001',
    'ward-001',
    NULL,
    'user-nurse-001',
    'user-nurse-001',
    'NURSE',
    'Medication',
    'NURSING_CARE_PLAN',
    'Administer Amoxicillin 500mg',
    'Antibiotic for respiratory tract infection - three times daily',
    'ROUTINE',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 8 HOUR),
    'PENDING',
    NULL,
    NULL,
    NULL
);

-- Post-round task (pending assignment by role)
INSERT INTO care_task (id, created_at, updated_at, hospital_id, patient_id, ward_id, round_id, created_by_id, assigned_to_id, assigned_to_role, task_type, source, title, description, priority, window_start, window_end, status, completed_by_id, completed_at, escalated_at)
VALUES (
    'task-002',
    NOW(),
    NOW(),
    'hosp-001',
    'pt-002',
    'ward-002',
    NULL,
    'user-cons-001',
    NULL,
    'JUNIOR_DOCTOR',
    'Bloods Review',
    'POST_ROUND_JOB',
    'Review FBC and U&Es from this morning',
    'Check for electrolyte imbalance - urgent given elevated respirations',
    'URGENT',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 2 HOUR),
    'PENDING',
    NULL,
    NULL,
    NULL
);

-- ============================================================================
-- 15. NOTIFICATIONS (careround_notification schema)
-- ============================================================================

USE careround_notification;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE failed_notifications;

INSERT INTO notifications (id, event_type, payload, status, failure_reason, sent_at, retry_count, created_at, updated_at)
VALUES 
    (
        'notif-001',
        'careround.round.completed',
        '{"hospitalId":"hosp-001","correlationId":"corr-001","roundId":"round-001","wardId":"ward-001","reviewedPatientIds":["pt-001","pt-002"]}',
        'SUCCESSFUL',
        NULL,
        NOW(),
        0,
        NOW(),
        NOW()
    ),
    (
        'notif-002',
        'careround.patient.deterioration',
        '{"hospitalId":"hosp-001","correlationId":"corr-002","patientId":"pt-003","wardId":"ward-003","newsScore":12,"severity":"RED"}',
        'FAILED',
        'SMTP provider timeout after 3 retries',
        NOW(),
        3,
        NOW(),
        NOW()
    ),
    (
        'notif-003',
        'careround.task.overdue',
        '{"hospitalId":"hosp-001","correlationId":"corr-003","careTaskId":"task-001","patientId":"pt-001","wardId":"ward-001","priority":"ROUTINE"}',
        'SUCCESSFUL',
        NULL,
        NOW(),
        0,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 16. AUDIT LOGS (careround_audit schema)
-- ============================================================================

USE careround_audit;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE audit_log;

INSERT INTO audit_log (id, event_type, hospital_id, correlation_id, payload, processed_at, created_at, updated_at)
VALUES 
    (
        'audit-001',
        'careround.round.completed',
        'hosp-001',
        'corr-001',
        '{"roundId":"round-001","wardId":"ward-001","reviewedPatientIds":["pt-001","pt-002"]}',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        'audit-002',
        'careround.patient.deterioration',
        'hosp-001',
        'corr-002',
        '{"patientId":"pt-003","wardId":"ward-003","newsScore":12,"severity":"RED"}',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        'audit-003',
        'careround.task.overdue',
        'hosp-001',
        'corr-003',
        '{"careTaskId":"task-001","patientId":"pt-001","wardId":"ward-001","priority":"ROUTINE"}',
        NOW(),
        NOW(),
        NOW()
    );

-- ============================================================================
-- SEED SCRIPT COMPLETE
-- ============================================================================
-- 
-- Test Users Created (all with password: admin123 hashed)
-- - Admin: alice.admin@stmarys.nhs.uk
-- - Consultants: james.harrison, sarah.kumar, michael.obrien
-- - Registrars: emma.wilson, priya.patel
-- - Junior Doctors: tom.anderson, lisa.chen, amir.hassan
-- - Nurses: rachel.nurse, david.nurse, margaret.nurse
-- - Ward Supervisors: karen.thompson, robert.brown, victoria.smith
--
-- Test Data Includes:
-- - 1 Hospital (St. Mary's)
-- - 3 Departments
-- - 4 Wards
-- - 3 Medical Teams with members
-- - 5 Test Patients with varying acuity levels
-- - Shift schedules and shifts
-- - On-call rotations
-- - Patient vitals with NEWS scores (2, 5, 12)
-- - Next of kin records
-- - Sample escalations and care tasks
-- - Sample handover, round, reviews and clinical notes
-- - Notification and audit records
--
-- Ready for testing workflows like:
-- - Ward rounds with multiple patients
-- - Task creation and completion
-- - Escalation handling
-- - Shift assignment and handovers
-- - Patient discharge
--
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 1;
