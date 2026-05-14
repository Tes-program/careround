-- ============================================================================
-- V20 — Demo Seed Data
-- ============================================================================
-- Runs exactly once via Flyway on first deployment against an empty schema.
-- INSERT IGNORE makes every statement safe to re-run if the migration is
-- retried after a partial failure.
--
-- Excluded from this migration (time-sensitive operational state):
--   shift, on_call_rotation, handover, patient_handover_note,
--   round, patient_round_review, clinical_note, care_task,
--   refresh_tokens, password_reset_token
--
-- Demo password for all seeded accounts: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================================================

SET @pw = '$2a$10$2NwVtI/BXBtLcuLQpDxm9udFsSN2shidqfCJLljiAI20Y8Mg.j9mu';

-- ── 1. Hospitals ─────────────────────────────────────────────────────────────

INSERT IGNORE INTO hospital (id, name, address, contact_email, contact_phone, created_at, updated_at) VALUES
    ('hosp-ng-luth',    'Lagos University Teaching Hospital',        'Idi-Araba, Surulere, Lagos, Nigeria',               'admin@luth.example.ng',              '+2348010000101', NOW(), NOW()),
    ('hosp-ng-abj',     'National Hospital Abuja',                   'Central Business District, Abuja, Nigeria',         'admin@nha.example.ng',               '+2348010000202', NOW(), NOW()),
    ('hosp-ng-cedar',   'Cedar Specialist Hospital Port Harcourt',   'GRA Phase 2, Port Harcourt, Rivers, Nigeria',       'admin@cedarspecialist.example.ng',   '+2348010000303', NOW(), NOW()),
    ('hosp-ke-knh',     'Kenyatta National Hospital',                'Hospital Road, Nairobi, Kenya',                     'admin@knh.example.ke',               '+254700100100',  NOW(), NOW()),
    ('hosp-uk-stmary',  'St Marys Hospital London',                  'Praed Street, London, United Kingdom',              'admin@stmarys.example.uk',           '+442070001001',  NOW(), NOW());

-- ── 2. System Configuration ──────────────────────────────────────────────────

INSERT IGNORE INTO system_configuration (id, hospital_id, news_amber_threshold, news_red_threshold, task_overdue_grace_minutes, round_notifications_enabled, nok_notification_enabled, created_at, updated_at) VALUES
    ('sys-ng-luth',    'hosp-ng-luth',   5, 7, 30, TRUE,  TRUE,  NOW(), NOW()),
    ('sys-ng-abj',     'hosp-ng-abj',    5, 7, 25, TRUE,  TRUE,  NOW(), NOW()),
    ('sys-ng-cedar',   'hosp-ng-cedar',  5, 7, 30, TRUE,  TRUE,  NOW(), NOW()),
    ('sys-ke-knh',     'hosp-ke-knh',    5, 7, 30, TRUE,  FALSE, NOW(), NOW()),
    ('sys-uk-stmary',  'hosp-uk-stmary', 5, 7, 20, TRUE,  TRUE,  NOW(), NOW());

-- ── 3. Platform Operator ─────────────────────────────────────────────────────

