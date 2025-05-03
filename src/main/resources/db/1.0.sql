CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_active TIMESTAMP
);

CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    display_name VARCHAR(50),
    age INTEGER,
    country VARCHAR(50),
    gender VARCHAR(20),
    bio TEXT,
    profile_image_url VARCHAR(255),
    interests JSONB
);

CREATE TABLE blocked_users (
    blocker_id BIGINT NOT NULL REFERENCES users(id),
    blocked_id BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (blocker_id, blocked_id)
);