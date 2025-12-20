CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    airbnb_id BIGINT NOT NULL,

    total_price VARCHAR(255) NOT NULL,
    booking_status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES user(id),

    CONSTRAINT fk_booking_airbnb
        FOREIGN KEY (airbnb_id) REFERENCES airbnb(id)
);
