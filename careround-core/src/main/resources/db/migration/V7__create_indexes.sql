-- Composite indexes for hospital-scoped task queries not yet covered by V5
CREATE INDEX idx_task_patient_hospital ON medication_task(patient_id, hospital_id, status);
CREATE INDEX idx_prescription_status ON prescription(hospital_id, status);
