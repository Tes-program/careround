CREATE INDEX idx_patient_ward_priority ON patients(ward_id, acuity_level, news_score);
CREATE INDEX idx_care_task_ward_status ON care_tasks(ward_id, status);
CREATE INDEX idx_care_task_overdue ON care_tasks(window_end, status);
CREATE INDEX idx_outbox_event_poller ON outbox_events(published, created_at);
CREATE INDEX idx_on_call_lookup ON on_call_rotations(department_id, role, start_time, end_time);
CREATE INDEX idx_shift_ward_status ON shifts(ward_id, status);
CREATE INDEX idx_escalation_hospital_status ON escalations(hospital_id, status);
CREATE INDEX idx_escalation_patient ON escalations(patient_id);
