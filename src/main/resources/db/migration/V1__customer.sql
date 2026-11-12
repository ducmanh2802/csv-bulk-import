CREATE TABLE customer (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    full_name  VARCHAR(200) NOT NULL,
    phone      VARCHAR(30),
    city       VARCHAR(100),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
