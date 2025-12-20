package com.example.AirbnbBookingSpring.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    
    @NotNull(message = "Airbnb ID is required")
    private Long airbnbId;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @NotNull(message = "User ID is required")
    private Long userId;
}
