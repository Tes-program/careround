ALTER TABLE care_task
    ADD COLUMN workload_conflict BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN workload_conflict_reason TEXT NULL;

CREATE INDEX idx_care_task_assignee_window
    ON care_task(hospital_id, assigned_to_id, status, window_start, window_end);
