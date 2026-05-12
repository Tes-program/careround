-- ============================================================================
-- CareRound Seed Data Script
-- ============================================================================
-- Run after all Flyway migrations have completed.
--
-- Demo login password for active platform and tenant users: Password123!
-- Demo activation token: activate-cedar-admin
--
-- Patient/user names are fictitious. Hospital/place context is realistic and
-- intentionally biased toward Nigerian clinical workflows.
-- ============================================================================

USE careround_core;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE account_activation_token;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE outbox_event;
TRUNCATE TABLE care_task;
TRUNCATE TABLE clinical_note;
TRUNCATE TABLE patient_round_review;
TRUNCATE TABLE round;
TRUNCATE TABLE patient_handover_note;
TRUNCATE TABLE handover;
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
TRUNCATE TABLE users;
TRUNCATE TABLE hospital_onboarding_request;
TRUNCATE TABLE platform_operator;
TRUNCATE TABLE hospital;

SET @password_hash = '$2a$10$X89u//KQzBi4GKRBxxV/mu40XBAkA5jJOyUMbXaKzg70pB.CXUNvS';

INSERT INTO hospital (id, name, address, contact_email, contact_phone, created_at, updated_at) VALUES
('hosp-ng-luth', 'Lagos University Teaching Hospital', 'Idi-Araba, Surulere, Lagos, Nigeria', 'admin@luth.example.ng', '+2348010000101', NOW(), NOW()),
('hosp-ng-abj', 'National Hospital Abuja', 'Central Business District, Abuja, Nigeria', 'admin@nha.example.ng', '+2348010000202', NOW(), NOW()),
('hosp-ng-cedar', 'Cedar Specialist Hospital Port Harcourt', 'GRA Phase 2, Port Harcourt, Rivers, Nigeria', 'admin@cedarspecialist.example.ng', '+2348010000303', NOW(), NOW()),
('hosp-ke-knh', 'Kenyatta National Hospital', 'Hospital Road, Nairobi, Kenya', 'admin@knh.example.ke', '+254700100100', NOW(), NOW()),
('hosp-uk-stmary', 'St Marys Hospital London', 'Praed Street, London, United Kingdom', 'admin@stmarys.example.uk', '+442070001001', NOW(), NOW());

INSERT INTO system_configuration (id, hospital_id, news_amber_threshold, news_red_threshold, task_overdue_grace_minutes, round_notifications_enabled, nok_notification_enabled, created_at, updated_at) VALUES
('sys-ng-luth', 'hosp-ng-luth', 5, 7, 30, TRUE, TRUE, NOW(), NOW()),
('sys-ng-abj', 'hosp-ng-abj', 5, 7, 25, TRUE, TRUE, NOW(), NOW()),
('sys-ng-cedar', 'hosp-ng-cedar', 5, 7, 30, TRUE, TRUE, NOW(), NOW()),
('sys-ke-knh', 'hosp-ke-knh', 5, 7, 30, TRUE, FALSE, NOW(), NOW()),
('sys-uk-stmary', 'hosp-uk-stmary', 5, 7, 20, TRUE, TRUE, NOW(), NOW());

