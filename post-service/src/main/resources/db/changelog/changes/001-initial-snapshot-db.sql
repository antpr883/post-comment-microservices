--liquibase formatted sql

--changeset user_snapshot:labels:initial-schema

--set schema v1_snap_user
SET search_path TO v1_snap_user;

CREATE TABLE users (
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    cached_at TIMESTAMP,
    expires_at TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_username UNIQUE (username)
);


CREATE INDEX idx_users_user_id ON users (user_id);


INSERT INTO users (user_id, username, cached_at, expires_at) VALUES
    (1, 'john_doe', NOW(), NOW() + INTERVAL '1 HOUR'),
    (2, 'jane_smith', NOW(), NOW() + INTERVAL '1 HOUR'),
    (3, 'alex_johnson', NOW(), NOW() + INTERVAL '1 HOUR'),
    (4, 'sarah_wilson', NOW(), NOW() + INTERVAL '1 HOUR'),
    (5, 'mike_brown', NOW(), NOW() + INTERVAL '1 HOUR'),
    (6, 'emma_davis', NOW(), NOW() + INTERVAL '1 HOUR'),
    (7, 'david_miller', NOW(), NOW() + INTERVAL '1 HOUR'),
    (8, 'lisa_taylor', NOW(), NOW() + INTERVAL '1 HOUR'),
    (9, 'chris_anderson', NOW(), NOW() + INTERVAL '1 HOUR'),
    (10, 'anna_thomas', NOW(), NOW() + INTERVAL '1 HOUR'),
    (11, 'robert_jackson', NOW(), NOW() + INTERVAL '1 HOUR'),
    (12, 'maria_garcia', NOW(), NOW() + INTERVAL '1 HOUR'),
    (13, 'james_white', NOW(), NOW() + INTERVAL '1 HOUR'),
    (14, 'linda_martinez', NOW(), NOW() + INTERVAL '1 HOUR'),
    (15, 'paul_rodriguez', NOW(), NOW() + INTERVAL '1 HOUR'),
    (16, 'helen_lee', NOW(), NOW() + INTERVAL '1 HOUR'),
    (17, 'tom_harris', NOW(), NOW() + INTERVAL '1 HOUR'),
    (18, 'nancy_clark', NOW(), NOW() + INTERVAL '1 HOUR'),
    (19, 'kevin_lewis', NOW(), NOW() + INTERVAL '1 HOUR'),
    (20, 'betty_walker', NOW(), NOW() + INTERVAL '1 HOUR');
