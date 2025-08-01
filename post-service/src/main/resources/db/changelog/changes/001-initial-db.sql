--liquibase formatted sql

--changeset post_user:1 labels:initial-schema

-- Create schema v1_post_hub
CREATE SCHEMA IF NOT EXISTS v1_post_hub;

--creating table for posts in v1_post_hub schema
CREATE TABLE v1_post_hub.posts (
   id BIGSERIAL PRIMARY KEY,
   title VARCHAR(255) NOT NULL,
   content TEXT NOT NULL,
   description JSONB,
   comment_ids BIGINT[] DEFAULT '{}',
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   likes INTEGER NOT NULL DEFAULT 0,
   post_status VARCHAR(30) NOT NULL,
   author_id BIGINT NOT NULL,
   created_by VARCHAR(255),
   modified_by VARCHAR(255),
   UNIQUE (title)
);

-- Pre defined data insertion
INSERT INTO v1_post_hub.posts (title, content, description, comment_ids, created_at, updated_at, likes, post_status, author_id, created_by, modified_by) VALUES
(
  'First Post',
  'This is the content of the first post.',
  '{"summary": "This is a summary of the first post."}',
  ARRAY[101, 102],
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  5,
  'ACTIVE',
  1,
  'system',
  'system'
),
(
  'Second Post',
  'This is the content of the second post.',
  '{"summary": "This is a summary of the second post."}',
  ARRAY[201],
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  3,
  'ACTIVE',
  2,
  'system',
  'system'
),
(
  'Third Post',
  'This is the content of the third post.',
  '{"summary": "This is a summary of the third post."}',
  ARRAY[]::bigint[],
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  0,
  'ACTIVE',
  1,
  'system',
  'system'
);
