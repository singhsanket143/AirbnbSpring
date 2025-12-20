CREATE TABLE availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    airbnb_id BIGINT NOT NULL,
    booking_id BIGINT,
    date VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_availability_airbnb
        FOREIGN KEY (airbnb_id) REFERENCES airbnb(id),

    CONSTRAINT fk_availability_booking
        FOREIGN KEY (booking_id) REFERENCES booking(id)
);
