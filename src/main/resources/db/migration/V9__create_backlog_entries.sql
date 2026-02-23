CREATE TABLE backlog_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    igdb_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    cover_url VARCHAR(500),
    release_date DATE,
    backlog_status VARCHAR(20) NOT NULL DEFAULT 'WANT_TO_PLAY',
    rating SMALLINT CHECK (rating >= 1 AND rating <= 10),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, igdb_id)
);
CREATE INDEX idx_backlog_entries_user_id ON backlog_entries(user_id);
