--liquibase formatted sql

--changeset performance-team:add-advanced-indexes version:1.0.5
-- Performance optimization: Add composite and specialized indexes

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_users_status_created_at ON v1_user.users(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_email_status ON v1_user.users(email, status);

-- Role-based queries optimization (users.role_id instead of user_roles table)
CREATE INDEX IF NOT EXISTS idx_users_role_created_at ON v1_user.users(role_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_role_status ON v1_user.users(role_id, status);

-- Covering index for user listing (reduces disk I/O)
CREATE INDEX IF NOT EXISTS idx_users_listing ON v1_user.users(status, created_at DESC) 
INCLUDE (id, email, nickname, role_id);

-- Partial indexes for active users (smaller, faster)
CREATE INDEX IF NOT EXISTS idx_users_active ON v1_user.users(created_at DESC) 
WHERE status = 'ACTIVE';

-- Index for role-based user searches
CREATE INDEX IF NOT EXISTS idx_users_by_role ON v1_user.users(role_id, status, created_at DESC);

-- Audit trail optimization
CREATE INDEX IF NOT EXISTS idx_users_audit ON v1_user.users(created_by, created_at);
CREATE INDEX IF NOT EXISTS idx_roles_audit ON v1_user.roles(created_by, created_at);

-- Performance indexes for updated_at (for cache invalidation)
CREATE INDEX IF NOT EXISTS idx_users_updated_at ON v1_user.users(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_roles_updated_at ON v1_user.roles(updated_at DESC);