INSERT INTO platform_operator (id, first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
('plat-admin-001', 'Adaeze', 'Okafor', 'platform-admin@careround.local', @password_hash, 'PLATFORM_ADMIN', TRUE, NOW(), NOW());

INSERT INTO hospital_onboarding_request (id, hospital_name, country_or_region, contact_email, contact_phone, hospital_type, estimated_beds, primary_need, status, review_notes, reviewed_by_user_id, reviewed_at, provisioned_hospital_id, created_at, updated_at) VALUES
('onboard-001', 'Cedar Specialist Hospital Port Harcourt', 'Nigeria', 'admin@cedarspecialist.example.ng', '+2348010000303', 'Private Specialist Hospital', '180', 'Digitise ward rounds, escalation tracking, and shift handovers.', 'PROVISIONED', 'Approved and provisioned after operations call with hospital leadership.', 'plat-admin-001', DATE_SUB(NOW(), INTERVAL 2 DAY), 'hosp-ng-cedar', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('onboard-002', 'Ibadan Heart and Vascular Centre', 'Nigeria', 'ops@ihvc.example.ng', '+2348010000404', 'Cardiology Centre', '75', 'Structured cardiology rounds and next-of-kin notification audit trail.', 'PENDING_REVIEW', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('onboard-003', 'Korle Bu Digital Pilot Ward', 'Ghana', 'pilot@kbth.example.gh', '+233201001001', 'Teaching Hospital Pilot', '120', 'Multidisciplinary handover and task escalation pilot.', 'CONTACTED', 'Awaiting data processing agreement.', 'plat-admin-001', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO department (id, hospital_id, name, head_of_department_id, created_at, updated_at) VALUES
('dept-luth-medicine', 'hosp-ng-luth', 'Internal Medicine', 'user-luth-cons-ade', NOW(), NOW()),
('dept-luth-cardiology', 'hosp-ng-luth', 'Cardiology', 'user-luth-cons-bello', NOW(), NOW()),
('dept-abj-emergency', 'hosp-ng-abj', 'Emergency Medicine', 'user-abj-cons-yusuf', NOW(), NOW()),
('dept-abj-paeds', 'hosp-ng-abj', 'Paediatrics', 'user-abj-cons-amina', NOW(), NOW()),
('dept-cedar-medicine', 'hosp-ng-cedar', 'General Medicine', NULL, NOW(), NOW()),
('dept-knh-medicine', 'hosp-ke-knh', 'Internal Medicine', 'user-knh-cons-otieno', NOW(), NOW()),
('dept-uk-medicine', 'hosp-uk-stmary', 'Acute Medicine', 'user-uk-cons-smith', NOW(), NOW());

INSERT INTO users (id, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active, created_at, updated_at) VALUES
('user-luth-admin', 'hosp-ng-luth', 'Chioma', 'Eze', 'chioma.eze@luth.example.ng', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-luth-cons-ade', 'hosp-ng-luth', 'Tunde', 'Adewale', 'tunde.adewale@luth.example.ng', @password_hash, 'CONSULTANT', 'dept-luth-medicine', TRUE, NOW(), NOW()),
('user-luth-cons-bello', 'hosp-ng-luth', 'Fatima', 'Bello', 'fatima.bello@luth.example.ng', @password_hash, 'CONSULTANT', 'dept-luth-cardiology', TRUE, NOW(), NOW()),
('user-luth-reg-nwosu', 'hosp-ng-luth', 'Ifeoma', 'Nwosu', 'ifeoma.nwosu@luth.example.ng', @password_hash, 'REGISTRAR', 'dept-luth-medicine', TRUE, NOW(), NOW()),
('user-luth-jd-ibrahim', 'hosp-ng-luth', 'Aisha', 'Ibrahim', 'aisha.ibrahim@luth.example.ng', @password_hash, 'JUNIOR_DOCTOR', 'dept-luth-medicine', TRUE, NOW(), NOW()),
('user-luth-nurse-kemi', 'hosp-ng-luth', 'Kemi', 'Balogun', 'kemi.balogun@luth.example.ng', @password_hash, 'NURSE', 'dept-luth-medicine', TRUE, NOW(), NOW()),
('user-luth-nurse-grace', 'hosp-ng-luth', 'Grace', 'Okafor', 'grace.okafor@luth.example.ng', @password_hash, 'NURSE', 'dept-luth-cardiology', TRUE, NOW(), NOW()),
('user-luth-ws-femi', 'hosp-ng-luth', 'Femi', 'Oladipo', 'femi.oladipo@luth.example.ng', @password_hash, 'WARD_SUPERVISOR', 'dept-luth-medicine', TRUE, NOW(), NOW()),
('user-abj-admin', 'hosp-ng-abj', 'Zainab', 'Musa', 'zainab.musa@nha.example.ng', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-abj-cons-yusuf', 'hosp-ng-abj', 'Bashir', 'Yusuf', 'bashir.yusuf@nha.example.ng', @password_hash, 'CONSULTANT', 'dept-abj-emergency', TRUE, NOW(), NOW()),
('user-abj-cons-amina', 'hosp-ng-abj', 'Amina', 'Sani', 'amina.sani@nha.example.ng', @password_hash, 'CONSULTANT', 'dept-abj-paeds', TRUE, NOW(), NOW()),
('user-abj-reg-okon', 'hosp-ng-abj', 'Eme', 'Okon', 'eme.okon@nha.example.ng', @password_hash, 'REGISTRAR', 'dept-abj-emergency', TRUE, NOW(), NOW()),
('user-abj-jd-musa', 'hosp-ng-abj', 'Musa', 'Danladi', 'musa.danladi@nha.example.ng', @password_hash, 'JUNIOR_DOCTOR', 'dept-abj-emergency', TRUE, NOW(), NOW()),
('user-abj-nurse-hauwa', 'hosp-ng-abj', 'Hauwa', 'Garba', 'hauwa.garba@nha.example.ng', @password_hash, 'NURSE', 'dept-abj-paeds', TRUE, NOW(), NOW()),
('user-abj-ws-ngozi', 'hosp-ng-abj', 'Ngozi', 'Anyanwu', 'ngozi.anyanwu@nha.example.ng', @password_hash, 'WARD_SUPERVISOR', 'dept-abj-emergency', TRUE, NOW(), NOW()),
('user-cedar-admin', 'hosp-ng-cedar', 'Tamuno', 'Briggs', 'tamuno.briggs@cedarspecialist.example.ng', @password_hash, 'ADMIN', NULL, FALSE, NOW(), NOW()),
('user-knh-admin', 'hosp-ke-knh', 'Wanjiku', 'Mwangi', 'wanjiku.mwangi@knh.example.ke', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-knh-cons-otieno', 'hosp-ke-knh', 'Peter', 'Otieno', 'peter.otieno@knh.example.ke', @password_hash, 'CONSULTANT', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-knh-nurse-achebe', 'hosp-ke-knh', 'Nneka', 'Achebe', 'nneka.achebe@knh.example.ke', @password_hash, 'NURSE', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-uk-admin', 'hosp-uk-stmary', 'Amelia', 'Turner', 'amelia.turner@stmarys.example.uk', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-uk-cons-smith', 'hosp-uk-stmary', 'Oliver', 'Smith', 'oliver.smith@stmarys.example.uk', @password_hash, 'CONSULTANT', 'dept-uk-medicine', TRUE, NOW(), NOW()),
('user-uk-nurse-evans', 'hosp-uk-stmary', 'Sophie', 'Evans', 'sophie.evans@stmarys.example.uk', @password_hash, 'NURSE', 'dept-uk-medicine', TRUE, NOW(), NOW());

INSERT INTO account_activation_token (id, token_hash, user_id, hospital_id, expires_at, used_at, created_at, updated_at) VALUES
('act-cedar-admin', '5f460a6b3d1bd0d099412ec512a8e69e54b98dd57a3e1585fb94a3c2e77d37d2', 'user-cedar-admin', 'hosp-ng-cedar', DATE_ADD(NOW(), INTERVAL 72 HOUR), NULL, NOW(), NOW());

INSERT INTO refresh_tokens (id, user_id, hospital_id, token, expires_at, revoked, created_at, updated_at) VALUES
('rt-luth-admin-active', 'user-luth-admin', 'hosp-ng-luth', 'seed-refresh-luth-admin-active', DATE_ADD(NOW(), INTERVAL 7 DAY), FALSE, NOW(), NOW()),
('rt-luth-reg-revoked', 'user-luth-reg-nwosu', 'hosp-ng-luth', 'seed-refresh-luth-reg-revoked', DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('rt-abj-expired', 'user-abj-admin', 'hosp-ng-abj', 'seed-refresh-abj-expired', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW());

INSERT INTO ward (id, hospital_id, name, specialty, total_beds, supervisor_id, created_at, updated_at) VALUES
('ward-luth-male-med', 'hosp-ng-luth', 'Male Medical Ward', 'Internal Medicine', 36, 'user-luth-ws-femi', NOW(), NOW()),
('ward-luth-female-med', 'hosp-ng-luth', 'Female Medical Ward', 'Internal Medicine', 34, 'user-luth-ws-femi', NOW(), NOW()),
('ward-luth-cardiac', 'hosp-ng-luth', 'Cardiac Stepdown Unit', 'Cardiology', 20, 'user-luth-ws-femi', NOW(), NOW()),
('ward-abj-emergency', 'hosp-ng-abj', 'Emergency Observation Ward', 'Emergency Medicine', 28, 'user-abj-ws-ngozi', NOW(), NOW()),
('ward-abj-paeds', 'hosp-ng-abj', 'Paediatric Medical Ward', 'Paediatrics', 32, 'user-abj-ws-ngozi', NOW(), NOW()),
('ward-knh-med', 'hosp-ke-knh', 'Medical Ward 7', 'Internal Medicine', 30, NULL, NOW(), NOW()),
('ward-uk-acute', 'hosp-uk-stmary', 'Acute Medical Unit', 'Acute Medicine', 26, NULL, NOW(), NOW());

INSERT INTO medical_team (id, hospital_id, name, consultant_id, department_id, created_at, updated_at) VALUES
('team-luth-med-a', 'hosp-ng-luth', 'LUTH Medicine Team A', 'user-luth-cons-ade', 'dept-luth-medicine', NOW(), NOW()),
('team-luth-cardio', 'hosp-ng-luth', 'LUTH Cardiology Team', 'user-luth-cons-bello', 'dept-luth-cardiology', NOW(), NOW()),
('team-abj-emergency', 'hosp-ng-abj', 'NHA Emergency Team', 'user-abj-cons-yusuf', 'dept-abj-emergency', NOW(), NOW()),
('team-abj-paeds', 'hosp-ng-abj', 'NHA Paediatrics Team', 'user-abj-cons-amina', 'dept-abj-paeds', NOW(), NOW()),
('team-knh-med', 'hosp-ke-knh', 'KNH Medicine Team', 'user-knh-cons-otieno', 'dept-knh-medicine', NOW(), NOW()),
('team-uk-acute', 'hosp-uk-stmary', 'St Marys Acute Medicine', 'user-uk-cons-smith', 'dept-uk-medicine', NOW(), NOW());

INSERT INTO medical_team_ward (medical_team_id, ward_id, assigned_at) VALUES
('team-luth-med-a', 'ward-luth-male-med', NOW()),
('team-luth-med-a', 'ward-luth-female-med', NOW()),
('team-luth-cardio', 'ward-luth-cardiac', NOW()),
('team-abj-emergency', 'ward-abj-emergency', NOW()),
('team-abj-paeds', 'ward-abj-paeds', NOW()),
('team-knh-med', 'ward-knh-med', NOW()),
('team-uk-acute', 'ward-uk-acute', NOW());

INSERT INTO medical_team_member (medical_team_id, user_id, joined_at) VALUES
('team-luth-med-a', 'user-luth-cons-ade', NOW()),
('team-luth-med-a', 'user-luth-reg-nwosu', NOW()),
('team-luth-med-a', 'user-luth-jd-ibrahim', NOW()),
('team-luth-med-a', 'user-luth-nurse-kemi', NOW()),
('team-luth-cardio', 'user-luth-cons-bello', NOW()),
('team-luth-cardio', 'user-luth-nurse-grace', NOW()),
('team-abj-emergency', 'user-abj-cons-yusuf', NOW()),
('team-abj-emergency', 'user-abj-reg-okon', NOW()),
('team-abj-emergency', 'user-abj-jd-musa', NOW()),
('team-abj-paeds', 'user-abj-cons-amina', NOW()),
('team-abj-paeds', 'user-abj-nurse-hauwa', NOW()),
('team-knh-med', 'user-knh-cons-otieno', NOW()),
('team-knh-med', 'user-knh-nurse-achebe', NOW()),
('team-uk-acute', 'user-uk-cons-smith', NOW()),
('team-uk-acute', 'user-uk-nurse-evans', NOW());

INSERT INTO medical_team_invite (id, hospital_id, medical_team_id, invited_user_id, invited_by_id, status, expires_at, created_at, updated_at) VALUES
('invite-luth-pending', 'hosp-ng-luth', 'team-luth-med-a', 'user-luth-nurse-grace', 'user-luth-cons-ade', 'PENDING', DATE_ADD(NOW(), INTERVAL 24 HOUR), NOW(), NOW()),
('invite-luth-expired', 'hosp-ng-luth', 'team-luth-cardio', 'user-luth-jd-ibrahim', 'user-luth-cons-bello', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('invite-abj-accepted', 'hosp-ng-abj', 'team-abj-emergency', 'user-abj-nurse-hauwa', 'user-abj-cons-yusuf', 'ACCEPTED', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

INSERT INTO shift_schedule (id, hospital_id, ward_id, shift_type, start_time, end_time, days_of_week, is_active, created_at, updated_at) VALUES
('sched-luth-day-all', 'hosp-ng-luth', NULL, 'DAY', '07:00:00', '19:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-luth-night-all', 'hosp-ng-luth', NULL, 'NIGHT', '19:00:00', '07:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-abj-emergency-day', 'hosp-ng-abj', 'ward-abj-emergency', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-knh-day', 'hosp-ke-knh', 'ward-knh-med', 'DAY', '07:30:00', '19:30:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-uk-day', 'hosp-uk-stmary', 'ward-uk-acute', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW());

INSERT INTO on_call_rotation (id, hospital_id, department_id, ward_id, doctor_id, role, start_time, end_time, created_at, updated_at) VALUES
('oncall-luth-med-cons', 'hosp-ng-luth', 'dept-luth-medicine', NULL, 'user-luth-cons-ade', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_ADD(NOW(), INTERVAL 18 HOUR), NOW(), NOW()),
('oncall-luth-med-reg', 'hosp-ng-luth', 'dept-luth-medicine', NULL, 'user-luth-reg-nwosu', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_ADD(NOW(), INTERVAL 18 HOUR), NOW(), NOW()),
('oncall-abj-emergency-cons', 'hosp-ng-abj', 'dept-abj-emergency', NULL, 'user-abj-cons-yusuf', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 21 HOUR), NOW(), NOW()),
('oncall-abj-emergency-reg', 'hosp-ng-abj', 'dept-abj-emergency', NULL, 'user-abj-reg-okon', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 21 HOUR), NOW(), NOW());

INSERT INTO shift (id, ward_id, shift_schedule_id, type, start_time, end_time, lead_doctor_id, nurse_in_charge_id, status, assigned_at, created_at, updated_at) VALUES
('shift-luth-male-day', 'ward-luth-male-med', 'sched-luth-day-all', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-luth-cons-ade', 'user-luth-nurse-kemi', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),
('shift-luth-male-night', 'ward-luth-male-med', 'sched-luth-night-all', 'NIGHT', TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-luth-cardio-day', 'ward-luth-cardiac', 'sched-luth-day-all', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-luth-cons-bello', 'user-luth-nurse-grace', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-abj-emergency-day', 'ward-abj-emergency', 'sched-abj-emergency-day', 'DAY', TIMESTAMP(CURDATE(), '08:00:00'), TIMESTAMP(CURDATE(), '20:00:00'), 'user-abj-cons-yusuf', 'user-abj-nurse-hauwa', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-knh-day', 'ward-knh-med', 'sched-knh-day', 'DAY', TIMESTAMP(CURDATE(), '07:30:00'), TIMESTAMP(CURDATE(), '19:30:00'), 'user-knh-cons-otieno', 'user-knh-nurse-achebe', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW());

INSERT INTO patient (id, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, estimated_discharge_date, status, created_at, updated_at) VALUES
('pt-luth-001', 'hosp-ng-luth', 'ward-luth-male-med', 'M12', 'team-luth-med-a', 'user-luth-cons-ade', 'Emeka', 'Okoye', '1959-03-12', 'M', 'LUTH-0001', DATE_SUB(NOW(), INTERVAL 4 DAY), 'EMERGENCY', 'Community acquired pneumonia with type 2 diabetes', 'Internal Medicine', 'MEDIUM', 5, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-luth-002', 'hosp-ng-luth', 'ward-luth-cardiac', 'C03', 'team-luth-cardio', 'user-luth-cons-bello', 'Bamidele', 'Ajayi', '1964-11-20', 'M', 'LUTH-0002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'EMERGENCY', 'Acute decompensated heart failure', 'Cardiology', 'HIGH', 8, FALSE, NULL, 'DETERIORATING', NOW(), NOW()),
('pt-luth-003', 'hosp-ng-luth', NULL, NULL, 'team-luth-med-a', 'user-luth-cons-ade', 'Nkechi', 'Umeh', '1988-02-18', 'F', 'LUTH-0003', DATE_SUB(NOW(), INTERVAL 7 DAY), 'ELECTIVE', 'Post-operative appendicectomy, discharged home', 'General Surgery', 'LOW', 0, FALSE, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'DISCHARGED', NOW(), NOW()),
('pt-abj-001', 'hosp-ng-abj', 'ward-abj-emergency', 'E05', 'team-abj-emergency', 'user-abj-cons-yusuf', 'Suleiman', 'Abdullahi', '1952-07-25', 'M', 'NHA-0001', DATE_SUB(NOW(), INTERVAL 6 HOUR), 'EMERGENCY', 'Sepsis likely secondary to urinary tract infection', 'Emergency Medicine', 'CRITICAL', 11, FALSE, NULL, 'DETERIORATING', NOW(), NOW()),
('pt-abj-002', 'hosp-ng-abj', 'ward-abj-paeds', 'P09', 'team-abj-paeds', 'user-abj-cons-amina', 'Maryam', 'Usman', '2018-05-14', 'F', 'NHA-0002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'EMERGENCY', 'Bronchiolitis requiring oxygen therapy', 'Paediatrics', 'MEDIUM', 6, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-knh-001', 'hosp-ke-knh', 'ward-knh-med', 'K17', 'team-knh-med', 'user-knh-cons-otieno', 'Achieng', 'Njoroge', '1976-06-08', 'F', 'KNH-0001', DATE_SUB(NOW(), INTERVAL 3 DAY), 'TRANSFER', 'Uncontrolled hypertension with acute kidney injury', 'Internal Medicine', 'MEDIUM', 5, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-uk-001', 'hosp-uk-stmary', 'ward-uk-acute', 'A11', 'team-uk-acute', 'user-uk-cons-smith', 'George', 'Williams', '1949-10-30', 'M', 'STM-0001', DATE_SUB(NOW(), INTERVAL 2 DAY), 'EMERGENCY', 'COPD exacerbation, improving', 'Acute Medicine', 'LOW', 3, TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'DISCHARGE_READY', NOW(), NOW());

INSERT INTO patient_vitals (id, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at, created_at, updated_at) VALUES
('vitals-luth-001', 'pt-luth-001', 'user-luth-nurse-kemi', 102, 24, 94.00, 128, 38.2, 'ALERT', 5, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), NOW()),
('vitals-luth-002', 'pt-luth-002', 'user-luth-nurse-grace', 124, 28, 88.00, 96, 37.9, 'VOICE', 10, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW(), NOW()),
('vitals-abj-001', 'pt-abj-001', 'user-abj-nurse-hauwa', 132, 30, 89.00, 88, 39.3, 'VOICE', 11, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), NOW()),
('vitals-abj-002', 'pt-abj-002', 'user-abj-nurse-hauwa', 118, 34, 92.00, 100, 38.0, 'ALERT', 6, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
('vitals-knh-001', 'pt-knh-001', 'user-knh-nurse-achebe', 96, 22, 95.00, 172, 37.4, 'ALERT', 5, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW(), NOW()),
('vitals-uk-001', 'pt-uk-001', 'user-uk-nurse-evans', 84, 20, 96.00, 132, 36.9, 'ALERT', 3, DATE_SUB(NOW(), INTERVAL 50 MINUTE), NOW(), NOW());

INSERT INTO next_of_kin (id, patient_id, name, relationship, phone, email, preferred_contact_method, is_emergency_contact, notification_consent, created_at, updated_at) VALUES
('nok-luth-001', 'pt-luth-001', 'Adaeze Okoye', 'Daughter', '+2348021111001', 'adaeze.okoye@example.ng', 'BOTH', TRUE, TRUE, NOW(), NOW()),
('nok-luth-002', 'pt-luth-002', 'Temitope Ajayi', 'Son', '+2348021111003', 'temitope.ajayi@example.ng', 'EMAIL', TRUE, TRUE, NOW(), NOW()),
('nok-abj-001', 'pt-abj-001', 'Hadiza Abdullahi', 'Wife', '+2348032222001', 'hadiza.abdullahi@example.ng', 'BOTH', TRUE, TRUE, NOW(), NOW()),
('nok-abj-002', 'pt-abj-002', 'Aisha Usman', 'Mother', '+2348032222002', 'aisha.usman@example.ng', 'SMS', TRUE, TRUE, NOW(), NOW()),
('nok-knh-001', 'pt-knh-001', 'Daniel Njoroge', 'Brother', '+254711000100', 'daniel.njoroge@example.ke', 'EMAIL', TRUE, FALSE, NOW(), NOW()),
('nok-uk-001', 'pt-uk-001', 'Helen Williams', 'Spouse', '+447700900100', 'helen.williams@example.uk', 'EMAIL', TRUE, TRUE, NOW(), NOW());

INSERT INTO escalation (id, hospital_id, patient_id, triggered_by_id, trigger_type, severity, assigned_to_id, status, notes, resolved_at, created_at, updated_at) VALUES
('esc-luth-red-001', 'hosp-ng-luth', 'pt-luth-002', 'user-luth-nurse-grace', 'HIGH_NEWS_SCORE', 'RED', 'user-luth-cons-bello', 'OPEN', 'NEWS2 score 10 with hypotension and low saturation.', NULL, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW()),
('esc-abj-red-001', 'hosp-ng-abj', 'pt-abj-001', 'user-abj-nurse-hauwa', 'DETERIORATION', 'RED', 'user-abj-cons-yusuf', 'ACKNOWLEDGED', 'Sepsis patient remains hypotensive after initial fluid bolus.', NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW()),
('esc-knh-amber-001', 'hosp-ke-knh', 'pt-knh-001', 'user-knh-nurse-achebe', 'HIGH_NEWS_SCORE', 'AMBER', 'user-knh-cons-otieno', 'RESOLVED', 'AKI patient reviewed and antihypertensive plan adjusted.', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW());

INSERT INTO handover (id, ward_id, outgoing_shift_id, incoming_shift_id, conducted_by_id, status, general_notes, completed_at, created_at, updated_at) VALUES
('handover-luth-male-001', 'ward-luth-male-med', 'shift-luth-male-day', 'shift-luth-male-night', 'user-luth-reg-nwosu', 'IN_PROGRESS', 'Monitor Emeka Okoye closely overnight; blood cultures pending.', NULL, NOW(), NOW());

INSERT INTO patient_handover_note (id, handover_id, patient_id, status_summary, outstanding_task_ids, urgency_flag, added_by_id, created_at, updated_at) VALUES
('hnote-luth-001', 'handover-luth-male-001', 'pt-luth-001', 'Still febrile but oxygen requirement reduced. Review cultures once available.', 'task-luth-001,task-luth-002', TRUE, 'user-luth-jd-ibrahim', NOW(), NOW());

INSERT INTO round (id, hospital_id, ward_id, medical_team_id, shift_id, round_type, lead_doctor_id, status, scheduled_time, started_at, completed_at, team_members, created_at, updated_at) VALUES
('round-luth-med-active', 'hosp-ng-luth', 'ward-luth-male-med', 'team-luth-med-a', 'shift-luth-male-day', 'MORNING', 'user-luth-cons-ade', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 40 MINUTE), NULL, 'user-luth-cons-ade,user-luth-reg-nwosu,user-luth-jd-ibrahim', NOW(), NOW()),
('round-luth-cardio-complete', 'hosp-ng-luth', 'ward-luth-cardiac', 'team-luth-cardio', 'shift-luth-cardio-day', 'POST_TAKE', 'user-luth-cons-bello', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'user-luth-cons-bello,user-luth-nurse-grace', DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
('round-abj-emergency-scheduled', 'hosp-ng-abj', 'ward-abj-emergency', 'team-abj-emergency', 'shift-abj-emergency-day', 'BOARD', 'user-abj-cons-yusuf', 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 1 HOUR), NULL, NULL, 'user-abj-cons-yusuf,user-abj-reg-okon,user-abj-jd-musa', NOW(), NOW());

INSERT INTO patient_round_review (id, round_id, patient_id, reviewed_by_id, review_order, news_score_at_review, clinical_status, was_examined, management_plan, discharge_assessment, notified_next_of_kin, reviewed_at, created_at, updated_at) VALUES
('review-luth-001', 'round-luth-med-active', 'pt-luth-001', 'user-luth-cons-ade', 1, 5, 'STABLE', TRUE, 'Continue ceftriaxone, repeat FBC tomorrow, encourage oral fluids.', 'NONE', FALSE, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
('review-luth-002', 'round-luth-cardio-complete', 'pt-luth-002', 'user-luth-cons-bello', 1, 10, 'DETERIORATING', TRUE, 'Increase diuretics, urgent echo review, keep on cardiac monitor.', 'BLOCKED_MEDICAL', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW());

INSERT INTO clinical_note (id, patient_id, patient_round_review_id, author_id, note_type, content, is_amended, amended_by_id, amended_at, created_at, updated_at) VALUES
('note-luth-001', 'pt-luth-001', 'review-luth-001', 'user-luth-jd-ibrahim', 'ROUND_NOTE', 'Reviewed on morning round. Fever trending down, chest findings improving, diabetes control acceptable.', FALSE, NULL, NULL, NOW(), NOW()),
('note-luth-002', 'pt-luth-002', 'review-luth-002', 'user-luth-reg-nwosu', 'ESCALATION_NOTE', 'Patient breathless at rest with raised JVP. Consultant informed and reviewed at bedside.', FALSE, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('note-abj-001', 'pt-abj-001', NULL, 'user-abj-jd-musa', 'ADMISSION_NOTE', 'Brought in from Garki with fever, confusion, and low blood pressure. Sepsis bundle started.', FALSE, NULL, NULL, NOW(), NOW());

INSERT INTO care_task (id, hospital_id, patient_id, ward_id, round_id, created_by_id, assigned_to_id, assigned_to_role, task_type, source, title, description, priority, window_start, window_end, status, completed_by_id, completed_at, escalated_at, workload_conflict, workload_conflict_reason, created_at, updated_at) VALUES
('task-luth-001', 'hosp-ng-luth', 'pt-luth-001', 'ward-luth-male-med', 'round-luth-med-active', 'user-luth-cons-ade', 'user-luth-nurse-kemi', 'NURSE', 'Medication', 'NURSING_CARE_PLAN', 'Administer IV ceftriaxone', 'Dose due after morning round.', 'URGENT', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'PENDING', NULL, NULL, NULL, FALSE, NULL, NOW(), NOW()),
('task-luth-002', 'hosp-ng-luth', 'pt-luth-001', 'ward-luth-male-med', 'round-luth-med-active', 'user-luth-reg-nwosu', 'user-luth-jd-ibrahim', 'JUNIOR_DOCTOR', 'Bloods Review', 'POST_ROUND_JOB', 'Review FBC, U&E, and culture results', 'Escalate to registrar if creatinine rises.', 'ROUTINE', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'PENDING', NULL, NULL, NULL, TRUE, 'No same-specialty junior doctor was free in the requested window; assigned with supervisor notification.', NOW(), NOW()),
('task-luth-003', 'hosp-ng-luth', 'pt-luth-002', 'ward-luth-cardiac', 'round-luth-cardio-complete', 'user-luth-cons-bello', 'user-luth-reg-nwosu', 'REGISTRAR', 'Echo Review', 'POST_ROUND_JOB', 'Chase urgent echocardiogram', 'Confirm LV function and valve status.', 'EMERGENCY', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'IN_PROGRESS', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('task-abj-001', 'hosp-ng-abj', 'pt-abj-001', 'ward-abj-emergency', NULL, 'user-abj-cons-yusuf', 'user-abj-nurse-hauwa', 'NURSE', 'Sepsis Bundle', 'NURSING_CARE_PLAN', 'Repeat lactate and fluid balance', 'Repeat lactate after initial fluids and chart urine output hourly.', 'EMERGENCY', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'PENDING', NULL, NULL, NULL, FALSE, NULL, NOW(), NOW()),
('task-abj-002', 'hosp-ng-abj', 'pt-abj-002', 'ward-abj-paeds', NULL, 'user-abj-cons-amina', 'user-abj-nurse-hauwa', 'NURSE', 'Oxygen Monitoring', 'NURSING_CARE_PLAN', 'Record paediatric respiratory observations', 'Respiratory rate, saturation, and work of breathing every hour.', 'URGENT', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'COMPLETED', 'user-abj-nurse-hauwa', DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW());

INSERT INTO outbox_event (id, hospital_id, event_type, payload, published, published_at, correlation_id, created_at, updated_at) VALUES
('outbox-001', 'hosp-ng-luth', 'PATIENT_DETERIORATION', '{"hospitalId":"hosp-ng-luth","patientId":"pt-luth-002","severity":"RED","newsScore":10}', TRUE, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'corr-luth-red-001', DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW()),
('outbox-002', 'hosp-ng-abj', 'TASK_OVERDUE', '{"hospitalId":"hosp-ng-abj","taskId":"task-abj-001","patientId":"pt-abj-001"}', FALSE, NULL, 'corr-abj-task-001', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW());

SET FOREIGN_KEY_CHECKS = 1;

USE careround_notification;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE failed_notifications;

INSERT INTO notifications (id, event_type, hospital_id, recipient_id, recipient_type, channel, subject, body, correlation_id, payload, status, failure_reason, sent_at, retry_count, created_at, updated_at) VALUES
('notif-luth-001', 'careround.patient.deterioration', 'hosp-ng-luth', 'user-luth-cons-bello', 'USER', 'SMS', 'RED escalation for Bamidele Ajayi', 'NEWS2 score 10. Patient requires urgent consultant review.', 'corr-luth-red-001', '{"patientId":"pt-luth-002","severity":"RED"}', 'SENT', NULL, DATE_SUB(NOW(), INTERVAL 12 MINUTE), 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW()),
('notif-abj-001', 'careround.task.overdue', 'hosp-ng-abj', 'user-abj-ws-ngozi', 'USER', 'SMS', 'Overdue emergency task', 'Repeat lactate and fluid balance task is overdue for Suleiman Abdullahi.', 'corr-abj-task-001', '{"taskId":"task-abj-001"}', 'FAILED', 'SMS provider timeout', NULL, 2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW());

INSERT INTO failed_notifications (id, event_type, topic, hospital_id, correlation_id, payload, error_message, failed_at, retry_count, resolved, created_at, updated_at) VALUES
('failed-notif-001', 'careround.task.overdue', 'careround.task.overdue', 'hosp-ng-abj', 'corr-abj-task-001', '{"taskId":"task-abj-001","recipientId":"user-abj-ws-ngozi"}', 'SMS provider timeout after retries', DATE_SUB(NOW(), INTERVAL 3 MINUTE), 3, FALSE, DATE_SUB(NOW(), INTERVAL 3 MINUTE), NOW());

SET FOREIGN_KEY_CHECKS = 1;

USE careround_audit;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE audit_log;

INSERT INTO audit_log (id, event_type, hospital_id, correlation_id, payload, kafka_topic, kafka_partition, kafka_offset, received_at, processed_at, created_at, updated_at) VALUES
('audit-001', 'PATIENT_DETERIORATION', 'hosp-ng-luth', 'corr-luth-red-001', '{"patientId":"pt-luth-002","severity":"RED","assignedToId":"user-luth-cons-bello"}', 'careround.patient.deterioration', 0, 1201, DATE_SUB(NOW(), INTERVAL 14 MINUTE), DATE_SUB(NOW(), INTERVAL 14 MINUTE), DATE_SUB(NOW(), INTERVAL 14 MINUTE), NOW()),
('audit-002', 'TASK_OVERDUE', 'hosp-ng-abj', 'corr-abj-task-001', '{"taskId":"task-abj-001","patientId":"pt-abj-001","wardId":"ward-abj-emergency"}', 'careround.task.overdue', 0, 1202, DATE_SUB(NOW(), INTERVAL 4 MINUTE), DATE_SUB(NOW(), INTERVAL 4 MINUTE), DATE_SUB(NOW(), INTERVAL 4 MINUTE), NOW()),
('audit-003', 'HOSPITAL_ONBOARDING_REVIEWED', 'PLATFORM', 'corr-onboard-001', '{"requestId":"onboard-001","status":"APPROVED","reviewedByUserId":"plat-admin-001"}', 'careround.hospital.onboarding_reviewed', 0, 1190, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Seed complete
-- ============================================================================
-- Active login examples, password: Password123!
--   platform-admin@careround.local
--   chioma.eze@luth.example.ng
--   tunde.adewale@luth.example.ng
--   kemi.balogun@luth.example.ng
--   zainab.musa@nha.example.ng
--   bashir.yusuf@nha.example.ng
--   wanjiku.mwangi@knh.example.ke
--   amelia.turner@stmarys.example.uk
-- ============================================================================
