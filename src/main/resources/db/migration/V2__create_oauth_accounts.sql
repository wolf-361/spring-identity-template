CREATE TABLE oauth_accounts (
    id               uuid         NOT NULL,
    user_id          uuid         NOT NULL,
    provider         varchar(50)  NOT NULL,
    provider_user_id varchar(255) NOT NULL,
    provider_email   varchar(255) NOT NULL,
    created_at       timestamptz  NOT NULL,
    updated_at       timestamptz  NOT NULL,

    CONSTRAINT pk_oauth_accounts PRIMARY KEY (id),
    CONSTRAINT uq_oauth_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_oauth_accounts_user_id ON oauth_accounts (user_id);
