ALTER TABLE users ADD COLUMN ward_id VARCHAR(36) NULL;
CREATE INDEX idx_users_ward_id ON users(ward_id);
