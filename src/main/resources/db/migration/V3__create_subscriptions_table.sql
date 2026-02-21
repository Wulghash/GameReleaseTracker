CREATE TABLE subscriptions (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id            UUID        NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    email              VARCHAR(255) NOT NULL,
    unsubscribe_token  UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    created_at         TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_subscription_game_email UNIQUE (game_id, email)
);

CREATE INDEX idx_subscriptions_game_id ON subscriptions(game_id);
CREATE INDEX idx_subscriptions_unsubscribe_token ON subscriptions(unsubscribe_token);
