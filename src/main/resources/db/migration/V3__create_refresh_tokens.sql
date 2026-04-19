CREATE TABLE refresh_tokens (
    id          uuid        NOT NULL,
    user_id     uuid        NOT NULL,
    token_hash  varchar(64) NOT NULL,
    family_id   uuid        NOT NULL,
    expires_at  timestamptz NOT NULL,
    revoked_at  timestamptz,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz NOT NULL,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Used by family-wide revocation on reuse detection
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);
-- Used by the cleanup scheduler
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
