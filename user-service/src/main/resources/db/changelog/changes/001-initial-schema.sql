--liquibase formatted sql

--changeset author:user-service-schema version:1.0.0
CREATE SCHEMA IF NOT EXISTS v1_user;

--changeset author:user-service-users-table version:1.0.1
CREATE TABLE IF NOT EXISTS v1_user.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset author:user-service-roles-table version:1.0.2
CREATE TABLE IF NOT EXISTS v1_user.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset author:user-service-user-roles-table version:1.0.3
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

--changeset author:user-service-indexes version:1.0.4
CREATE INDEX IF NOT EXISTS idx_users_email ON v1_user.users(email);
CREATE INDEX IF NOT EXISTS idx_users_nickname ON v1_user.users(nickname);
CREATE INDEX IF NOT EXISTS idx_users_status ON v1_user.users(status);
CREATE INDEX IF NOT EXISTS idx_roles_name ON v1_user.roles(name);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON v1_user.user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON v1_user.user_roles(role_id);

--changeset author:user-service-roles version:1.0.4
INSERT INTO v1_user.roles (name, description) VALUES 
('ADMIN', 'Administrator role with full access'),
('MODERATOR', 'Moderator role with limited administrative access'),
('USER', 'Regular user role'),
('GUEST', 'Guest user role with minimal access');