CREATE TABLE id_counters (
    name VARCHAR(50) PRIMARY KEY,
    next_value BIGINT NOT NULL CHECK (next_value > 0)
);

INSERT INTO id_counters (name, next_value) VALUES ('short_url', 1);

CREATE TABLE short_urls (
    id BIGINT PRIMARY KEY,
    code VARCHAR(11) NOT NULL UNIQUE,
    long_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT expires_after_creation CHECK (expires_at IS NULL OR expires_at > created_at)
);
