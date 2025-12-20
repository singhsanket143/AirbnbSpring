CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
