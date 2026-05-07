-- High-frequency query path indexes

-- Patient lookup by ward, sorted by acuity and NEWS score
CREATE INDEX idx_patient_hospital_ward_acuity_news
    ON patient (hospital_id, ward_id, acuity_level, news_score);

-- Care task lookup by ward and status
CREATE INDEX idx_care_task_hospital_ward_status_window
    ON care_task (hospital_id, ward_id, status, window_end);

-- On-call lookup by department and time range
CREATE INDEX idx_on_call_dept_time
    ON on_call_rotation (department_id, start_time, end_time);

-- Shift lookup by ward, type, status
CREATE INDEX idx_shift_ward_type_status
    ON shift (ward_id, type, status);

-- Round lookup by ward, team, type, status
CREATE INDEX idx_round_ward_team_type_status
    ON round (ward_id, medical_team_id, round_type, status);

-- Vitals ordered by patient and time
CREATE INDEX idx_patient_vitals_patient_recorded
    ON patient_vitals (patient_id, recorded_at);

-- Escalation lookup
CREATE INDEX idx_escalation_hospital_status
    ON escalation (hospital_id, status);
