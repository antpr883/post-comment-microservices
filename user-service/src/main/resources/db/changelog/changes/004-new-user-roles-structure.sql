--liquibase formatted sql

--changeset author:user-service-new-structure version:2.0.0
-- Create new schema for the updated structure

-- Drop existing tables if they exist (for clean start)
DROP TABLE IF EXISTS v1_user.user_roles CASCADE;
DROP TABLE IF EXISTS v1_user.users CASCADE;
DROP TABLE IF EXISTS v1_user.roles CASCADE;

-- Create roles table first
CREATE TABLE v1_user.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create users table with role reference
CREATE TABLE v1_user.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role_id BIGINT REFERENCES v1_user.roles(id),
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);



-- Create indexes
CREATE INDEX idx_users_email ON v1_user.users(email);
CREATE INDEX idx_users_nickname ON v1_user.users(nickname);
CREATE INDEX idx_users_status ON v1_user.users(status);
CREATE INDEX idx_roles_name ON v1_user.roles(name);
CREATE INDEX idx_users_role_id ON v1_user.users(role_id);

-- Insert base roles
INSERT INTO v1_user.roles (name, description, created_by) VALUES 
('ADMIN', 'Administrator role with full access', 'system'),
('MODERATOR', 'Moderator role with limited administrative access', 'system'),
('USER', 'Regular user role', 'system'),
('GUEST', 'Guest user role with minimal access', 'system');

-- Insert test users
INSERT INTO v1_user.users (email, nickname, password, status, created_by) VALUES
('admin1@example.com', 'admin1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('admin2@example.com', 'admin2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('john.doe@example.com', 'johndoe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('jane.smith@example.com', 'janesmith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('bob.wilson@example.com', 'bobwilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('alice.johnson@example.com', 'alicejohnson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('mike.brown@example.com', 'mikebrown', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system');

-- Assign roles to users
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'ADMIN') WHERE email = 'admin1@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'ADMIN') WHERE email = 'admin2@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'USER') WHERE email = 'john.doe@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'MODERATOR') WHERE email = 'jane.smith@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'USER') WHERE email = 'bob.wilson@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'USER') WHERE email = 'alice.johnson@example.com';
UPDATE v1_user.users SET role_id = (SELECT id FROM v1_user.roles WHERE name = 'USER') WHERE email = 'mike.brown@example.com';
