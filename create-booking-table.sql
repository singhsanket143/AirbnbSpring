-- Create booking table for CDC testing
-- This matches the Booking entity structure

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    airbnb_id BIGINT NOT NULL,
    total_price DOUBLE NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    idempotency_key VARCHAR(255) UNIQUE,
    check_in_date DATE,
    check_out_date DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

