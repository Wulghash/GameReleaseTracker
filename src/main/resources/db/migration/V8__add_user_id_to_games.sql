-- Remove all existing games (no users exist yet, so all rows are orphaned pre-auth data)
TRUNCATE games CASCADE;
ALTER TABLE games
    ADD COLUMN user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE;
CREATE INDEX idx_games_user_id ON games(user_id);
