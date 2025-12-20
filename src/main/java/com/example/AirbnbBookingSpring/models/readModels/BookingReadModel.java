package com.example.AirbnbBookingSpring.models.readModels;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingReadModel {

    private Long id;

    private Long airbnbId;

    private Long userId;

    private double totalPrice;

    private String bookingStatus;

    private String idempotencyKey;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;
    
}
