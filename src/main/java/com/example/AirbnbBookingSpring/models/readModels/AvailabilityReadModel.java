package com.example.AirbnbBookingSpring.models.readModels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityReadModel {
    
    private Long id;

    private Long airbnbId;

    private String date;

    private Long bookingId;

    private Boolean isAvailable;
}
