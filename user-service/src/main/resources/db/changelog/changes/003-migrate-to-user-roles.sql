--liquibase formatted sql

--changeset author:user-service-migrate-to-user-roles version:1.0.7
-- Drop the old user_id column from roles table and create new user_roles table

-- First, create the new user_roles table
CREATE TABLE IF NOT EXISTS v1_user.user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES v1_user.users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES v1_user.roles(id) ON DELETE CASCADE,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

-- Insert data from old structure to new structure
INSERT INTO v1_user.user_roles (user_id, role_id, created_by)
SELECT user_id, id, 'system'
FROM v1_user.roles 
WHERE user_id IS NOT NULL;

-- Drop the old user_id column from roles table
ALTER TABLE v1_user.roles DROP COLUMN IF EXISTS user_id;

-- Add indexes for the new table
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON v1_user.user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON v1_user.user_roles(role_id);
