-- ============================================================================
-- CareRound Seed Data Script
-- ============================================================================
-- Run after all Flyway migrations have completed.
--
-- Demo login password for active platform and tenant users: Password123
-- Demo activation token: activate-cedar-admin
--
-- Patient/user names are fictitious. Hospital/place context is realistic and
-- intentionally biased toward Nigerian clinical workflows.
-- ============================================================================

USE careround_core;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE account_activation_token;
TRUNCATE TABLE password_reset_token;
TRUNCATE TABLE notification_read_receipt;
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

SET @password_hash = '$2a$10$BxWU1teADySM1SiaFy2jhuzN3JKhgWR41Hlfn25dctv7Cw7zmDSaC';

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
('onboard-003', 'Korle Bu Digital Pilot Ward', 'Ghana', 'pilot@kbth.example.gh', '+233201001001', 'Teaching Hospital Pilot', '120', 'Multidisciplinary handover and task escalation pilot.', 'CONTACTED', 'Awaiting data processing agreement.', 'plat-admin-001', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('onboard-004', 'Calabar Critical Care Partnership', 'Nigeria', 'hello@cccp.example.ng', '+2348010000505', 'Regional Referral Hospital', '210', 'Reduce handover failures across surgical and medical wards.', 'APPROVED', 'Commercial and clinical reviews are complete; awaiting provisioning.', 'plat-admin-001', DATE_SUB(NOW(), INTERVAL 10 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('onboard-005', 'Accra Home Recovery Network', 'Ghana', 'ops@ahrn.example.gh', '+233201001002', 'Post Acute Care Network', '45', 'Out-of-scope deployment request for current inpatient-first roadmap.', 'REJECTED', 'Rejected because the request is not aligned with the inpatient product scope.', 'plat-admin-001', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

INSERT INTO department (id, hospital_id, name, head_of_department_id, created_at, updated_at) VALUES
('dept-luth-medicine', 'hosp-ng-luth', 'Internal Medicine', 'user-luth-cons-ade', NOW(), NOW()),
('dept-luth-cardiology', 'hosp-ng-luth', 'Cardiology', 'user-luth-cons-bello', NOW(), NOW()),
('dept-abj-emergency', 'hosp-ng-abj', 'Emergency Medicine', 'user-abj-cons-yusuf', NOW(), NOW()),
('dept-abj-paeds', 'hosp-ng-abj', 'Paediatrics', 'user-abj-cons-amina', NOW(), NOW()),
('dept-cedar-medicine', 'hosp-ng-cedar', 'General Medicine', 'user-cedar-cons-ibiso', NOW(), NOW()),
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
('user-cedar-cons-ibiso', 'hosp-ng-cedar', 'Ibiso', 'George', 'ibiso.george@cedarspecialist.example.ng', @password_hash, 'CONSULTANT', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
('user-cedar-reg-jaja', 'hosp-ng-cedar', 'Jaja', 'Fubara', 'jaja.fubara@cedarspecialist.example.ng', @password_hash, 'REGISTRAR', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
('user-cedar-jd-daniel', 'hosp-ng-cedar', 'Daniel', 'Tamunotonye', 'daniel.tamunotonye@cedarspecialist.example.ng', @password_hash, 'JUNIOR_DOCTOR', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
('user-cedar-nurse-ama', 'hosp-ng-cedar', 'Ama', 'Erekosima', 'ama.erekosima@cedarspecialist.example.ng', @password_hash, 'NURSE', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
('user-cedar-ws-ifunanya', 'hosp-ng-cedar', 'Ifunanya', 'Nwankwo', 'ifunanya.nwankwo@cedarspecialist.example.ng', @password_hash, 'WARD_SUPERVISOR', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
('user-knh-admin', 'hosp-ke-knh', 'Wanjiku', 'Mwangi', 'wanjiku.mwangi@knh.example.ke', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-knh-cons-otieno', 'hosp-ke-knh', 'Peter', 'Otieno', 'peter.otieno@knh.example.ke', @password_hash, 'CONSULTANT', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-knh-reg-kimani', 'hosp-ke-knh', 'Mercy', 'Kimani', 'mercy.kimani@knh.example.ke', @password_hash, 'REGISTRAR', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-knh-jd-wairimu', 'hosp-ke-knh', 'Wairimu', 'Njeri', 'wairimu.njeri@knh.example.ke', @password_hash, 'JUNIOR_DOCTOR', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-knh-nurse-achebe', 'hosp-ke-knh', 'Nneka', 'Achebe', 'nneka.achebe@knh.example.ke', @password_hash, 'NURSE', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-knh-ws-maina', 'hosp-ke-knh', 'Joseph', 'Maina', 'joseph.maina@knh.example.ke', @password_hash, 'WARD_SUPERVISOR', 'dept-knh-medicine', TRUE, NOW(), NOW()),
('user-uk-admin', 'hosp-uk-stmary', 'Amelia', 'Turner', 'amelia.turner@stmarys.example.uk', @password_hash, 'ADMIN', NULL, TRUE, NOW(), NOW()),
('user-uk-cons-smith', 'hosp-uk-stmary', 'Oliver', 'Smith', 'oliver.smith@stmarys.example.uk', @password_hash, 'CONSULTANT', 'dept-uk-medicine', TRUE, NOW(), NOW()),
('user-uk-reg-khan', 'hosp-uk-stmary', 'Sara', 'Khan', 'sara.khan@stmarys.example.uk', @password_hash, 'REGISTRAR', 'dept-uk-medicine', TRUE, NOW(), NOW()),
('user-uk-jd-patel', 'hosp-uk-stmary', 'Ravi', 'Patel', 'ravi.patel@stmarys.example.uk', @password_hash, 'JUNIOR_DOCTOR', 'dept-uk-medicine', TRUE, NOW(), NOW()),
('user-uk-nurse-evans', 'hosp-uk-stmary', 'Sophie', 'Evans', 'sophie.evans@stmarys.example.uk', @password_hash, 'NURSE', 'dept-uk-medicine', TRUE, NOW(), NOW()),
('user-uk-ws-clarke', 'hosp-uk-stmary', 'Helen', 'Clarke', 'helen.clarke@stmarys.example.uk', @password_hash, 'WARD_SUPERVISOR', 'dept-uk-medicine', TRUE, NOW(), NOW());

INSERT INTO account_activation_token (id, token_hash, user_id, hospital_id, expires_at, used_at, created_at, updated_at) VALUES
('act-cedar-admin', '5f460a6b3d1bd0d099412ec512a8e69e54b98dd57a3e1585fb94a3c2e77d37d2', 'user-cedar-admin', 'hosp-ng-cedar', DATE_ADD(NOW(), INTERVAL 72 HOUR), NULL, NOW(), NOW()),
('act-luth-admin-used', '881144bb9ae7ec088c1dbaaf82031f8d68e5e989a6f3620d3ae7f3f1f6a6f0e4', 'user-luth-admin', 'hosp-ng-luth', DATE_ADD(NOW(), INTERVAL 48 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NOW());

INSERT INTO password_reset_token (id, hospital_id, user_id, token_hash, expires_at, used_at, created_at, updated_at) VALUES
('reset-luth-admin-open', 'hosp-ng-luth', 'user-luth-admin', '0d6fffdaf8bfa5f56c4a0f1ddf4a4e49cf4cc8ad013810db07e050668b98a001', DATE_ADD(NOW(), INTERVAL 2 HOUR), NULL, NOW(), NOW()),
('reset-abj-used', 'hosp-ng-abj', 'user-abj-admin', '42b28f4880322b49ee8c3f447f6691641bc6e9d749e77424f4b08a1e94e9d101', DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()),
('reset-uk-expired', 'hosp-uk-stmary', 'user-uk-admin', 'c68c9819a47e5ce05dd3a657f59b5efbdadca04bd67eab6260e29cd9d0c47602', DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW());

INSERT INTO refresh_tokens (id, user_id, hospital_id, token, expires_at, revoked, created_at, updated_at) VALUES
('rt-luth-admin-active', 'user-luth-admin', 'hosp-ng-luth', 'seed-refresh-luth-admin-active', DATE_ADD(NOW(), INTERVAL 7 DAY), FALSE, NOW(), NOW()),
('rt-luth-reg-revoked', 'user-luth-reg-nwosu', 'hosp-ng-luth', 'seed-refresh-luth-reg-revoked', DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('rt-abj-expired', 'user-abj-admin', 'hosp-ng-abj', 'seed-refresh-abj-expired', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
('rt-cedar-cons-active', 'user-cedar-cons-ibiso', 'hosp-ng-cedar', 'seed-refresh-cedar-cons-active', DATE_ADD(NOW(), INTERVAL 5 DAY), FALSE, NOW(), NOW()),
('rt-uk-admin-revoked', 'user-uk-admin', 'hosp-uk-stmary', 'seed-refresh-uk-admin-revoked', DATE_ADD(NOW(), INTERVAL 5 DAY), TRUE, DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW());

INSERT INTO ward (id, hospital_id, name, specialty, total_beds, supervisor_id, created_at, updated_at) VALUES
('ward-luth-male-med', 'hosp-ng-luth', 'Male Medical Ward', 'Internal Medicine', 36, 'user-luth-ws-femi', NOW(), NOW()),
('ward-luth-female-med', 'hosp-ng-luth', 'Female Medical Ward', 'Internal Medicine', 34, 'user-luth-ws-femi', NOW(), NOW()),
('ward-luth-cardiac', 'hosp-ng-luth', 'Cardiac Stepdown Unit', 'Cardiology', 20, 'user-luth-ws-femi', NOW(), NOW()),
('ward-abj-emergency', 'hosp-ng-abj', 'Emergency Observation Ward', 'Emergency Medicine', 28, 'user-abj-ws-ngozi', NOW(), NOW()),
('ward-abj-paeds', 'hosp-ng-abj', 'Paediatric Medical Ward', 'Paediatrics', 32, 'user-abj-ws-ngozi', NOW(), NOW()),
('ward-cedar-med', 'hosp-ng-cedar', 'General Medical Ward', 'General Medicine', 24, 'user-cedar-ws-ifunanya', NOW(), NOW()),
('ward-cedar-hdu', 'hosp-ng-cedar', 'High Dependency Bay', 'General Medicine', 10, 'user-cedar-ws-ifunanya', NOW(), NOW()),
('ward-knh-med', 'hosp-ke-knh', 'Medical Ward 7', 'Internal Medicine', 30, 'user-knh-ws-maina', NOW(), NOW()),
('ward-uk-acute', 'hosp-uk-stmary', 'Acute Medical Unit', 'Acute Medicine', 26, 'user-uk-ws-clarke', NOW(), NOW());

INSERT INTO medical_team (id, hospital_id, name, consultant_id, department_id, created_at, updated_at) VALUES
('team-luth-med-a', 'hosp-ng-luth', 'LUTH Medicine Team A', 'user-luth-cons-ade', 'dept-luth-medicine', NOW(), NOW()),
('team-luth-cardio', 'hosp-ng-luth', 'LUTH Cardiology Team', 'user-luth-cons-bello', 'dept-luth-cardiology', NOW(), NOW()),
('team-abj-emergency', 'hosp-ng-abj', 'NHA Emergency Team', 'user-abj-cons-yusuf', 'dept-abj-emergency', NOW(), NOW()),
('team-abj-paeds', 'hosp-ng-abj', 'NHA Paediatrics Team', 'user-abj-cons-amina', 'dept-abj-paeds', NOW(), NOW()),
('team-cedar-med', 'hosp-ng-cedar', 'Cedar General Medicine Team', 'user-cedar-cons-ibiso', 'dept-cedar-medicine', NOW(), NOW()),
('team-knh-med', 'hosp-ke-knh', 'KNH Medicine Team', 'user-knh-cons-otieno', 'dept-knh-medicine', NOW(), NOW()),
('team-uk-acute', 'hosp-uk-stmary', 'St Marys Acute Medicine', 'user-uk-cons-smith', 'dept-uk-medicine', NOW(), NOW());

INSERT INTO medical_team_ward (medical_team_id, ward_id, assigned_at) VALUES
('team-luth-med-a', 'ward-luth-male-med', NOW()),
('team-luth-med-a', 'ward-luth-female-med', NOW()),
('team-luth-cardio', 'ward-luth-cardiac', NOW()),
('team-abj-emergency', 'ward-abj-emergency', NOW()),
('team-abj-paeds', 'ward-abj-paeds', NOW()),
('team-cedar-med', 'ward-cedar-med', NOW()),
('team-cedar-med', 'ward-cedar-hdu', NOW()),
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
('team-cedar-med', 'user-cedar-cons-ibiso', NOW()),
('team-cedar-med', 'user-cedar-reg-jaja', NOW()),
('team-cedar-med', 'user-cedar-jd-daniel', NOW()),
('team-cedar-med', 'user-cedar-nurse-ama', NOW()),
('team-knh-med', 'user-knh-cons-otieno', NOW()),
('team-knh-med', 'user-knh-reg-kimani', NOW()),
('team-knh-med', 'user-knh-jd-wairimu', NOW()),
('team-knh-med', 'user-knh-nurse-achebe', NOW()),
('team-uk-acute', 'user-uk-cons-smith', NOW()),
('team-uk-acute', 'user-uk-reg-khan', NOW()),
('team-uk-acute', 'user-uk-jd-patel', NOW()),
('team-uk-acute', 'user-uk-nurse-evans', NOW());

INSERT INTO medical_team_invite (id, hospital_id, medical_team_id, invited_user_id, invited_by_id, status, expires_at, created_at, updated_at) VALUES
('invite-luth-pending', 'hosp-ng-luth', 'team-luth-med-a', 'user-luth-nurse-grace', 'user-luth-cons-ade', 'PENDING', DATE_ADD(NOW(), INTERVAL 24 HOUR), NOW(), NOW()),
('invite-luth-expired', 'hosp-ng-luth', 'team-luth-cardio', 'user-luth-jd-ibrahim', 'user-luth-cons-bello', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('invite-abj-accepted', 'hosp-ng-abj', 'team-abj-emergency', 'user-abj-nurse-hauwa', 'user-abj-cons-yusuf', 'ACCEPTED', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('invite-cedar-declined', 'hosp-ng-cedar', 'team-cedar-med', 'user-cedar-jd-daniel', 'user-cedar-cons-ibiso', 'DECLINED', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 8 HOUR), NOW()),
('invite-knh-expired', 'hosp-ke-knh', 'team-knh-med', 'user-knh-jd-wairimu', 'user-knh-cons-otieno', 'EXPIRED', DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

INSERT INTO shift_schedule (id, hospital_id, ward_id, shift_type, start_time, end_time, days_of_week, is_active, created_at, updated_at) VALUES
('sched-luth-day-all', 'hosp-ng-luth', NULL, 'DAY', '07:00:00', '19:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-luth-night-all', 'hosp-ng-luth', NULL, 'NIGHT', '19:00:00', '07:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-luth-weekend-med', 'hosp-ng-luth', 'ward-luth-female-med', 'DAY', '08:00:00', '20:00:00', 'SATURDAY,SUNDAY', TRUE, NOW(), NOW()),
('sched-abj-emergency-day', 'hosp-ng-abj', 'ward-abj-emergency', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-abj-emergency-night', 'hosp-ng-abj', 'ward-abj-emergency', 'NIGHT', '20:00:00', '08:00:00', 'MONDAY,WEDNESDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-abj-paeds-day', 'hosp-ng-abj', 'ward-abj-paeds', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-abj-paeds-night', 'hosp-ng-abj', 'ward-abj-paeds', 'NIGHT', '20:00:00', '08:00:00', 'MONDAY,WEDNESDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-cedar-day', 'hosp-ng-cedar', NULL, 'DAY', '07:00:00', '19:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-cedar-night-med', 'hosp-ng-cedar', 'ward-cedar-med', 'NIGHT', '19:00:00', '07:00:00', 'TUESDAY,THURSDAY,SATURDAY', TRUE, NOW(), NOW()),
('sched-cedar-night-hdu', 'hosp-ng-cedar', 'ward-cedar-hdu', 'NIGHT', '19:00:00', '07:00:00', 'TUESDAY,THURSDAY,SATURDAY', TRUE, NOW(), NOW()),
('sched-knh-day', 'hosp-ke-knh', 'ward-knh-med', 'DAY', '07:30:00', '19:30:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-knh-night', 'hosp-ke-knh', 'ward-knh-med', 'NIGHT', '19:30:00', '07:30:00', 'SATURDAY,SUNDAY', FALSE, NOW(), NOW()),
('sched-uk-day', 'hosp-uk-stmary', 'ward-uk-acute', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', TRUE, NOW(), NOW()),
('sched-uk-night', 'hosp-uk-stmary', 'ward-uk-acute', 'NIGHT', '20:00:00', '08:00:00', 'MONDAY,WEDNESDAY,FRIDAY', TRUE, NOW(), NOW());

INSERT INTO on_call_rotation (id, hospital_id, department_id, ward_id, doctor_id, role, start_time, end_time, created_at, updated_at) VALUES
('oncall-luth-med-cons', 'hosp-ng-luth', 'dept-luth-medicine', NULL, 'user-luth-cons-ade', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_ADD(NOW(), INTERVAL 18 HOUR), NOW(), NOW()),
('oncall-luth-med-reg', 'hosp-ng-luth', 'dept-luth-medicine', NULL, 'user-luth-reg-nwosu', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_ADD(NOW(), INTERVAL 18 HOUR), NOW(), NOW()),
('oncall-abj-emergency-cons', 'hosp-ng-abj', 'dept-abj-emergency', NULL, 'user-abj-cons-yusuf', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 21 HOUR), NOW(), NOW()),
('oncall-abj-emergency-reg', 'hosp-ng-abj', 'dept-abj-emergency', NULL, 'user-abj-reg-okon', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 21 HOUR), NOW(), NOW()),
('oncall-cedar-med-cons', 'hosp-ng-cedar', 'dept-cedar-medicine', NULL, 'user-cedar-cons-ibiso', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_ADD(NOW(), INTERVAL 20 HOUR), NOW(), NOW()),
('oncall-cedar-med-reg', 'hosp-ng-cedar', 'dept-cedar-medicine', 'ward-cedar-hdu', 'user-cedar-reg-jaja', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_ADD(NOW(), INTERVAL 20 HOUR), NOW(), NOW()),
('oncall-knh-med-reg', 'hosp-ke-knh', 'dept-knh-medicine', NULL, 'user-knh-reg-kimani', 'REGISTRAR_ON_CALL', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_ADD(NOW(), INTERVAL 19 HOUR), NOW(), NOW()),
('oncall-uk-med-cons', 'hosp-uk-stmary', 'dept-uk-medicine', NULL, 'user-uk-cons-smith', 'CONSULTANT_ON_CALL', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 22 HOUR), NOW(), NOW());

INSERT INTO shift (id, ward_id, shift_schedule_id, type, start_time, end_time, lead_doctor_id, nurse_in_charge_id, status, assigned_at, created_at, updated_at) VALUES
('shift-luth-male-day', 'ward-luth-male-med', 'sched-luth-day-all', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-luth-cons-ade', 'user-luth-nurse-kemi', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),
('shift-luth-male-night', 'ward-luth-male-med', 'sched-luth-night-all', 'NIGHT', TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-luth-female-day', 'ward-luth-female-med', 'sched-luth-day-all', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-luth-reg-nwosu', 'user-luth-nurse-kemi', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW(), NOW()),
('shift-luth-female-night', 'ward-luth-female-med', 'sched-luth-night-all', 'NIGHT', TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-luth-cardiac-prev-night', 'ward-luth-cardiac', 'sched-luth-night-all', 'NIGHT', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '19:00:00'), TIMESTAMP(CURDATE(), '07:00:00'), 'user-luth-cons-bello', 'user-luth-nurse-grace', 'HANDED_OVER', DATE_SUB(NOW(), INTERVAL 14 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('shift-luth-cardio-day', 'ward-luth-cardiac', 'sched-luth-day-all', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-luth-cons-bello', 'user-luth-nurse-grace', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-abj-emergency-day', 'ward-abj-emergency', 'sched-abj-emergency-day', 'DAY', TIMESTAMP(CURDATE(), '08:00:00'), TIMESTAMP(CURDATE(), '20:00:00'), 'user-abj-cons-yusuf', 'user-abj-nurse-hauwa', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-abj-emergency-night', 'ward-abj-emergency', 'sched-abj-emergency-night', 'NIGHT', TIMESTAMP(CURDATE(), '20:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-abj-paeds-day', 'ward-abj-paeds', 'sched-abj-paeds-day', 'DAY', TIMESTAMP(CURDATE(), '08:00:00'), TIMESTAMP(CURDATE(), '20:00:00'), 'user-abj-cons-amina', 'user-abj-nurse-hauwa', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 55 MINUTE), NOW(), NOW()),
('shift-abj-paeds-prev-night', 'ward-abj-paeds', 'sched-abj-paeds-night', 'NIGHT', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '20:00:00'), TIMESTAMP(CURDATE(), '08:00:00'), 'user-abj-reg-okon', 'user-abj-nurse-hauwa', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 13 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('shift-abj-paeds-night', 'ward-abj-paeds', 'sched-abj-paeds-night', 'NIGHT', TIMESTAMP(CURDATE(), '20:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-cedar-med-day', 'ward-cedar-med', 'sched-cedar-day', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-cedar-cons-ibiso', 'user-cedar-nurse-ama', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),
('shift-cedar-med-night', 'ward-cedar-med', 'sched-cedar-night-med', 'NIGHT', TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-cedar-hdu-day', 'ward-cedar-hdu', 'sched-cedar-day', 'DAY', TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '19:00:00'), 'user-cedar-reg-jaja', 'user-cedar-nurse-ama', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 95 MINUTE), NOW(), NOW()),
('shift-cedar-hdu-night', 'ward-cedar-hdu', 'sched-cedar-night-hdu', 'NIGHT', TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW()),
('shift-knh-prev-night', 'ward-knh-med', 'sched-knh-night', 'NIGHT', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '19:30:00'), TIMESTAMP(CURDATE(), '07:30:00'), 'user-knh-reg-kimani', 'user-knh-nurse-achebe', 'HANDED_OVER', DATE_SUB(NOW(), INTERVAL 15 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('shift-knh-day', 'ward-knh-med', 'sched-knh-day', 'DAY', TIMESTAMP(CURDATE(), '07:30:00'), TIMESTAMP(CURDATE(), '19:30:00'), 'user-knh-cons-otieno', 'user-knh-nurse-achebe', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-uk-prev-night', 'ward-uk-acute', 'sched-uk-night', 'NIGHT', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '20:00:00'), TIMESTAMP(CURDATE(), '08:00:00'), 'user-uk-reg-khan', 'user-uk-nurse-evans', 'HANDED_OVER', DATE_SUB(NOW(), INTERVAL 13 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('shift-uk-day', 'ward-uk-acute', 'sched-uk-day', 'DAY', TIMESTAMP(CURDATE(), '08:00:00'), TIMESTAMP(CURDATE(), '20:00:00'), 'user-uk-cons-smith', 'user-uk-nurse-evans', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
('shift-uk-night', 'ward-uk-acute', 'sched-uk-night', 'NIGHT', TIMESTAMP(CURDATE(), '20:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), NULL, NULL, 'PENDING_ASSIGNMENT', NULL, NOW(), NOW());

INSERT INTO patient (id, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, estimated_discharge_date, status, created_at, updated_at) VALUES
('pt-luth-001', 'hosp-ng-luth', 'ward-luth-male-med', 'M12', 'team-luth-med-a', 'user-luth-cons-ade', 'Emeka', 'Okoye', '1959-03-12', 'M', 'LUTH-0001', DATE_SUB(NOW(), INTERVAL 4 DAY), 'EMERGENCY', 'Community acquired pneumonia with type 2 diabetes', 'Internal Medicine', 'MEDIUM', 5, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-luth-002', 'hosp-ng-luth', 'ward-luth-cardiac', 'C03', 'team-luth-cardio', 'user-luth-cons-bello', 'Bamidele', 'Ajayi', '1964-11-20', 'M', 'LUTH-0002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'EMERGENCY', 'Acute decompensated heart failure', 'Cardiology', 'HIGH', 8, FALSE, NULL, 'DETERIORATING', NOW(), NOW()),
('pt-luth-003', 'hosp-ng-luth', NULL, NULL, 'team-luth-med-a', 'user-luth-cons-ade', 'Nkechi', 'Umeh', '1988-02-18', 'F', 'LUTH-0003', DATE_SUB(NOW(), INTERVAL 7 DAY), 'ELECTIVE', 'Post-operative appendicectomy, discharged home', 'General Surgery', 'LOW', 0, FALSE, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'DISCHARGED', NOW(), NOW()),
('pt-luth-004', 'hosp-ng-luth', 'ward-luth-female-med', 'F08', 'team-luth-med-a', 'user-luth-cons-ade', 'Morenike', 'Afolayan', '1973-08-04', 'F', 'LUTH-0004', DATE_SUB(NOW(), INTERVAL 2 DAY), 'TRANSFER', 'Uncontrolled diabetes with infected foot ulcer', 'Internal Medicine', 'MEDIUM', 4, FALSE, NULL, 'STABLE', NOW(), NOW()),
('pt-luth-005', 'hosp-ng-luth', 'ward-luth-female-med', 'F11', 'team-luth-med-a', 'user-luth-cons-ade', 'Hauwa', 'Lawal', '1981-01-22', 'F', 'LUTH-0005', DATE_SUB(NOW(), INTERVAL 5 DAY), 'EMERGENCY', 'Severe anaemia now clinically improved', 'Internal Medicine', 'LOW', 2, TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'DISCHARGE_READY', NOW(), NOW()),
('pt-abj-001', 'hosp-ng-abj', 'ward-abj-emergency', 'E05', 'team-abj-emergency', 'user-abj-cons-yusuf', 'Suleiman', 'Abdullahi', '1952-07-25', 'M', 'NHA-0001', DATE_SUB(NOW(), INTERVAL 6 HOUR), 'EMERGENCY', 'Sepsis likely secondary to urinary tract infection', 'Emergency Medicine', 'CRITICAL', 11, FALSE, NULL, 'DETERIORATING', NOW(), NOW()),
('pt-abj-002', 'hosp-ng-abj', 'ward-abj-paeds', 'P09', 'team-abj-paeds', 'user-abj-cons-amina', 'Maryam', 'Usman', '2018-05-14', 'F', 'NHA-0002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'EMERGENCY', 'Bronchiolitis requiring oxygen therapy', 'Paediatrics', 'MEDIUM', 6, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-abj-003', 'hosp-ng-abj', 'ward-abj-emergency', 'E11', 'team-abj-emergency', 'user-abj-cons-yusuf', 'Chinonso', 'Eze', '1990-09-09', 'M', 'NHA-0003', DATE_SUB(NOW(), INTERVAL 10 HOUR), 'TRANSFER', 'Diabetic ketoacidosis after stabilization in resus', 'Emergency Medicine', 'HIGH', 7, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-cedar-001', 'hosp-ng-cedar', 'ward-cedar-med', 'G04', 'team-cedar-med', 'user-cedar-cons-ibiso', 'Tonye', 'Hart', '1968-05-02', 'M', 'CEDAR-0001', DATE_SUB(NOW(), INTERVAL 3 DAY), 'EMERGENCY', 'Hypertensive emergency with pulmonary oedema', 'General Medicine', 'HIGH', 7, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-cedar-002', 'hosp-ng-cedar', 'ward-cedar-hdu', 'H02', 'team-cedar-med', 'user-cedar-cons-ibiso', 'Boma', 'Georgewill', '1979-12-12', 'F', 'CEDAR-0002', DATE_SUB(NOW(), INTERVAL 18 HOUR), 'TRANSFER', 'Post-stroke monitoring with fluctuating consciousness', 'General Medicine', 'CRITICAL', 10, FALSE, NULL, 'DETERIORATING', NOW(), NOW()),
('pt-knh-001', 'hosp-ke-knh', 'ward-knh-med', 'K17', 'team-knh-med', 'user-knh-cons-otieno', 'Achieng', 'Njoroge', '1976-06-08', 'F', 'KNH-0001', DATE_SUB(NOW(), INTERVAL 3 DAY), 'TRANSFER', 'Uncontrolled hypertension with acute kidney injury', 'Internal Medicine', 'MEDIUM', 5, FALSE, NULL, 'ADMITTED', NOW(), NOW()),
('pt-knh-002', 'hosp-ke-knh', 'ward-knh-med', 'K21', 'team-knh-med', 'user-knh-cons-otieno', 'Mwende', 'Mutiso', '1986-03-17', 'F', 'KNH-0002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'ELECTIVE', 'Diagnostic work-up for recurrent syncope', 'Internal Medicine', 'LOW', 1, FALSE, NULL, 'STABLE', NOW(), NOW()),
('pt-uk-001', 'hosp-uk-stmary', 'ward-uk-acute', 'A11', 'team-uk-acute', 'user-uk-cons-smith', 'George', 'Williams', '1949-10-30', 'M', 'STM-0001', DATE_SUB(NOW(), INTERVAL 2 DAY), 'EMERGENCY', 'COPD exacerbation, improving', 'Acute Medicine', 'LOW', 3, TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'DISCHARGE_READY', NOW(), NOW()),
('pt-uk-002', 'hosp-uk-stmary', 'ward-uk-acute', 'A18', 'team-uk-acute', 'user-uk-cons-smith', 'Nadia', 'Hassan', '1971-07-07', 'F', 'STM-0002', DATE_SUB(NOW(), INTERVAL 14 HOUR), 'EMERGENCY', 'Community acquired pneumonia on IV antibiotics', 'Acute Medicine', 'MEDIUM', 5, FALSE, NULL, 'ADMITTED', NOW(), NOW());

INSERT INTO patient_vitals (id, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at, created_at, updated_at) VALUES
('vitals-luth-001', 'pt-luth-001', 'user-luth-nurse-kemi', 102, 24, 94.00, 128, 38.2, 'ALERT', 5, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), NOW()),
('vitals-luth-002', 'pt-luth-002', 'user-luth-nurse-grace', 124, 28, 88.00, 96, 37.9, 'VOICE', 10, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW(), NOW()),
('vitals-luth-004', 'pt-luth-004', 'user-luth-nurse-kemi', 88, 18, 97.00, 134, 36.8, 'ALERT', 2, DATE_SUB(NOW(), INTERVAL 70 MINUTE), NOW(), NOW()),
('vitals-luth-005', 'pt-luth-005', 'user-luth-nurse-kemi', 82, 16, 98.00, 122, 36.7, 'ALERT', 1, DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW(), NOW()),
('vitals-abj-001', 'pt-abj-001', 'user-abj-nurse-hauwa', 132, 30, 89.00, 88, 39.3, 'VOICE', 11, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), NOW()),
('vitals-abj-002', 'pt-abj-002', 'user-abj-nurse-hauwa', 118, 34, 92.00, 100, 38.0, 'ALERT', 6, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
('vitals-abj-003', 'pt-abj-003', 'user-abj-nurse-hauwa', 116, 26, 94.00, 104, 37.6, 'ALERT', 7, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW(), NOW()),
('vitals-cedar-001', 'pt-cedar-001', 'user-cedar-nurse-ama', 110, 25, 93.00, 168, 37.8, 'PAIN', 8, DATE_SUB(NOW(), INTERVAL 35 MINUTE), NOW(), NOW()),
('vitals-cedar-002', 'pt-cedar-002', 'user-cedar-nurse-ama', 126, 31, 90.00, 92, 38.5, 'UNRESPONSIVE', 12, DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW(), NOW()),
('vitals-knh-001', 'pt-knh-001', 'user-knh-nurse-achebe', 96, 22, 95.00, 172, 37.4, 'ALERT', 5, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW(), NOW()),
('vitals-knh-002', 'pt-knh-002', 'user-knh-nurse-achebe', 74, 15, 99.00, 118, 36.6, 'ALERT', 0, DATE_SUB(NOW(), INTERVAL 80 MINUTE), NOW(), NOW()),
('vitals-uk-001', 'pt-uk-001', 'user-uk-nurse-evans', 84, 20, 96.00, 132, 36.9, 'ALERT', 3, DATE_SUB(NOW(), INTERVAL 50 MINUTE), NOW(), NOW()),
('vitals-uk-002', 'pt-uk-002', 'user-uk-nurse-evans', 101, 23, 94.00, 126, 37.9, 'VOICE', 5, DATE_SUB(NOW(), INTERVAL 32 MINUTE), NOW(), NOW());

INSERT INTO next_of_kin (id, patient_id, name, relationship, phone, email, preferred_contact_method, is_emergency_contact, notification_consent, created_at, updated_at) VALUES
('nok-luth-001', 'pt-luth-001', 'Adaeze Okoye', 'Daughter', '+2348021111001', 'adaeze.okoye@example.ng', 'BOTH', TRUE, TRUE, NOW(), NOW()),
('nok-luth-002', 'pt-luth-002', 'Temitope Ajayi', 'Son', '+2348021111003', 'temitope.ajayi@example.ng', 'EMAIL', TRUE, TRUE, NOW(), NOW()),
('nok-luth-003', 'pt-luth-003', 'Olamide Umeh', 'Sibling', '+2348021111006', 'olamide.umeh@example.ng', 'EMAIL', TRUE, TRUE, NOW(), NOW()),
('nok-luth-004', 'pt-luth-004', 'Kunle Afolayan', 'Spouse', '+2348021111004', NULL, 'SMS', FALSE, FALSE, NOW(), NOW()),
('nok-luth-005', 'pt-luth-005', 'Mariam Lawal', 'Sister', '+2348021111005', 'mariam.lawal@example.ng', 'EMAIL', TRUE, TRUE, NOW(), NOW()),
('nok-abj-001', 'pt-abj-001', 'Hadiza Abdullahi', 'Wife', '+2348032222001', 'hadiza.abdullahi@example.ng', 'BOTH', TRUE, TRUE, NOW(), NOW()),
('nok-abj-002', 'pt-abj-002', 'Aisha Usman', 'Mother', '+2348032222002', 'aisha.usman@example.ng', 'SMS', TRUE, TRUE, NOW(), NOW()),
('nok-abj-003', 'pt-abj-003', 'Chidera Eze', 'Brother', NULL, 'chidera.eze@example.ng', 'EMAIL', FALSE, TRUE, NOW(), NOW()),
('nok-cedar-001', 'pt-cedar-001', 'Peace Hart', 'Daughter', '+2348043333001', 'peace.hart@example.ng', 'BOTH', TRUE, TRUE, NOW(), NOW()),
('nok-cedar-002', 'pt-cedar-002', 'Tamara Georgewill', 'Partner', '+2348043333002', 'tamara.georgewill@example.ng', 'SMS', TRUE, TRUE, NOW(), NOW()),
('nok-knh-001', 'pt-knh-001', 'Daniel Njoroge', 'Brother', '+254711000100', 'daniel.njoroge@example.ke', 'EMAIL', TRUE, FALSE, NOW(), NOW()),
('nok-knh-002', 'pt-knh-002', 'Faith Mutiso', 'Mother', '+254711000101', NULL, 'SMS', FALSE, TRUE, NOW(), NOW()),
('nok-uk-001', 'pt-uk-001', 'Helen Williams', 'Spouse', '+447700900100', 'helen.williams@example.uk', 'EMAIL', TRUE, TRUE, NOW(), NOW()),
('nok-uk-002', 'pt-uk-002', 'Yusuf Hassan', 'Brother', '+447700900101', 'yusuf.hassan@example.uk', 'BOTH', TRUE, TRUE, NOW(), NOW());

INSERT INTO escalation (id, hospital_id, patient_id, triggered_by_id, trigger_type, severity, assigned_to_id, status, notes, resolved_at, created_at, updated_at) VALUES
('esc-luth-red-001', 'hosp-ng-luth', 'pt-luth-002', 'user-luth-nurse-grace', 'HIGH_NEWS_SCORE', 'RED', 'user-luth-cons-bello', 'OPEN', 'NEWS2 score 10 with hypotension and low saturation.', NULL, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW()),
('esc-abj-red-001', 'hosp-ng-abj', 'pt-abj-001', 'user-abj-nurse-hauwa', 'DETERIORATION', 'RED', 'user-abj-cons-yusuf', 'ACKNOWLEDGED', 'Sepsis patient remains hypotensive after initial fluid bolus.', NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW()),
('esc-cedar-red-001', 'hosp-ng-cedar', 'pt-cedar-002', 'user-cedar-nurse-ama', 'NURSE_CONCERN', 'RED', 'user-cedar-cons-ibiso', 'OPEN', 'Reduced responsiveness in HDU after transfer from theatre recovery.', NULL, DATE_SUB(NOW(), INTERVAL 18 MINUTE), NOW()),
('esc-luth-amber-002', 'hosp-ng-luth', 'pt-luth-005', 'user-luth-nurse-kemi', 'TASK_OVERDUE', 'AMBER', 'user-luth-reg-nwosu', 'ACKNOWLEDGED', 'Discharge counselling task still open close to expected departure window.', NULL, DATE_SUB(NOW(), INTERVAL 70 MINUTE), NOW()),
('esc-knh-amber-001', 'hosp-ke-knh', 'pt-knh-001', 'user-knh-nurse-achebe', 'HIGH_NEWS_SCORE', 'AMBER', 'user-knh-cons-otieno', 'RESOLVED', 'AKI patient reviewed and antihypertensive plan adjusted.', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
('esc-uk-amber-001', 'hosp-uk-stmary', 'pt-uk-002', 'user-uk-nurse-evans', 'DETERIORATION', 'AMBER', 'user-uk-reg-khan', 'RESOLVED', 'Respiratory symptoms stabilized after nebulizer treatment and reassessment.', DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW());

INSERT INTO handover (id, ward_id, outgoing_shift_id, incoming_shift_id, conducted_by_id, status, general_notes, completed_at, created_at, updated_at) VALUES
('handover-luth-male-001', 'ward-luth-male-med', 'shift-luth-male-day', 'shift-luth-male-night', 'user-luth-reg-nwosu', 'IN_PROGRESS', 'Monitor Emeka Okoye closely overnight; blood cultures pending.', NULL, NOW(), NOW()),
('handover-abj-paeds-pending', 'ward-abj-paeds', 'shift-abj-paeds-day', 'shift-abj-paeds-night', 'user-abj-cons-amina', 'PENDING', 'Prepare paediatric oxygen escalation summary before evening closeout.', NULL, NOW(), NOW()),
('handover-cedar-med-progress', 'ward-cedar-med', 'shift-cedar-med-day', 'shift-cedar-med-night', 'user-cedar-reg-jaja', 'IN_PROGRESS', 'Weekend handover in progress with HDU escalation dependencies flagged.', NULL, NOW(), NOW()),
('handover-luth-cardio-complete', 'ward-luth-cardiac', 'shift-luth-cardiac-prev-night', 'shift-luth-cardio-day', 'user-luth-cons-bello', 'COMPLETED', 'Cardiac unit handed over with echo follow-up and fluid balance priorities.', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('handover-knh-complete', 'ward-knh-med', 'shift-knh-prev-night', 'shift-knh-day', 'user-knh-reg-kimani', 'COMPLETED', 'Renal and blood pressure concerns handed over to the day medicine team.', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
('handover-uk-acute-complete', 'ward-uk-acute', 'shift-uk-prev-night', 'shift-uk-day', 'user-uk-reg-khan', 'COMPLETED', 'Night shift handed over two respiratory cases and one planned discharge.', DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW());

INSERT INTO patient_handover_note (id, handover_id, patient_id, status_summary, outstanding_task_ids, urgency_flag, added_by_id, created_at, updated_at) VALUES
('hnote-luth-001', 'handover-luth-male-001', 'pt-luth-001', 'Still febrile but oxygen requirement reduced. Review cultures once available.', 'task-luth-001,task-luth-002', TRUE, 'user-luth-jd-ibrahim', NOW(), NOW()),
('hnote-luth-002', 'handover-luth-cardio-complete', 'pt-luth-002', 'Echo remains urgent; senior review completed and plan documented.', 'task-luth-003', TRUE, 'user-luth-reg-nwosu', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('hnote-abj-001', 'handover-abj-paeds-pending', 'pt-abj-002', 'Continue oxygen saturation trending and assess feeding tolerance overnight.', 'task-abj-002', FALSE, 'user-abj-cons-amina', NOW(), NOW()),
('hnote-cedar-001', 'handover-cedar-med-progress', 'pt-cedar-001', 'Monitor response to diuresis and recheck respiratory effort overnight.', 'task-cedar-002', TRUE, 'user-cedar-jd-daniel', NOW(), NOW()),
('hnote-knh-001', 'handover-knh-complete', 'pt-knh-001', 'Blood pressure improving. Day registrar to review U&E results once back.', 'task-knh-001', FALSE, 'user-knh-reg-kimani', DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('hnote-uk-001', 'handover-uk-acute-complete', 'pt-uk-001', 'Discharge paperwork almost ready; confirm pharmacy before patient leaves.', 'task-uk-001', FALSE, 'user-uk-jd-patel', DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW());

INSERT INTO round (id, hospital_id, ward_id, medical_team_id, shift_id, round_type, lead_doctor_id, status, scheduled_time, started_at, completed_at, team_members, created_at, updated_at) VALUES
('round-luth-med-active', 'hosp-ng-luth', 'ward-luth-male-med', 'team-luth-med-a', 'shift-luth-male-day', 'MORNING', 'user-luth-cons-ade', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 40 MINUTE), NULL, 'user-luth-cons-ade,user-luth-reg-nwosu,user-luth-jd-ibrahim', NOW(), NOW()),
('round-luth-cardio-complete', 'hosp-ng-luth', 'ward-luth-cardiac', 'team-luth-cardio', 'shift-luth-cardio-day', 'POST_TAKE', 'user-luth-cons-bello', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'user-luth-cons-bello,user-luth-nurse-grace', DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
('round-luth-female-cancelled', 'hosp-ng-luth', 'ward-luth-female-med', 'team-luth-med-a', 'shift-luth-female-day', 'EVENING', 'user-luth-cons-ade', 'CANCELLED', DATE_ADD(NOW(), INTERVAL 2 HOUR), NULL, NULL, 'user-luth-cons-ade,user-luth-reg-nwosu', NOW(), NOW()),
('round-abj-emergency-scheduled', 'hosp-ng-abj', 'ward-abj-emergency', 'team-abj-emergency', 'shift-abj-emergency-day', 'BOARD', 'user-abj-cons-yusuf', 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 1 HOUR), NULL, NULL, 'user-abj-cons-yusuf,user-abj-reg-okon,user-abj-jd-musa', NOW(), NOW()),
('round-cedar-weekend-active', 'hosp-ng-cedar', 'ward-cedar-med', 'team-cedar-med', 'shift-cedar-med-day', 'WEEKEND', 'user-cedar-cons-ibiso', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, 'user-cedar-cons-ibiso,user-cedar-reg-jaja,user-cedar-jd-daniel', NOW(), NOW()),
('round-knh-board-complete', 'hosp-ke-knh', 'ward-knh-med', 'team-knh-med', 'shift-knh-day', 'BOARD', 'user-knh-cons-otieno', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 'user-knh-cons-otieno,user-knh-reg-kimani,user-knh-jd-wairimu', DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
('round-uk-evening-active', 'hosp-uk-stmary', 'ward-uk-acute', 'team-uk-acute', 'shift-uk-day', 'EVENING', 'user-uk-cons-smith', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), NULL, 'user-uk-cons-smith,user-uk-reg-khan,user-uk-jd-patel', NOW(), NOW());

INSERT INTO patient_round_review (id, round_id, patient_id, reviewed_by_id, review_order, news_score_at_review, clinical_status, was_examined, management_plan, discharge_assessment, notified_next_of_kin, reviewed_at, created_at, updated_at) VALUES
('review-luth-001', 'round-luth-med-active', 'pt-luth-001', 'user-luth-cons-ade', 1, 5, 'STABLE', TRUE, 'Continue ceftriaxone, repeat FBC tomorrow, encourage oral fluids.', 'NONE', FALSE, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
('review-luth-002', 'round-luth-cardio-complete', 'pt-luth-002', 'user-luth-cons-bello', 1, 10, 'DETERIORATING', TRUE, 'Increase diuretics, urgent echo review, keep on cardiac monitor.', 'BLOCKED_MEDICAL', TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('review-luth-003', 'round-luth-med-active', 'pt-luth-005', 'user-luth-cons-ade', 2, 2, 'IMPROVING', TRUE, 'Symptoms resolved. Complete discharge counselling and arrange outpatient review.', 'CONFIRMED', TRUE, DATE_SUB(NOW(), INTERVAL 24 MINUTE), NOW(), NOW()),
('review-cedar-001', 'round-cedar-weekend-active', 'pt-cedar-001', 'user-cedar-cons-ibiso', 1, 8, 'DETERIORATING', TRUE, 'Continue IV diuresis, repeat blood gas, consultant to reassess after noon.', 'POSSIBLE', TRUE, DATE_SUB(NOW(), INTERVAL 18 MINUTE), NOW(), NOW()),
('review-cedar-002', 'round-cedar-weekend-active', 'pt-cedar-002', 'user-cedar-cons-ibiso', 2, 12, 'CRITICAL', TRUE, 'Immediate neuro review, continue HDU monitoring, prepare escalation if GCS worsens.', 'BLOCKED_SOCIAL', FALSE, DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW(), NOW()),
('review-knh-001', 'round-knh-board-complete', 'pt-knh-001', 'user-knh-cons-otieno', 1, 5, 'IMPROVING', TRUE, 'Renal function responding. Continue monitoring urine output and repeat U&E tomorrow.', 'POSSIBLE', FALSE, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('review-uk-001', 'round-uk-evening-active', 'pt-uk-002', 'user-uk-cons-smith', 1, 5, 'STABLE', TRUE, 'Continue oxygen wean and review chest physiotherapy response later tonight.', 'NONE', FALSE, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW(), NOW());

INSERT INTO clinical_note (id, patient_id, patient_round_review_id, vitals_id, author_id, note_type, content, is_amended, amended_by_id, amended_at, created_at, updated_at) VALUES
('note-luth-001', 'pt-luth-001', 'review-luth-001', 'vitals-luth-001', 'user-luth-jd-ibrahim', 'ROUND_NOTE', 'Reviewed on morning round. Fever trending down, chest findings improving, diabetes control acceptable.', FALSE, NULL, NULL, NOW(), NOW()),
('note-luth-002', 'pt-luth-002', 'review-luth-002', 'vitals-luth-002', 'user-luth-reg-nwosu', 'ESCALATION_NOTE', 'Patient breathless at rest with raised JVP. Consultant informed and reviewed at bedside.', FALSE, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('note-luth-003', 'pt-luth-005', 'review-luth-003', NULL, 'user-luth-cons-ade', 'DISCHARGE_NOTE', 'Clinically suitable for discharge once pharmacy and counselling tasks are complete.', TRUE, 'user-luth-cons-ade', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 35 MINUTE), NOW()),
('note-abj-001', 'pt-abj-001', NULL, 'vitals-abj-001', 'user-abj-jd-musa', 'ADMISSION_NOTE', 'Brought in from Garki with fever, confusion, and low blood pressure. Sepsis bundle started.', FALSE, NULL, NULL, NOW(), NOW()),
('note-abj-002', 'pt-abj-003', NULL, 'vitals-abj-003', 'user-abj-reg-okon', 'PROGRESS_NOTE', 'Glucose improving after insulin infusion. Continue hourly monitoring and check potassium.', FALSE, NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW()),
('note-cedar-001', 'pt-cedar-002', 'review-cedar-002', 'vitals-cedar-002', 'user-cedar-reg-jaja', 'ESCALATION_NOTE', 'Neurologic status remains concerning. Consultant notified and airway risk documented.', FALSE, NULL, NULL, NOW(), NOW()),
('note-knh-001', 'pt-knh-001', 'review-knh-001', 'vitals-knh-001', 'user-knh-reg-kimani', 'PROGRESS_NOTE', 'Blood pressure improving with adjusted regimen. Continue AKI surveillance.', FALSE, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('note-uk-001', 'pt-uk-002', 'review-uk-001', 'vitals-uk-002', 'user-uk-jd-patel', 'ROUND_NOTE', 'Stable on current oxygen requirement. Review chest findings again overnight.', FALSE, NULL, NULL, NOW(), NOW());

INSERT INTO care_task (id, hospital_id, patient_id, ward_id, round_id, created_by_id, assigned_to_id, assigned_to_role, task_type, source, title, description, priority, window_start, window_end, status, completed_by_id, completed_at, escalated_at, workload_conflict, workload_conflict_reason, created_at, updated_at) VALUES
('task-luth-001', 'hosp-ng-luth', 'pt-luth-001', 'ward-luth-male-med', 'round-luth-med-active', 'user-luth-cons-ade', 'user-luth-nurse-kemi', 'NURSE', 'Medication', 'NURSING_CARE_PLAN', 'Administer IV ceftriaxone', 'Dose due after morning round.', 'URGENT', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'PENDING', NULL, NULL, NULL, FALSE, NULL, NOW(), NOW()),
('task-luth-002', 'hosp-ng-luth', 'pt-luth-001', 'ward-luth-male-med', 'round-luth-med-active', 'user-luth-reg-nwosu', 'user-luth-jd-ibrahim', 'JUNIOR_DOCTOR', 'Bloods Review', 'POST_ROUND_JOB', 'Review FBC, U&E, and culture results', 'Escalate to registrar if creatinine rises.', 'ROUTINE', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'PENDING', NULL, NULL, NULL, TRUE, 'No same-specialty junior doctor was free in the requested window; assigned with supervisor notification.', NOW(), NOW()),
('task-luth-003', 'hosp-ng-luth', 'pt-luth-002', 'ward-luth-cardiac', 'round-luth-cardio-complete', 'user-luth-cons-bello', 'user-luth-reg-nwosu', 'REGISTRAR', 'Echo Review', 'POST_ROUND_JOB', 'Chase urgent echocardiogram', 'Confirm LV function and valve status.', 'EMERGENCY', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'IN_PROGRESS', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('task-luth-004', 'hosp-ng-luth', 'pt-luth-005', 'ward-luth-female-med', 'round-luth-med-active', 'user-luth-cons-ade', 'user-luth-nurse-kemi', 'NURSE', 'Discharge Counselling', 'POST_ROUND_JOB', 'Complete medication counselling before discharge', 'This task intentionally remains open to test discharge blocking logic.', 'URGENT', DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'PENDING', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW()),
('task-luth-005', 'hosp-ng-luth', 'pt-luth-005', 'ward-luth-female-med', 'round-luth-med-active', 'user-luth-reg-nwosu', 'user-luth-jd-ibrahim', 'JUNIOR_DOCTOR', 'Discharge Summary', 'POST_ROUND_JOB', 'Finalize discharge summary', 'Summary completed and signed.', 'ROUTINE', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'COMPLETED', 'user-luth-jd-ibrahim', DATE_SUB(NOW(), INTERVAL 95 MINUTE), NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
('task-abj-001', 'hosp-ng-abj', 'pt-abj-001', 'ward-abj-emergency', NULL, 'user-abj-cons-yusuf', 'user-abj-nurse-hauwa', 'NURSE', 'Sepsis Bundle', 'NURSING_CARE_PLAN', 'Repeat lactate and fluid balance', 'Repeat lactate after initial fluids and chart urine output hourly.', 'EMERGENCY', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'PENDING', NULL, NULL, NULL, FALSE, NULL, NOW(), NOW()),
('task-abj-002', 'hosp-ng-abj', 'pt-abj-002', 'ward-abj-paeds', NULL, 'user-abj-cons-amina', 'user-abj-nurse-hauwa', 'NURSE', 'Oxygen Monitoring', 'NURSING_CARE_PLAN', 'Record paediatric respiratory observations', 'Respiratory rate, saturation, and work of breathing every hour.', 'URGENT', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'COMPLETED', 'user-abj-nurse-hauwa', DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('task-cedar-001', 'hosp-ng-cedar', 'pt-cedar-002', 'ward-cedar-hdu', 'round-cedar-weekend-active', 'user-cedar-cons-ibiso', 'user-cedar-nurse-ama', 'NURSE', 'Neurologic Checks', 'NURSING_CARE_PLAN', 'Complete 15-minute neurologic observations', 'Escalate immediately for any worsening response.', 'EMERGENCY', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'OVERDUE', NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE), TRUE, 'HDU nurse workload is saturated during the neurologic observation window.', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('task-cedar-002', 'hosp-ng-cedar', 'pt-cedar-001', 'ward-cedar-med', 'round-cedar-weekend-active', 'user-cedar-reg-jaja', 'user-cedar-jd-daniel', 'JUNIOR_DOCTOR', 'Repeat Blood Gas', 'POST_ROUND_JOB', 'Arrange repeat venous blood gas', 'Cancelled after consultant changed monitoring plan.', 'ROUTINE', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'CANCELLED', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('task-knh-001', 'hosp-ke-knh', 'pt-knh-001', 'ward-knh-med', 'round-knh-board-complete', 'user-knh-cons-otieno', 'user-knh-reg-kimani', 'REGISTRAR', 'Medication Review', 'POST_ROUND_JOB', 'Review antihypertensive regimen', 'Documented after board round.', 'ROUTINE', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 'COMPLETED', 'user-knh-reg-kimani', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
('task-uk-001', 'hosp-uk-stmary', 'pt-uk-001', 'ward-uk-acute', NULL, 'user-uk-cons-smith', 'user-uk-jd-patel', 'JUNIOR_DOCTOR', 'Pharmacy Liaison', 'POST_ROUND_JOB', 'Confirm take-home medications', 'Needed before discharge can be finalized.', 'URGENT', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 45 MINUTE), 'PENDING', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('task-uk-002', 'hosp-uk-stmary', 'pt-uk-002', 'ward-uk-acute', 'round-uk-evening-active', 'user-uk-cons-smith', 'user-uk-nurse-evans', 'NURSE', 'Oxygen Wean', 'NURSING_CARE_PLAN', 'Trial reduction in oxygen support', 'Review saturation after 30 minutes.', 'URGENT', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 'IN_PROGRESS', NULL, NULL, NULL, FALSE, NULL, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW());

INSERT INTO outbox_event (id, hospital_id, event_type, payload, published, published_at, correlation_id, created_at, updated_at) VALUES
('outbox-001', 'hosp-ng-luth', 'PATIENT_DETERIORATION', '{"hospitalId":"hosp-ng-luth","patientId":"pt-luth-002","severity":"RED","newsScore":10}', TRUE, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'corr-luth-red-001', DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW()),
('outbox-002', 'hosp-ng-abj', 'TASK_OVERDUE', '{"hospitalId":"hosp-ng-abj","taskId":"task-abj-001","patientId":"pt-abj-001"}', FALSE, NULL, 'corr-abj-task-001', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
('outbox-003', 'hosp-ng-luth', 'ROUND_COMPLETED', '{"hospitalId":"hosp-ng-luth","roundId":"round-luth-cardio-complete","wardId":"ward-luth-cardiac"}', TRUE, DATE_SUB(NOW(), INTERVAL 110 MINUTE), 'corr-round-luth-cardio', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('outbox-004', 'hosp-ng-luth', 'HANDOVER_COMPLETED', '{"hospitalId":"hosp-ng-luth","handoverId":"handover-luth-cardio-complete","wardId":"ward-luth-cardiac"}', TRUE, DATE_SUB(NOW(), INTERVAL 105 MINUTE), 'corr-handover-luth-cardio', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('outbox-005', 'hosp-ng-luth', 'PATIENT_DISCHARGE_READY', '{"hospitalId":"hosp-ng-luth","patientId":"pt-luth-005","wardId":"ward-luth-female-med"}', FALSE, NULL, 'corr-discharge-ready-luth-005', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()),
('outbox-006', 'hosp-ng-luth', 'PATIENT_DISCHARGED', '{"hospitalId":"hosp-ng-luth","patientId":"pt-luth-003","wardId":"ward-luth-female-med"}', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), 'corr-discharged-luth-003', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('outbox-007', 'hosp-ng-cedar', 'TEAM_INVITE_SENT', '{"hospitalId":"hosp-ng-cedar","inviteId":"invite-cedar-declined","teamId":"team-cedar-med"}', TRUE, DATE_SUB(NOW(), INTERVAL 8 HOUR), 'corr-invite-cedar-001', DATE_SUB(NOW(), INTERVAL 8 HOUR), NOW()),
('outbox-008', 'hosp-ke-knh', 'INVITE_EXPIRED', '{"hospitalId":"hosp-ke-knh","inviteId":"invite-knh-expired","teamId":"team-knh-med"}', FALSE, NULL, 'corr-invite-knh-expired', DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW()),
('outbox-009', 'hosp-ng-cedar', 'careround.care_task.workload_conflict', '{"hospitalId":"hosp-ng-cedar","taskId":"task-cedar-001","wardId":"ward-cedar-hdu","patientId":"pt-cedar-002"}', FALSE, NULL, 'corr-cedar-conflict-001', DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW()),
('outbox-010', 'hosp-uk-stmary', 'PATIENT_DETERIORATION', '{"hospitalId":"hosp-uk-stmary","patientId":"pt-uk-002","severity":"AMBER","newsScore":5}', TRUE, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 'corr-uk-amber-001', DATE_SUB(NOW(), INTERVAL 50 MINUTE), NOW());

INSERT INTO notification_read_receipt (id, hospital_id, user_id, notification_id, read_at, created_at, updated_at) VALUES
('receipt-luth-001', 'hosp-ng-luth', 'user-luth-cons-bello', 'notif-luth-001', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW()),
('receipt-cedar-001', 'hosp-ng-cedar', 'user-cedar-ws-ifunanya', 'notif-cedar-001', DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW());

SET FOREIGN_KEY_CHECKS = 1;

USE careround_notification;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE failed_notifications;

INSERT INTO notifications (id, event_type, hospital_id, recipient_id, recipient_type, channel, subject, body, correlation_id, payload, status, failure_reason, sent_at, retry_count, created_at, updated_at) VALUES
('notif-luth-001', 'careround.patient.deterioration', 'hosp-ng-luth', 'user-luth-cons-bello', 'USER', 'SMS', 'RED escalation for Bamidele Ajayi', 'NEWS2 score 10. Patient requires urgent consultant review.', 'corr-luth-red-001', '{"patientId":"pt-luth-002","severity":"RED"}', 'SENT', NULL, DATE_SUB(NOW(), INTERVAL 12 MINUTE), 0, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW()),
('notif-abj-001', 'careround.task.overdue', 'hosp-ng-abj', 'user-abj-ws-ngozi', 'USER', 'SMS', 'Overdue emergency task', 'Repeat lactate and fluid balance task is overdue for Suleiman Abdullahi.', 'corr-abj-task-001', '{"taskId":"task-abj-001"}', 'FAILED', 'SMS provider timeout', NULL, 2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
('notif-cedar-001', 'careround.care_task.workload_conflict', 'hosp-ng-cedar', 'user-cedar-ws-ifunanya', 'USER', 'EMAIL', 'Workload conflict in High Dependency Bay', 'Neurologic observation workload needs supervisor review.', 'corr-cedar-conflict-001', '{"taskId":"task-cedar-001","wardId":"ward-cedar-hdu"}', 'SENT', NULL, DATE_SUB(NOW(), INTERVAL 8 MINUTE), 0, DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW()),
('notif-luth-002', 'careround.patient.discharge-ready', 'hosp-ng-luth', 'user-luth-ws-femi', 'USER', 'EMAIL', 'Patient ready for discharge workflow', 'Hauwa Lawal is discharge ready but open tasks remain.', 'corr-discharge-ready-luth-005', '{"patientId":"pt-luth-005"}', 'PENDING', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW()),
('notif-luth-003', 'careround.patient.discharged', 'hosp-ng-luth', 'nok-luth-003', 'NOK', 'EMAIL', 'Discharge Notification', 'Your family member has been discharged from our care.', 'corr-discharged-luth-003', '{"patientId":"pt-luth-003"}', 'SENT', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('notif-knh-001', 'careround.invite.expired', 'hosp-ke-knh', 'user-knh-cons-otieno', 'USER', 'EMAIL', 'Team invite expired', 'A pending team invitation expired before it was accepted.', 'corr-invite-knh-expired', '{"inviteId":"invite-knh-expired"}', 'FAILED', 'SMTP mailbox temporarily unavailable', NULL, 1, DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW()),
('notif-uk-001', 'careround.patient.deterioration', 'hosp-uk-stmary', 'user-uk-reg-khan', 'USER', 'EMAIL', 'AMBER deterioration review requested', 'Nadia Hassan needs prompt reassessment after a rise in respiratory observations.', 'corr-uk-amber-001', '{"patientId":"pt-uk-002","severity":"AMBER"}', 'SENT', NULL, DATE_SUB(NOW(), INTERVAL 42 MINUTE), 0, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW());

INSERT INTO failed_notifications (id, event_type, topic, hospital_id, correlation_id, payload, error_message, failed_at, retry_count, resolved, created_at, updated_at) VALUES
('failed-notif-001', 'careround.task.overdue', 'careround.task.overdue', 'hosp-ng-abj', 'corr-abj-task-001', '{"taskId":"task-abj-001","recipientId":"user-abj-ws-ngozi"}', 'SMS provider timeout after retries', DATE_SUB(NOW(), INTERVAL 3 MINUTE), 3, FALSE, DATE_SUB(NOW(), INTERVAL 3 MINUTE), NOW()),
('failed-notif-002', 'careround.invite.expired', 'careround.invite.expired', 'hosp-ke-knh', 'corr-invite-knh-expired', '{"inviteId":"invite-knh-expired","recipientId":"user-knh-cons-otieno"}', 'Email provider rejected message during retry 1', DATE_SUB(NOW(), INTERVAL 10 MINUTE), 1, TRUE, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW());

SET FOREIGN_KEY_CHECKS = 1;

USE careround_audit;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE audit_log;

INSERT INTO audit_log (id, event_type, hospital_id, correlation_id, payload, kafka_topic, kafka_partition, kafka_offset, received_at, processed_at, created_at, updated_at) VALUES
('audit-001', 'PATIENT_DETERIORATION', 'hosp-ng-luth', 'corr-luth-red-001', '{"patientId":"pt-luth-002","severity":"RED","assignedToId":"user-luth-cons-bello"}', 'careround.patient.deterioration', 0, 1201, DATE_SUB(NOW(), INTERVAL 14 MINUTE), DATE_SUB(NOW(), INTERVAL 14 MINUTE), DATE_SUB(NOW(), INTERVAL 14 MINUTE), NOW()),
('audit-002', 'TASK_OVERDUE', 'hosp-ng-abj', 'corr-abj-task-001', '{"taskId":"task-abj-001","patientId":"pt-abj-001","wardId":"ward-abj-emergency"}', 'careround.task.overdue', 0, 1202, DATE_SUB(NOW(), INTERVAL 4 MINUTE), DATE_SUB(NOW(), INTERVAL 4 MINUTE), DATE_SUB(NOW(), INTERVAL 4 MINUTE), NOW()),
('audit-003', 'HOSPITAL_ONBOARDING_REVIEWED', 'PLATFORM', 'corr-onboard-001', '{"requestId":"onboard-001","status":"APPROVED","reviewedByUserId":"plat-admin-001"}', 'careround.hospital.onboarding_reviewed', 0, 1190, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('audit-004', 'ROUND_COMPLETED', 'hosp-ng-luth', 'corr-round-luth-cardio', '{"roundId":"round-luth-cardio-complete","wardId":"ward-luth-cardiac"}', 'careround.round.completed', 0, 1203, DATE_SUB(NOW(), INTERVAL 108 MINUTE), DATE_SUB(NOW(), INTERVAL 108 MINUTE), DATE_SUB(NOW(), INTERVAL 108 MINUTE), NOW()),
('audit-005', 'HANDOVER_COMPLETED', 'hosp-ng-luth', 'corr-handover-luth-cardio', '{"handoverId":"handover-luth-cardio-complete","wardId":"ward-luth-cardiac"}', 'careround.handover.completed', 0, 1204, DATE_SUB(NOW(), INTERVAL 104 MINUTE), DATE_SUB(NOW(), INTERVAL 104 MINUTE), DATE_SUB(NOW(), INTERVAL 104 MINUTE), NOW()),
('audit-006', 'CARE_TASK_WORKLOAD_CONFLICT', 'hosp-ng-cedar', 'corr-cedar-conflict-001', '{"taskId":"task-cedar-001","wardId":"ward-cedar-hdu"}', 'careround.care_task.workload_conflict', 0, 1205, DATE_SUB(NOW(), INTERVAL 9 MINUTE), DATE_SUB(NOW(), INTERVAL 9 MINUTE), DATE_SUB(NOW(), INTERVAL 9 MINUTE), NOW()),
('audit-007', 'INVITE_EXPIRED', 'hosp-ke-knh', 'corr-invite-knh-expired', '{"inviteId":"invite-knh-expired","teamId":"team-knh-med"}', 'careround.invite.expired', 0, 1206, DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 8 MINUTE), NOW()),
('audit-008', 'PATIENT_DETERIORATION', 'hosp-uk-stmary', 'corr-uk-amber-001', '{"patientId":"pt-uk-002","severity":"AMBER","assignedToId":"user-uk-reg-khan"}', 'careround.patient.deterioration', 0, 1207, DATE_SUB(NOW(), INTERVAL 43 MINUTE), DATE_SUB(NOW(), INTERVAL 43 MINUTE), DATE_SUB(NOW(), INTERVAL 43 MINUTE), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Seed complete
-- ============================================================================
-- Active login examples, password: Password123
--   platform-admin@careround.local
--   chioma.eze@luth.example.ng
--   tunde.adewale@luth.example.ng
--   kemi.balogun@luth.example.ng
--   zainab.musa@nha.example.ng
--   bashir.yusuf@nha.example.ng
--   ibiso.george@cedarspecialist.example.ng
--   ama.erekosima@cedarspecialist.example.ng
--   wanjiku.mwangi@knh.example.ke
--   mercy.kimani@knh.example.ke
--   amelia.turner@stmarys.example.uk
--   sara.khan@stmarys.example.uk
-- ============================================================================
