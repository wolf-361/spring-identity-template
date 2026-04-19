CREATE TABLE users (
    id          uuid         NOT NULL,
    email       varchar(255) NOT NULL,
    password    varchar(255),
    first_name  varchar(100) NOT NULL,
    last_name   varchar(100) NOT NULL,
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);
