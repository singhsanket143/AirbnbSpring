package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Availability extends  BaseModel {

    @Column(nullable = false)
    private String airbnbId;

    @Column(nullable = false)
    private String date;
    
    private Long bookingId; // null if available
}
