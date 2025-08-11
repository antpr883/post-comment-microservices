--liquibase formatted sql

--changeset author:user-service-users-data version:1.0.5
INSERT INTO v1_user.users (email, nickname, password, status, created_by) VALUES 
('admin@example.com', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('john.doe@example.com', 'johndoe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('jane.smith@example.com', 'janesmith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ACTIVE', 'system'),
('bob.wilson@example.com', 'bobwilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'INACTIVE', 'system'),
('alice.johnson@example.com', 'alicejohnson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'PENDING', 'system');

--changeset author:user-service-user-roles-data version:1.0.6
-- Assign roles to users through user_roles table
INSERT INTO v1_user.user_roles (user_id, role_id, created_by) VALUES 
((SELECT id FROM v1_user.users WHERE email = 'admin@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'ADMIN'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'admin@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'USER'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'john.doe@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'USER'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'jane.smith@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'MODERATOR'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'jane.smith@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'USER'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'bob.wilson@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'USER'), 'system'),
((SELECT id FROM v1_user.users WHERE email = 'alice.johnson@example.com'), (SELECT id FROM v1_user.roles WHERE name = 'GUEST'), 'system');
