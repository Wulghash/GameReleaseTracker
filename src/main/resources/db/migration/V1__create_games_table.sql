CREATE TABLE games (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255)  NOT NULL,
    description  TEXT,
    release_date DATE          NOT NULL,
    shop_url     VARCHAR(500),
    image_url    VARCHAR(500),
    developer    VARCHAR(255),
    publisher    VARCHAR(255),
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE game_platforms (
    game_id  UUID        NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    PRIMARY KEY (game_id, platform)
);

CREATE INDEX idx_games_release_date ON games(release_date);
CREATE INDEX idx_game_platforms_game_id ON game_platforms(game_id);
