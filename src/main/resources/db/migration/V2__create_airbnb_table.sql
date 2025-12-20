CREATE TABLE airbnb (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(255),
    description TEXT,
    location VARCHAR(255),
    price_per_night BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
