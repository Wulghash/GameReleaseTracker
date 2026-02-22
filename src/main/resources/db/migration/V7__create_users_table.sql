CREATE TABLE app_users (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id  VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_app_users_google_id ON app_users(google_id);
