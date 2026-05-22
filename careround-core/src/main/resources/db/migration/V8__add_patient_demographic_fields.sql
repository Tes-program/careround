ALTER TABLE patient
    ADD COLUMN phone_number          VARCHAR(50)  AFTER hospital_number,
    ADD COLUMN address               TEXT         AFTER phone_number,
    ADD COLUMN previous_conditions   TEXT         AFTER address,
    ADD COLUMN current_medications   TEXT         AFTER previous_conditions,
    ADD COLUMN allergies             TEXT         AFTER current_medications,
    ADD COLUMN emergency_contact_name  VARCHAR(255) AFTER allergies,
    ADD COLUMN emergency_contact_phone VARCHAR(50)  AFTER emergency_contact_name,
    ADD COLUMN registered_by_id      VARCHAR(36)  AFTER emergency_contact_phone;
