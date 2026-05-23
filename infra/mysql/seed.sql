-- ============================================================
-- CareRound Demo Seed Data
-- Password for all accounts: Password123
-- Run: mysql -u careround -pcareround_password careround_core < seed.sql
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE medication_task;
TRUNCATE TABLE medication_chart;
TRUNCATE TABLE prescription;
TRUNCATE TABLE handover_note;
TRUNCATE TABLE clinical_note;
TRUNCATE TABLE patient_vitals;
TRUNCATE TABLE patient;
TRUNCATE TABLE ward;
TRUNCATE TABLE system_configuration;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE activation_token;
TRUNCATE TABLE hospital_onboarding_request;
TRUNCATE TABLE users;
TRUNCATE TABLE hospital;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. HOSPITAL
-- ============================================================
-- Sentinel record required for PLATFORM_ADMIN login (hospitalCode: "PLATFORM")
INSERT INTO hospital (id, name, code, address, contact_email, contact_phone, is_active, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000000',
 'CareRound Platform', 'PLATFORM',
 '', 'platform@careround.com', '',
 TRUE, '2026-01-01 00:00:00', '2026-01-01 00:00:00');

INSERT INTO hospital (id, name, code, address, contact_email, contact_phone, is_active, created_at, updated_at) VALUES
('10000000-0000-0000-0000-000000000001',
 'City General Hospital', 'CGH',
 '1 Hospital Road, London, W1A 1AA',
 'info@citygeneral.nhs.uk', '+44 20 7946 0000',
 TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 2. SYSTEM CONFIGURATION
-- ============================================================
INSERT INTO system_configuration (id, hospital_id, task_overdue_reminder_minutes, task_escalation_minutes, push_notifications_enabled, created_at, updated_at) VALUES
('20000000-0000-0000-0000-000000000001',
 '10000000-0000-0000-0000-000000000001',
 10, 20, TRUE,
 '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 3. USERS  (password: Password123)
-- ============================================================
INSERT INTO users (id, hospital_id, first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
-- Admin
('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 'Rebecca', 'Morgan', 'admin@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'ADMIN', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
-- Supervisor
('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 'Linda', 'Walsh', 'l.walsh@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'SUPERVISOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
-- Doctors
('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 'Sarah', 'Chen', 's.chen@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'DOCTOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 'James', 'Okafor', 'j.okafor@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'DOCTOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
-- Nurses
('30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 'Emily', 'Foster', 'e.foster@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 'Michael', 'Adeyemi', 'm.adeyemi@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 'Priya', 'Sharma', 'p.sharma@citygeneral.nhs.uk',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00');

-- ============================================================
-- 4. WARDS
-- ============================================================
INSERT INTO ward (id, hospital_id, name, specialty, total_beds, is_active, created_at, updated_at) VALUES
('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Ward A', 'General Medicine',  10, TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00'),
('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Ward B', 'Surgery',           8,  TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00'),
('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'ICU',    'Intensive Care',    6,  TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 5. PATIENTS
-- Acuity key: VHI 0-2 GREEN, 3-4 AMBER, 5+ RED
-- ============================================================
INSERT INTO patient (
  id, hospital_id, ward_id, bed_number,
  first_name, last_name, date_of_birth, gender,
  hospital_number, phone_number, address,
  previous_conditions, current_medications, allergies,
  emergency_contact_name, emergency_contact_phone, registered_by_id,
  admission_date, admission_type, primary_diagnosis,
  acuity_color, status, estimated_discharge_date,
  created_at, updated_at
) VALUES

-- Ward A — General Medicine
('50000000-0000-0000-0000-000000000001',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'A1',
 'James', 'Harrington', '1965-03-15', 'MALE',
 'CGH-2026-001', '+44 7700 900001', '14 Elm Close, London, SE1 2PQ',
 'Type 2 Diabetes Mellitus, Hypertension',
 'Metformin 500mg BD, Lisinopril 5mg OD',
 'Penicillin',
 'Margaret Harrington', '+44 7700 900101',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-20 10:30:00', 'ELECTIVE',
 'Type 2 Diabetes Mellitus with peripheral neuropathy',
 'GREEN', 'ADMITTED', '2026-05-23',
 '2026-05-20 10:30:00', '2026-05-22 09:00:00'),

('50000000-0000-0000-0000-000000000002',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'A2',
 'Margaret', 'Osei', '1948-07-22', 'FEMALE',
 'CGH-2026-002', '+44 7700 900002', '7 Maple Street, London, N1 4RW',
 'Hypertension, Chronic Kidney Disease stage 2',
 'Ramipril 5mg OD',
 'Aspirin, NSAIDs',
 'Kofi Osei', '+44 7700 900102',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-21 02:15:00', 'EMERGENCY',
 'Hypertensive crisis with acute kidney injury stage 1',
 'AMBER', 'ADMITTED', NULL,
 '2026-05-21 02:15:00', '2026-05-22 08:30:00'),

('50000000-0000-0000-0000-000000000003',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'A3',
 'Thomas', 'Brennan', '1955-11-08', 'MALE',
 'CGH-2026-003', '+44 7700 900003', '22 Oak Avenue, London, W6 0TH',
 'COPD Gold Stage III, Ex-smoker 30 pack-years, Ischaemic Heart Disease',
 'Tiotropium 18mcg OD, Salbutamol 100mcg PRN, Aspirin 75mg OD',
 'Morphine',
 'Patricia Brennan', '+44 7700 900103',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-22 04:00:00', 'EMERGENCY',
 'Acute exacerbation of COPD with type 2 respiratory failure',
 'RED', 'ADMITTED', NULL,
 '2026-05-22 04:00:00', '2026-05-22 09:00:00'),

-- Ward B — Surgery
('50000000-0000-0000-0000-000000000004',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', 'B1',
 'Fatima', 'Al-Hassan', '1978-04-30', 'FEMALE',
 'CGH-2026-004', '+44 7700 900004', '5 Cedar Road, London, E3 2LN',
 'Gallstone disease',
 'None',
 'None known',
 'Ahmed Al-Hassan', '+44 7700 900104',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-19 07:00:00', 'ELECTIVE',
 'Elective laparoscopic cholecystectomy — post-operative day 3',
 'GREEN', 'ADMITTED', '2026-05-23',
 '2026-05-19 07:00:00', '2026-05-22 09:00:00'),

('50000000-0000-0000-0000-000000000005',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', 'B2',
 'Robert', 'Kowalski', '1970-09-14', 'MALE',
 'CGH-2026-005', '+44 7700 900005', '33 Pine Lane, London, SE15 3HK',
 'Osteoarthritis, Type 2 Diabetes Mellitus',
 'Metformin 1g BD',
 'Cephalosporins',
 'Anna Kowalski', '+44 7700 900105',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-20 14:00:00', 'TRANSFER',
 'Post-operative wound infection following right total hip replacement',
 'AMBER', 'ADMITTED', NULL,
 '2026-05-20 14:00:00', '2026-05-22 08:00:00'),

('50000000-0000-0000-0000-000000000006',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', 'B3',
 'Aisha', 'Mensah', '1990-12-03', 'FEMALE',
 'CGH-2026-006', '+44 7700 900006', '9 Birch Way, London, SW9 8MQ',
 'Nil significant',
 'None',
 'Latex, Ibuprofen',
 'Samuel Mensah', '+44 7700 900106',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-22 03:30:00', 'EMERGENCY',
 'Acute appendicitis — emergency appendicectomy, post-operative day 1',
 'RED', 'ADMITTED', NULL,
 '2026-05-22 03:30:00', '2026-05-22 09:30:00'),

-- ICU
('50000000-0000-0000-0000-000000000007',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', 'IC1',
 'David', 'Patel', '1942-06-18', 'MALE',
 'CGH-2026-007', '+44 7700 900007', '47 Willow Grove, London, NW3 5PP',
 'Chronic heart failure NYHA class III, Atrial fibrillation, Hypertension',
 'Furosemide 40mg OD, Bisoprolol 5mg OD, Warfarin 3mg OD, Ramipril 2.5mg OD',
 'Vancomycin',
 'Sunita Patel', '+44 7700 900107',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-21 21:00:00', 'EMERGENCY',
 'Septic shock secondary to community-acquired pneumonia',
 'RED', 'ADMITTED', NULL,
 '2026-05-21 21:00:00', '2026-05-22 06:00:00'),

('50000000-0000-0000-0000-000000000008',
 '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', 'IC2',
 'Susan', 'Clarke', '1961-02-27', 'FEMALE',
 'CGH-2026-008', '+44 7700 900008', '15 Chestnut Drive, London, TW3 1YZ',
 'Hypertension',
 'Amlodipine 5mg OD',
 'Codeine',
 'Robert Clarke', '+44 7700 900108',
 '30000000-0000-0000-0000-000000000001',
 '2026-05-21 19:30:00', 'TRANSFER',
 'Severe traumatic brain injury following road traffic accident',
 'AMBER', 'ADMITTED', NULL,
 '2026-05-21 19:30:00', '2026-05-22 09:00:00');

-- ============================================================
-- 6. PATIENT VITALS
-- VHI scores computed from AcuityComputationService thresholds:
--   HR: ≤40/≥130→3, ≤50/≥111→2, ≤60/≥101→1
--   SBP: ≤80→3, ≤90/≥200→2, ≤100/≥160→1
--   RR: ≤8/≥30→3, ≥21→2, ≥15→1
--   Temp: ≤35/≥39→3, ≤36/≥38.5→2, 37.5-38.4→1
--   SpO2: ≤91→3, ≤93→2, ≤95→1
-- ============================================================
INSERT INTO patient_vitals (
  id, patient_id, hospital_id, recorded_by_id,
  pulse, systolic_bp, diastolic_bp, respiratory_rate, temperature, spo2,
  vhi_score, vhi_status, recorded_at, created_at, updated_at
) VALUES
-- James Harrington: VHI=1 (RR 16 →+1, rest 0) → GREEN
('60000000-0000-0000-0000-000000000001',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 78, 132, 80, 16, 37.1, 97.00, 1, 'STABLE',
 '2026-05-20 14:00:00', '2026-05-20 14:00:00', '2026-05-20 14:00:00'),
('60000000-0000-0000-0000-000000000002',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 80, 128, 78, 16, 36.9, 98.00, 1, 'STABLE',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- Margaret Osei: VHI=3 latest (SBP≥160+1, RR 18+1, temp 37.8+1) → AMBER
('60000000-0000-0000-0000-000000000003',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 102, 168, 98, 20, 37.8, 96.00, 4, 'WATCH',
 '2026-05-21 10:00:00', '2026-05-21 10:00:00', '2026-05-21 10:00:00'),
('60000000-0000-0000-0000-000000000004',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 94, 162, 95, 18, 37.8, 97.00, 3, 'WATCH',
 '2026-05-22 08:30:00', '2026-05-22 08:30:00', '2026-05-22 08:30:00'),

-- Thomas Brennan: VHI=6 (HR108+1, SBP95+1, RR24+2, temp37.8+1, SpO294+1) → RED
('60000000-0000-0000-0000-000000000005',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 112, 92, 60, 26, 38.6, 93.00, 9, 'CRITICAL',
 '2026-05-22 06:00:00', '2026-05-22 06:00:00', '2026-05-22 06:00:00'),
('60000000-0000-0000-0000-000000000006',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 108, 95, 62, 24, 37.8, 94.00, 6, 'CRITICAL',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- Fatima Al-Hassan: VHI=0 (all normal) → GREEN
('60000000-0000-0000-0000-000000000007',
 '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 72, 118, 72, 14, 36.8, 99.00, 0, 'STABLE',
 '2026-05-20 14:00:00', '2026-05-20 14:00:00', '2026-05-20 14:00:00'),
('60000000-0000-0000-0000-000000000008',
 '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 76, 114, 70, 14, 36.7, 99.00, 0, 'STABLE',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- Robert Kowalski: VHI=4 (HR106+1, RR22+2, temp38.2+1) → AMBER
('60000000-0000-0000-0000-000000000009',
 '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 106, 158, 92, 22, 38.2, 97.00, 4, 'WATCH',
 '2026-05-21 10:00:00', '2026-05-21 10:00:00', '2026-05-21 10:00:00'),
('60000000-0000-0000-0000-000000000010',
 '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 104, 155, 90, 22, 38.2, 97.00, 4, 'WATCH',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Aisha Mensah: VHI=5 (HR108+1, RR22+2, temp38.6+2) → RED
('60000000-0000-0000-0000-000000000011',
 '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 108, 104, 66, 22, 38.6, 96.00, 5, 'CRITICAL',
 '2026-05-22 07:00:00', '2026-05-22 07:00:00', '2026-05-22 07:00:00'),
('60000000-0000-0000-0000-000000000012',
 '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 108, 104, 66, 22, 38.6, 96.00, 5, 'CRITICAL',
 '2026-05-22 09:30:00', '2026-05-22 09:30:00', '2026-05-22 09:30:00'),

-- David Patel: VHI=12 (HR128+2, SBP84+2, RR28+2, temp39.2+3, SpO290+3) → RED critical
('60000000-0000-0000-0000-000000000013',
 '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 128, 84, 52, 28, 39.2, 90.00, 12, 'CRITICAL',
 '2026-05-21 22:00:00', '2026-05-21 22:00:00', '2026-05-21 22:00:00'),
('60000000-0000-0000-0000-000000000014',
 '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 118, 88, 54, 26, 39.0, 91.00, 12, 'CRITICAL',
 '2026-05-22 06:00:00', '2026-05-22 06:00:00', '2026-05-22 06:00:00'),

-- Susan Clarke: VHI=3 (SBP162+1, RR19+1, temp38.2+1) → AMBER
('60000000-0000-0000-0000-000000000015',
 '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 86, 162, 90, 19, 38.2, 97.00, 3, 'WATCH',
 '2026-05-21 16:00:00', '2026-05-21 16:00:00', '2026-05-21 16:00:00'),
('60000000-0000-0000-0000-000000000016',
 '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 92, 162, 88, 18, 38.2, 97.00, 3, 'WATCH',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00');

-- ============================================================
-- 7. CLINICAL NOTES
-- Notes n03, n07, n09 are AI-generated and confirmed by a doctor
-- ============================================================
INSERT INTO clinical_note (
  id, patient_id, hospital_id, author_id,
  note_type, content, raw_transcription,
  is_ai_generated, confirmed_by_doctor_at, ai_model_used,
  created_at, updated_at
) VALUES

-- n01: James Harrington — Ward Round (manual, Dr Chen)
('70000000-0000-0000-0000-000000000001',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000003',
 'WARD_ROUND_NOTE',
 'S: Patient reports well-controlled blood glucose at home. No hypoglycaemic episodes this admission. Tingling in both feet has improved slightly since admission. No chest pain or shortness of breath.\nO: BP 128/78, HR 80, RR 16, Temp 36.9°C, SpO2 98%. BGL 8.2 mmol/L this morning. Peripheral sensation diminished in both feet bilaterally, more pronounced in left. No peripheral oedema. Wound sites clean.\nA: Type 2 Diabetes Mellitus with peripheral neuropathy — stable and improving. VHI 1 (STABLE).\nP: Continue Metformin 500mg BD. Refer to podiatry for neuropathy assessment. Arrange outpatient endocrinology review in 6 weeks. Discharge planning for tomorrow pending HbA1c result.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 09:30:00', '2026-05-22 09:30:00'),

-- n02: Margaret Osei — Admission Note (manual, Dr Okafor)
('70000000-0000-0000-0000-000000000002',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000004',
 'ADMISSION_NOTE',
 'S: 75-year-old female brought in by ambulance with 6-hour history of severe occipital headache, confusion, and oliguria. BP on arrival 212/118. No prior episodes. Known hypertensive on Ramipril, known CKD stage 2. No chest pain. No focal neurology.\nO: BP 168/98 on admission (now improving), HR 102, RR 20, Temp 37.8°C, SpO2 96%. GCS 14/15 (confused, E4V4M6). Creatinine 184 μmol/L (baseline 90 three months ago). Urine output 20 ml/hr. CT head: no haemorrhage or infarct. ECG: sinus tachycardia.\nA: Hypertensive crisis with acute kidney injury stage 1. Likely hypertensive nephropathy exacerbation. No evidence of hypertensive encephalopathy on CT.\nP: Amlodipine 10mg OD commenced. Furosemide 40mg OD commenced. Strict fluid balance charting. Hourly urine output monitoring. Repeat U&Es at 18:00. Renal team review requested. Ophthalmology for fundoscopy tomorrow.',
 NULL, FALSE, NULL, NULL,
 '2026-05-21 03:00:00', '2026-05-21 03:00:00'),

-- n03: Thomas Brennan — Ward Round, AI-generated + confirmed (Dr Chen)
-- This note is linked to prescriptions rx04 (Salbutamol) and rx05 (Prednisolone)
('70000000-0000-0000-0000-000000000003',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000003',
 'WARD_ROUND_NOTE',
 '{"subjective":"68-year-old male with known COPD Gold Stage III admitted 6 hours ago with worsening dyspnoea over 3 days, productive cough with purulent green sputum, and reduced exercise tolerance. Using accessory muscles. Previous admission for COPD exacerbation 4 months ago. Currently on home nebulisers. No fever at home. Ex-smoker 30 pack-years.","objective":"HR 108, BP 95/62, RR 24, Temp 37.8°C, SpO2 94% on 2L O2. Widespread bilateral expiratory wheeze. Reduced air entry at both bases. ABG: pH 7.34, pCO2 52 mmHg, pO2 68 mmHg on 2L O2 — type 2 respiratory failure. CXR: hyperinflation, no consolidation, no pneumothorax. WBC 14.2, CRP 68.","assessment":"Acute exacerbation of COPD with type 2 respiratory failure. VHI 6 (CRITICAL). Likely infective trigger given purulent sputum. No pneumonia on CXR. INR 1.1.","plan":"Controlled O2 via 28% Venturi mask — target SpO2 88-92%. Salbutamol 2.5mg nebulised every 4 hours. Ipratropium 500mcg nebulised QDS. Prednisolone 40mg PO OD for 5 days. Doxycycline 200mg loading dose then 100mg OD (penicillin-allergic on notes — morphine allergy, not penicillin; double-check). Repeat ABG in 90 minutes. ITU/HDU review if pCO2 rises above 60 or GCS falls. Physiotherapy referral."}',
 'Thomas is a 68-year-old male with known COPD. He came in 6 hours ago with worsening breathing for 3 days, green sputum, and he is using his neck muscles to breathe. His blood gas shows type 2 respiratory failure. HR 108, BP 95 over 62, RR 24, saturations 94 on 2 litres. He needs Salbutamol nebs every 4 hours and a 5-day course of Prednisolone 40 milligrams. Repeat gas in 90 minutes. ITU to review if he worsens.',
 TRUE, '2026-05-22 09:15:00', 'careround-ai-v1',
 '2026-05-22 09:00:00', '2026-05-22 09:15:00'),

-- n04: Fatima Al-Hassan — Ward Round (manual, Dr Okafor)
('70000000-0000-0000-0000-000000000004',
 '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000004',
 'WARD_ROUND_NOTE',
 'S: Post-operative day 3 following elective laparoscopic cholecystectomy. Patient reports mild port-site discomfort, well-controlled with regular paracetamol. Passing flatus, tolerating light diet. No nausea or vomiting. Mobilising independently.\nO: BP 114/70, HR 76, RR 14, Temp 36.7°C, SpO2 99%. Abdomen soft, port sites clean and dry, no signs of infection. Bowel sounds present.\nA: Uncomplicated post-operative recovery. VHI 0 (STABLE).\nP: Continue Paracetamol 1g QDS for analgesia. Diet as tolerated. Discharge planned for 2026-05-23 with outpatient surgical review at 2 weeks.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 09:45:00', '2026-05-22 09:45:00'),

-- n05: Robert Kowalski — Ward Round (manual, Dr Chen)
('70000000-0000-0000-0000-000000000005',
 '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000003',
 'WARD_ROUND_NOTE',
 'S: Day 2 post-transfer from St Mary''s for right hip wound infection. Patient reports wound pain 4/10 with antibiotics. No systemic symptoms. Blood glucose 9.8 mmol/L this morning — slightly elevated.\nO: HR 104, BP 155/90, RR 22, Temp 38.2°C, SpO2 97%. Right hip wound: 3cm area of erythema, wound edge moderately inflamed, no tracking, no crepitus. Swab sent on admission. WBC 16.8, CRP 142 (down from 210 on admission).\nA: Post-operative surgical site infection — responding to antibiotics. VHI 4 (WATCH). CRP trending down.\nP: Continue Co-amoxiclav 625mg PO TDS (cephalosporin allergy — avoid cefuroxime). Wound swab result pending — change to narrow spectrum if sensitivities allow. Maintain strict BGL monitoring. Microbiology review of sensitivities tomorrow.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 10:00:00', '2026-05-22 10:00:00'),

-- n06: Aisha Mensah — Admission Note, AI-generated + confirmed (Dr Okafor)
('70000000-0000-0000-0000-000000000006',
 '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000004',
 'ADMISSION_NOTE',
 '{"subjective":"35-year-old female, no significant past medical history. Presented with 18-hour history of central abdominal pain migrating to right iliac fossa, anorexia, and nausea. Vomiting x2 this morning. No diarrhoea. LMP 2 weeks ago. Allergy to Latex and Ibuprofen — documented and wristband applied.","objective":"HR 108, BP 104/66, RR 22, Temp 38.6°C, SpO2 96%. Abdomen: guarding and rebound tenderness in right iliac fossa, Rovsing sign positive. WBC 18.4, CRP 112. CT abdomen/pelvis: inflamed appendix with periappendiceal fat stranding, no perforation. Beta-hCG negative.","assessment":"Acute appendicitis — non-perforated. Emergency appendicectomy performed at 05:30 under GA. Post-operative day 1. VHI 5 (CRITICAL) — likely reflecting post-operative physiological response.","plan":"Metronidazole 500mg IV TDS for 5 days (latex-free gloves and IV giving set in use). Regular paracetamol and ibuprofen alternatives — avoid ibuprofen. Monitor for signs of post-operative complications. NBM to soft diet as tolerated. Mobilise with physiotherapy today."}',
 'Aisha is a 35-year-old female with no past history. She came in last night with right iliac fossa pain and vomiting. CT confirmed acute appendicitis, no perforation. She went to theatre at half past five this morning and had a laparoscopic appendicectomy. She is allergic to latex and ibuprofen — both documented. Post-op day one. She needs Metronidazole 500 milligrams IV three times daily. Keep her on regular paracetamol instead of NSAIDs.',
 TRUE, '2026-05-22 08:00:00', 'careround-ai-v1',
 '2026-05-22 07:45:00', '2026-05-22 08:00:00'),

-- n07: David Patel — Ward Round, AI-generated + confirmed (Dr Chen)
-- This note is linked to prescription rx09 (Pip-Tazo)
('70000000-0000-0000-0000-000000000007',
 '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000003',
 'WARD_ROUND_NOTE',
 '{"subjective":"84-year-old male admitted yesterday evening in septic shock. Background of CCF NYHA III, AF on warfarin, hypertension. Presented with 2-day history of productive cough, high fever, and acute-on-chronic confusion. Brought in by family. INR on admission 2.8. Allergy to Vancomycin — RAST confirmed.","objective":"HR 118, BP 88/54, RR 26, Temp 39.0°C, SpO2 91% on 10L non-rebreathe. GCS 11/15. Sputum: mucopurulent. CXR: right lower lobe consolidation. Lactate 4.2 mmol/L on admission (now 2.8). WBC 22.4, CRP 310, procalcitonin 42. Blood cultures x2 sent. On noradrenaline 0.08 mcg/kg/min via central line. Urine output 28 ml/hr last 4 hours.","assessment":"Septic shock secondary to community-acquired pneumonia — right lower lobe. Sepsis-3 criteria met. Known CCF complicating fluid resuscitation. Warfarin held. VHI 12 (CRITICAL).","plan":"Continue Piperacillin-Tazobactam 4.5g IV every 6 hours (vancomycin-allergic — avoid). Add Clarithromycin 500mg IV BD for atypical cover. Target MAP >65 with vasopressors. Fluid resuscitation cautiously given CCF — 250ml boluses with reassessment. Foley catheter — hourly urine output. ICU consultant review at 14:00. Hold warfarin — INR monitoring daily. Cardiology input re CCF management."}',
 'David is an 84-year-old male admitted last night in septic shock from a right lower lobe pneumonia. He has got congestive heart failure, AF on warfarin, and he is allergic to Vancomycin. His lactate was 4.2 on admission, now improving to 2.8. He is on noradrenaline centrally. CXR shows right lower lobe consolidation. We need to continue Pip-Tazo 4.5 grams IV every 6 hours — he cannot have vancomycin. Add Clarithromycin 500mg IV twice daily. ICU review at 2 this afternoon.',
 TRUE, '2026-05-22 07:00:00', 'careround-ai-v1',
 '2026-05-22 06:45:00', '2026-05-22 07:00:00'),

-- n08: Susan Clarke — Ward Round (manual, Dr Okafor)
('70000000-0000-0000-0000-000000000008',
 '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000004',
 'WARD_ROUND_NOTE',
 'S: 65-year-old female transferred from Royal Victoria Hospital following RTA with severe TBI. GCS on transfer 9/15, now 11/15 — marginal improvement. Known hypertension. Allergy to codeine. On dexamethasone for cerebral oedema.\nO: HR 92, BP 162/88, RR 18, Temp 38.2°C, SpO2 97% on 2L O2. GCS 11/15 (E3V3M5). Pupils: left 3mm reactive, right 3mm reactive. CT head (from referring hospital): right frontal contusion, midline shift 4mm. Repeat CT today shows stable appearance — no new haemorrhage.\nA: Severe TBI — right frontal contusion with 4mm midline shift, stable on repeat imaging. Vasogenic cerebral oedema. VHI 3 (WATCH). GCS trending up.\nP: Continue Dexamethasone 4mg IV QDS — review need at 48 hours. Maintain head of bed at 30°. Target SBP 120-160 — current at upper acceptable limit. Physiotherapy for passive limb movement. Neuro HDU monitoring. Neurosurgery review if midline shift increases.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 09:15:00', '2026-05-22 09:15:00'),

-- n09: James Harrington — Progress Note (manual, Dr Chen)
('70000000-0000-0000-0000-000000000009',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000003',
 'PROGRESS_NOTE',
 'HbA1c result received: 68 mmol/mol (target <53). Discussed with patient — will need medication review as outpatient. No acute changes to management. Discharge confirmed for tomorrow (2026-05-23) pending morning bloods. District nurse referral placed for ongoing foot care.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 11:00:00', '2026-05-22 11:00:00'),

-- n10: Margaret Osei — Ward Round (manual, Dr Okafor)
('70000000-0000-0000-0000-000000000010',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000004',
 'WARD_ROUND_NOTE',
 'S: Improved from admission. Headache resolving. Less confused — GCS 15 now. Urine output improving, 45 ml/hr last 4 hours.\nO: BP 142/88 (improved), HR 88, RR 16, Temp 37.2°C, SpO2 98%. Creatinine 148 μmol/L (down from 184). U&Es otherwise stable. No oedema.\nA: Hypertensive crisis with AKI stage 1 — improving on treatment. Renal function recovering.\nP: Continue Amlodipine 10mg OD and Furosemide 40mg OD. Fluid balance monitoring daily. Repeat U&Es tomorrow morning. Renal team confirmed follow-up in outpatients in 4 weeks.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 10:30:00', '2026-05-22 10:30:00');

-- ============================================================
-- 8. PRESCRIPTIONS
-- administration_times stored as JSON array of ISO-8601 strings
-- ============================================================
INSERT INTO prescription (
  id, patient_id, hospital_id, clinical_note_id,
  drug_name, dose, route, frequency_string, frequency_hours, total_doses,
  start_time, administration_times,
  confirmed_by_id, confirmed_at, status,
  created_at, updated_at
) VALUES

-- rx01: James Harrington — Metformin 500mg PO BD
('80000000-0000-0000-0000-000000000001',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000001',
 'Metformin', '500mg', 'Oral', 'Twice daily', 12, 14,
 '2026-05-20 08:00:00',
 '["2026-05-20T08:00:00","2026-05-20T20:00:00","2026-05-21T08:00:00","2026-05-21T20:00:00","2026-05-22T08:00:00","2026-05-22T20:00:00","2026-05-23T08:00:00","2026-05-23T20:00:00","2026-05-24T08:00:00","2026-05-24T20:00:00","2026-05-25T08:00:00","2026-05-25T20:00:00","2026-05-26T08:00:00","2026-05-26T20:00:00"]',
 '30000000-0000-0000-0000-000000000003', '2026-05-20 10:30:00', 'ACTIVE',
 '2026-05-20 10:30:00', '2026-05-20 10:30:00'),

-- rx02: Margaret Osei — Amlodipine 10mg PO OD
('80000000-0000-0000-0000-000000000002',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000002',
 'Amlodipine', '10mg', 'Oral', 'Once daily', 24, 7,
 '2026-05-21 08:00:00',
 '["2026-05-21T08:00:00","2026-05-22T08:00:00","2026-05-23T08:00:00","2026-05-24T08:00:00","2026-05-25T08:00:00","2026-05-26T08:00:00","2026-05-27T08:00:00"]',
 '30000000-0000-0000-0000-000000000004', '2026-05-21 03:30:00', 'ACTIVE',
 '2026-05-21 03:30:00', '2026-05-21 03:30:00'),

-- rx03: Margaret Osei — Furosemide 40mg PO OD
('80000000-0000-0000-0000-000000000003',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000002',
 'Furosemide', '40mg', 'Oral', 'Once daily', 24, 7,
 '2026-05-21 09:00:00',
 '["2026-05-21T09:00:00","2026-05-22T09:00:00","2026-05-23T09:00:00","2026-05-24T09:00:00","2026-05-25T09:00:00","2026-05-26T09:00:00","2026-05-27T09:00:00"]',
 '30000000-0000-0000-0000-000000000004', '2026-05-21 03:30:00', 'ACTIVE',
 '2026-05-21 03:30:00', '2026-05-21 03:30:00'),

-- rx04: Thomas Brennan — Salbutamol 2.5mg nebs Q4H (linked to AI-confirmed note)
('80000000-0000-0000-0000-000000000004',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000003',
 'Salbutamol', '2.5mg', 'Nebulised', 'Every 4 hours', 4, 42,
 '2026-05-22 06:00:00',
 '["2026-05-22T06:00:00","2026-05-22T10:00:00","2026-05-22T14:00:00","2026-05-22T18:00:00","2026-05-22T22:00:00","2026-05-23T02:00:00","2026-05-23T06:00:00","2026-05-23T10:00:00","2026-05-23T14:00:00","2026-05-23T18:00:00","2026-05-23T22:00:00","2026-05-24T02:00:00"]',
 '30000000-0000-0000-0000-000000000003', '2026-05-22 09:15:00', 'ACTIVE',
 '2026-05-22 09:15:00', '2026-05-22 09:15:00'),

-- rx05: Thomas Brennan — Prednisolone 40mg PO OD (linked to AI-confirmed note)
('80000000-0000-0000-0000-000000000005',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000003',
 'Prednisolone', '40mg', 'Oral', 'Once daily for 5 days', 24, 5,
 '2026-05-22 08:00:00',
 '["2026-05-22T08:00:00","2026-05-23T08:00:00","2026-05-24T08:00:00","2026-05-25T08:00:00","2026-05-26T08:00:00"]',
 '30000000-0000-0000-0000-000000000003', '2026-05-22 09:15:00', 'ACTIVE',
 '2026-05-22 09:15:00', '2026-05-22 09:15:00'),

-- rx06: Fatima Al-Hassan — Paracetamol 1g PO QDS
('80000000-0000-0000-0000-000000000006',
 '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000004',
 'Paracetamol', '1g', 'Oral', 'Four times daily', 6, 20,
 '2026-05-19 06:00:00',
 '["2026-05-22T06:00:00","2026-05-22T12:00:00","2026-05-22T18:00:00","2026-05-23T00:00:00","2026-05-23T06:00:00","2026-05-23T12:00:00","2026-05-23T18:00:00","2026-05-24T00:00:00","2026-05-24T06:00:00","2026-05-24T12:00:00"]',
 '30000000-0000-0000-0000-000000000004', '2026-05-19 08:00:00', 'ACTIVE',
 '2026-05-19 08:00:00', '2026-05-19 08:00:00'),

-- rx07: Robert Kowalski — Co-amoxiclav 625mg PO TDS
('80000000-0000-0000-0000-000000000007',
 '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000005',
 'Co-amoxiclav', '625mg', 'Oral', 'Three times daily', 8, 21,
 '2026-05-21 08:00:00',
 '["2026-05-21T08:00:00","2026-05-21T16:00:00","2026-05-22T00:00:00","2026-05-22T08:00:00","2026-05-22T16:00:00","2026-05-23T00:00:00","2026-05-23T08:00:00","2026-05-23T16:00:00","2026-05-24T00:00:00","2026-05-24T08:00:00","2026-05-24T16:00:00","2026-05-25T00:00:00","2026-05-25T08:00:00","2026-05-25T16:00:00","2026-05-26T00:00:00","2026-05-26T08:00:00","2026-05-26T16:00:00","2026-05-27T00:00:00","2026-05-27T08:00:00","2026-05-27T16:00:00","2026-05-28T00:00:00"]',
 '30000000-0000-0000-0000-000000000003', '2026-05-20 15:00:00', 'ACTIVE',
 '2026-05-20 15:00:00', '2026-05-20 15:00:00'),

-- rx08: Aisha Mensah — Metronidazole 500mg IV TDS (linked to AI-confirmed note)
('80000000-0000-0000-0000-000000000008',
 '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000006',
 'Metronidazole', '500mg', 'Intravenous', 'Three times daily', 8, 21,
 '2026-05-22 08:00:00',
 '["2026-05-22T08:00:00","2026-05-22T16:00:00","2026-05-23T00:00:00","2026-05-23T08:00:00","2026-05-23T16:00:00","2026-05-24T00:00:00","2026-05-24T08:00:00","2026-05-24T16:00:00","2026-05-25T00:00:00","2026-05-25T08:00:00","2026-05-25T16:00:00","2026-05-26T00:00:00","2026-05-26T08:00:00","2026-05-26T16:00:00","2026-05-27T00:00:00","2026-05-27T08:00:00","2026-05-27T16:00:00","2026-05-28T00:00:00","2026-05-28T08:00:00","2026-05-28T16:00:00","2026-05-29T00:00:00"]',
 '30000000-0000-0000-0000-000000000004', '2026-05-22 08:00:00', 'ACTIVE',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- rx09: David Patel — Piperacillin-Tazobactam 4.5g IV QDS (linked to AI-confirmed note)
('80000000-0000-0000-0000-000000000009',
 '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000007',
 'Piperacillin-Tazobactam', '4.5g', 'Intravenous', 'Every 6 hours', 6, 28,
 '2026-05-21 22:00:00',
 '["2026-05-21T22:00:00","2026-05-22T04:00:00","2026-05-22T10:00:00","2026-05-22T16:00:00","2026-05-22T22:00:00","2026-05-23T04:00:00","2026-05-23T10:00:00","2026-05-23T16:00:00","2026-05-23T22:00:00","2026-05-24T04:00:00","2026-05-24T10:00:00","2026-05-24T16:00:00","2026-05-24T22:00:00","2026-05-25T04:00:00","2026-05-25T10:00:00","2026-05-25T16:00:00","2026-05-25T22:00:00","2026-05-26T04:00:00","2026-05-26T10:00:00","2026-05-26T16:00:00","2026-05-26T22:00:00","2026-05-27T04:00:00","2026-05-27T10:00:00","2026-05-27T16:00:00","2026-05-27T22:00:00","2026-05-28T04:00:00","2026-05-28T10:00:00","2026-05-28T16:00:00"]',
 '30000000-0000-0000-0000-000000000003', '2026-05-22 07:00:00', 'ACTIVE',
 '2026-05-22 07:00:00', '2026-05-22 07:00:00'),

-- rx10: Susan Clarke — Dexamethasone 4mg IV QDS
('80000000-0000-0000-0000-000000000010',
 '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001',
 '70000000-0000-0000-0000-000000000008',
 'Dexamethasone', '4mg', 'Intravenous', 'Every 6 hours', 6, 28,
 '2026-05-22 00:00:00',
 '["2026-05-22T06:00:00","2026-05-22T12:00:00","2026-05-22T18:00:00","2026-05-23T00:00:00","2026-05-23T06:00:00","2026-05-23T12:00:00","2026-05-23T18:00:00","2026-05-24T00:00:00","2026-05-24T06:00:00","2026-05-24T12:00:00","2026-05-24T18:00:00","2026-05-25T00:00:00","2026-05-25T06:00:00","2026-05-25T12:00:00","2026-05-25T18:00:00","2026-05-26T00:00:00","2026-05-26T06:00:00","2026-05-26T12:00:00","2026-05-26T18:00:00","2026-05-27T00:00:00","2026-05-27T06:00:00","2026-05-27T12:00:00","2026-05-27T18:00:00","2026-05-28T00:00:00","2026-05-28T06:00:00","2026-05-28T12:00:00","2026-05-28T18:00:00","2026-05-29T00:00:00"]',
 '30000000-0000-0000-0000-000000000004', '2026-05-22 00:30:00', 'ACTIVE',
 '2026-05-22 00:30:00', '2026-05-22 00:30:00');

-- ============================================================
-- 9. MEDICATION CHARTS (one per prescription)
-- ============================================================
INSERT INTO medication_chart (id, patient_id, hospital_id, prescription_id, status, nurse_notes, created_at, updated_at) VALUES
('90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', 'ACTIVE', 'Patient self-administers. Remind with evening meal.',        '2026-05-20 10:30:00', '2026-05-22 09:00:00'),
('90000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000002', 'ACTIVE', 'Monitor BP before administration. Hold if SBP <100.',        '2026-05-21 03:30:00', '2026-05-22 08:30:00'),
('90000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000003', 'ACTIVE', 'Monitor urine output. Withhold if UO <20ml/hr — contact doctor.', '2026-05-21 03:30:00', '2026-05-22 09:00:00'),
('90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000004', 'ACTIVE', 'Controlled O2 via Venturi 28% during nebs. Observe for tremor or palpitations.', '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000005', 'ACTIVE', 'Give with food or milk to reduce gastric irritation. Monitor BGL.',      '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('90000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000006', 'ACTIVE', 'Regular analgesia — do not delay. Patient tolerating orally.',             '2026-05-19 08:00:00', '2026-05-22 09:00:00'),
('90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000007', 'ACTIVE', 'Give with food. CEPHALOSPORIN ALLERGY on wristband — do not substitute.', '2026-05-20 15:00:00', '2026-05-22 08:00:00'),
('90000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000008', 'ACTIVE', 'LATEX ALLERGY — use latex-free gloves and giving set. Infuse over 20 mins.', '2026-05-22 08:00:00', '2026-05-22 08:00:00'),
('90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000009', 'ACTIVE', 'VANCOMYCIN ALLERGY — confirmed. Central line only. Infuse over 30 mins. Do not delay doses — septic shock patient.', '2026-05-22 07:00:00', '2026-05-22 07:00:00'),
('90000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000010', 'ACTIVE', 'Infuse over 15 mins. Monitor for hyperglycaemia — daily BGL checks.',        '2026-05-22 00:30:00', '2026-05-22 06:00:00');

-- ============================================================
-- 10. MEDICATION TASKS
-- Reference time: 2026-05-22 10:00 (demo "now")
-- OVERDUE = past scheduled time + 5 min, not completed
-- ============================================================
INSERT INTO medication_task (
  id, medication_chart_id, patient_id, hospital_id, ward_id,
  assigned_nurse_id, scheduled_time, status,
  completed_at, completed_by_id, actual_dose_given,
  pre_reminder_sent_at, overdue_alert_sent_at,
  created_at, updated_at
) VALUES

-- Chart 01: Metformin BD — James Harrington (Ward A, nurse Foster)
('a0000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-20 08:00:00', 'COMPLETED', '2026-05-20 08:07:00', '30000000-0000-0000-0000-000000000005', '500mg', NULL, NULL, '2026-05-20 08:00:00', '2026-05-20 08:07:00'),
('a0000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-20 20:00:00', 'COMPLETED', '2026-05-20 20:05:00', '30000000-0000-0000-0000-000000000006', '500mg', NULL, NULL, '2026-05-20 20:00:00', '2026-05-20 20:05:00'),
('a0000000-0000-0000-0000-000000000003', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-21 08:00:00', 'COMPLETED', '2026-05-21 08:04:00', '30000000-0000-0000-0000-000000000005', '500mg', NULL, NULL, '2026-05-21 08:00:00', '2026-05-21 08:04:00'),
('a0000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-21 20:00:00', 'COMPLETED', '2026-05-21 20:09:00', '30000000-0000-0000-0000-000000000006', '500mg', NULL, NULL, '2026-05-21 20:00:00', '2026-05-21 20:09:00'),
('a0000000-0000-0000-0000-000000000005', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:06:00', '30000000-0000-0000-0000-000000000005', '500mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:06:00'),
('a0000000-0000-0000-0000-000000000006', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 20:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Chart 02: Amlodipine OD — Margaret Osei (Ward A, nurse Adeyemi)
('a0000000-0000-0000-0000-000000000007', '90000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-21 08:00:00', 'COMPLETED', '2026-05-21 08:12:00', '30000000-0000-0000-0000-000000000006', '10mg', NULL, NULL, '2026-05-21 08:00:00', '2026-05-21 08:12:00'),
('a0000000-0000-0000-0000-000000000008', '90000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:10:00', '30000000-0000-0000-0000-000000000006', '10mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:10:00'),
('a0000000-0000-0000-0000-000000000009', '90000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-23 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Chart 03: Furosemide OD — Margaret Osei (Ward A, nurse Adeyemi)
('a0000000-0000-0000-0000-000000000010', '90000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-21 09:00:00', 'COMPLETED', '2026-05-21 09:08:00', '30000000-0000-0000-0000-000000000006', '40mg', NULL, NULL, '2026-05-21 09:00:00', '2026-05-21 09:08:00'),
('a0000000-0000-0000-0000-000000000011', '90000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-22 09:00:00', 'COMPLETED', '2026-05-22 09:05:00', '30000000-0000-0000-0000-000000000006', '40mg', NULL, NULL, '2026-05-22 09:00:00', '2026-05-22 09:05:00'),
('a0000000-0000-0000-0000-000000000012', '90000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000006', '2026-05-23 09:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- Chart 04: Salbutamol Q4H — Thomas Brennan (Ward A, nurse Foster)
-- OVERDUE: 10:00 dose not given (demo shows a missed critical dose)
('a0000000-0000-0000-0000-000000000013', '90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 06:00:00', 'COMPLETED', '2026-05-22 06:08:00', '30000000-0000-0000-0000-000000000005', '2.5mg', NULL, NULL, '2026-05-22 06:00:00', '2026-05-22 06:08:00'),
('a0000000-0000-0000-0000-000000000014', '90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 10:00:00', 'OVERDUE', NULL, NULL, NULL, '2026-05-22 09:55:00', '2026-05-22 10:10:00', '2026-05-22 09:15:00', '2026-05-22 10:10:00'),
('a0000000-0000-0000-0000-000000000015', '90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 14:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('a0000000-0000-0000-0000-000000000016', '90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 18:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('a0000000-0000-0000-0000-000000000017', '90000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 22:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),

-- Chart 05: Prednisolone OD — Thomas Brennan (Ward A, nurse Foster)
('a0000000-0000-0000-0000-000000000018', '90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:15:00', '30000000-0000-0000-0000-000000000005', '40mg', NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 08:15:00'),
('a0000000-0000-0000-0000-000000000019', '90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-23 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('a0000000-0000-0000-0000-000000000020', '90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-24 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('a0000000-0000-0000-0000-000000000021', '90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-25 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),
('a0000000-0000-0000-0000-000000000022', '90000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', '2026-05-26 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:15:00', '2026-05-22 09:15:00'),

-- Chart 06: Paracetamol QDS — Fatima Al-Hassan (Ward B, nurse Sharma)
('a0000000-0000-0000-0000-000000000023', '90000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 06:00:00', 'COMPLETED', '2026-05-22 06:10:00', '30000000-0000-0000-0000-000000000007', '1g', NULL, NULL, '2026-05-19 08:00:00', '2026-05-22 06:10:00'),
('a0000000-0000-0000-0000-000000000024', '90000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 12:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-19 08:00:00', '2026-05-22 06:00:00'),
('a0000000-0000-0000-0000-000000000025', '90000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 18:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-19 08:00:00', '2026-05-22 06:00:00'),
('a0000000-0000-0000-0000-000000000026', '90000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-23 00:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-19 08:00:00', '2026-05-22 06:00:00'),

-- Chart 07: Co-amoxiclav TDS — Robert Kowalski (Ward B, nurse Sharma)
('a0000000-0000-0000-0000-000000000027', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-21 08:00:00', 'COMPLETED', '2026-05-21 08:09:00', '30000000-0000-0000-0000-000000000007', '625mg', NULL, NULL, '2026-05-20 15:00:00', '2026-05-21 08:09:00'),
('a0000000-0000-0000-0000-000000000028', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-21 16:00:00', 'COMPLETED', '2026-05-21 16:06:00', '30000000-0000-0000-0000-000000000007', '625mg', NULL, NULL, '2026-05-20 15:00:00', '2026-05-21 16:06:00'),
('a0000000-0000-0000-0000-000000000029', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 00:00:00', 'COMPLETED', '2026-05-22 00:04:00', '30000000-0000-0000-0000-000000000007', '625mg', NULL, NULL, '2026-05-20 15:00:00', '2026-05-22 00:04:00'),
('a0000000-0000-0000-0000-000000000030', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:11:00', '30000000-0000-0000-0000-000000000007', '625mg', NULL, NULL, '2026-05-20 15:00:00', '2026-05-22 08:11:00'),
('a0000000-0000-0000-0000-000000000031', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 16:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-20 15:00:00', '2026-05-22 08:00:00'),
('a0000000-0000-0000-0000-000000000032', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-23 00:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-20 15:00:00', '2026-05-22 08:00:00'),
('a0000000-0000-0000-0000-000000000033', '90000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-23 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-20 15:00:00', '2026-05-22 08:00:00'),

-- Chart 08: Metronidazole IV TDS — Aisha Mensah (Ward B, nurse Sharma)
('a0000000-0000-0000-0000-000000000034', '90000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:20:00', '30000000-0000-0000-0000-000000000007', '500mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:20:00'),
('a0000000-0000-0000-0000-000000000035', '90000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-22 16:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),
('a0000000-0000-0000-0000-000000000036', '90000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-23 00:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),
('a0000000-0000-0000-0000-000000000037', '90000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000007', '2026-05-23 08:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Chart 09: Pip-Tazo QDS — David Patel (ICU, nurse Adeyemi)
-- OVERDUE: 10:00 dose not given (critical septic shock patient — urgent!)
('a0000000-0000-0000-0000-000000000038', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-21 22:00:00', 'COMPLETED', '2026-05-21 22:05:00', '30000000-0000-0000-0000-000000000006', '4.5g', NULL, NULL, '2026-05-22 07:00:00', '2026-05-21 22:05:00'),
('a0000000-0000-0000-0000-000000000039', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 04:00:00', 'COMPLETED', '2026-05-22 04:08:00', '30000000-0000-0000-0000-000000000006', '4.5g', NULL, NULL, '2026-05-22 07:00:00', '2026-05-22 04:08:00'),
('a0000000-0000-0000-0000-000000000040', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 10:00:00', 'OVERDUE', NULL, NULL, NULL, '2026-05-22 09:55:00', '2026-05-22 10:10:00', '2026-05-22 07:00:00', '2026-05-22 10:10:00'),
('a0000000-0000-0000-0000-000000000041', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 16:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 07:00:00', '2026-05-22 07:00:00'),
('a0000000-0000-0000-0000-000000000042', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 22:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 07:00:00', '2026-05-22 07:00:00'),
('a0000000-0000-0000-0000-000000000043', '90000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-23 04:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 07:00:00', '2026-05-22 07:00:00'),

-- Chart 10: Dexamethasone QDS — Susan Clarke (ICU, nurse Adeyemi)
('a0000000-0000-0000-0000-000000000044', '90000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 06:00:00', 'COMPLETED', '2026-05-22 06:12:00', '30000000-0000-0000-0000-000000000006', '4mg', NULL, NULL, '2026-05-22 00:30:00', '2026-05-22 06:12:00'),
('a0000000-0000-0000-0000-000000000045', '90000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 12:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 00:30:00', '2026-05-22 06:00:00'),
('a0000000-0000-0000-0000-000000000046', '90000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-22 18:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 00:30:00', '2026-05-22 06:00:00'),
('a0000000-0000-0000-0000-000000000047', '90000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006', '2026-05-23 00:00:00', 'PENDING', NULL, NULL, NULL, NULL, NULL, '2026-05-22 00:30:00', '2026-05-22 06:00:00');

-- ============================================================
-- 11. HANDOVER NOTES
-- ============================================================
INSERT INTO handover_note (id, patient_id, hospital_id, author_id, content, created_at, updated_at) VALUES

('b0000000-0000-0000-0000-000000000001',
 '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 'James Harrington, Bed A1. Stable throughout shift. BGL 8.2 this morning. Morning Metformin given at 08:06. Patient mobile and self-caring. Awaiting HbA1c result for discharge planning. Evening Metformin due 20:00. Podiatry referral submitted.',
 '2026-05-22 07:30:00', '2026-05-22 07:30:00'),

('b0000000-0000-0000-0000-000000000002',
 '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 'Margaret Osei, Bed A2. BP improving — 142/88 at 08:00 (was 168/98 on admission). UO 45ml/hr last 4 hours — improving. Amlodipine and Furosemide both given this morning. Creatinine trending down to 148. Renal team reviewed yesterday, follow-up in outpatients arranged. Strict fluid balance ongoing. U&Es repeat due this evening.',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

('b0000000-0000-0000-0000-000000000003',
 '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000005',
 'Thomas Brennan, Bed A3. RED — COPD exacerbation with type 2 respiratory failure. SpO2 94% on 28% Venturi — do NOT increase O2 without doctor review (hypercapnic driver). 06:00 Salbutamol nebs given. IMPORTANT: 10:00 Salbutamol dose OVERDUE — patient requesting it, please action immediately. Prednisolone 40mg given at 08:15 with breakfast. ABG being repeated by Dr Chen at 10:30. ITU/HDU aware.',
 '2026-05-22 09:50:00', '2026-05-22 09:50:00'),

('b0000000-0000-0000-0000-000000000004',
 '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 'Fatima Al-Hassan, Bed B1. Post-op day 3 laparoscopic cholecystectomy. Progressing well. Port sites clean. Tolerating soft diet. Mobilising independently around ward. Paracetamol 1g given at 06:10. Next dose 12:00 noon. Pain 2/10 at rest. Discharge planned for tomorrow — take-home analgesia to be prescribed.',
 '2026-05-22 07:00:00', '2026-05-22 07:00:00'),

('b0000000-0000-0000-0000-000000000005',
 '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 'Robert Kowalski, Bed B2. Post-op wound infection — AMBER. CEPHALOSPORIN ALLERGY on wristband. Co-amoxiclav 625mg given 08:11. Next IV dose 16:00. Wound erythema slightly improved from yesterday. Wound swab result still pending from microbiology — chase today. BGL 9.8 this morning — slightly elevated, continue monitoring. Pain 4/10.',
 '2026-05-22 08:30:00', '2026-05-22 08:30:00'),

('b0000000-0000-0000-0000-000000000006',
 '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000007',
 'Aisha Mensah, Bed B3. Post-op day 1 appendicectomy — RED. LATEX ALLERGY and IBUPROFEN ALLERGY — wristband checked, latex-free equipment in use throughout. Metronidazole 500mg IV given at 08:20 (latex-free giving set used). Next dose 16:00. No ibuprofen or NSAIDs to be given. Pain 5/10 — paracetamol only for analgesia. Passing flatus. Tolerating sips of water.',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

('b0000000-0000-0000-0000-000000000007',
 '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 'David Patel, ICU Bed IC1. CRITICAL — septic shock. VANCOMYCIN ALLERGY confirmed on central line label and wristband. Noradrenaline 0.08 mcg/kg/min via right CVC — MAP 68 at last check. Lactate improving: 2.8 (was 4.2). Pip-Tazo 4.5g given at 22:05 and 04:08. URGENT: 10:00 dose OVERDUE — please administer immediately via central line only. UO 28ml/hr last 4 hours — below target, discuss with ICU consultant. Warfarin held. INR 2.8. Blood cultures x2 pending.',
 '2026-05-22 06:30:00', '2026-05-22 06:30:00'),

('b0000000-0000-0000-0000-000000000008',
 '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001',
 '30000000-0000-0000-0000-000000000006',
 'Susan Clarke, ICU Bed IC2. AMBER — severe TBI, right frontal contusion. GCS improved 9 on transfer to 11 today. CODEINE ALLERGY — wristband checked. Dexamethasone 4mg IV given at 06:12. Next dose 12:00. Head of bed maintained at 30°. Repeat CT head this morning — stable, no new haemorrhage. Neurosurgery aware. BP 162/88 at 09:00 — at upper acceptable limit, discuss with Dr Okafor. Passive limb physio completed.',
 '2026-05-22 07:00:00', '2026-05-22 07:00:00');

-- ============================================================
-- HOSPITAL 2: Lagos Island General Hospital (LIGH)
-- Nigerian clinical scenarios
-- ============================================================

-- ============================================================
-- 1b. HOSPITAL
-- ============================================================
INSERT INTO hospital (id, name, code, address, contact_email, contact_phone, is_active, created_at, updated_at) VALUES
('10000000-0000-0000-0000-000000000002',
 'Lagos Island General Hospital', 'LIGH',
 '1 Hospital Road, Lagos Island, Lagos, Nigeria',
 'info@ligh.gov.ng', '+234 1 460 3509',
 TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 2b. SYSTEM CONFIGURATION
-- ============================================================
INSERT INTO system_configuration (id, hospital_id, task_overdue_reminder_minutes, task_escalation_minutes, push_notifications_enabled, created_at, updated_at) VALUES
('20000000-0000-0000-0000-000000000002',
 '10000000-0000-0000-0000-000000000002',
 15, 30, TRUE,
 '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 3b. USERS  (password: Password123)
-- ============================================================
INSERT INTO users (id, hospital_id, first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
('30000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000002',
 'Chioma', 'Okonkwo', 'admin@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'ADMIN', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 'Adaeze', 'Nwosu', 'a.nwosu@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'SUPERVISOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 'Emeka', 'Obi', 'e.obi@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'DOCTOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 'Fatimah', 'Abubakar', 'f.abubakar@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'DOCTOR', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 'Ngozi', 'Eze', 'n.eze@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 'Chukwudi', 'Obiora', 'c.obiora@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('30000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 'Aminat', 'Balogun', 'a.balogun@ligh.gov.ng',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'NURSE', TRUE, '2026-01-01 09:00:00', '2026-01-01 09:00:00');

-- Platform admin (sentinel hospitalId, not a tenant)
INSERT INTO users (id, hospital_id, first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
('f0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000000',
 'Platform', 'Admin', 'admin@careround.com',
 '$2a$10$cBQPTiKg7gtasDFZ1HLo8OWT.HsIGrPQhn526wOWJpVDSx.g7WnWG',
 'PLATFORM_ADMIN', TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 4b. WARDS
-- ============================================================
INSERT INTO ward (id, hospital_id, name, specialty, total_beds, is_active, created_at, updated_at) VALUES
('40000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'Medical Ward',  'General Medicine', 12, TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00'),
('40000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002', 'Surgical Ward', 'Surgery',          10, TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00'),
('40000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000002', 'HDU',           'High Dependency',   6, TRUE, '2026-01-01 08:00:00', '2026-01-01 08:00:00');

-- ============================================================
-- 5b. PATIENTS
-- ============================================================
INSERT INTO patient (
  id, hospital_id, ward_id, bed_number,
  first_name, last_name, date_of_birth, gender,
  hospital_number, phone_number, address,
  previous_conditions, current_medications, allergies,
  emergency_contact_name, emergency_contact_phone, registered_by_id,
  admission_date, admission_type, primary_diagnosis,
  acuity_color, status, estimated_discharge_date,
  created_at, updated_at
) VALUES

-- Medical Ward
('50000000-0000-0000-0000-000000000009',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', 'M1',
 'Babatunde', 'Adeyemi', '1978-06-12', 'MALE',
 'LIGH-2026-001', '+234 803 456 7890', '14 Broad Street, Lagos Island, Lagos',
 'Hypertension',
 'Amlodipine 5mg OD',
 'None known',
 'Funke Adeyemi', '+234 802 345 6789',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-20 09:00:00', 'EMERGENCY',
 'Severe falciparum malaria with haematological complications',
 'AMBER', 'ADMITTED', NULL,
 '2026-05-20 09:00:00', '2026-05-22 08:00:00'),

('50000000-0000-0000-0000-00000000000a',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', 'M2',
 'Sade', 'Oyelaran', '1996-03-22', 'FEMALE',
 'LIGH-2026-002', '+234 806 789 0123', '7 Catholic Mission Street, Lagos Island, Lagos',
 'Sickle Cell Disease (HbSS)',
 'Hydroxyurea 500mg OD, Folic Acid 5mg OD, Phenoxymethylpenicillin 250mg BD',
 'NSAIDs, Aspirin',
 'Bola Oyelaran', '+234 805 678 9012',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-21 03:30:00', 'EMERGENCY',
 'Sickle cell vaso-occlusive crisis with acute chest syndrome',
 'RED', 'ADMITTED', NULL,
 '2026-05-21 03:30:00', '2026-05-22 07:00:00'),

('50000000-0000-0000-0000-00000000000b',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', 'M3',
 'Musa', 'Garba', '1955-11-04', 'MALE',
 'LIGH-2026-003', '+234 811 234 5678', '22 Nnamdi Azikiwe Street, Lagos Island, Lagos',
 'Hypertension (20 years), Type 2 Diabetes Mellitus, CKD Stage 3',
 'Lisinopril 10mg OD, Metformin 500mg BD',
 'Aspirin',
 'Maimuna Garba', '+234 810 123 4567',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-21 10:00:00', 'ELECTIVE',
 'Hypertensive heart disease with decompensated cardiac failure',
 'GREEN', 'ADMITTED', '2026-05-25',
 '2026-05-21 10:00:00', '2026-05-22 09:00:00'),

-- Surgical Ward
('50000000-0000-0000-0000-00000000000c',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', 'S1',
 'Amaka', 'Igwe', '1990-08-15', 'FEMALE',
 'LIGH-2026-004', '+234 808 901 2345', '5 Marina Road, Lagos Island, Lagos',
 'Nil significant',
 'None',
 'None known',
 'Chidi Igwe', '+234 807 890 1234',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-20 22:00:00', 'EMERGENCY',
 'Typhoid intestinal perforation — emergency exploratory laparotomy, post-operative day 2',
 'RED', 'ADMITTED', NULL,
 '2026-05-20 22:00:00', '2026-05-22 06:00:00'),

('50000000-0000-0000-0000-00000000000d',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', 'S2',
 'Tunde', 'Olatunji', '1967-04-18', 'MALE',
 'LIGH-2026-005', '+234 802 567 8901', '33 Kakawa Street, Lagos Island, Lagos',
 'Type 2 Diabetes Mellitus (15 years, poorly controlled), Hypertension',
 'Glibenclamide 5mg BD, Metformin 1g BD, Amlodipine 10mg OD',
 'Penicillin',
 'Bukola Olatunji', '+234 801 456 7890',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-20 11:00:00', 'EMERGENCY',
 'Diabetic foot with wet gangrene — right 4th and 5th toes, below-knee amputation planned',
 'AMBER', 'ADMITTED', NULL,
 '2026-05-20 11:00:00', '2026-05-22 08:30:00'),

-- HDU
('50000000-0000-0000-0000-00000000000e',
 '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', 'H1',
 'Aisha', 'Mohammed', '1988-12-01', 'FEMALE',
 'LIGH-2026-006', '+234 809 012 3456', '9 Igbosere Road, Lagos Island, Lagos',
 'G2P1, Gestational hypertension',
 'None',
 'Vancomycin',
 'Ibrahim Mohammed', '+234 808 901 2346',
 '30000000-0000-0000-0000-000000000008',
 '2026-05-20 14:00:00', 'EMERGENCY',
 'Peripartum sepsis with septic shock — wound infection following emergency caesarean section',
 'RED', 'ADMITTED', NULL,
 '2026-05-20 14:00:00', '2026-05-22 06:00:00');

-- ============================================================
-- 6b. PATIENT VITALS
-- VHI thresholds: HR ≥101→1, ≥111→2, ≥130→3
--                 SBP ≤100/≥160→1, ≤90/≥200→2, ≤80→3
--                 RR ≥15→1, ≥21→2, ≥30→3
--                 Temp 37.5-38.4→1, ≥38.5→2, ≥39→3
--                 SpO2 ≤95→1, ≤93→2, ≤91→3
-- ============================================================
INSERT INTO patient_vitals (
  id, patient_id, hospital_id, recorded_by_id,
  pulse, systolic_bp, diastolic_bp, respiratory_rate, temperature, spo2,
  vhi_score, vhi_status, recorded_at, created_at, updated_at
) VALUES

-- Babatunde Adeyemi — malaria, AMBER: HR104+1, RR18+1, Temp38.6+2 = VHI 4 WATCH
('60000000-0000-0000-0000-000000000017',
 '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 104, 110, 70, 18, 38.6, 97.00, 4, 'WATCH',
 '2026-05-20 11:00:00', '2026-05-20 11:00:00', '2026-05-20 11:00:00'),
('60000000-0000-0000-0000-000000000018',
 '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 98, 112, 72, 18, 38.5, 97.00, 3, 'WATCH',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Sade Oyelaran — SCD VOC + ACS, RED: HR118+2, RR24+2, Temp38.8+2, SpO294+1 = VHI 7 CRITICAL
('60000000-0000-0000-0000-000000000019',
 '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 118, 108, 68, 24, 38.8, 94.00, 7, 'CRITICAL',
 '2026-05-21 05:00:00', '2026-05-21 05:00:00', '2026-05-21 05:00:00'),
('60000000-0000-0000-0000-000000000020',
 '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 112, 106, 66, 22, 38.6, 95.00, 7, 'CRITICAL',
 '2026-05-22 07:00:00', '2026-05-22 07:00:00', '2026-05-22 07:00:00'),

-- Musa Garba — hypertensive HF, GREEN: RR16+1 = VHI 1 STABLE
('60000000-0000-0000-0000-000000000021',
 '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 82, 148, 90, 16, 36.8, 96.00, 1, 'STABLE',
 '2026-05-21 12:00:00', '2026-05-21 12:00:00', '2026-05-21 12:00:00'),
('60000000-0000-0000-0000-000000000022',
 '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 78, 145, 88, 15, 36.9, 97.00, 1, 'STABLE',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- Amaka Igwe — typhoid perf post-op, RED: HR122+2, SBP96+1, RR26+2, Temp39.4+3, SpO293+2 = VHI 10 CRITICAL
('60000000-0000-0000-0000-000000000023',
 '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000e',
 122, 96, 58, 26, 39.4, 93.00, 10, 'CRITICAL',
 '2026-05-21 06:00:00', '2026-05-21 06:00:00', '2026-05-21 06:00:00'),
('60000000-0000-0000-0000-000000000024',
 '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000e',
 110, 100, 62, 22, 38.8, 94.00, 7, 'CRITICAL',
 '2026-05-22 06:00:00', '2026-05-22 06:00:00', '2026-05-22 06:00:00'),

-- Tunde Olatunji — diabetic foot, AMBER: HR104+1, SBP162+1, RR18+1, Temp38.4+1 = VHI 4 WATCH
('60000000-0000-0000-0000-000000000025',
 '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 104, 162, 96, 18, 38.4, 97.00, 4, 'WATCH',
 '2026-05-20 13:00:00', '2026-05-20 13:00:00', '2026-05-20 13:00:00'),
('60000000-0000-0000-0000-000000000026',
 '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 102, 158, 94, 17, 38.2, 97.00, 3, 'WATCH',
 '2026-05-22 08:30:00', '2026-05-22 08:30:00', '2026-05-22 08:30:00'),

-- Aisha Mohammed — peripartum septic shock, RED: HR128+2, SBP88+2, RR28+2, Temp39.6+3, SpO292+2 = VHI 11 CRITICAL
('60000000-0000-0000-0000-000000000027',
 '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 128, 88, 52, 28, 39.6, 92.00, 11, 'CRITICAL',
 '2026-05-20 15:00:00', '2026-05-20 15:00:00', '2026-05-20 15:00:00'),
('60000000-0000-0000-0000-000000000028',
 '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 118, 92, 56, 26, 39.2, 92.00, 10, 'CRITICAL',
 '2026-05-22 06:00:00', '2026-05-22 06:00:00', '2026-05-22 06:00:00');

-- ============================================================
-- 7b. CLINICAL NOTES
-- Notes n13, n14, n16 are AI-generated and confirmed by a doctor
-- ============================================================
INSERT INTO clinical_note (
  id, patient_id, hospital_id, author_id,
  note_type, content, raw_transcription,
  is_ai_generated, confirmed_by_doctor_at, ai_model_used,
  created_at, updated_at
) VALUES

-- n11: Babatunde Adeyemi — Ward Round (manual, Dr Obi)
('70000000-0000-0000-0000-000000000011',
 '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000a',
 'WARD_ROUND_NOTE',
 'S: 47-year-old male, known hypertensive, presenting day 2 of admission with fever, rigors, and generalised body aches for 4 days before admission. Lives on Lagos Island. No travel history outside Lagos. Reports previous malaria episodes. On Amlodipine 5mg OD. No drug allergies.\nO: HR 98, BP 112/72, RR 18, Temp 38.5°C, SpO2 97%. Febrile, mildly pale, no jaundice, no neck stiffness. Splenomegaly — 3 cm below left costal margin. RDT: Plasmodium falciparum positive (3+). Hb 8.4 g/dL (admission 7.8). Platelets 62 × 10⁹/L. Parasite density 2.4%. LFTs mildly deranged — ALT 68, bilirubin 32. Creatinine 98 (normal). Urinalysis: normal.\nA: Severe falciparum malaria with haematological complications — significant anaemia (Hb 7.8 on admission, improving to 8.4) and thrombocytopaenia. VHI 3 (WATCH).\nP: Continue IV Artesunate 2.4mg/kg OD — day 2 of 7. Continue Amlodipine for hypertension. Blood transfusion indicated if Hb falls below 7.0 — monitor closely. Repeat FBC and malaria film in 48 hours. Strict fluid balance. Nurse in side bay — malaria not directly communicable but isolation for monitoring purposes.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

-- n12: Sade Oyelaran — Admission Note (manual, Dr Abubakar)
('70000000-0000-0000-0000-000000000012',
 '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000b',
 'ADMISSION_NOTE',
 'S: 30-year-old female with known HbSS sickle cell disease presenting at 03:30 with severe bilateral chest pain 9/10, dyspnoea, and generalised bone pain in arms and legs over the preceding 12 hours. Reports recent upper respiratory tract infection 1 week ago. Last crisis 4 months ago requiring hospitalisation for 3 days. Current medications: Hydroxyurea 500mg OD, Folic Acid 5mg OD, Phenoxymethylpenicillin 250mg BD. Allergy to NSAIDs and Aspirin — documented and wristband applied. No recent sickling triggers identified beyond the URTI.\nO: HR 118, BP 108/68, RR 24, Temp 38.8°C, SpO2 94% on room air. Distressed, pale (conjunctivae), icteric. Chest: reduced air entry bilateral bases, dullness to percussion at right base — possible consolidation vs pleural effusion. Hands and feet: dactylitis. Hb 6.9 g/dL (baseline 8.2). WBC 14.8 (likely stress demargination). CXR: new right lower lobe infiltrate consistent with acute chest syndrome.\nA: Sickle cell vaso-occlusive crisis complicated by acute chest syndrome — right lower lobe. VHI 7 (CRITICAL). Life-threatening complication requiring urgent management.\nP: Supplemental O2 via face mask — target SpO2 ≥95%. IV Morphine 5mg every 4 hours — do NOT use NSAIDs (allergy and contraindicated in SCD). IV fluids — 0.9% NaCl at 80ml/hr. Exchange transfusion assessment — haematology team review urgently. Ceftriaxone 1g IV BD for community-acquired pneumonia cover (NOT Aspirin — allergy; cephalosporin safe as there is no cross-reactivity with the NSAIDs allergy). Incentive spirometry. Strict monitoring of SpO2 and respiratory rate. Repeat CXR at 24 hours.',
 NULL, FALSE, NULL, NULL,
 '2026-05-21 04:30:00', '2026-05-21 04:30:00'),

-- n13: Musa Garba — Ward Round, AI-generated + confirmed (Dr Obi)
('70000000-0000-0000-0000-000000000013',
 '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000a',
 'WARD_ROUND_NOTE',
 '{"subjective":"70-year-old male with 20-year history of hypertension, Type 2 DM, and CKD stage 3 admitted electively for optimisation of decompensated cardiac failure. Reports progressive dyspnoea on exertion over 3 weeks, orthopnoea, and bilateral ankle swelling. No chest pain. Aspirin allergy — documented.","objective":"HR 78, BP 145/88, RR 15, Temp 36.9°C, SpO2 97% on room air. Alert, mildly dyspnoeic at rest. JVP raised at 4 cm. Bilateral pitting oedema to mid-shin. Fine bibasal crepitations on auscultation. CXR: cardiomegaly, bilateral pleural effusions, upper lobe venous diversion. Echo (outpatient, 3 months ago): EF 35%, concentric LV hypertrophy. BNP 1240 pg/mL. Creatinine 148 μmol/L (baseline 135). eGFR 38.","assessment":"Decompensated heart failure secondary to hypertensive cardiomyopathy — EF 35%. CKD stage 3 complicating diuretic management. VHI 1 (STABLE) — clinically improving since admission.","plan":"IV Furosemide 40mg OD — monitor fluid balance strictly, target negative 500ml/day. Continue Lisinopril 10mg OD — watch creatinine as may worsen on diuresis. Withhold Metformin while creatinine elevated and on diuretics — restart on discharge when stable. Repeat U&Es and BNP in 48 hours. Cardiology outpatient review to be arranged. Low-sodium diet advised. Aspirin contraindicated — do NOT prescribe."}',
 'Musa Garba is a 70-year-old man with longstanding hypertension, diabetes, and CKD stage 3. He came in for cardiac failure optimisation. He has got ankle swelling, orthopnoea, and bibasal crepitations. Echo shows EF 35%, BNP 1240. We need IV Furosemide 40 milligrams once daily. Continue Lisinopril but hold the Metformin while he is on diuretics. He is allergic to Aspirin — do not prescribe. Repeat U&Es in 48 hours.',
 TRUE, '2026-05-22 09:30:00', 'careround-ai-v1',
 '2026-05-22 09:00:00', '2026-05-22 09:30:00'),

-- n14: Amaka Igwe — Post-Op Ward Round, AI-generated + confirmed (Dr Abubakar)
('70000000-0000-0000-0000-000000000014',
 '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000b',
 'WARD_ROUND_NOTE',
 '{"subjective":"35-year-old female, no past medical history, admitted as emergency two nights ago with 5-day history of high fever, generalised abdominal pain, and vomiting. Typhoid screen (Widal) strongly positive. Intraoperative finding: single ileal perforation at 80 cm from ileocaecal junction. Emergency exploratory laparotomy with primary repair and peritoneal lavage performed at 02:00 on 21/05/2026. Now post-operative day 2. No known drug allergies.","objective":"HR 110, BP 100/62, RR 22, Temp 38.8°C, SpO2 94% on 4L O2. Alert but fatigued. Abdomen: laparotomy wound intact, mild periumbilical tenderness, no signs of wound dehiscence. Bowel sounds absent. NG tube in situ draining bilious fluid 200ml since midnight. Urine output 40ml/hr last 4 hours. WBC 18.4 (down from 22.6 on admission). CRP 210 (down from 310). Widal: O-antigen 1:640, H-antigen 1:320.","assessment":"Post-operative typhoid intestinal perforation — day 2. Ongoing sepsis response but improving trend on antibiotics. Paralytic ileus expected. VHI 7 (CRITICAL).","plan":"Continue IV Metronidazole 500mg TDS and IV Ceftriaxone 2g OD for typhoid and peritonitis cover — 14-day course total. Maintain NG tube on free drainage. NBM — commence oral sips only when bowel sounds return. IV fluids — Ringers Lactate at 125ml/hr. Strict fluid balance and hourly urine output. Physiotherapy for chest and early mobilisation when tolerating. Repeat FBC, CRP, and LFTs in 48 hours. Surgical review of wound daily."}',
 'Amaka Igwe is a 35-year-old lady, post-op day 2 from emergency laparotomy for typhoid perforation. Single ileal perforation was repaired. She still has a fever, HR 110, abdomen tender but wound intact. Bowel sounds absent — ileus expected. WBC is coming down from 22 to 18. Continue Metronidazole 500 milligrams three times a day IV and Ceftriaxone 2 grams once daily for 14 days total. Keep her nil by mouth, NG on free drainage, IV fluids at 125 per hour.',
 TRUE, '2026-05-22 08:00:00', 'careround-ai-v1',
 '2026-05-22 07:30:00', '2026-05-22 08:00:00'),

-- n15: Tunde Olatunji — Ward Round (manual, Dr Obi)
('70000000-0000-0000-0000-000000000015',
 '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000a',
 'WARD_ROUND_NOTE',
 'S: 59-year-old male with 15-year history of poorly controlled Type 2 DM and hypertension. Admitted 2 days ago with wet gangrene affecting right 4th and 5th toes — developed from a small blister after wearing tight shoes. Pain 5/10. Admission RBS 28.4 mmol/L. PENICILLIN ALLERGY — wristband applied and confirmed on admission. Unable to feel monofilament on both feet bilaterally.\nO: HR 102, BP 158/94, RR 17, Temp 38.2°C, SpO2 97%. Right foot: 4th and 5th toes — black discolouration with liquefactive necrosis extending to web space, offensive odour, surrounding erythema tracking to mid-foot. Pedal pulses: dorsalis pedis absent bilaterally, posterior tibial faint. Duplex ultrasound: severe peripheral arterial disease, ABI 0.42 right, 0.54 left. RBS this morning 14.8 mmol/L (improving with insulin sliding scale). HbA1c 11.2% on admission.\nA: Diabetic foot with wet gangrene secondary to peripheral arterial disease and peripheral neuropathy. Vascular surgery review — below-knee amputation likely required. VHI 3 (WATCH). Improving glycaemic control.\nP: Continue IV Clindamycin 600mg TDS — PENICILLIN ALLERGY, no beta-lactams. Strict glycaemic control — insulin sliding scale, target RBS 6-10. Commence Metformin at reduced dose once RBS controlled. Wound: daily saline irrigation and dry dressing by ward nurse. Vascular surgery booked for theatre next available slot for right below-knee amputation. Podiatry referral. Diabetic educator review before discharge.',
 NULL, FALSE, NULL, NULL,
 '2026-05-22 10:00:00', '2026-05-22 10:00:00'),

-- n16: Aisha Mohammed — Admission Note, AI-generated + confirmed (Dr Abubakar)
('70000000-0000-0000-0000-000000000016',
 '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000b',
 'ADMISSION_NOTE',
 '{"subjective":"37-year-old female, G2P1, delivered via emergency caesarean section 3 days ago for foetal distress at term. Presented to HDU with 24-hour history of high fever, lower abdominal pain, and purulent discharge from the CS wound. Blood pressure on admission 80/50. Known allergy to Vancomycin — documented and verified with family.","objective":"HR 128, BP 88/52, RR 28, Temp 39.6°C, SpO2 92% on 10L non-rebreathe mask. GCS 14/15 (confused). CS wound: 6cm area of breakdown with copious purulent discharge, surrounding cellulitis extending to flanks. Uterus tender. Lochia: offensive. WBC 24.2, CRP 380, Lactate 3.8 mmol/L. Blood cultures x2 drawn. Urine output 18ml/hr via Foley catheter — oliguria. Septic shock — Sepsis-3 criteria met.","assessment":"Peripartum sepsis with septic shock secondary to post-caesarean wound infection and endometritis. Sepsis-3 criteria met. VHI 11 (CRITICAL). VANCOMYCIN ALLERGY — confirmed, do not prescribe.","plan":"IV Piperacillin-Tazobactam 4.5g every 6 hours — vancomycin allergy, Pip-Tazo provides adequate Gram-positive and anaerobic cover. IV Metronidazole 500mg TDS for additional anaerobic cover. Fluid resuscitation: 500ml 0.9% NaCl bolus then reassess. Target MAP ≥65 — commence noradrenaline if no response to fluids. Strict hourly urine output via Foley. Wound: surgical debridement at bedside today, irrigate and pack with saline-soaked gauze. Gynaecology and general surgery review. Blood cultures pending — narrow antibiotic spectrum when sensitivities available. Lactate trend every 4 hours. ICU escalation criteria: GCS fall, lactate not clearing, MAP unresponsive to vasopressors."}',
 'Aisha Mohammed, 37 years old, G2P1, had an emergency caesarean 3 days ago. She came into HDU with septic shock from a wound infection. BP 88 over 52, lactate 3.8, WBC 24.2. She is allergic to Vancomycin — it is on the wristband and confirmed. She needs Pip-Tazo 4.5 grams every 6 hours IV — do not give Vancomycin. Add Metronidazole 500 milligrams TDS for anaerobic cover. Fluid resus 500ml bolus then reassess. Foley in situ — UO only 18 per hour. Gynaecology and surgery to review for wound debridement today.',
 TRUE, '2026-05-20 15:30:00', 'careround-ai-v1',
 '2026-05-20 15:00:00', '2026-05-20 15:30:00');

-- ============================================================
-- 8b. PRESCRIPTIONS
-- ============================================================
INSERT INTO prescription (
  id, patient_id, hospital_id, clinical_note_id,
  drug_name, dose, route, frequency_string, frequency_hours, total_doses,
  start_time, administration_times,
  confirmed_by_id, confirmed_at, status,
  created_at, updated_at
) VALUES

-- rx11: Babatunde Adeyemi — Artesunate 200mg IV OD (7 days)
('80000000-0000-0000-0000-000000000011',
 '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000011',
 'Artesunate', '200mg', 'Intravenous', 'Once daily', 24, 7,
 '2026-05-20 10:00:00',
 '["2026-05-20T10:00:00","2026-05-21T10:00:00","2026-05-22T10:00:00","2026-05-23T10:00:00","2026-05-24T10:00:00","2026-05-25T10:00:00","2026-05-26T10:00:00"]',
 '30000000-0000-0000-0000-00000000000a', '2026-05-20 11:00:00', 'ACTIVE',
 '2026-05-20 11:00:00', '2026-05-20 11:00:00'),

-- rx12: Sade Oyelaran — Morphine 5mg IV Q4H (SCD VOC — NSAIDs contraindicated)
('80000000-0000-0000-0000-000000000012',
 '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000012',
 'Morphine', '5mg', 'Intravenous', 'Every 4 hours', 4, 12,
 '2026-05-21 08:00:00',
 '["2026-05-21T08:00:00","2026-05-21T12:00:00","2026-05-21T16:00:00","2026-05-21T20:00:00","2026-05-22T00:00:00","2026-05-22T04:00:00","2026-05-22T08:00:00","2026-05-22T12:00:00","2026-05-22T16:00:00","2026-05-22T20:00:00","2026-05-23T00:00:00","2026-05-23T04:00:00"]',
 '30000000-0000-0000-0000-00000000000b', '2026-05-21 05:00:00', 'ACTIVE',
 '2026-05-21 05:00:00', '2026-05-21 05:00:00'),

-- rx13: Musa Garba — Furosemide 40mg IV OD (decompensated HF)
('80000000-0000-0000-0000-000000000013',
 '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000013',
 'Furosemide', '40mg', 'Intravenous', 'Once daily', 24, 7,
 '2026-05-21 08:00:00',
 '["2026-05-21T08:00:00","2026-05-22T08:00:00","2026-05-23T08:00:00","2026-05-24T08:00:00","2026-05-25T08:00:00","2026-05-26T08:00:00","2026-05-27T08:00:00"]',
 '30000000-0000-0000-0000-00000000000a', '2026-05-22 09:30:00', 'ACTIVE',
 '2026-05-22 09:30:00', '2026-05-22 09:30:00'),

-- rx14: Amaka Igwe — Metronidazole 500mg IV TDS (typhoid perforation cover)
('80000000-0000-0000-0000-000000000014',
 '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000014',
 'Metronidazole', '500mg', 'Intravenous', 'Three times daily', 8, 21,
 '2026-05-21 10:00:00',
 '["2026-05-21T10:00:00","2026-05-21T18:00:00","2026-05-22T02:00:00","2026-05-22T10:00:00","2026-05-22T18:00:00","2026-05-23T02:00:00","2026-05-23T10:00:00","2026-05-23T18:00:00","2026-05-24T02:00:00","2026-05-24T10:00:00","2026-05-24T18:00:00","2026-05-25T02:00:00","2026-05-25T10:00:00","2026-05-25T18:00:00","2026-05-26T02:00:00","2026-05-26T10:00:00","2026-05-26T18:00:00","2026-05-27T02:00:00","2026-05-27T10:00:00","2026-05-27T18:00:00","2026-05-28T02:00:00"]',
 '30000000-0000-0000-0000-00000000000b', '2026-05-22 08:00:00', 'ACTIVE',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- rx15: Tunde Olatunji — Clindamycin 600mg IV TDS (PENICILLIN ALLERGY — no beta-lactams)
('80000000-0000-0000-0000-000000000015',
 '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000015',
 'Clindamycin', '600mg', 'Intravenous', 'Three times daily', 8, 21,
 '2026-05-20 14:00:00',
 '["2026-05-20T14:00:00","2026-05-20T22:00:00","2026-05-21T06:00:00","2026-05-21T14:00:00","2026-05-21T22:00:00","2026-05-22T06:00:00","2026-05-22T14:00:00","2026-05-22T22:00:00","2026-05-23T06:00:00","2026-05-23T14:00:00","2026-05-23T22:00:00","2026-05-24T06:00:00","2026-05-24T14:00:00","2026-05-24T22:00:00","2026-05-25T06:00:00","2026-05-25T14:00:00","2026-05-25T22:00:00","2026-05-26T06:00:00","2026-05-26T14:00:00","2026-05-26T22:00:00","2026-05-27T06:00:00"]',
 '30000000-0000-0000-0000-00000000000a', '2026-05-20 13:00:00', 'ACTIVE',
 '2026-05-20 13:00:00', '2026-05-20 13:00:00'),

-- rx16: Aisha Mohammed — Piperacillin-Tazobactam 4.5g IV Q6H (VANCOMYCIN ALLERGY — Pip-Tazo used)
('80000000-0000-0000-0000-000000000016',
 '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 '70000000-0000-0000-0000-000000000016',
 'Piperacillin-Tazobactam', '4.5g', 'Intravenous', 'Every 6 hours', 6, 28,
 '2026-05-20 16:00:00',
 '["2026-05-20T16:00:00","2026-05-20T22:00:00","2026-05-21T04:00:00","2026-05-21T10:00:00","2026-05-21T16:00:00","2026-05-21T22:00:00","2026-05-22T04:00:00","2026-05-22T10:00:00","2026-05-22T16:00:00","2026-05-22T22:00:00","2026-05-23T04:00:00","2026-05-23T10:00:00","2026-05-23T16:00:00","2026-05-23T22:00:00","2026-05-24T04:00:00","2026-05-24T10:00:00","2026-05-24T16:00:00","2026-05-24T22:00:00","2026-05-25T04:00:00","2026-05-25T10:00:00","2026-05-25T16:00:00","2026-05-25T22:00:00","2026-05-26T04:00:00","2026-05-26T10:00:00","2026-05-26T16:00:00","2026-05-26T22:00:00","2026-05-27T04:00:00","2026-05-27T10:00:00"]',
 '30000000-0000-0000-0000-00000000000b', '2026-05-20 15:30:00', 'ACTIVE',
 '2026-05-20 15:30:00', '2026-05-20 15:30:00');

-- ============================================================
-- 9b. MEDICATION CHARTS
-- ============================================================
INSERT INTO medication_chart (id, patient_id, hospital_id, prescription_id, status, nurse_notes, created_at, updated_at) VALUES
('90000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000011', 'ACTIVE', 'Infuse slowly over 1–2 hours. Monitor for hypersensitivity. Check parasite density before each dose.',                                     '2026-05-20 11:00:00', '2026-05-22 08:00:00'),
('90000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000012', 'ACTIVE', 'NSAIDs AND ASPIRIN ALLERGY — do not substitute with ibuprofen or diclofenac. Assess pain score before each dose. Monitor respiratory rate post-dose.', '2026-05-21 05:00:00', '2026-05-22 07:00:00'),
('90000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000013', 'ACTIVE', 'Monitor urine output before administration. Withhold and call doctor if UO <20ml/hr or creatinine rises >20% from baseline.',                      '2026-05-22 09:30:00', '2026-05-22 09:30:00'),
('90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000014', 'ACTIVE', 'Infuse over 30 minutes. Patient is nil by mouth — IV route only. Monitor abdominal signs and bowel sounds at each medication round.',                '2026-05-22 08:00:00', '2026-05-22 08:00:00'),
('90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000015', 'ACTIVE', 'PENICILLIN ALLERGY on wristband — do NOT substitute with Augmentin or Amoxicillin. Monitor for Clindamycin-associated diarrhoea.',                  '2026-05-20 13:00:00', '2026-05-22 08:30:00'),
('90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000016', 'ACTIVE', 'VANCOMYCIN ALLERGY confirmed — do not substitute. Infuse over 30 minutes via peripheral or central line. Critical septic shock patient — do not delay doses.', '2026-05-20 15:30:00', '2026-05-22 06:00:00');

-- ============================================================
-- 10b. MEDICATION TASKS
-- Reference time: 2026-05-22 10:00 (demo "now")
-- ============================================================
INSERT INTO medication_task (
  id, medication_chart_id, patient_id, hospital_id, ward_id,
  assigned_nurse_id, scheduled_time, status,
  completed_at, completed_by_id, actual_dose_given,
  pre_reminder_sent_at, overdue_alert_sent_at,
  created_at, updated_at
) VALUES

-- Chart 11: Artesunate OD — Babatunde Adeyemi (Medical Ward, nurse Eze)
('a0000000-0000-0000-0000-000000000048', '90000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-20 10:00:00', 'COMPLETED', '2026-05-20 10:12:00', '30000000-0000-0000-0000-00000000000c', '200mg', NULL, NULL, '2026-05-20 11:00:00', '2026-05-20 10:12:00'),
('a0000000-0000-0000-0000-000000000049', '90000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-21 10:00:00', 'COMPLETED', '2026-05-21 10:09:00', '30000000-0000-0000-0000-00000000000c', '200mg', NULL, NULL, '2026-05-20 11:00:00', '2026-05-21 10:09:00'),
('a0000000-0000-0000-0000-000000000050', '90000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-22 10:00:00', 'OVERDUE',    NULL, NULL, NULL, '2026-05-22 09:55:00', '2026-05-22 10:10:00', '2026-05-20 11:00:00', '2026-05-22 10:10:00'),
('a0000000-0000-0000-0000-000000000051', '90000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-23 10:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-20 11:00:00', '2026-05-20 11:00:00'),

-- Chart 12: Morphine Q4H — Sade Oyelaran (Medical Ward, nurse Obiora)
('a0000000-0000-0000-0000-000000000052', '90000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000d', '2026-05-21 08:00:00', 'COMPLETED', '2026-05-21 08:06:00', '30000000-0000-0000-0000-00000000000d', '5mg', NULL, NULL, '2026-05-21 05:00:00', '2026-05-21 08:06:00'),
('a0000000-0000-0000-0000-000000000053', '90000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000d', '2026-05-21 12:00:00', 'COMPLETED', '2026-05-21 12:04:00', '30000000-0000-0000-0000-00000000000d', '5mg', NULL, NULL, '2026-05-21 05:00:00', '2026-05-21 12:04:00'),
('a0000000-0000-0000-0000-000000000054', '90000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000d', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:08:00', '30000000-0000-0000-0000-00000000000d', '5mg', NULL, NULL, '2026-05-21 05:00:00', '2026-05-22 08:08:00'),
('a0000000-0000-0000-0000-000000000055', '90000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000d', '2026-05-22 12:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-21 05:00:00', '2026-05-22 07:00:00'),

-- Chart 13: Furosemide OD — Musa Garba (Medical Ward, nurse Eze)
('a0000000-0000-0000-0000-000000000056', '90000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-21 08:00:00', 'COMPLETED', '2026-05-21 08:11:00', '30000000-0000-0000-0000-00000000000c', '40mg', NULL, NULL, '2026-05-22 09:30:00', '2026-05-21 08:11:00'),
('a0000000-0000-0000-0000-000000000057', '90000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-22 08:00:00', 'COMPLETED', '2026-05-22 08:14:00', '30000000-0000-0000-0000-00000000000c', '40mg', NULL, NULL, '2026-05-22 09:30:00', '2026-05-22 08:14:00'),
('a0000000-0000-0000-0000-000000000058', '90000000-0000-0000-0000-000000000013', '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-00000000000c', '2026-05-23 08:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-22 09:30:00', '2026-05-22 09:30:00'),

-- Chart 14: Metronidazole TDS — Amaka Igwe (Surgical Ward, nurse Balogun)
-- OVERDUE: 10:00 dose missed — critical post-perforation patient
('a0000000-0000-0000-0000-000000000059', '90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000e', '2026-05-21 10:00:00', 'COMPLETED', '2026-05-21 10:07:00', '30000000-0000-0000-0000-00000000000e', '500mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-21 10:07:00'),
('a0000000-0000-0000-0000-000000000060', '90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000e', '2026-05-21 18:00:00', 'COMPLETED', '2026-05-21 18:05:00', '30000000-0000-0000-0000-00000000000e', '500mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-21 18:05:00'),
('a0000000-0000-0000-0000-000000000061', '90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000d', '2026-05-22 02:00:00', 'COMPLETED', '2026-05-22 02:09:00', '30000000-0000-0000-0000-00000000000d', '500mg', NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 02:09:00'),
('a0000000-0000-0000-0000-000000000062', '90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000e', '2026-05-22 10:00:00', 'OVERDUE',    NULL, NULL, NULL, '2026-05-22 09:55:00', '2026-05-22 10:10:00', '2026-05-22 08:00:00', '2026-05-22 10:10:00'),
('a0000000-0000-0000-0000-000000000063', '90000000-0000-0000-0000-000000000014', '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000e', '2026-05-22 18:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

-- Chart 15: Clindamycin TDS — Tunde Olatunji (Surgical Ward, nurse Eze)
('a0000000-0000-0000-0000-000000000064', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000c', '2026-05-20 14:00:00', 'COMPLETED', '2026-05-20 14:08:00', '30000000-0000-0000-0000-00000000000c', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-20 14:08:00'),
('a0000000-0000-0000-0000-000000000065', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000d', '2026-05-20 22:00:00', 'COMPLETED', '2026-05-20 22:05:00', '30000000-0000-0000-0000-00000000000d', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-20 22:05:00'),
('a0000000-0000-0000-0000-000000000066', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000c', '2026-05-21 06:00:00', 'COMPLETED', '2026-05-21 06:06:00', '30000000-0000-0000-0000-00000000000c', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-21 06:06:00'),
('a0000000-0000-0000-0000-000000000067', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000c', '2026-05-21 14:00:00', 'COMPLETED', '2026-05-21 14:10:00', '30000000-0000-0000-0000-00000000000c', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-21 14:10:00'),
('a0000000-0000-0000-0000-000000000068', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000d', '2026-05-21 22:00:00', 'COMPLETED', '2026-05-21 22:03:00', '30000000-0000-0000-0000-00000000000d', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-21 22:03:00'),
('a0000000-0000-0000-0000-000000000069', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000c', '2026-05-22 06:00:00', 'COMPLETED', '2026-05-22 06:07:00', '30000000-0000-0000-0000-00000000000c', '600mg', NULL, NULL, '2026-05-20 13:00:00', '2026-05-22 06:07:00'),
('a0000000-0000-0000-0000-000000000070', '90000000-0000-0000-0000-000000000015', '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-00000000000c', '2026-05-22 14:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-20 13:00:00', '2026-05-22 08:30:00'),

-- Chart 16: Pip-Tazo Q6H — Aisha Mohammed (HDU, nurse Obiora)
('a0000000-0000-0000-0000-000000000071', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-20 16:00:00', 'COMPLETED', '2026-05-20 16:08:00', '30000000-0000-0000-0000-00000000000d', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-20 16:08:00'),
('a0000000-0000-0000-0000-000000000072', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-20 22:00:00', 'COMPLETED', '2026-05-20 22:04:00', '30000000-0000-0000-0000-00000000000d', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-20 22:04:00'),
('a0000000-0000-0000-0000-000000000073', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000e', '2026-05-21 04:00:00', 'COMPLETED', '2026-05-21 04:06:00', '30000000-0000-0000-0000-00000000000e', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-21 04:06:00'),
('a0000000-0000-0000-0000-000000000074', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000e', '2026-05-21 10:00:00', 'COMPLETED', '2026-05-21 10:10:00', '30000000-0000-0000-0000-00000000000e', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-21 10:10:00'),
('a0000000-0000-0000-0000-000000000075', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-21 16:00:00', 'COMPLETED', '2026-05-21 16:05:00', '30000000-0000-0000-0000-00000000000d', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-21 16:05:00'),
('a0000000-0000-0000-0000-000000000076', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-21 22:00:00', 'COMPLETED', '2026-05-21 22:07:00', '30000000-0000-0000-0000-00000000000d', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-21 22:07:00'),
('a0000000-0000-0000-0000-000000000077', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000e', '2026-05-22 04:00:00', 'COMPLETED', '2026-05-22 04:09:00', '30000000-0000-0000-0000-00000000000e', '4.5g', NULL, NULL, '2026-05-20 15:30:00', '2026-05-22 04:09:00'),
('a0000000-0000-0000-0000-000000000078', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-22 10:00:00', 'OVERDUE',    NULL, NULL, NULL, '2026-05-22 09:55:00', '2026-05-22 10:10:00', '2026-05-20 15:30:00', '2026-05-22 10:10:00'),
('a0000000-0000-0000-0000-000000000079', '90000000-0000-0000-0000-000000000016', '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-00000000000d', '2026-05-22 16:00:00', 'PENDING',    NULL, NULL, NULL, NULL, NULL, '2026-05-20 15:30:00', '2026-05-22 06:00:00');

-- ============================================================
-- 11b. HANDOVER NOTES
-- ============================================================
INSERT INTO handover_note (id, patient_id, hospital_id, author_id, content, created_at, updated_at) VALUES

('b0000000-0000-0000-0000-000000000009',
 '50000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 'Babatunde Adeyemi, Bed M1. Malaria — AMBER. Fever persisting but trending down. Hb improving: 7.8 on admission, now 8.4. Platelets still low at 62. May 20 and May 21 Artesunate doses given without issues. URGENT: May 22 10:00 Artesunate dose OVERDUE — please administer immediately and monitor for infusion reaction. Repeat malaria film due this afternoon. IV line patent in right AC. Monitor urine colour for haemoglobinuria.',
 '2026-05-22 08:30:00', '2026-05-22 08:30:00'),

('b0000000-0000-0000-0000-00000000000a',
 '50000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 'Sade Oyelaran, Bed M2. Sickle cell VOC + acute chest syndrome — RED. NSAIDS AND ASPIRIN ALLERGY — do not use ibuprofen, diclofenac, or aspirin under any circumstances. Morphine 5mg IV given at 08:08 this morning — pain score now 5/10 (was 9/10 on admission). SpO2 94-95% on 10L O2 via face mask — do not reduce O2 without doctor approval. Haematology team review scheduled for 11:00 re: exchange transfusion. CXR infiltrate at right base unchanged. Next Morphine dose due 12:00. Observe respiratory rate closely after each dose.',
 '2026-05-22 09:00:00', '2026-05-22 09:00:00'),

('b0000000-0000-0000-0000-00000000000b',
 '50000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 'Musa Garba, Bed M3. Decompensated heart failure — GREEN, improving. Furosemide 40mg IV given at 08:14 — UO 450ml since last dose, good response. Ankle oedema visibly reduced. BP 145/88. Creatinine 148 at last check — within acceptable range for diuresis. ASPIRIN ALLERGY on wristband — no aspirin or aspirin-containing products. Metformin held while on diuretics. Repeat U&Es due at 16:00. Next Furosemide due tomorrow 08:00.',
 '2026-05-22 09:30:00', '2026-05-22 09:30:00'),

('b0000000-0000-0000-0000-00000000000c',
 '50000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000e',
 'Amaka Igwe, Bed S1. Post-op typhoid perforation day 2 — RED. WBC falling (22.6 → 18.4), CRP 210, trending in right direction. NBM — NG tube draining 200ml bilious overnight, bowel sounds still absent. IV fluids running at 125ml/hr. UO 40ml/hr — satisfactory. URGENT: 10:00 Metronidazole dose OVERDUE — administer immediately. Wound: clean, sutures intact. Morning Metronidazole and Ceftriaxone given on schedule but 10:00 Metronidazole missed. SpO2 94% on 4L O2 — watch for deterioration.',
 '2026-05-22 08:00:00', '2026-05-22 08:00:00'),

('b0000000-0000-0000-0000-00000000000d',
 '50000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000c',
 'Tunde Olatunji, Bed S2. Diabetic foot with wet gangrene — AMBER. PENICILLIN ALLERGY on wristband — Clindamycin in use, no beta-lactams. Morning Clindamycin given at 06:07. Next dose 14:00. RBS this morning 14.8 mmol/L — improving from 28.4 on admission. Insulin sliding scale in progress. Wound: right 4th and 5th toes gangrenous with erythema extending to mid-foot, dressing changed this morning with saline irrigation — offensive odour reduced slightly. Vascular surgery booked for theatre — awaiting slot. Pain 5/10. Patient anxious about amputation — chaplaincy and counselling referral requested.',
 '2026-05-22 08:30:00', '2026-05-22 08:30:00'),

('b0000000-0000-0000-0000-00000000000e',
 '50000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000002',
 '30000000-0000-0000-0000-00000000000d',
 'Aisha Mohammed, HDU Bed H1. Peripartum septic shock — RED. VANCOMYCIN ALLERGY confirmed on central line label and wristband — Pip-Tazo in use only. Lactate improving: 3.8 on admission → 2.4 at 06:00. MAP 70 — IV fluids ongoing, no vasopressors required yet. UO 28ml/hr — oliguria, discuss with HDU consultant. Morning Pip-Tazo given at 04:09. URGENT: 10:00 dose OVERDUE — administer immediately via peripheral line. CS wound debrided at bedside yesterday evening — packing with saline gauze, change due this afternoon. Gynaecology reviewed at 07:00 — plan for formal surgical debridement in theatre if wound worsens. Blood cultures from admission still pending.',
 '2026-05-22 06:30:00', '2026-05-22 06:30:00');

-- ============================================================
-- SEED COMPLETE
-- Summary:
--
-- HOSPITAL 1: City General Hospital (CGH) — London, UK
--   1  hospital
--   1  system config
--   7  users       (1 admin, 1 supervisor, 2 doctors, 3 nurses)
--   3  wards       (Ward A General Medicine, Ward B Surgery, ICU)
--   8  patients    (3x Ward A, 3x Ward B, 2x ICU)
--   16 vitals      (2 readings per patient)
--   10 clinical notes (3 AI-generated + confirmed, 7 manual)
--   10 prescriptions
--   10 medication charts
--   47 medication tasks (2x OVERDUE, 22x COMPLETED, 23x PENDING)
--   8  handover notes
--
-- HOSPITAL 2: Lagos Island General Hospital (LIGH) — Lagos, Nigeria
--   1  hospital
--   1  system config
--   7  users       (1 admin, 1 supervisor, 2 doctors, 3 nurses)
--   3  wards       (Medical Ward, Surgical Ward, HDU)
--   6  patients    (3x Medical, 2x Surgical, 1x HDU)
--   12 vitals      (2 readings per patient)
--   6  clinical notes (3 AI-generated + confirmed, 3 manual)
--   6  prescriptions
--   6  medication charts
--   32 medication tasks (3x OVERDUE, 17x COMPLETED, 12x PENDING)
--   6  handover notes
--
-- Nigerian clinical scenarios: falciparum malaria, sickle cell VOC
--   with acute chest syndrome, hypertensive cardiomyopathy,
--   typhoid intestinal perforation, diabetic foot with wet gangrene,
--   peripartum septic shock.
--
-- Login credentials (all accounts):
--   Password: Password123
--
--   --- CGH (Hospital 1) ---
--   admin@citygeneral.nhs.uk       ADMIN
--   l.walsh@citygeneral.nhs.uk     SUPERVISOR
--   s.chen@citygeneral.nhs.uk      DOCTOR
--   j.okafor@citygeneral.nhs.uk    DOCTOR
--   e.foster@citygeneral.nhs.uk    NURSE
--   m.adeyemi@citygeneral.nhs.uk   NURSE
--   p.sharma@citygeneral.nhs.uk    NURSE
--
--   --- LIGH (Hospital 2) ---
--   admin@ligh.gov.ng              ADMIN
--   a.nwosu@ligh.gov.ng            SUPERVISOR
--   e.obi@ligh.gov.ng              DOCTOR
--   f.abubakar@ligh.gov.ng         DOCTOR
--   n.eze@ligh.gov.ng              NURSE
--   c.obiora@ligh.gov.ng           NURSE
--   a.balogun@ligh.gov.ng          NURSE
--
--   --- PLATFORM_ADMIN (system-wide, no hospital tenant) ---
--   admin@careround.com            PLATFORM_ADMIN
--     hospitalId: 00000000-0000-0000-0000-000000000000 (sentinel)
-- ============================================================