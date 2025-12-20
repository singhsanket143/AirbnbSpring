package com.example.AirbnbBookingSpring.services;

import java.util.Optional;

import com.example.AirbnbBookingSpring.models.Booking;

public interface IIdempotencyService {
    
    boolean isIdempotencyKeyUsed(String idempotencyKey);

    Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey);
}
