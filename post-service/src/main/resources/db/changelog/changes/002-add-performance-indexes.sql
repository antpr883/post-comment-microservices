--liquibase formatted sql

--changeset performance-team:add-missing-indexes version:2.0.0
-- Performance optimization: Add missing indexes for frequent queries

-- Posts table indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON v1_post_hub.posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_status ON v1_post_hub.posts(post_status);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON v1_post_hub.posts(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_updated_at ON v1_post_hub.posts(updated_at);
CREATE INDEX IF NOT EXISTS idx_posts_likes ON v1_post_hub.posts(likes);

-- Composite indexes for common query combinations
CREATE INDEX IF NOT EXISTS idx_posts_status_created_at ON v1_post_hub.posts(post_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_author_status ON v1_post_hub.posts(author_id, post_status);
CREATE INDEX IF NOT EXISTS idx_posts_status_likes ON v1_post_hub.posts(post_status, likes DESC);

-- JSONB index for description field queries (PostgreSQL specific)
CREATE INDEX IF NOT EXISTS idx_posts_description_gin ON v1_post_hub.posts USING GIN(description);

-- Full-text search index for title and content
CREATE INDEX IF NOT EXISTS idx_posts_title_search ON v1_post_hub.posts USING GIN(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_posts_content_search ON v1_post_hub.posts USING GIN(to_tsvector('english', content));

-- Comment IDs array index (for array operations)
CREATE INDEX IF NOT EXISTS idx_posts_comment_ids_gin ON v1_post_hub.posts USING GIN(comment_ids);