INSERT IGNORE INTO platform_operator (id, first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
    ('plat-admin-001', 'Adaeze', 'Okafor', 'platform-admin@careround.local', @pw, 'PLATFORM_ADMIN', TRUE, NOW(), NOW());

-- ── 4. Hospital Onboarding Requests ─────────────────────────────────────────

INSERT IGNORE INTO hospital_onboarding_request (id, hospital_name, country_or_region, contact_email, contact_phone, hospital_type, estimated_beds, primary_need, status, review_notes, reviewed_by_user_id, reviewed_at, provisioned_hospital_id, created_at, updated_at) VALUES
    ('onboard-001', 'Cedar Specialist Hospital Port Harcourt', 'Nigeria', 'admin@cedarspecialist.example.ng', '+2348010000303', 'Private Specialist Hospital', '180',
     'Digitise ward rounds, escalation tracking, and shift handovers.',
     'PROVISIONED', 'Approved and provisioned after operations call with hospital leadership.',
     'plat-admin-001', DATE_SUB(NOW(), INTERVAL 2 DAY), 'hosp-ng-cedar', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

    ('onboard-002', 'Ibadan Heart and Vascular Centre', 'Nigeria', 'ops@ihvc.example.ng', '+2348010000404', 'Cardiology Centre', '75',
     'Structured cardiology rounds and next-of-kin notification audit trail.',
     'PENDING_REVIEW', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

    ('onboard-003', 'Korle Bu Digital Pilot Ward', 'Ghana', 'pilot@kbth.example.gh', '+233201001001', 'Teaching Hospital Pilot', '120',
     'Multidisciplinary handover and task escalation pilot.',
     'CONTACTED', 'Awaiting data processing agreement.',
     'plat-admin-001', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

    ('onboard-004', 'Calabar Critical Care Partnership', 'Nigeria', 'hello@cccp.example.ng', '+2348010000505', 'Regional Referral Hospital', '210',
     'Reduce handover failures across surgical and medical wards.',
     'APPROVED', 'Commercial and clinical reviews are complete; awaiting provisioning.',
     'plat-admin-001', DATE_SUB(NOW(), INTERVAL 10 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

    ('onboard-005', 'Accra Home Recovery Network', 'Ghana', 'ops@ahrn.example.gh', '+233201001002', 'Post Acute Care Network', '45',
     'Out-of-scope deployment request for current inpatient-first roadmap.',
     'REJECTED', 'Rejected because the request is not aligned with the inpatient product scope.',
     'plat-admin-001', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- ── 5. Departments ───────────────────────────────────────────────────────────
-- head_of_department_id forward-references users inserted in step 6.
-- No MySQL-level FK constraint exists on this column — safe to insert ahead.

INSERT IGNORE INTO department (id, hospital_id, name, head_of_department_id, created_at, updated_at) VALUES
    ('dept-luth-medicine',    'hosp-ng-luth',   'Internal Medicine',   'user-luth-cons-ade',    NOW(), NOW()),
    ('dept-luth-cardiology',  'hosp-ng-luth',   'Cardiology',          'user-luth-cons-bello',  NOW(), NOW()),
    ('dept-abj-emergency',    'hosp-ng-abj',    'Emergency Medicine',  'user-abj-cons-yusuf',   NOW(), NOW()),
    ('dept-abj-paeds',        'hosp-ng-abj',    'Paediatrics',         'user-abj-cons-amina',   NOW(), NOW()),
    ('dept-cedar-medicine',   'hosp-ng-cedar',  'General Medicine',    'user-cedar-cons-ibiso', NOW(), NOW()),
    ('dept-knh-medicine',     'hosp-ke-knh',    'Internal Medicine',   'user-knh-cons-otieno',  NOW(), NOW()),
    ('dept-uk-medicine',      'hosp-uk-stmary', 'Acute Medicine',      'user-uk-cons-smith',    NOW(), NOW());

-- ── 6. Users ─────────────────────────────────────────────────────────────────

INSERT IGNORE INTO users (id, hospital_id, first_name, last_name, email, password_hash, role, department_id, is_active, created_at, updated_at) VALUES
    -- Lagos University Teaching Hospital
    ('user-luth-admin',       'hosp-ng-luth', 'Chioma',    'Eze',          'chioma.eze@luth.example.ng',          @pw, 'ADMIN',          NULL,                   TRUE,  NOW(), NOW()),
    ('user-luth-cons-ade',    'hosp-ng-luth', 'Tunde',     'Adewale',      'tunde.adewale@luth.example.ng',       @pw, 'CONSULTANT',     'dept-luth-medicine',   TRUE,  NOW(), NOW()),
    ('user-luth-cons-bello',  'hosp-ng-luth', 'Fatima',    'Bello',        'fatima.bello@luth.example.ng',        @pw, 'CONSULTANT',     'dept-luth-cardiology', TRUE,  NOW(), NOW()),
    ('user-luth-reg-nwosu',   'hosp-ng-luth', 'Ifeoma',    'Nwosu',        'ifeoma.nwosu@luth.example.ng',        @pw, 'REGISTRAR',      'dept-luth-medicine',   TRUE,  NOW(), NOW()),
    ('user-luth-jd-ibrahim',  'hosp-ng-luth', 'Aisha',     'Ibrahim',      'aisha.ibrahim@luth.example.ng',       @pw, 'JUNIOR_DOCTOR',  'dept-luth-medicine',   TRUE,  NOW(), NOW()),
    ('user-luth-nurse-kemi',  'hosp-ng-luth', 'Kemi',      'Balogun',      'kemi.balogun@luth.example.ng',        @pw, 'NURSE',          'dept-luth-medicine',   TRUE,  NOW(), NOW()),
    ('user-luth-nurse-grace', 'hosp-ng-luth', 'Grace',     'Okafor',       'grace.okafor@luth.example.ng',        @pw, 'NURSE',          'dept-luth-cardiology', TRUE,  NOW(), NOW()),
    ('user-luth-ws-femi',     'hosp-ng-luth', 'Femi',      'Oladipo',      'femi.oladipo@luth.example.ng',        @pw, 'WARD_SUPERVISOR', 'dept-luth-medicine',  TRUE,  NOW(), NOW()),
    -- National Hospital Abuja
    ('user-abj-admin',        'hosp-ng-abj',  'Zainab',    'Musa',         'zainab.musa@nha.example.ng',          @pw, 'ADMIN',          NULL,                   TRUE,  NOW(), NOW()),
    ('user-abj-cons-yusuf',   'hosp-ng-abj',  'Bashir',    'Yusuf',        'bashir.yusuf@nha.example.ng',         @pw, 'CONSULTANT',     'dept-abj-emergency',   TRUE,  NOW(), NOW()),
    ('user-abj-cons-amina',   'hosp-ng-abj',  'Amina',     'Sani',         'amina.sani@nha.example.ng',           @pw, 'CONSULTANT',     'dept-abj-paeds',       TRUE,  NOW(), NOW()),
    ('user-abj-reg-okon',     'hosp-ng-abj',  'Eme',       'Okon',         'eme.okon@nha.example.ng',             @pw, 'REGISTRAR',      'dept-abj-emergency',   TRUE,  NOW(), NOW()),
    ('user-abj-jd-musa',      'hosp-ng-abj',  'Musa',      'Danladi',      'musa.danladi@nha.example.ng',         @pw, 'JUNIOR_DOCTOR',  'dept-abj-emergency',   TRUE,  NOW(), NOW()),
    ('user-abj-nurse-hauwa',  'hosp-ng-abj',  'Hauwa',     'Garba',        'hauwa.garba@nha.example.ng',          @pw, 'NURSE',          'dept-abj-paeds',       TRUE,  NOW(), NOW()),
    ('user-abj-ws-ngozi',     'hosp-ng-abj',  'Ngozi',     'Anyanwu',      'ngozi.anyanwu@nha.example.ng',        @pw, 'WARD_SUPERVISOR', 'dept-abj-emergency',  TRUE,  NOW(), NOW()),
    -- Cedar Specialist Hospital (admin is inactive — pending first login)
    ('user-cedar-admin',      'hosp-ng-cedar', 'Tamuno',   'Briggs',       'tamuno.briggs@cedarspecialist.example.ng',    @pw, 'ADMIN',         NULL,                  FALSE, NOW(), NOW()),
    ('user-cedar-cons-ibiso', 'hosp-ng-cedar', 'Ibiso',    'George',       'ibiso.george@cedarspecialist.example.ng',     @pw, 'CONSULTANT',    'dept-cedar-medicine', TRUE,  NOW(), NOW()),
    ('user-cedar-reg-jaja',   'hosp-ng-cedar', 'Jaja',     'Fubara',       'jaja.fubara@cedarspecialist.example.ng',      @pw, 'REGISTRAR',     'dept-cedar-medicine', TRUE,  NOW(), NOW()),
    ('user-cedar-jd-daniel',  'hosp-ng-cedar', 'Daniel',   'Tamunotonye',  'daniel.tamunotonye@cedarspecialist.example.ng', @pw, 'JUNIOR_DOCTOR', 'dept-cedar-medicine', TRUE,  NOW(), NOW()),
    ('user-cedar-nurse-ama',  'hosp-ng-cedar', 'Ama',      'Erekosima',    'ama.erekosima@cedarspecialist.example.ng',    @pw, 'NURSE',         'dept-cedar-medicine', TRUE,  NOW(), NOW()),
    ('user-cedar-ws-ifunanya','hosp-ng-cedar', 'Ifunanya', 'Nwankwo',      'ifunanya.nwankwo@cedarspecialist.example.ng', @pw, 'WARD_SUPERVISOR', 'dept-cedar-medicine', TRUE, NOW(), NOW()),
    -- Kenyatta National Hospital
    ('user-knh-admin',        'hosp-ke-knh',  'Wanjiku',   'Mwangi',       'wanjiku.mwangi@knh.example.ke',       @pw, 'ADMIN',          NULL,                   TRUE,  NOW(), NOW()),
    ('user-knh-cons-otieno',  'hosp-ke-knh',  'Peter',     'Otieno',       'peter.otieno@knh.example.ke',         @pw, 'CONSULTANT',     'dept-knh-medicine',    TRUE,  NOW(), NOW()),
    ('user-knh-reg-kimani',   'hosp-ke-knh',  'Mercy',     'Kimani',       'mercy.kimani@knh.example.ke',         @pw, 'REGISTRAR',      'dept-knh-medicine',    TRUE,  NOW(), NOW()),
    ('user-knh-jd-wairimu',   'hosp-ke-knh',  'Wairimu',   'Njeri',        'wairimu.njeri@knh.example.ke',        @pw, 'JUNIOR_DOCTOR',  'dept-knh-medicine',    TRUE,  NOW(), NOW()),
    ('user-knh-nurse-achebe', 'hosp-ke-knh',  'Nneka',     'Achebe',       'nneka.achebe@knh.example.ke',         @pw, 'NURSE',          'dept-knh-medicine',    TRUE,  NOW(), NOW()),
    ('user-knh-ws-maina',     'hosp-ke-knh',  'Joseph',    'Maina',        'joseph.maina@knh.example.ke',         @pw, 'WARD_SUPERVISOR', 'dept-knh-medicine',   TRUE,  NOW(), NOW()),
    -- St Mary's Hospital London
    ('user-uk-admin',         'hosp-uk-stmary', 'Amelia',  'Turner',       'amelia.turner@stmarys.example.uk',    @pw, 'ADMIN',          NULL,                   TRUE,  NOW(), NOW()),
    ('user-uk-cons-smith',    'hosp-uk-stmary', 'Oliver',  'Smith',        'oliver.smith@stmarys.example.uk',     @pw, 'CONSULTANT',     'dept-uk-medicine',     TRUE,  NOW(), NOW()),
    ('user-uk-reg-khan',      'hosp-uk-stmary', 'Sara',    'Khan',         'sara.khan@stmarys.example.uk',        @pw, 'REGISTRAR',      'dept-uk-medicine',     TRUE,  NOW(), NOW()),
    ('user-uk-jd-patel',      'hosp-uk-stmary', 'Ravi',    'Patel',        'ravi.patel@stmarys.example.uk',       @pw, 'JUNIOR_DOCTOR',  'dept-uk-medicine',     TRUE,  NOW(), NOW()),
    ('user-uk-nurse-evans',   'hosp-uk-stmary', 'Sophie',  'Evans',        'sophie.evans@stmarys.example.uk',     @pw, 'NURSE',          'dept-uk-medicine',     TRUE,  NOW(), NOW()),
    ('user-uk-ws-clarke',     'hosp-uk-stmary', 'Helen',   'Clarke',       'helen.clarke@stmarys.example.uk',     @pw, 'WARD_SUPERVISOR', 'dept-uk-medicine',    TRUE,  NOW(), NOW());

-- ── 7. Account Activation Tokens ─────────────────────────────────────────────
-- cedar-admin is inactive; this token lets them activate their account.

INSERT IGNORE INTO account_activation_token (id, token_hash, user_id, hospital_id, expires_at, used_at, created_at, updated_at) VALUES
    ('act-cedar-admin',
     '5f460a6b3d1bd0d099412ec512a8e69e54b98dd57a3e1585fb94a3c2e77d37d2',
     'user-cedar-admin', 'hosp-ng-cedar',
     DATE_ADD(NOW(), INTERVAL 72 HOUR), NULL, NOW(), NOW()),
    ('act-luth-admin-used',
     '881144bb9ae7ec088c1dbaaf82031f8d68e5e989a6f3620d3ae7f3f1f6a6f0e4',
     'user-luth-admin', 'hosp-ng-luth',
     DATE_ADD(NOW(), INTERVAL 48 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NOW());

-- ── 8. Wards ─────────────────────────────────────────────────────────────────

INSERT IGNORE INTO ward (id, hospital_id, name, specialty, total_beds, supervisor_id, created_at, updated_at) VALUES
    ('ward-luth-male-med',   'hosp-ng-luth',   'Male Medical Ward',          'Internal Medicine',   36, 'user-luth-ws-femi',      NOW(), NOW()),
    ('ward-luth-female-med', 'hosp-ng-luth',   'Female Medical Ward',        'Internal Medicine',   34, 'user-luth-ws-femi',      NOW(), NOW()),
    ('ward-luth-cardiac',    'hosp-ng-luth',   'Cardiac Stepdown Unit',      'Cardiology',          20, 'user-luth-ws-femi',      NOW(), NOW()),
    ('ward-abj-emergency',   'hosp-ng-abj',    'Emergency Observation Ward', 'Emergency Medicine',  28, 'user-abj-ws-ngozi',      NOW(), NOW()),
    ('ward-abj-paeds',       'hosp-ng-abj',    'Paediatric Medical Ward',    'Paediatrics',         32, 'user-abj-ws-ngozi',      NOW(), NOW()),
    ('ward-cedar-med',       'hosp-ng-cedar',  'General Medical Ward',       'General Medicine',    24, 'user-cedar-ws-ifunanya', NOW(), NOW()),
    ('ward-cedar-hdu',       'hosp-ng-cedar',  'High Dependency Bay',        'General Medicine',    10, 'user-cedar-ws-ifunanya', NOW(), NOW()),
    ('ward-knh-med',         'hosp-ke-knh',    'Medical Ward 7',             'Internal Medicine',   30, 'user-knh-ws-maina',      NOW(), NOW()),
    ('ward-uk-acute',        'hosp-uk-stmary', 'Acute Medical Unit',         'Acute Medicine',      26, 'user-uk-ws-clarke',      NOW(), NOW());

-- ── 9. Medical Teams ─────────────────────────────────────────────────────────

INSERT IGNORE INTO medical_team (id, hospital_id, name, consultant_id, department_id, created_at, updated_at) VALUES
    ('team-luth-med-a',    'hosp-ng-luth',   'LUTH Medicine Team A',       'user-luth-cons-ade',    'dept-luth-medicine',   NOW(), NOW()),
    ('team-luth-cardio',   'hosp-ng-luth',   'LUTH Cardiology Team',       'user-luth-cons-bello',  'dept-luth-cardiology', NOW(), NOW()),
    ('team-abj-emergency', 'hosp-ng-abj',    'NHA Emergency Team',         'user-abj-cons-yusuf',   'dept-abj-emergency',   NOW(), NOW()),
    ('team-abj-paeds',     'hosp-ng-abj',    'NHA Paediatrics Team',       'user-abj-cons-amina',   'dept-abj-paeds',       NOW(), NOW()),
    ('team-cedar-med',     'hosp-ng-cedar',  'Cedar General Medicine Team', 'user-cedar-cons-ibiso', 'dept-cedar-medicine',  NOW(), NOW()),
    ('team-knh-med',       'hosp-ke-knh',    'KNH Medicine Team',          'user-knh-cons-otieno',  'dept-knh-medicine',    NOW(), NOW()),
    ('team-uk-acute',      'hosp-uk-stmary', 'St Marys Acute Medicine',    'user-uk-cons-smith',    'dept-uk-medicine',     NOW(), NOW());

-- ── 10. Medical Team → Ward Assignments ──────────────────────────────────────

INSERT IGNORE INTO medical_team_ward (medical_team_id, ward_id, assigned_at) VALUES
    ('team-luth-med-a',    'ward-luth-male-med',  NOW()),
    ('team-luth-med-a',    'ward-luth-female-med', NOW()),
    ('team-luth-cardio',   'ward-luth-cardiac',   NOW()),
    ('team-abj-emergency', 'ward-abj-emergency',  NOW()),
    ('team-abj-paeds',     'ward-abj-paeds',      NOW()),
    ('team-cedar-med',     'ward-cedar-med',       NOW()),
    ('team-cedar-med',     'ward-cedar-hdu',       NOW()),
    ('team-knh-med',       'ward-knh-med',         NOW()),
    ('team-uk-acute',      'ward-uk-acute',        NOW());

-- ── 11. Medical Team Members ─────────────────────────────────────────────────

INSERT IGNORE INTO medical_team_member (medical_team_id, user_id, joined_at) VALUES
    ('team-luth-med-a',    'user-luth-cons-ade',    NOW()),
    ('team-luth-med-a',    'user-luth-reg-nwosu',   NOW()),
    ('team-luth-med-a',    'user-luth-jd-ibrahim',  NOW()),
    ('team-luth-med-a',    'user-luth-nurse-kemi',  NOW()),
    ('team-luth-cardio',   'user-luth-cons-bello',  NOW()),
    ('team-luth-cardio',   'user-luth-nurse-grace', NOW()),
    ('team-abj-emergency', 'user-abj-cons-yusuf',   NOW()),
    ('team-abj-emergency', 'user-abj-reg-okon',     NOW()),
    ('team-abj-emergency', 'user-abj-jd-musa',      NOW()),
    ('team-abj-paeds',     'user-abj-cons-amina',   NOW()),
    ('team-abj-paeds',     'user-abj-nurse-hauwa',  NOW()),
    ('team-cedar-med',     'user-cedar-cons-ibiso', NOW()),
    ('team-cedar-med',     'user-cedar-reg-jaja',   NOW()),
    ('team-cedar-med',     'user-cedar-jd-daniel',  NOW()),
    ('team-cedar-med',     'user-cedar-nurse-ama',  NOW()),
    ('team-knh-med',       'user-knh-cons-otieno',  NOW()),
    ('team-knh-med',       'user-knh-reg-kimani',   NOW()),
    ('team-knh-med',       'user-knh-jd-wairimu',   NOW()),
    ('team-knh-med',       'user-knh-nurse-achebe', NOW()),
    ('team-uk-acute',      'user-uk-cons-smith',    NOW()),
    ('team-uk-acute',      'user-uk-reg-khan',      NOW()),
    ('team-uk-acute',      'user-uk-jd-patel',      NOW()),
    ('team-uk-acute',      'user-uk-nurse-evans',   NOW());

-- ── 12. Medical Team Invites ─────────────────────────────────────────────────

INSERT IGNORE INTO medical_team_invite (id, hospital_id, medical_team_id, invited_user_id, invited_by_id, status, expires_at, created_at, updated_at) VALUES
    ('invite-luth-pending',  'hosp-ng-luth',   'team-luth-med-a',    'user-luth-nurse-grace', 'user-luth-cons-ade',    'PENDING',  DATE_ADD(NOW(), INTERVAL 24 HOUR),  NOW(),                           NOW()),
    ('invite-luth-expired',  'hosp-ng-luth',   'team-luth-cardio',   'user-luth-jd-ibrahim',  'user-luth-cons-bello',  'PENDING',  DATE_SUB(NOW(), INTERVAL 2 HOUR),   DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
    ('invite-abj-accepted',  'hosp-ng-abj',    'team-abj-emergency', 'user-abj-nurse-hauwa',  'user-abj-cons-yusuf',   'ACCEPTED', DATE_ADD(NOW(), INTERVAL 2 DAY),    DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
    ('invite-cedar-declined','hosp-ng-cedar',  'team-cedar-med',     'user-cedar-jd-daniel',  'user-cedar-cons-ibiso', 'DECLINED', DATE_ADD(NOW(), INTERVAL 1 DAY),    DATE_SUB(NOW(), INTERVAL 8 HOUR),NOW()),
    ('invite-knh-expired',   'hosp-ke-knh',    'team-knh-med',       'user-knh-jd-wairimu',   'user-knh-cons-otieno',  'EXPIRED',  DATE_SUB(NOW(), INTERVAL 12 HOUR),  DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- ── 13. Shift Schedules (reference data) ─────────────────────────────────────

INSERT IGNORE INTO shift_schedule (id, hospital_id, ward_id, shift_type, start_time, end_time, days_of_week, is_active, created_at, updated_at) VALUES
    ('sched-luth-day-all',       'hosp-ng-luth',   NULL,              'DAY',   '07:00:00', '19:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-luth-night-all',     'hosp-ng-luth',   NULL,              'NIGHT', '19:00:00', '07:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-luth-weekend-med',   'hosp-ng-luth',   'ward-luth-female-med', 'DAY', '08:00:00', '20:00:00', 'SATURDAY,SUNDAY',                                TRUE,  NOW(), NOW()),
    ('sched-abj-emergency-day',  'hosp-ng-abj',    'ward-abj-emergency', 'DAY', '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',          TRUE,  NOW(), NOW()),
    ('sched-abj-emergency-night','hosp-ng-abj',    'ward-abj-emergency', 'NIGHT','20:00:00','08:00:00', 'MONDAY,WEDNESDAY,FRIDAY',                            TRUE,  NOW(), NOW()),
    ('sched-abj-paeds-day',      'hosp-ng-abj',    'ward-abj-paeds',  'DAY',   '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-abj-paeds-night',    'hosp-ng-abj',    'ward-abj-paeds',  'NIGHT', '20:00:00', '08:00:00', 'MONDAY,WEDNESDAY,FRIDAY',                            TRUE,  NOW(), NOW()),
    ('sched-cedar-day',          'hosp-ng-cedar',  NULL,              'DAY',   '07:00:00', '19:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-cedar-night-med',    'hosp-ng-cedar',  'ward-cedar-med',  'NIGHT', '19:00:00', '07:00:00', 'TUESDAY,THURSDAY,SATURDAY',                          TRUE,  NOW(), NOW()),
    ('sched-cedar-night-hdu',    'hosp-ng-cedar',  'ward-cedar-hdu',  'NIGHT', '19:00:00', '07:00:00', 'TUESDAY,THURSDAY,SATURDAY',                          TRUE,  NOW(), NOW()),
    ('sched-knh-day',            'hosp-ke-knh',    'ward-knh-med',    'DAY',   '07:30:00', '19:30:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-knh-night',          'hosp-ke-knh',    'ward-knh-med',    'NIGHT', '19:30:00', '07:30:00', 'SATURDAY,SUNDAY',                                    FALSE, NOW(), NOW()),
    ('sched-uk-day',             'hosp-uk-stmary', 'ward-uk-acute',   'DAY',   '08:00:00', '20:00:00', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',           TRUE,  NOW(), NOW()),
    ('sched-uk-night',           'hosp-uk-stmary', 'ward-uk-acute',   'NIGHT', '20:00:00', '08:00:00', 'MONDAY,WEDNESDAY,FRIDAY',                            TRUE,  NOW(), NOW());

-- ── 14. Patients ─────────────────────────────────────────────────────────────
-- Admission dates are static so this migration is deterministic on any run date.
-- pt-luth-003 is DISCHARGED and has no ward_id (allowed since V15).

INSERT IGNORE INTO patient (id, hospital_id, ward_id, bed_number, medical_team_id, admitting_consultant_id, first_name, last_name, date_of_birth, gender, hospital_number, admission_date, admission_type, primary_diagnosis, specialty_required, acuity_level, news_score, is_discharge_ready, estimated_discharge_date, status, created_at, updated_at) VALUES
    -- LUTH
    ('pt-luth-001', 'hosp-ng-luth', 'ward-luth-male-med',   'M12', 'team-luth-med-a',    'user-luth-cons-ade',   'Emeka',     'Okoye',       '1959-03-12', 'M', 'LUTH-0001', '2026-05-11 09:00:00', 'EMERGENCY', 'Community acquired pneumonia with type 2 diabetes',     'Internal Medicine', 'MEDIUM',   5,  FALSE, NULL,         'ADMITTED',        NOW(), NOW()),
    ('pt-luth-002', 'hosp-ng-luth', 'ward-luth-cardiac',    'C03', 'team-luth-cardio',   'user-luth-cons-bello', 'Bamidele',  'Ajayi',       '1964-11-20', 'M', 'LUTH-0002', '2026-05-14 07:30:00', 'EMERGENCY', 'Acute decompensated heart failure',                     'Cardiology',        'HIGH',     8,  FALSE, NULL,         'DETERIORATING',   NOW(), NOW()),
    ('pt-luth-003', 'hosp-ng-luth', NULL,                    NULL, 'team-luth-med-a',    'user-luth-cons-ade',   'Nkechi',    'Umeh',        '1988-02-18', 'F', 'LUTH-0003', '2026-05-08 10:00:00', 'ELECTIVE',  'Post-operative appendicectomy, discharged home',         'General Surgery',   'LOW',      0,  FALSE, '2026-05-14', 'DISCHARGED',      NOW(), NOW()),
    ('pt-luth-004', 'hosp-ng-luth', 'ward-luth-female-med', 'F08', 'team-luth-med-a',    'user-luth-cons-ade',   'Morenike',  'Afolayan',    '1973-08-04', 'F', 'LUTH-0004', '2026-05-13 11:00:00', 'TRANSFER',  'Uncontrolled diabetes with infected foot ulcer',         'Internal Medicine', 'MEDIUM',   4,  FALSE, NULL,         'STABLE',          NOW(), NOW()),
    ('pt-luth-005', 'hosp-ng-luth', 'ward-luth-female-med', 'F11', 'team-luth-med-a',    'user-luth-cons-ade',   'Hauwa',     'Lawal',       '1981-01-22', 'F', 'LUTH-0005', '2026-05-10 08:00:00', 'EMERGENCY', 'Severe anaemia now clinically improved',                  'Internal Medicine', 'LOW',      2,  TRUE,  '2026-05-16', 'DISCHARGE_READY', NOW(), NOW()),
    -- NHA Abuja
    ('pt-abj-001',  'hosp-ng-abj',  'ward-abj-emergency',   'E05', 'team-abj-emergency', 'user-abj-cons-yusuf',  'Suleiman',  'Abdullahi',   '1952-07-25', 'M', 'NHA-0001',  '2026-05-15 03:00:00', 'EMERGENCY', 'Sepsis likely secondary to urinary tract infection',     'Emergency Medicine','CRITICAL', 11, FALSE, NULL,         'DETERIORATING',   NOW(), NOW()),
    ('pt-abj-002',  'hosp-ng-abj',  'ward-abj-paeds',       'P09', 'team-abj-paeds',     'user-abj-cons-amina',  'Maryam',    'Usman',       '2018-05-14', 'F', 'NHA-0002',  '2026-05-14 09:00:00', 'EMERGENCY', 'Bronchiolitis requiring oxygen therapy',                  'Paediatrics',       'MEDIUM',   6,  FALSE, NULL,         'ADMITTED',        NOW(), NOW()),
    ('pt-abj-003',  'hosp-ng-abj',  'ward-abj-emergency',   'E11', 'team-abj-emergency', 'user-abj-cons-yusuf',  'Chinonso',  'Eze',         '1990-09-09', 'M', 'NHA-0003',  '2026-05-15 05:00:00', 'TRANSFER',  'Diabetic ketoacidosis after stabilization in resus',     'Emergency Medicine','HIGH',     7,  FALSE, NULL,         'ADMITTED',        NOW(), NOW()),
    -- Cedar
    ('pt-cedar-001','hosp-ng-cedar','ward-cedar-med',        'G04', 'team-cedar-med',     'user-cedar-cons-ibiso','Tonye',     'Hart',        '1968-05-02', 'M', 'CEDAR-0001','2026-05-12 08:00:00', 'EMERGENCY', 'Hypertensive emergency with pulmonary oedema',            'General Medicine',  'HIGH',     7,  FALSE, NULL,         'ADMITTED',        NOW(), NOW()),
    ('pt-cedar-002','hosp-ng-cedar','ward-cedar-hdu',        'H02', 'team-cedar-med',     'user-cedar-cons-ibiso','Boma',      'Georgewill',  '1979-12-12', 'F', 'CEDAR-0002','2026-05-14 21:00:00', 'TRANSFER',  'Post-stroke monitoring with fluctuating consciousness',   'General Medicine',  'CRITICAL', 10, FALSE, NULL,         'DETERIORATING',   NOW(), NOW()),
    -- KNH
    ('pt-knh-001',  'hosp-ke-knh',  'ward-knh-med',         'K17', 'team-knh-med',       'user-knh-cons-otieno', 'Achieng',   'Njoroge',     '1976-06-08', 'F', 'KNH-0001',  '2026-05-12 10:00:00', 'TRANSFER',  'Uncontrolled hypertension with acute kidney injury',     'Internal Medicine', 'MEDIUM',   5,  FALSE, NULL,         'ADMITTED',        NOW(), NOW()),
    ('pt-knh-002',  'hosp-ke-knh',  'ward-knh-med',         'K21', 'team-knh-med',       'user-knh-cons-otieno', 'Mwende',    'Mutiso',      '1986-03-17', 'F', 'KNH-0002',  '2026-05-14 09:00:00', 'ELECTIVE',  'Diagnostic work-up for recurrent syncope',               'Internal Medicine', 'LOW',      1,  FALSE, NULL,         'STABLE',          NOW(), NOW()),
    -- St Mary's
    ('pt-uk-001',   'hosp-uk-stmary','ward-uk-acute',       'A11', 'team-uk-acute',      'user-uk-cons-smith',   'George',    'Williams',    '1949-10-30', 'M', 'STM-0001',  '2026-05-13 07:00:00', 'EMERGENCY', 'COPD exacerbation, improving',                           'Acute Medicine',    'LOW',      3,  TRUE,  '2026-05-16', 'DISCHARGE_READY', NOW(), NOW()),
    ('pt-uk-002',   'hosp-uk-stmary','ward-uk-acute',       'A18', 'team-uk-acute',      'user-uk-cons-smith',   'Nadia',     'Hassan',      '1971-07-07', 'F', 'STM-0002',  '2026-05-15 01:00:00', 'EMERGENCY', 'Community acquired pneumonia on IV antibiotics',          'Acute Medicine',    'MEDIUM',   5,  FALSE, NULL,         'ADMITTED',        NOW(), NOW());

-- ── 15. Patient Vitals ────────────────────────────────────────────────────────

INSERT IGNORE INTO patient_vitals (id, patient_id, recorded_by_id, heart_rate, respiratory_rate, oxygen_saturation, systolic_bp, temperature, consciousness_level, news_score, recorded_at, created_at, updated_at) VALUES
    ('vitals-luth-001',  'pt-luth-001', 'user-luth-nurse-kemi',  102, 24, 94.00, 128, 38.2, 'ALERT',       5,  DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), NOW()),
    ('vitals-luth-002',  'pt-luth-002', 'user-luth-nurse-grace', 124, 28, 88.00,  96, 37.9, 'VOICE',       10, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW(), NOW()),
    ('vitals-luth-004',  'pt-luth-004', 'user-luth-nurse-kemi',   88, 18, 97.00, 134, 36.8, 'ALERT',       2,  DATE_SUB(NOW(), INTERVAL 70 MINUTE), NOW(), NOW()),
    ('vitals-luth-005',  'pt-luth-005', 'user-luth-nurse-kemi',   82, 16, 98.00, 122, 36.7, 'ALERT',       1,  DATE_SUB(NOW(), INTERVAL 90 MINUTE), NOW(), NOW()),
    ('vitals-abj-001',   'pt-abj-001',  'user-abj-nurse-hauwa',  132, 30, 89.00,  88, 39.3, 'VOICE',       11, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), NOW()),
    ('vitals-abj-002',   'pt-abj-002',  'user-abj-nurse-hauwa',  118, 34, 92.00, 100, 38.0, 'ALERT',       6,  DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
    ('vitals-abj-003',   'pt-abj-003',  'user-abj-nurse-hauwa',  116, 26, 94.00, 104, 37.6, 'ALERT',       7,  DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW(), NOW()),
    ('vitals-cedar-001', 'pt-cedar-001','user-cedar-nurse-ama',  110, 25, 93.00, 168, 37.8, 'PAIN',        8,  DATE_SUB(NOW(), INTERVAL 35 MINUTE), NOW(), NOW()),
    ('vitals-cedar-002', 'pt-cedar-002','user-cedar-nurse-ama',  126, 31, 90.00,  92, 38.5, 'UNRESPONSIVE',12, DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW(), NOW()),
    ('vitals-knh-001',   'pt-knh-001',  'user-knh-nurse-achebe',  96, 22, 95.00, 172, 37.4, 'ALERT',       5,  DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW(), NOW()),
    ('vitals-knh-002',   'pt-knh-002',  'user-knh-nurse-achebe',  74, 15, 99.00, 118, 36.6, 'ALERT',       0,  DATE_SUB(NOW(), INTERVAL 80 MINUTE), NOW(), NOW()),
    ('vitals-uk-001',    'pt-uk-001',   'user-uk-nurse-evans',    84, 20, 96.00, 132, 36.9, 'ALERT',       3,  DATE_SUB(NOW(), INTERVAL 50 MINUTE), NOW(), NOW()),
    ('vitals-uk-002',    'pt-uk-002',   'user-uk-nurse-evans',   101, 23, 94.00, 126, 37.9, 'VOICE',       5,  DATE_SUB(NOW(), INTERVAL 32 MINUTE), NOW(), NOW());

-- ── 16. Next of Kin ──────────────────────────────────────────────────────────

INSERT IGNORE INTO next_of_kin (id, patient_id, name, relationship, phone, email, preferred_contact_method, is_emergency_contact, notification_consent, created_at, updated_at) VALUES
    ('nok-luth-001',  'pt-luth-001', 'Adaeze Okoye',     'Daughter', '+2348021111001', 'adaeze.okoye@example.ng',    'BOTH',  TRUE,  TRUE,  NOW(), NOW()),
    ('nok-luth-002',  'pt-luth-002', 'Temitope Ajayi',   'Son',      '+2348021111003', 'temitope.ajayi@example.ng',  'EMAIL', TRUE,  TRUE,  NOW(), NOW()),
    ('nok-luth-003',  'pt-luth-003', 'Olamide Umeh',     'Sibling',  '+2348021111006', 'olamide.umeh@example.ng',    'EMAIL', TRUE,  TRUE,  NOW(), NOW()),
    ('nok-luth-004',  'pt-luth-004', 'Kunle Afolayan',   'Spouse',   '+2348021111004', NULL,                          'SMS',  FALSE, FALSE, NOW(), NOW()),
    ('nok-luth-005',  'pt-luth-005', 'Mariam Lawal',     'Sister',   '+2348021111005', 'mariam.lawal@example.ng',    'EMAIL', TRUE,  TRUE,  NOW(), NOW()),
    ('nok-abj-001',   'pt-abj-001',  'Hadiza Abdullahi', 'Wife',     '+2348032222001', 'hadiza.abdullahi@example.ng','BOTH',  TRUE,  TRUE,  NOW(), NOW()),
    ('nok-abj-002',   'pt-abj-002',  'Aisha Usman',      'Mother',   '+2348032222002', 'aisha.usman@example.ng',     'SMS',   TRUE,  TRUE,  NOW(), NOW()),
    ('nok-abj-003',   'pt-abj-003',  'Chidera Eze',      'Brother',  NULL,             'chidera.eze@example.ng',     'EMAIL', FALSE, TRUE,  NOW(), NOW()),
    ('nok-cedar-001', 'pt-cedar-001','Peace Hart',        'Daughter', '+2348043333001', 'peace.hart@example.ng',      'BOTH',  TRUE,  TRUE,  NOW(), NOW()),
    ('nok-cedar-002', 'pt-cedar-002','Tamara Georgewill', 'Partner',  '+2348043333002', 'tamara.georgewill@example.ng','SMS',  TRUE,  TRUE,  NOW(), NOW()),
    ('nok-knh-001',   'pt-knh-001',  'Daniel Njoroge',   'Brother',  '+254711000100',  'daniel.njoroge@example.ke',  'EMAIL', TRUE,  FALSE, NOW(), NOW()),
    ('nok-knh-002',   'pt-knh-002',  'Faith Mutiso',     'Mother',   '+254711000101',  NULL,                          'SMS',  FALSE, TRUE,  NOW(), NOW()),
    ('nok-uk-001',    'pt-uk-001',   'Helen Williams',   'Spouse',   '+447700900100',  'helen.williams@example.uk',  'EMAIL', TRUE,  TRUE,  NOW(), NOW()),
    ('nok-uk-002',    'pt-uk-002',   'Yusuf Hassan',     'Brother',  '+447700900101',  'yusuf.hassan@example.uk',    'BOTH',  TRUE,  TRUE,  NOW(), NOW());

-- ── 17. Escalations ──────────────────────────────────────────────────────────

INSERT IGNORE INTO escalation (id, hospital_id, patient_id, triggered_by_id, trigger_type, severity, assigned_to_id, status, notes, resolved_at, created_at, updated_at) VALUES
    ('esc-luth-red-001',   'hosp-ng-luth',   'pt-luth-002', 'user-luth-nurse-grace', 'HIGH_NEWS_SCORE', 'RED',   'user-luth-cons-bello',  'OPEN',         'NEWS2 score 10 with hypotension and low saturation.',                           NULL,                          DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW()),
    ('esc-abj-red-001',    'hosp-ng-abj',    'pt-abj-001',  'user-abj-nurse-hauwa',  'DETERIORATION',   'RED',   'user-abj-cons-yusuf',   'ACKNOWLEDGED', 'Sepsis patient remains hypotensive after initial fluid bolus.',                 NULL,                          DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW()),
    ('esc-cedar-red-001',  'hosp-ng-cedar',  'pt-cedar-002','user-cedar-nurse-ama',  'NURSE_CONCERN',   'RED',   'user-cedar-cons-ibiso', 'OPEN',         'Reduced responsiveness in HDU after transfer from theatre recovery.',           NULL,                          DATE_SUB(NOW(), INTERVAL 18 MINUTE), NOW()),
    ('esc-luth-amber-002', 'hosp-ng-luth',   'pt-luth-005', 'user-luth-nurse-kemi',  'TASK_OVERDUE',    'AMBER', 'user-luth-reg-nwosu',   'ACKNOWLEDGED', 'Discharge counselling task still open close to expected departure window.',     NULL,                          DATE_SUB(NOW(), INTERVAL 70 MINUTE), NOW()),
    ('esc-knh-amber-001',  'hosp-ke-knh',    'pt-knh-001',  'user-knh-nurse-achebe', 'HIGH_NEWS_SCORE', 'AMBER', 'user-knh-cons-otieno',  'RESOLVED',     'AKI patient reviewed and antihypertensive plan adjusted.',                      DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
    ('esc-uk-amber-001',   'hosp-uk-stmary', 'pt-uk-002',   'user-uk-nurse-evans',   'DETERIORATION',   'AMBER', 'user-uk-reg-khan',      'RESOLVED',     'Respiratory symptoms stabilized after nebulizer treatment and reassessment.',  DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW());
